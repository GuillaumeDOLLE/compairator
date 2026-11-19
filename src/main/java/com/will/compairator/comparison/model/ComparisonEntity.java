package com.will.compairator.comparison.model;

import com.will.compairator.comparison.enums.*;
import com.will.compairator.user.model.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "comparison_attempt", indexes = {
        @Index(name = "idx_comparison_created", columnList = "createdAt"),
        @Index(name = "idx_comparison_user_created", columnList = "user_id,createdAt")})
@Getter @Setter @NoArgsConstructor
public class ComparisonEntity {
    @Id
    private UUID id = UUID.randomUUID();
    @Version
    private Long version;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private UserEntity user;
    @Column(nullable = false)
    private Instant createdAt;
    private Instant completedAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ComparisonStatus status = ComparisonStatus.STARTED;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TaskType taskType = TaskType.GENERAL;
    @Enumerated(EnumType.STRING)
    private Preference preference;
    private Boolean useful;
    private Instant evaluatedAt;
}
