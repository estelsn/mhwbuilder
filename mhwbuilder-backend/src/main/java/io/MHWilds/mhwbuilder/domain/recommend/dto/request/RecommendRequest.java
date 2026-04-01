package io.MHWilds.mhwbuilder.domain.recommend.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class RecommendRequest {

    private List<SelectedNormalSkillRequest> normalSkills = new ArrayList<>();
    private List<SelectedSetSkillRequest> setSkills = new ArrayList<>();
}