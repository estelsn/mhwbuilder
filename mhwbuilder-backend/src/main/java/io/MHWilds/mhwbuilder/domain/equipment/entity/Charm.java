package io.MHWilds.mhwbuilder.domain.equipment.entity;

import io.MHWilds.mhwbuilder.domain.skill.entity.Skill;
import io.MHWilds.mhwbuilder.util.KsuidUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "charm")
@Getter
@Setter
@NoArgsConstructor
public class Charm {

    @Id
    @Column(nullable = false)
    private String id; // 고유 ID ("charm_ + 난수")

    @Column(nullable = false, unique = true)
    private int code;

    @Column(nullable = false)
    private String name; // 호석 이름

    private String version; // 추가된 게임 버전

    @PrePersist
    private void onCreate(){
        if(this.id == null){
            this.id = KsuidUtil.generate("charm");
        }
    }
}