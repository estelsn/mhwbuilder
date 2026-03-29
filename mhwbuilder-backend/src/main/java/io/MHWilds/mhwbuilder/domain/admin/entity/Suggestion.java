package io.MHWilds.mhwbuilder.domain.admin.entity;

import io.MHWilds.mhwbuilder.domain.buildset.entity.BuildSet;
import io.MHWilds.mhwbuilder.domain.user.entity.MHUser;
import io.MHWilds.mhwbuilder.util.KsuidUtil;
import io.MHWilds.mhwbuilder.util.entityenums.ReportType;
import io.MHWilds.mhwbuilder.util.entityenums.SuggestionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "suggestion")
@Getter
@Setter
@NoArgsConstructor
public class Suggestion {

    @Id
    @Column(nullable = false)
    private String id; // PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mh_user_id", nullable = false)
    private MHUser mhUser; // 신고자

    @Column(nullable = false)
    private String title; // 제목

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType = ReportType.POST; // 신고 종류, 기본값 POST

    @Lob
    @Column(nullable = false)
    private String content; // 내용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_build_id")
    private BuildSet reportedBuild; // 신고 대상 빌드 (선택)

    private String reportedUrl; // 원본 URL (선택)

    @Column(nullable = false)
    private LocalDateTime requestDate = LocalDateTime.now(); // 요청일, 기본 현재 시각

    @Lob
    private String answer; // 답변

    private LocalDateTime answerDate; // 답변일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mh_admin_id", nullable = false)
    private MHAdmin mhAdmin; // 답변 입력한 관리자.

    @Enumerated(EnumType.STRING)
    @Column( nullable = false)
    private SuggestionStatus status = SuggestionStatus.PENDING; // 상태, 기본값 PENDING

    @PrePersist
    private void onCreate(){
        if(this.id == null){
            this.id = KsuidUtil.generate("suggestion");
        }
    }
}
