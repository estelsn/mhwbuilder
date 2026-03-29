package io.MHWilds.mhwbuilder.domain.equipment.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="weapon")
@Getter
@Setter
@NoArgsConstructor
public class Weapon {

    @Id
    @Column(nullable = false)
    private String id; //고유 무기 id

    @Column(nullable = false, unique = true)
    private String name; //무기 이름

    @Column(nullable = false)
    private String type; // 무기 종류

    @Column(nullable = false)
    private int rarity; //무기 레어도

    @Column(name = "slot_map", columnDefinition = "json")
    private String slotMap; //장식주 칸 {"lv1":2, "lv2":1, "lv3":1}

    @Column(nullable = false)
    private int attack; //공격력

    @Column(columnDefinition = "json")
    private String elements; //속성 공격력 {"type": "fire", "value":200}

    @Column(nullable = false)
    private int critical = 0; //회심률(기본 0%)

    @Column(length = 100)
    private String version; //버전

}
