package com.will.compairator.comparison.dto;

import com.will.compairator.comparison.enums.Preference;
import jakarta.validation.constraints.AssertTrue;

public record FeedbackDTO(Preference preference, Boolean useful) {
    @AssertTrue(message = "Au moins une évaluation est nécessaire")
    public boolean isEvaluated() { return preference != null || useful != null; }
}
