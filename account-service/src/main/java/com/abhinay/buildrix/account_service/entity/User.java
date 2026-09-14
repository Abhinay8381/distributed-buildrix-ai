package com.abhinay.buildrix.account_service.entity;

import com.abhinay.buildrix.common_lib.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity{

    @Column(name = "email", length = 50, nullable = false, unique = true)
    private String email;

    private String password;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(unique = true)
    private String stripeSubscriptionId;

    @Column(unique = true)
    private String stripeCustomerId;

    @Column(name = "avatar_url", length = 200)
    private String avatarUrl;

    private Instant deletedAt;
}
