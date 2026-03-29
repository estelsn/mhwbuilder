package io.MHWilds.mhwbuilder.domain.admin.entity;

import io.MHWilds.mhwbuilder.domain.user.entity.MHUser;
import io.MHWilds.mhwbuilder.util.entityenums.AdminRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mh_admin")
@Getter
@Setter
@NoArgsConstructor
public class MHAdmin {

    @Id
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // id 컬럼과 매핑
    @JoinColumn(name = "id")
    private MHUser mhUser; //MHUser.id

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminRole role; //관리자 등급

    private LocalDateTime createdAt = LocalDateTime.now(); //임명일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by")
    private MHUser grantedBy; // 임명자


}
