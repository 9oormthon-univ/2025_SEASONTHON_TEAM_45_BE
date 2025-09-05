package org.carefreepass.com.carefreepassserver.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.Member;
import org.carefreepass.com.carefreepassserver.golbal.domain.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

// 채팅 세션 엔티티 - 환자와 AI 간의 증상 상담 채팅 세션 관리 (여러 메시지와 증상 분석 결과 포함)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_sessions")
public class ChatSession extends BaseTimeEntity {

    // 채팅 세션 고유 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    // 채팅 세션을 시작한 환자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 채팅 세션 상태 (ACTIVE, COMPLETED, CANCELLED)
    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false)
    private ChatSessionStatus status;

    // 채팅 세션 제목 (보통 첫 번째 메시지 내용 기반)
    @Column(name = "session_title", length = 200)
    private String title;

    // 채팅 세션에 포함된 모든 메시지들
    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    // 채팅 내용을 분석한 증상 분석 결과 (선택적)
    @OneToOne(mappedBy = "chatSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private SymptomAnalysis symptomAnalysis;

    // 새로운 채팅 세션 생성 - 초기 상태 ACTIVE로 설정
    public static ChatSession createSession(Member member, String title) {
        ChatSession session = new ChatSession();
        session.member = member;
        session.title = title;
        session.status = ChatSessionStatus.ACTIVE;
        return session;
    }

    // 채팅 세션 완료 처리 - 세션 상태를 COMPLETED로 변경
    public void completeSession() {
        this.status = ChatSessionStatus.COMPLETED;
    }


    // 채팅 세션에 메시지 추가 - 양방향 연관관계 설정
    public void addMessage(ChatMessage message) {
        this.messages.add(message);
        message.setChatSession(this);
    }

    // 채팅 세션에 증상 분석 결과 설정 - 양방향 연관관계 설정
    public void setSymptomAnalysis(SymptomAnalysis analysis) {
        this.symptomAnalysis = analysis;
        analysis.setChatSession(this);
    }
}