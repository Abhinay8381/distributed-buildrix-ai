package com.abhinay.buildrix.intelligence_service.entity;

import com.abhinay.buildrix.common_lib.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usage_logs", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"}))
public class UsageLog extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    UUID userId;

    @Column(nullable = false)
    LocalDate date;

    Integer tokensUsed;
}
