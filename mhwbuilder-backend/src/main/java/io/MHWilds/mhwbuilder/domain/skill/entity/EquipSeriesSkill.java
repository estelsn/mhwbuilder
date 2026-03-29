package io.MHWilds.mhwbuilder.domain.skill.entity;


import io.MHWilds.mhwbuilder.domain.equipment.entity.Armor;
import io.MHWilds.mhwbuilder.util.KsuidUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "equip_series_map")
@Getter
@Setter
@NoArgsConstructor
public class EquipSeriesSkill { //방어구-시리즈 스킬 간 매핑
    @Id
    @Column(nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)  //(앞쪽이 현재 클래스. 뒤쪽이 외래키 클래스)
    @JoinColumn(name = "armor_id", nullable = false)
    private Armor armor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_skill_id", nullable = false)
    private SeriesSkill seriesSkill;

    @PrePersist
    private void onCreate(){
        if(this.id == null){
          this.id = KsuidUtil.generate("equipSkillMap");
        }
    }
}
