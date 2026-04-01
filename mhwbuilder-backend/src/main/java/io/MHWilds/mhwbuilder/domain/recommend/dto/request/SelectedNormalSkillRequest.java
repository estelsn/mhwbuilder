package io.MHWilds.mhwbuilder.domain.recommend.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SelectedNormalSkillRequest {

    private String skillId;
    private int targetLevel;
}