package io.MHWilds.mhwbuilder.domain.recommend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RecommendDetailResponse {

    private List<EquipDetailResponse> equips;
    private List<DecorationDetailResponse> decorations;
    private List<FinalSkillResponse> finalSkills;
    private DefenseStatResponse stats;
}