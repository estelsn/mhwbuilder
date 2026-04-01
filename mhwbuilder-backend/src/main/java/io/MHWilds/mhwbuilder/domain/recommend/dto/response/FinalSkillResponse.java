package io.MHWilds.mhwbuilder.domain.recommend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FinalSkillResponse {

    private String skillName;
    private int level;
}