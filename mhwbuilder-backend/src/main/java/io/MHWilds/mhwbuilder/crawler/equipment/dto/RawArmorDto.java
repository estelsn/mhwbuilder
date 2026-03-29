package io.MHWilds.mhwbuilder.crawler.equipment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RawArmorDto {
    private int code;
    private String name;
    private int category;
    private String categoryText;
    private int rare;
    private int defense;
    private RawSlotDto slot;
    private List<RawElementalDto> elementals;
    private List<RawEquipSkillDto> skills;
    private List<RawSetSkillDto> setSkills;
}