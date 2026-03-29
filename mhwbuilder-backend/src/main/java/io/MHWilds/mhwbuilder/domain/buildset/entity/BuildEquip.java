package io.MHWilds.mhwbuilder.domain.buildset.entity;

import io.MHWilds.mhwbuilder.util.KsuidUtil;
import io.MHWilds.mhwbuilder.util.entityenums.EquipCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "build_equip")
@Getter
@Setter
@NoArgsConstructor
public class BuildEquip { //BuildSet 연동 방어구/호석

    @Id
    @Column(nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_set_id", nullable = false)
    private BuildSet buildSet; //상위 buildSet

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipCategory category; // 타입. Head, Body, Waist, Arm, Leg, Charm

    @Column(name = "item_id", nullable = false)
    private String itemId; //Armor.id, Charm.id

    @OneToMany(mappedBy = "buildEquip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BuildEquipDecoration> buildEquipDecorations = new ArrayList<>();

    public void addDecoration(BuildEquipDecoration buildEquipDecoration) {
        this.buildEquipDecorations.add(buildEquipDecoration);
        buildEquipDecoration.setBuildEquip(this);
    }

    public void removeDecoration(BuildEquipDecoration buildEquipDecoration) {
        this.buildEquipDecorations.remove(buildEquipDecoration);
        buildEquipDecoration.setBuildEquip(null);
    }

    @PrePersist
    private void onCreate(){
        if(this.id == null){
            this.id = KsuidUtil.generate("buildEquip");
        }
    }
}
