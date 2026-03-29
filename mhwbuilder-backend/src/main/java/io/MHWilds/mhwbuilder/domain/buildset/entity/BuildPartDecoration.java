package io.MHWilds.mhwbuilder.domain.buildset.entity;

import io.MHWilds.mhwbuilder.domain.equipment.entity.Decoration;
import io.MHWilds.mhwbuilder.util.KsuidUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "build_part_decoration")
@Getter
@Setter
@NoArgsConstructor
public class BuildPartDecoration {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_part_id", nullable = false)
    private BuildPart buildPart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decoration_id", nullable = false)
    private Decoration decoration;

    @Column(nullable = false)
    private int count=1; //장식주 갯수

    @PrePersist
    private void onCreate(){
        if(this.id == null){
            this.id = KsuidUtil.generate("buildPartDecoration");
        }
    }
}
