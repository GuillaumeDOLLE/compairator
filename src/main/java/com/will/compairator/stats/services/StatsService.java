package com.will.compairator.stats.services;

import com.will.compairator.ai.enums.AiApiRequestStatus;
import com.will.compairator.ai.model.AiApiRequestLogEntity;
import com.will.compairator.ai.persistence.AiApiRequestLogRepository;
import com.will.compairator.comparison.enums.*;
import com.will.compairator.comparison.model.ComparisonEntity;
import com.will.compairator.comparison.persistence.ComparisonRepository;
import com.will.compairator.stats.dto.StatsDTO;
import com.will.compairator.stats.dto.StatsDTO.*;
import com.will.compairator.user.model.UserEntity;
import com.will.compairator.user.persistence.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class StatsService {
    private final UserRepository users;
    private final ComparisonRepository comparisons;
    private final AiApiRequestLogRepository logs;

    public StatsService(UserRepository users, ComparisonRepository comparisons, AiApiRequestLogRepository logs) {
        this.users = users;
        this.comparisons = comparisons;
        this.logs = logs;
    }

    @Transactional(readOnly = true)
    public StatsDTO calculate(LocalDate from, LocalDate to) {
        return calculate(from, to, Instant.now());
    }

    // Explicit observation time also makes cohort boundaries deterministic in tests.
    StatsDTO calculate(LocalDate from, LocalDate to, Instant now) {
        if (!from.isBefore(to) || ChronoUnit.DAYS.between(from, to) > 366
                || from.isAfter(now.atZone(ZoneOffset.UTC).toLocalDate())
                || to.isAfter(now.atZone(ZoneOffset.UTC).toLocalDate().plusDays(1))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Période invalide (maximum 366 jours, sans dates futures)");
        }
        Instant start = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = to.atStartOfDay().toInstant(ZoneOffset.UTC);
        if (end.isAfter(now)) end = now;
        final Instant observationEnd = end;
        Instant monthStart = end.minus(30, ChronoUnit.DAYS);
        Instant loadStart = monthStart.isBefore(start) ? monthStart : start;
        List<ComparisonEntity> activity = comparisons.findCreatedBetween(loadStart, end);
        List<ComparisonEntity> period = activity.stream().filter(c -> !c.getCreatedAt().isBefore(start)).toList();
        List<UserEntity> registered = users.findCreatedBetween(start, end);
        List<AiApiRequestLogEntity> calls = logs.findCreatedBetween(start, end);
        Map<Long, List<ComparisonEntity>> byUser = period.stream().collect(Collectors.groupingBy(c -> c.getUser().getId()));
        List<UserEntity> activationCohort = registered.stream()
                .filter(u -> !u.getCreatedAt().plus(7, ChronoUnit.DAYS).isAfter(observationEnd)).toList();
        List<UserEntity> retentionCohort = registered.stream()
                .filter(u -> !u.getCreatedAt().plus(14, ChronoUnit.DAYS).isAfter(observationEnd)).toList();
        long activated = activationCohort.stream().filter(u -> byUser.getOrDefault(u.getId(), List.of()).stream()
                .anyMatch(c -> c.getStatus() == ComparisonStatus.SUCCESS && c.getCompletedAt() != null
                        && !c.getCompletedAt().isBefore(u.getCreatedAt())
                        && c.getCompletedAt().isBefore(u.getCreatedAt().plus(7, ChronoUnit.DAYS)))).count();
        long retained = retentionCohort.stream().filter(u -> byUser.getOrDefault(u.getId(), List.of()).stream()
                .anyMatch(c -> !c.getCreatedAt().isBefore(u.getCreatedAt().plus(7, ChronoUnit.DAYS))
                        && c.getCreatedAt().isBefore(u.getCreatedAt().plus(14, ChronoUnit.DAYS)))).count();

        List<ComparisonEntity> successful = period.stream().filter(c -> c.getStatus() == ComparisonStatus.SUCCESS
                && c.getCompletedAt() != null && c.getCompletedAt().isBefore(observationEnd)).toList();
        long failed = period.stream().filter(c -> c.getStatus() == ComparisonStatus.ERROR
                && c.getCompletedAt() != null && c.getCompletedAt().isBefore(observationEnd)).count();
        long resolved = successful.size() + failed;
        List<ComparisonEntity> evaluated = successful.stream().filter(c -> c.getEvaluatedAt() != null
                && c.getEvaluatedAt().isBefore(observationEnd)).toList();
        long usefulVotes = evaluated.stream().filter(c -> c.getUseful() != null).count();
        long useful = evaluated.stream().filter(c -> Boolean.TRUE.equals(c.getUseful())).count();
        long preferenceVotes = evaluated.stream().filter(c -> c.getPreference() != null).count();

        List<Daily> daily = from.datesUntil(to).map(day -> {
            List<ComparisonEntity> dayActivity = period.stream().filter(c -> utcDate(c.getCreatedAt()).equals(day)).toList();
            return new Daily(day, registered.stream().filter(u -> utcDate(u.getCreatedAt()).equals(day)).count(),
                    active(dayActivity, c -> true), dayActivity.size(),
                    successful.stream().filter(c -> utcDate(c.getCreatedAt()).equals(day)).count());
        }).toList();
        LocalDate lastDay = end.minusNanos(1).atZone(ZoneOffset.UTC).toLocalDate();
        List<AiApiRequestLogEntity> comparisonCalls = logs
                .findComparisonCalls(start, end, end);
        long unknownCost = comparisonCalls.stream().filter(l -> l.getEstimatedCostUsd() == null).count();
        BigDecimal cost = knownCost(comparisonCalls);
        BigDecimal averageCost = unknownCost > 0 || successful.isEmpty() ? null
                : cost.divide(BigDecimal.valueOf(successful.size()), 12, RoundingMode.HALF_UP);
        Set<Long> usageUsers = new TreeSet<>(byUser.keySet());
        calls.stream().filter(l -> l.getUser() != null).forEach(l -> usageUsers.add(l.getUser().getId()));
        List<UserUsage> usage = usageUsers.stream().map(id -> {
            List<AiApiRequestLogEntity> userCalls = calls.stream()
                    .filter(l -> l.getUser() != null && l.getUser().getId().equals(id)).toList();
            return new UserUsage(id, byUser.getOrDefault(id, List.of()).size(), promptTokens(userCalls),
                    completionTokens(userCalls), unknownTokens(userCalls), knownCost(userCalls),
                    userCalls.stream().filter(l -> l.getEstimatedCostUsd() == null).count());
        }).toList();

        return new StatsDTO(start, end, "UTC", users.countRegisteredBefore(end), registered.size(),
                Rate.of(activated, activationCohort.size()), Rate.of(retained, retentionCohort.size()),
                byUser.size(), active(activity, c -> utcDate(c.getCreatedAt()).equals(lastDay)),
                active(activity, c -> !c.getCreatedAt().isBefore(monthStart)),
                period.size(), successful.size(), failed, period.size() - resolved,
                Rate.of(successful.size(), resolved).rate(), Rate.of(successful.size(), byUser.size()).rate(),
                medianDuration(successful), Rate.of(useful, usefulVotes), Rate.of(usefulVotes, successful.size()),
                Rate.of(preferenceVotes, successful.size()),
                new Cost("USD", cost, unknownCost, averageCost, promptTokens(calls), completionTokens(calls), unknownTokens(calls)),
                daily, usage, preferences(evaluated, comparisonCalls));
    }

    private static List<ModelPreference> preferences(List<ComparisonEntity> evaluated, List<AiApiRequestLogEntity> calls) {
        record Key(TaskType task, String model) {}
        Map<Key, long[]> counts = new HashMap<>();
        Map<UUID, ComparisonEntity> votes = evaluated.stream().filter(c -> c.getPreference() != null)
                .collect(Collectors.toMap(ComparisonEntity::getId, c -> c));
        for (AiApiRequestLogEntity call : calls) {
            ComparisonEntity vote = votes.get(call.getComparison().getId());
            if (vote == null || call.getStatus() != AiApiRequestStatus.SUCCESS) continue;
            long[] count = counts.computeIfAbsent(new Key(vote.getTaskType(), call.getModel()), key -> new long[2]);
            count[1]++;
            if (vote.getPreference().name().equals(call.getProvider().name())) count[0]++;
        }
        return counts.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().task().name() + e.getKey().model()))
                .map(e -> new ModelPreference(e.getKey().task(), e.getKey().model(), e.getValue()[0], e.getValue()[1],
                        Rate.of(e.getValue()[0], e.getValue()[1]).rate())).toList();
    }

    private static long active(List<ComparisonEntity> list, Predicate<ComparisonEntity> predicate) {
        return list.stream().filter(predicate).map(c -> c.getUser().getId()).distinct().count();
    }
    private static LocalDate utcDate(Instant instant) { return instant.atZone(ZoneOffset.UTC).toLocalDate(); }
    private static BigDecimal knownCost(List<AiApiRequestLogEntity> calls) {
        return calls.stream().map(AiApiRequestLogEntity::getEstimatedCostUsd).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private static long promptTokens(List<AiApiRequestLogEntity> calls) {
        return calls.stream().map(AiApiRequestLogEntity::getPromptTokens).filter(Objects::nonNull).mapToLong(Long::longValue).sum();
    }
    private static long completionTokens(List<AiApiRequestLogEntity> calls) {
        return calls.stream().map(AiApiRequestLogEntity::getCompletionTokens).filter(Objects::nonNull).mapToLong(Long::longValue).sum();
    }
    private static long unknownTokens(List<AiApiRequestLogEntity> calls) {
        return calls.stream().filter(l -> l.getPromptTokens() == null || l.getCompletionTokens() == null).count();
    }
    private static Long medianDuration(List<ComparisonEntity> comparisons) {
        long[] durations = comparisons.stream().mapToLong(c -> Duration.between(c.getCreatedAt(), c.getCompletedAt()).toMillis()).sorted().toArray();
        if (durations.length == 0) return null;
        int mid = durations.length / 2;
        return durations.length % 2 == 1 ? durations[mid] : durations[mid - 1] + (durations[mid] - durations[mid - 1]) / 2;
    }
}
