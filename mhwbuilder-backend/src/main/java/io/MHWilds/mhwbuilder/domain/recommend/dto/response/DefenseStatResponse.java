package io.MHWilds.mhwbuilder.domain.recommend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DefenseStatResponse {
    private int defense;
    private int fireRes;
    private int waterRes;
    private int thunderRes;
    private int iceRes;
    private int dragonRes;
}