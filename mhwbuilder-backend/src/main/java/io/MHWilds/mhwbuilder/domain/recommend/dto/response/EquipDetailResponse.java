package io.MHWilds.mhwbuilder.domain.recommend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EquipDetailResponse {

    private String part;
    private String name;
}