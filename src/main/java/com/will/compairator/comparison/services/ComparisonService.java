package com.will.compairator.comparison.services;

import com.will.compairator.comparison.dto.FeedbackDTO;
import com.will.compairator.comparison.enums.*;
import com.will.compairator.comparison.model.ComparisonEntity;
import com.will.compairator.comparison.persistence.ComparisonRepository;
import com.will.compairator.user.model.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.UUID;

@Service
public class ComparisonService {
    private final ComparisonRepository comparisons;
    public ComparisonService(ComparisonRepository comparisons) { this.comparisons = comparisons; }

    public ComparisonEntity start(UserEntity user, TaskType task) {
        ComparisonEntity comparison = new ComparisonEntity();
        comparison.setUser(user);
        comparison.setCreatedAt(Instant.now());
        comparison.setTaskType(task == null ? TaskType.GENERAL : task);
        return comparisons.saveAndFlush(comparison);
    }

    public void finish(ComparisonEntity comparison, ComparisonStatus status) {
        comparison.setStatus(status);
        comparison.setCompletedAt(Instant.now());
        comparisons.saveAndFlush(comparison);
    }

    @Transactional
    public FeedbackDTO evaluate(UUID id, Long userId, FeedbackDTO feedback) {
        ComparisonEntity comparison = comparisons.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comparaison introuvable"));
        if (comparison.getStatus() != ComparisonStatus.SUCCESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La comparaison n’est pas terminée avec succès");
        }
        // A partial update keeps the other optional answer; each comparison has at most one current vote.
        if (feedback.preference() != null) comparison.setPreference(feedback.preference());
        if (feedback.useful() != null) comparison.setUseful(feedback.useful());
        comparison.setEvaluatedAt(Instant.now());
        return new FeedbackDTO(comparison.getPreference(), comparison.getUseful());
    }
}
