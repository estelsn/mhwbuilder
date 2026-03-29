package io.MHWilds.mhwbuilder.domain.equipment.entity;

import io.MHWilds.mhwbuilder.util.KsuidUtil;
import io.MHWilds.mhwbuilder.util.entityenums.EquipCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "armor")
@Getter
@Setter
@NoArgsConstructor
public class Armor {

    @Id
    @Column(nullable = false)
    private String id; // 고유 장비 ID ("armor_ + 난수")

    @Column(nullable = false, unique = true)
    private int code;

    @Column(nullable = false)
    private String name; // 장비 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipCategory category; // 부위 (head, body 등)

    @Column(nullable = false)
    private int rarity; // 희귀도

    @Column(nullable = false)
    private int slot1Lv;

    @Column(nullable = false)
    private int slot2Lv;

    @Column(nullable = false)
    private int slot3Lv;

    @Column(nullable = false)
    private int defense;

    @Lob
    @Column(nullable = false)
    private String elementals;

    @Column(nullable = false)
    private String version;

    @PrePersist
    private void onCreate(){
        if(this.id == null){
            this.id = KsuidUtil.generate("armor");
        }
    }
}