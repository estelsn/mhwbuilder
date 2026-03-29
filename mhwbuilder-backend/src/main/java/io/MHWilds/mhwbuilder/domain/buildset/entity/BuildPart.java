package io.MHWilds.mhwbuilder.domain.buildset.entity;

import io.MHWilds.mhwbuilder.util.KsuidUtil;
import io.MHWilds.mhwbuilder.util.entityenums.EquipType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "build_part")
@Getter
@Setter
@NoArgsConstructor
public class BuildPart { //커스텀 데이터

    @Id
    @Column(nullable = false)
    private String id; // UUID 또는 난수로 생성

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_set_id", nullable = false)
    private BuildSet buildSet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipType type; //WEAPON, CHARM

    @Column(columnDefinition = "json")
    private String slotMap; // {"lv3": 3} 형태

    @OneToMany(mappedBy = "buildPart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BuildPartSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "buildPart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BuildPartDecoration> decorations = new ArrayList<>();

    @PrePersist
    private void onCreate(){
        if(this.slotMap == null){
            this.slotMap = "{\"lv3\":3}";
        }
        if(this.id == null){
            this.id = KsuidUtil.generate("buildPart");
        }
    }
}