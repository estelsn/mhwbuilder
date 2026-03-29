package io.MHWilds.mhwbuilder.domain.buildset.entity;

import io.MHWilds.mhwbuilder.domain.skill.entity.Skill;
import io.MHWilds.mhwbuilder.util.KsuidUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aggregated_build_skill")
@Getter
@Setter
@NoArgsConstructor
public class AggregatedBuildSkill {
    @Id
    @Column(nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_set_id", nullable = false)
    private BuildSet buildSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false)
    private int totalLevel; // 모든 장비 + 장식주 + 호석 포함 총합 레벨

    @PrePersist
    private void onCreate() {
        if (this.id == null) {
            this.id = KsuidUtil.generate("buildSetSkill");
        }
    }
}
//dto 계산용 클래스로 변경해서 이동할 예정