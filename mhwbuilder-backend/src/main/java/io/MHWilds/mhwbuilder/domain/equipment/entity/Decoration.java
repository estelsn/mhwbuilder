package io.MHWilds.mhwbuilder.domain.equipment.entity;

import io.MHWilds.mhwbuilder.util.KsuidUtil;
import io.MHWilds.mhwbuilder.util.entityenums.GearSkillType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "decoration")
@Getter
@Setter
@NoArgsConstructor
public class Decoration {

    @Id
    @Column(nullable = false)
    private String id; // 고유 장식 아이템 ID ("deco_ + 난수")

    @Column(nullable = false, unique = true)
    private int code;

    @Column(nullable = false)
    private String name; // 장식주 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GearSkillType type; // 슬롯 타입 (무기weapon/방어구armor 슬롯)

    @Column(nullable = false)
    private int rarity;

    @Column(name = "slot_level", nullable = false)
    private int slotLevel; // 장착에 필요한 슬롯 레벨

    private String version; // 추가된 게임 버전

    @PrePersist
    private void onCreate(){
        if(this.id == null){
            this.id = KsuidUtil.generate("decoration");
        }
    }
}