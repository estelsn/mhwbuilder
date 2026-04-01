package io.MHWilds.mhwbuilder.domain.skill.controller;

import io.MHWilds.mhwbuilder.domain.skill.dto.response.SkillSelectPageResponse;
import io.MHWilds.mhwbuilder.domain.skill.service.SkillQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillQueryController {

    private final SkillQueryService skillQueryService;

    @GetMapping("/select-page")
    public SkillSelectPageResponse getSkillSelectPage() {
        return skillQueryService.getSkillSelectPage();
    }
}