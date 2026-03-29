package io.MHWilds.mhwbuilder.domain.skill.entity;
import io.MHWilds.mhwbuilder.util.KsuidUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "series_skill_effect",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"series_skill_id", "required_count"})
        }
      )
@Getter
@Setter
@NoArgsConstructor
public class SeriesSkillEffect {
    @Id
    @Column(nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_skill_id", nullable = false)
    private SeriesSkill seriesSkill;

    @Column(nullable = false)
    private String effectTitle;

    @Column( name = "required_count", nullable = false)
    private int requiredCount;

    @Lob
    @Column(nullable = false)
    private String description;

    @PrePersist
    private void onCreate(){
        if(this.id == null){
            this.id = KsuidUtil.generate("seriesSkillEffect");
        }
    }
}
