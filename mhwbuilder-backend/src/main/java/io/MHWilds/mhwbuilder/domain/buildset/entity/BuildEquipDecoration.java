package io.MHWilds.mhwbuilder.domain.buildset.entity;

import io.MHWilds.mhwbuilder.domain.equipment.entity.Decoration;
import io.MHWilds.mhwbuilder.util.KsuidUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "build_equip_decoration",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"build_equip_id", "decoration_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class BuildEquipDecoration {
    @Id
    @Column(nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_equip_id", nullable = false)
    private BuildEquip buildEquip; //연동되는 BuildEquip

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decoration_id", nullable = false)
    private Decoration decoration; // 장식주

    @Column(nullable = false)
    private int count = 1; // 장식주 갯수

    @PrePersist
    private void onCreate(){
        if(this.id == null){
            this.id = KsuidUtil.generate("buildEquipDecoration");
        }
    }
}

