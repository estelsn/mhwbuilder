package io.MHWilds.mhwbuilder.domain.recommend.service;

import io.MHWilds.mhwbuilder.domain.recommend.dto.request.RecommendRequest;
import io.MHWilds.mhwbuilder.domain.recommend.dto.response.RecommendResultResponse;

public interface RecommendService {

    RecommendResultResponse recommend(RecommendRequest request);
}