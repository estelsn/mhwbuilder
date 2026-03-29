package io.MHWilds.mhwbuilder.crawler.skill;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RawSkillDto {
    private int code;
    private String name;
    private int category;
    private String categoryText;
    private int slotSize;
    private int maxLevel;
    private String description;
    private List<String> levels;
    private RawSeriesSkillDto seriesSkill1;
    private RawSeriesSkillDto seriesSkill2;


}
