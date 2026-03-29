package io.MHWilds.mhwbuilder.crawler.equipment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RawCharmDto {
    private int code;
    private String name;
    private int maxLevel;
    private RawEquipSkillDto equipSkill;
}
