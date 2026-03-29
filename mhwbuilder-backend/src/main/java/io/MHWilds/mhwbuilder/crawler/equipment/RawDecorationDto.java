package io.MHWilds.mhwbuilder.crawler.equipment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RawDecorationDto {
    private int code;
    private String name;
    private int category;
    private int rarity;
    private int slotLevel;
    private List<RawEquipSkillDto> equipSkillList;
}
