package io.MHWilds.mhwbuilder.domain.user.entity;


import io.MHWilds.mhwbuilder.domain.admin.entity.MHAdmin;
import io.MHWilds.mhwbuilder.util.KsuidUtil;
import io.MHWilds.mhwbuilder.util.entityenums.RestrictionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_restriction")
@Getter
@Setter
@NoArgsConstructor
public class UserRestriction { //제재대상자 목록

    @Id
    @Column(nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private MHUser mhUser; // 제재대상자

    @Enumerated(EnumType.STRING) //enum입력법
    @Column(name = "restriction_type", nullable = false)
    private RestrictionType restriction = RestrictionType.MOD; // 제재 종류
    // = R.MOD를 쓴 이유는 초기값, 객체 생성시 초기화. null 방지.

    @Lob //jpa 표준 문자열, 긴 텍스트 저장용. 인덱스 걸기는 부담스러움.
    @Column(nullable = false)
    private String reason; // 사유

    @Column(name = "restricted_until")
    private LocalDateTime restrictedUntil; // 제재기간. null일 경우 무제한

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 제재일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mh_admin_id", nullable = false)
    private MHAdmin mhAdmin; // 제재 권한자


    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.id == null){
            this.id = KsuidUtil.generate("userRestriction");
        }
    }
}
