package io.MHWilds.mhwbuilder.domain.skill.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SetSkillEffectOptionResponse {

    private int requiredCount;
    private String effectName;
    private String effectDescription;
}