package io.MHWilds.mhwbuilder.domain.recommend.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SelectedSetSkillRequest {

    private String setSkillId;
    private int requiredCount;
}