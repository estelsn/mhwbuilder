package io.MHWilds.mhwbuilder.domain.skill.entity;

import io.MHWilds.mhwbuilder.util.KsuidUtil;
import io.MHWilds.mhwbuilder.util.entityenums.SkillCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "series_skill")
@Getter
@Setter
@NoArgsConstructor
public class SeriesSkill { //스킬 중 시리즈 스킬과 그룹스킬 모음

    @Id
    @Column(nullable = false)
    private String id;

    @Column(name = "external_code", nullable = false, unique = true)
    private int code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillCategory category; //스킬타입. 시리즈, 그룹

    @Column(name = "max_level", nullable = false)
    private int maxLevel;

    @Column(nullable = false, unique = true)
    private String name; //스킬명

    private String version;


    @PrePersist
    private void onCreate(){
        if(this.id == null){
            this.id = KsuidUtil.generate("seriesSkill");
        }
    }

}
