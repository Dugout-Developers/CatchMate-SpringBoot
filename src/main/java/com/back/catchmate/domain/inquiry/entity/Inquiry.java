package com.back.catchmate.domain.inquiry.entity;

import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.global.entity.BaseTimeEntity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "inquiries")
public class Inquiry extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InquiryType inquiryType; // 문의 유형

    @Column(nullable = false, length = 1000)
    private String content; // 문의 내용

    @Column(nullable = false)
    private Boolean isCompleted; // 문의 내용

    @Column(length = 1000)
    private String answer; // 문의 답변

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answered_by")
    private User answeredBy;

    public void updateAnswer(String answer, User answeredBy) {
        this.answer = answer;
        this.answeredBy = answeredBy;
        this.isCompleted = true;
    }
}
