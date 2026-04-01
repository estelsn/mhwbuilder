package io.MHWilds.mhwbuilder.domain.recommend.controller;

import io.MHWilds.mhwbuilder.domain.recommend.dto.request.RecommendRequest;
import io.MHWilds.mhwbuilder.domain.recommend.dto.response.RecommendResultResponse;
import io.MHWilds.mhwbuilder.domain.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @PostMapping
    public RecommendResultResponse recommend(@RequestBody RecommendRequest request) {
        return recommendService.recommend(request);
    }
}