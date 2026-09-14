package com.abhinay.buildrix.intelligence_service.entity;

import com.abhinay.buildrix.common_lib.entity.BaseEntity;
import com.abhinay.buildrix.common_lib.enums.MessageRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false),
            @JoinColumn(name = "project_id", referencedColumnName = "projectId", nullable = false)
    })
    private ChatSession chatSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageRole role;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Builder.Default
    private Integer tokensUsed = 0;

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sequenceOrder ASC")
    List<ChatEvent> events;
}
