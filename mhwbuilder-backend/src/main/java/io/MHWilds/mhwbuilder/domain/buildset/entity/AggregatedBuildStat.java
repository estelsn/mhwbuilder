package io.MHWilds.mhwbuilder.domain.buildset.entity;

import io.MHWilds.mhwbuilder.util.KsuidUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aggregated_build_stat")
@Getter
@Setter
@NoArgsConstructor
public class AggregatedBuildStat {

    @Id
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_set_id", nullable = false)
    private BuildSet buildSet;

    // 기본 방어력
    @Column(nullable = false)
    private int baseDefense;

    // 스킬 포함 최종 방어력
    @Column(nullable = false)
    private int totalDefense;

    // 속성 내성 (기본 + 스킬 보정 포함)
    @Column(nullable = false)
    private int fireRes;
    @Column(nullable = false)
    private int waterRes;
    @Column(nullable = false)
    private int thunderRes;
    @Column(nullable = false)
    private int iceRes;
    @Column(nullable = false)
    private int dragonRes;

    @PrePersist
    private void onCreate(){
        if(this.id == null){
            this.id = KsuidUtil.generate("aggregatedStat");
        }
    }
}