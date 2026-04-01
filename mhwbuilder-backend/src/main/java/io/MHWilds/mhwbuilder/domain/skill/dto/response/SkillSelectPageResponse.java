package io.MHWilds.mhwbuilder.domain.skill.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SkillSelectPageResponse {

    private List<NormalSkillOptionResponse> normalSkills;
    private List<SetSkillOptionResponse> seriesSkills;
    private List<SetSkillOptionResponse> groupSkills;
}