package io.MHWilds.mhwbuilder.crawler.equipment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RawCharmDto {
    private int code;
    private String name;
    private int maxLevel;
    private RawEquipSkillDto equipSkill;
}
