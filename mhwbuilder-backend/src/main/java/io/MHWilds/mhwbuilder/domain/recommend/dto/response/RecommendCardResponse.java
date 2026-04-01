package io.MHWilds.mhwbuilder.domain.recommend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendCardResponse {

    private String title;
    private String summary;
    private RecommendDetailResponse detail;
}