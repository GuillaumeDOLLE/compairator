package com.will.compairator.ai.model;

import com.will.compairator.ai.enums.AiApiRequestStatus;
import com.will.compairator.ai.enums.AiApiResponseOrigin;
import com.will.compairator.ai.enums.AiProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.math.BigDecimal;
import com.will.compairator.user.model.UserEntity;
import com.will.compairator.comparison.model.ComparisonEntity;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ai_api_request_log")
@Entity
public class AiApiRequestLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    AiProvider provider;

    String model;

    @Lob
    String prompt;

    @Lob
    String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AiApiRequestStatus status;

    @Lob
    String errorMessage;

    @Column(nullable = false)
    Instant createdAt;

    @Column(nullable = false)
    long durationMs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AiApiResponseOrigin origin;

    @ManyToOne(fetch = FetchType.LAZY)
    UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    ComparisonEntity comparison;

    Long promptTokens;
    Long completionTokens;

    @Column(precision = 24, scale = 12)
    BigDecimal estimatedCostUsd;

}
