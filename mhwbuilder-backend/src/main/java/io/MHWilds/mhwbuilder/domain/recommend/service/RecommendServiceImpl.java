package io.MHWilds.mhwbuilder.domain.recommend.service;

import io.MHWilds.mhwbuilder.domain.recommend.dto.request.RecommendRequest;
import io.MHWilds.mhwbuilder.domain.recommend.dto.response.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendServiceImpl implements RecommendService {

    @Override
    public RecommendResultResponse recommend(RecommendRequest request) {

        RecommendCardResponse dummyCard = RecommendCardResponse.builder()
                .title("테스트 추천 세팅")
                .summary(makeSummary(request))
                .detail(
                        RecommendDetailResponse.builder()
                                .equips(List.of(
                                        EquipDetailResponse.builder().part("HEAD").name("테스트 헬름").build(),
                                        EquipDetailResponse.builder().part("BODY").name("테스트 메일").build(),
                                        EquipDetailResponse.builder().part("ARM").name("테스트 암").build(),
                                        EquipDetailResponse.builder().part("WAIST").name("테스트 코일").build(),
                                        EquipDetailResponse.builder().part("LEG").name("테스트 그리브").build(),
                                        EquipDetailResponse.builder().part("CHARM").name("테스트 참").build()
                                ))
                                .decorations(List.of(
                                        DecorationDetailResponse.builder().name("공격주").count(2).build(),
                                        DecorationDetailResponse.builder().name("달인주").count(1).build()
                                ))
                                .finalSkills(List.of(
                                        FinalSkillResponse.builder().skillName("공격").level(3).build(),
                                        FinalSkillResponse.builder().skillName("간파").level(2).build()
                                ))
                                .stats(
                                        DefenseStatResponse.builder()
                                                .defense(321)
                                                .fireRes(3)
                                                .waterRes(1)
                                                .thunderRes(-2)
                                                .iceRes(0)
                                                .dragonRes(4)
                                                .build()
                                )
                                .build()
                )
                .build();

        return RecommendResultResponse.builder()
                .cards(List.of(dummyCard))
                .build();
    }

    private String makeSummary(RecommendRequest request) {
        int normalSkillCount = request.getNormalSkills() == null ? 0 : request.getNormalSkills().size();
        int setSkillCount = request.getSetSkills() == null ? 0 : request.getSetSkills().size();

        return "일반 스킬 " + normalSkillCount + "개, 세트 스킬 " + setSkillCount + "개 요청";
    }
}