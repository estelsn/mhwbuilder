package io.MHWilds.mhwbuilder.domain.buildset.entity;

import io.MHWilds.mhwbuilder.domain.user.entity.MHUser;
import io.MHWilds.mhwbuilder.util.KsuidUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "build_set_like",
        uniqueConstraints = @UniqueConstraint(columnNames = {"mh_user_id", "build_set_id"}) )
@Getter
@Setter
@NoArgsConstructor
public class BuildSetLike {

    @Id
    @Column(nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mh_user_id", nullable = false)
    private MHUser mhUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_set_id", nullable = false)
    private BuildSet buildSet;

    @PrePersist
    private void onCreate(){
        if(this.id == null){
            this.id = KsuidUtil.generate("buildSetLike");
        }
    }
}
