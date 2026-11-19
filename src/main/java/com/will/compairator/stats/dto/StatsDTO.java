package com.will.compairator.stats.dto;

import com.will.compairator.comparison.enums.TaskType;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public record StatsDTO(
        Instant fromInclusive, Instant toExclusive, String timezone,
        long registeredUsers, long newUsers,
        Rate activationWithin7Days, Rate retentionDays7To14,
        long activeUsers, long dailyActiveUsers, long monthlyActiveUsers,
        long comparisonsStarted, long comparisonsSuccessful, long comparisonsFailed, long comparisonsPending,
        Double technicalSuccessRate, Double successfulComparisonsPerActiveUser,
        Long medianSuccessfulDurationMs,
        Rate usefulness, Rate usefulnessParticipation, Rate preferenceParticipation,
        Cost cost, List<Daily> daily, List<UserUsage> usageByUser, List<ModelPreference> modelPreferences) {

    // Rates are fractions in [0,1]. A missing denominator yields null rather than a misleading zero.
    public record Rate(long numerator, long denominator, Double rate) {
        public static Rate of(long numerator, long denominator) {
            return new Rate(numerator, denominator, denominator == 0 ? null : (double) numerator / denominator);
        }
    }
    public record Cost(String currency, BigDecimal knownComparisonCost,
                       long comparisonCallsWithUnknownCost, BigDecimal averagePerSuccessfulComparison,
                       long knownPromptTokens, long knownCompletionTokens, long callsWithUnknownTokens) {}
    public record Daily(LocalDate date, long newUsers, long activeUsers, long comparisons, long successfulComparisons) {}
    public record UserUsage(Long userId, long comparisons, long knownPromptTokens, long knownCompletionTokens,
                            long callsWithUnknownTokens, BigDecimal knownCostUsd, long callsWithUnknownCost) {}
    public record ModelPreference(TaskType taskType, String model, long wins, long evaluatedAppearances, Double preferenceRate) {}
}
