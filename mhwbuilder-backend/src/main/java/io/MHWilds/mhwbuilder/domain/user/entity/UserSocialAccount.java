package io.MHWilds.mhwbuilder.domain.user.entity;

import io.MHWilds.mhwbuilder.util.KsuidUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_social_account", uniqueConstraints = {
         @UniqueConstraint( columnNames = {"provider", "provider_id"})
         }
)
@Getter
@Setter
@NoArgsConstructor
public class UserSocialAccount { //소셜로그인 연동

    @Id
    @Column(nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mh_user_id", nullable = false)
    private MHUser mhUser;//연동된 계정

    @Column(nullable = false)
    private String provider; // 예: Google, Kakao

    @Column(name = "provider_id", nullable = false)
    private String providerId; // 소셜 로그인 고유 ID

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; //생성일


    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.id == null){
            this.id = KsuidUtil.generate("userSocialAccount");
        }
    }
}