package com.will.compairator.stats.services;

import com.will.compairator.ai.enums.*;
import com.will.compairator.ai.model.AiApiRequestLogEntity;
import com.will.compairator.ai.persistence.AiApiRequestLogRepository;
import com.will.compairator.comparison.enums.*;
import com.will.compairator.comparison.model.ComparisonEntity;
import com.will.compairator.comparison.persistence.ComparisonRepository;
import com.will.compairator.user.model.UserEntity;
import com.will.compairator.user.persistence.UserRepository;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StatsServiceTest {
    UserRepository users = mock(UserRepository.class);
    ComparisonRepository comparisons = mock(ComparisonRepository.class);
    AiApiRequestLogRepository logs = mock(AiApiRequestLogRepository.class);
    StatsService service = new StatsService(users, comparisons, logs);
    final Instant end = Instant.parse("2026-11-18T00:00:00Z");
    final LocalDate from = LocalDate.of(2026, 11, 1), to = LocalDate.of(2026, 11, 18);

    UserEntity user(long id, String created) {
        UserEntity user = new UserEntity(); user.setId(id); user.setCreatedAt(Instant.parse(created)); return user;
    }
    ComparisonEntity comparison(UserEntity user, String created, ComparisonStatus status) {
        ComparisonEntity c = new ComparisonEntity(); c.setUser(user); c.setCreatedAt(Instant.parse(created));
        c.setCompletedAt(c.getCreatedAt().plusSeconds(2)); c.setStatus(status); return c;
    }
    void seed(List<UserEntity> registered, List<ComparisonEntity> activity, List<AiApiRequestLogEntity> calls) {
        when(users.findCreatedBetween(any(), any())).thenReturn(registered);
        when(users.countRegisteredBefore(any())).thenReturn((long) registered.size());
        when(comparisons.findCreatedBetween(any(), any())).thenReturn(activity);
        when(logs.findCreatedBetween(any(), any())).thenReturn(calls);
        when(logs.findComparisonCalls(any(), any(), any())).thenReturn(calls);
    }

    @Test void emptyDataHasUnknownRatesAndContinuousZeroDailySeries() {
        seed(List.of(), List.of(), List.of());
        var stats = service.calculate(from, to, end);
        assertEquals(0, stats.registeredUsers()); assertEquals(17, stats.daily().size());
        assertNull(stats.technicalSuccessRate()); assertNull(stats.usefulness().rate());
        assertNull(stats.activationWithin7Days().rate()); assertNull(stats.retentionDays7To14().rate());
        assertNull(stats.cost().averagePerSuccessfulComparison());
    }

    @Test void cohortsExcludeImmatureAccountsAndUseHalfOpenWindows() {
        UserEntity mature = user(1, "2026-11-01T10:00:00Z");
        UserEntity notActivated = user(2, "2026-11-02T10:00:00Z");
        UserEntity immature = user(3, "2026-11-16T10:00:00Z");
        ComparisonEntity success = comparison(mature, "2026-11-02T12:00:00Z", ComparisonStatus.SUCCESS);
        ComparisonEntity returned = comparison(mature, "2026-11-08T10:00:00Z", ComparisonStatus.ERROR);
        ComparisonEntity tooLate = comparison(notActivated, "2026-11-09T10:00:00Z", ComparisonStatus.SUCCESS);
        ComparisonEntity outsideRetention = comparison(notActivated, "2026-11-16T10:00:00Z", ComparisonStatus.SUCCESS);
        seed(List.of(mature, notActivated, immature), List.of(success, returned, tooLate, outsideRetention), List.of());
        var stats = service.calculate(from, to, end);
        assertEquals(2, stats.activationWithin7Days().denominator());
        assertEquals(0.5, stats.activationWithin7Days().rate());
        assertEquals(2, stats.retentionDays7To14().denominator());
        // An attempt at exactly signup + 7 days belongs to retention, not activation.
        assertEquals(1.0, stats.retentionDays7To14().rate());
        assertEquals(2, stats.activeUsers()); assertEquals(3, stats.comparisonsSuccessful());
        assertEquals(1.5, stats.successfulComparisonsPerActiveUser());
    }

    @Test void nonVotesDoNotCountAsNegativeAndRepeatedPreferenceHasOneCurrentVote() {
        UserEntity user = user(1, "2026-11-01T00:00:00Z");
        ComparisonEntity first = comparison(user, "2026-11-10T00:00:00Z", ComparisonStatus.SUCCESS);
        first.setPreference(Preference.GROQ); first.setUseful(true); first.setEvaluatedAt(first.getCompletedAt());
        ComparisonEntity second = comparison(user, "2026-11-11T00:00:00Z", ComparisonStatus.SUCCESS);
        AiApiRequestLogEntity call = AiApiRequestLogEntity.builder().comparison(first).user(user).provider(AiProvider.GROQ)
                .model("model-at-comparison-time").status(AiApiRequestStatus.SUCCESS).promptTokens(10L).completionTokens(20L)
                .estimatedCostUsd(new BigDecimal("0.01")).build();
        seed(List.of(user), List.of(first, second), List.of(call));
        var stats = service.calculate(from, to, end);
        assertEquals(1.0, stats.usefulness().rate());
        assertEquals(0.5, stats.usefulnessParticipation().rate());
        assertEquals(1, stats.modelPreferences().getFirst().wins());
        assertEquals("model-at-comparison-time", stats.modelPreferences().getFirst().model());
    }

    @Test void unknownCostPreventsMisleadingAverageAndPendingDoesNotCountAsFailure() {
        UserEntity user = user(1, "2026-11-01T00:00:00Z");
        ComparisonEntity success = comparison(user, "2026-11-10T00:00:00Z", ComparisonStatus.SUCCESS);
        ComparisonEntity pending = comparison(user, "2026-11-11T00:00:00Z", ComparisonStatus.STARTED);
        AiApiRequestLogEntity call = AiApiRequestLogEntity.builder().comparison(success).user(user).build();
        seed(List.of(user), List.of(success, pending), List.of(call));
        var stats = service.calculate(from, to, end);
        assertNull(stats.cost().averagePerSuccessfulComparison());
        assertEquals(1, stats.cost().comparisonCallsWithUnknownCost());
        assertEquals(1, stats.cost().callsWithUnknownTokens());
        assertEquals(1, stats.comparisonsPending()); assertEquals(0, stats.comparisonsFailed());
        assertEquals(1.0, stats.technicalSuccessRate());
    }

    @Test void monthIncludesActivityBeforeSelectedPeriodAndDailyUsersAreDistinct() {
        UserEntity user = user(1, "2026-10-01T00:00:00Z");
        UserEntity other = user(2, "2026-10-01T00:00:00Z");
        seed(List.of(), List.of(comparison(other, "2026-10-31T00:00:00Z", ComparisonStatus.SUCCESS),
                comparison(user, "2026-11-17T10:00:00Z", ComparisonStatus.SUCCESS),
                comparison(user, "2026-11-17T11:00:00Z", ComparisonStatus.SUCCESS)), List.of());
        var stats = service.calculate(from, to, end);
        assertEquals(2, stats.monthlyActiveUsers()); assertEquals(1, stats.dailyActiveUsers());
        assertEquals(1, stats.activeUsers()); assertEquals(2, stats.comparisonsStarted());
        assertEquals(1, stats.daily().getLast().activeUsers());
    }
}
