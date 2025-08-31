package org.carefreepass.com.carefreepassserver.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.member.entity.Member;
import org.carefreepass.com.carefreepassserver.golbal.domain.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_sessions")
public class ChatSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false)
    private ChatSessionStatus status;

    @Column(name = "session_title", length = 200)
    private String title;

    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @OneToOne(mappedBy = "chatSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private SymptomAnalysis symptomAnalysis;

    public static ChatSession createSession(Member member, String title) {
        ChatSession session = new ChatSession();
        session.member = member;
        session.title = title;
        session.status = ChatSessionStatus.ACTIVE;
        return session;
    }

    public void completeSession() {
        this.status = ChatSessionStatus.COMPLETED;
    }

    public void cancelSession() {
        this.status = ChatSessionStatus.CANCELLED;
    }

    public void addMessage(ChatMessage message) {
        this.messages.add(message);
        message.setChatSession(this);
    }

    public void setSymptomAnalysis(SymptomAnalysis analysis) {
        this.symptomAnalysis = analysis;
        analysis.setChatSession(this);
    }
}