package io.MHWilds.mhwbuilder.domain.skill.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NormalSkillOptionResponse {

    private String skillId;
    private String skillName;
    private int maxLevel;
    private Integer decorationSlotLevel;
}