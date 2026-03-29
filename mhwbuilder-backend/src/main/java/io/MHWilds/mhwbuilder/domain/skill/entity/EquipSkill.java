package io.MHWilds.mhwbuilder.domain.skill.entity;


import io.MHWilds.mhwbuilder.util.KsuidUtil;
import io.MHWilds.mhwbuilder.util.entityenums.EquipType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "equip_skill",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"equip_id", "skill_id"})}
      )
@Getter
@Setter
@NoArgsConstructor
public class EquipSkill { //장비-스킬 매핑 테이블

    @Id
    @Column(nullable = false)
    private String id;

    @Column(name = "equip_id", nullable = false)
    private String equipId; // armor.id, deco.id, charm.id 등 논리적 연결

    @Enumerated(EnumType.STRING)
    @Column(name = "equip_type", nullable = false)
    private EquipType equipType; //equipId의 타입. 어떤 장비가 연결되었는지.

    @ManyToOne(fetch = FetchType.LAZY)  //(EquipSkill쪽이 many. 앞쪽이 현재 클래스. 뒤쪽이 외래키 클래스)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill; // Skill 엔티티와 연동. 저장되는건 Skill.id

    @Min(1)
    @Column(name = "skill_level", nullable = false)
    private int skillLevel; //연동된 스킬 레벨


    @PrePersist
    private void onCreate(){
        if(this.id == null){
            this.id = KsuidUtil.generate("equipSkill");
        }
    }
}