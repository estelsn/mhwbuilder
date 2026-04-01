package io.MHWilds.mhwbuilder.domain.skill.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SetSkillOptionResponse {

    private String setSkillId;
    private String setSkillName;
    private List<SetSkillEffectOptionResponse> effects;
}