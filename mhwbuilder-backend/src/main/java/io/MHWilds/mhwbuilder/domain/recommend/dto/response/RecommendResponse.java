package io.MHWilds.mhwbuilder.domain.recommend.dto.response;

import java.util.List;

public record RecommendResponse(
        List<RecommendCardResponse> recommendations
) {
}