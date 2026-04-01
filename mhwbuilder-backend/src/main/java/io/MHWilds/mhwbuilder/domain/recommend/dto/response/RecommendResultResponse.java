package io.MHWilds.mhwbuilder.domain.recommend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RecommendResultResponse {

    private List<RecommendCardResponse> cards;
}