package io.MHWilds.mhwbuilder.domain.recommend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.MHWilds.mhwbuilder.domain.equipment.entity.Armor;
import io.MHWilds.mhwbuilder.domain.equipment.entity.Charm;
import io.MHWilds.mhwbuilder.domain.equipment.entity.Decoration;
import io.MHWilds.mhwbuilder.domain.equipment.repository.ArmorRepository;
import io.MHWilds.mhwbuilder.domain.equipment.repository.CharmRepository;
import io.MHWilds.mhwbuilder.domain.equipment.repository.DecorationRepository;
import io.MHWilds.mhwbuilder.domain.recommend.dto.request.RecommendRequest;
import io.MHWilds.mhwbuilder.domain.recommend.dto.request.SelectedNormalSkillRequest;
import io.MHWilds.mhwbuilder.domain.recommend.dto.request.SelectedSetSkillRequest;
import io.MHWilds.mhwbuilder.domain.recommend.dto.response.DecorationDetailResponse;
import io.MHWilds.mhwbuilder.domain.recommend.dto.response.DefenseStatResponse;
import io.MHWilds.mhwbuilder.domain.recommend.dto.response.EquipDetailResponse;
import io.MHWilds.mhwbuilder.domain.recommend.dto.response.FinalSkillResponse;
import io.MHWilds.mhwbuilder.domain.recommend.dto.response.RecommendCardResponse;
import io.MHWilds.mhwbuilder.domain.recommend.dto.response.RecommendDetailResponse;
import io.MHWilds.mhwbuilder.domain.recommend.dto.response.RecommendResultResponse;
import io.MHWilds.mhwbuilder.domain.recommend.model.ArmorCandidate;
import io.MHWilds.mhwbuilder.domain.recommend.model.AvailableSlot;
import io.MHWilds.mhwbuilder.domain.recommend.model.DecorationCandidate;
import io.MHWilds.mhwbuilder.domain.recommend.model.DecorationPlacement;
import io.MHWilds.mhwbuilder.domain.recommend.model.RecommendCandidate;
import io.MHWilds.mhwbuilder.domain.recommend.model.SetSkillBundle;
import io.MHWilds.mhwbuilder.domain.recommend.model.SkillGap;
import io.MHWilds.mhwbuilder.domain.recommend.model.SlotCounts;
import io.MHWilds.mhwbuilder.domain.recommend.model.SlotDemand;
import io.MHWilds.mhwbuilder.domain.skill.entity.EquipSkill;
import io.MHWilds.mhwbuilder.domain.skill.repository.EquipSkillRepository;
import io.MHWilds.mhwbuilder.util.entityenums.EquipCategory;
import io.MHWilds.mhwbuilder.util.entityenums.EquipType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendServiceImpl implements RecommendService {

    private final ArmorRepository armorRepository;
    private final EquipSkillRepository equipSkillRepository;
    private final CharmRepository charmRepository;
    private final DecorationRepository decorationRepository;
    private final ObjectMapper objectMapper;

    /**
     * 추천 요청 진입점
     *
     * 현재 흐름:
     * 1) 사용자가 선택한 set skill 조건을 만족하는 armor bundle 생성
     * 2) 남은 armor 부위를 일반 스킬 점수 기준으로 채워 5부위 완성
     * 3) 완성된 armor bundle에 charm 후보 부착
     * 4) 최종 armor 5부위 기준으로 선택 set skill 조건 재검증
     * 5) 장식주 실제 배치 가능 여부 검사
     * 6) 통과한 candidate를 카드 응답으로 변환
     */
    @Override
    public RecommendResultResponse recommend(RecommendRequest request) {
        List<SetSkillBundle> setSkillBundles = buildSetSkillBundles(request);
        List<SetSkillBundle> completedBundles = completeBundlesWithRemainingParts(setSkillBundles, request);
        List<RecommendCandidate> candidates = attachCharmCandidates(completedBundles, request);
        List<RecommendCandidate> validatedCandidates = filterCandidatesBySelectedSetSkillValidation(candidates, request);
        List<RecommendCandidate> feasibleCandidates = filterCandidatesByDecorationFeasibility(validatedCandidates, request);
        List<RecommendCardResponse> cards = buildRecommendCards(feasibleCandidates, request);

        /**
         * 조건을 만족하는 추천 결과가 하나도 없으면 예외를 던진다.
         *
         * 프론트에서는 이 메시지를 받아 사용자에게
         * "조건을 만족하는 세팅이 없습니다" 형태로 보여주면 된다.
         */
        if (cards.isEmpty()) {
            throw new IllegalArgumentException("조건을 만족하는 세팅이 없습니다.");
        }

        return RecommendResultResponse.builder()
                .cards(cards)
                .build();
    }

    /**
     * 사용자가 선택한 set skill(시리즈/그룹) 조건을 만족하는 armor bundle 생성
     *
     * 핵심 규칙:
     * - 각 set skill 처리 시, 현재 bundle에 이미 포함된 armor가 해당 set skill에 몇 개 기여 중인지 먼저 계산
     * - 이미 requiredCount를 만족했다면 추가 armor 없이 다음 조건으로 진행
     * - 부족한 개수만큼만 새 armor를 조합해서 추가
     * - 같은 armor가 여러 set skill에 동시에 기여 가능
     * - 같은 부위 armor 중복 장착은 불가
     */
    private List<SetSkillBundle> buildSetSkillBundles(RecommendRequest request) {
        List<SelectedSetSkillRequest> setSkills = request.getSetSkills();

        if (setSkills == null || setSkills.isEmpty()) {
            return Collections.singletonList(new SetSkillBundle());
        }

        Map<String, List<Armor>> setSkillArmorMap = new LinkedHashMap<>();
        for (SelectedSetSkillRequest setSkillRequest : setSkills) {
            setSkillArmorMap.put(
                    setSkillRequest.getSetSkillId(),
                    armorRepository.findAllBySetSkillId(setSkillRequest.getSetSkillId())
            );
        }

        List<SetSkillBundle> result = new ArrayList<>();
        buildSetSkillBundlesRecursive(
                setSkills,
                0,
                new SetSkillBundle(),
                result,
                setSkillArmorMap
        );

        return result;
    }

    /**
     * 선택된 set skill 목록을 순서대로 만족시키는 bundle 조합 생성
     */
    private void buildSetSkillBundlesRecursive(
            List<SelectedSetSkillRequest> setSkills,
            int setSkillIndex,
            SetSkillBundle currentBundle,
            List<SetSkillBundle> result,
            Map<String, List<Armor>> setSkillArmorMap
    ) {
        if (result.size() >= 50) {
            return;
        }

        if (setSkillIndex == setSkills.size()) {
            result.add(currentBundle.copy());
            return;
        }

        SelectedSetSkillRequest setSkillRequest = setSkills.get(setSkillIndex);
        List<Armor> candidateArmors = setSkillArmorMap.getOrDefault(
                setSkillRequest.getSetSkillId(),
                Collections.emptyList()
        );

        int currentCount = countMatchedArmorsForSetSkill(currentBundle, candidateArmors);
        int remainingNeeded = setSkillRequest.getRequiredCount() - currentCount;

        if (remainingNeeded <= 0) {
            SetSkillBundle nextBundle = currentBundle.copy();
            nextBundle.addSatisfiedSetSkill(
                    setSkillRequest.getSetSkillId(),
                    setSkillRequest.getRequiredCount()
            );

            buildSetSkillBundlesRecursive(
                    setSkills,
                    setSkillIndex + 1,
                    nextBundle,
                    result,
                    setSkillArmorMap
            );
            return;
        }

        if (candidateArmors.isEmpty() || currentCount + candidateArmors.size() < setSkillRequest.getRequiredCount()) {
            return;
        }

        addArmorsForRemainingRequirement(
                candidateArmors,
                setSkillRequest,
                setSkills,
                setSkillIndex,
                0,
                remainingNeeded,
                currentBundle,
                result,
                setSkillArmorMap
        );
    }

    /**
     * 현재 set skill 조건에서 부족한 개수만큼만 armor를 추가로 채운다.
     *
     * 규칙:
     * - 이미 bundle에 있는 armor는 중복 추가하지 않는다.
     * - 같은 부위 armor 중복 장착은 불가
     * - 필요한 수만큼만 채우면 다음 set skill로 넘어간다.
     */
    private void addArmorsForRemainingRequirement(
            List<Armor> candidateArmors,
            SelectedSetSkillRequest currentSetSkillRequest,
            List<SelectedSetSkillRequest> allSetSkills,
            int currentSetSkillIndex,
            int startIndex,
            int remainingNeeded,
            SetSkillBundle currentBundle,
            List<SetSkillBundle> result,
            Map<String, List<Armor>> setSkillArmorMap
    ) {
        if (result.size() >= 50) {
            return;
        }

        if (remainingNeeded == 0) {
            SetSkillBundle nextBundle = currentBundle.copy();
            nextBundle.addSatisfiedSetSkill(
                    currentSetSkillRequest.getSetSkillId(),
                    currentSetSkillRequest.getRequiredCount()
            );

            buildSetSkillBundlesRecursive(
                    allSetSkills,
                    currentSetSkillIndex + 1,
                    nextBundle,
                    result,
                    setSkillArmorMap
            );
            return;
        }

        for (int i = startIndex; i < candidateArmors.size(); i++) {
            Armor armor = candidateArmors.get(i);

            if (currentBundle.containsArmor(armor.getId())) {
                continue;
            }

            if (currentBundle.containsPart(armor.getCategory())) {
                continue;
            }

            SetSkillBundle nextBundle = currentBundle.copy();
            nextBundle.addArmor(armor);

            addArmorsForRemainingRequirement(
                    candidateArmors,
                    currentSetSkillRequest,
                    allSetSkills,
                    currentSetSkillIndex,
                    i + 1,
                    remainingNeeded - 1,
                    nextBundle,
                    result,
                    setSkillArmorMap
            );
        }
    }

    /**
     * 현재 bundle 안에 있는 armor 중 특정 set skill에 기여하는 armor 수 계산
     */
    private int countMatchedArmorsForSetSkill(SetSkillBundle bundle, List<Armor> candidateArmors) {
        Set<String> candidateArmorIds = candidateArmors.stream()
                .map(Armor::getId)
                .collect(Collectors.toSet());

        int count = 0;
        for (Armor armor : bundle.getArmors()) {
            if (candidateArmorIds.contains(armor.getId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * 세트 조건 bundle을 기준으로 비어 있는 armor 부위를 채워 5부위 완성
     */
    private List<SetSkillBundle> completeBundlesWithRemainingParts(
            List<SetSkillBundle> baseBundles,
            RecommendRequest request
    ) {
        List<SetSkillBundle> result = new ArrayList<>();

        for (SetSkillBundle baseBundle : baseBundles) {
            List<EquipCategory> missingParts = getMissingArmorParts(baseBundle);

            if (missingParts.isEmpty()) {
                result.add(baseBundle);
                continue;
            }

            List<SetSkillBundle> completed = new ArrayList<>();
            fillMissingParts(baseBundle, missingParts, 0, request, completed);
            result.addAll(completed);

            if (result.size() >= 20) {
                return result.stream().limit(20).toList();
            }
        }

        return result.stream().limit(20).toList();
    }

    /**
     * 현재 bundle에서 비어 있는 armor 부위 반환
     */
    private List<EquipCategory> getMissingArmorParts(SetSkillBundle bundle) {
        List<EquipCategory> allArmorParts = List.of(
                EquipCategory.HEAD,
                EquipCategory.BODY,
                EquipCategory.ARM,
                EquipCategory.WAIST,
                EquipCategory.LEG
        );

        return allArmorParts.stream()
                .filter(part -> !bundle.containsPart(part))
                .toList();
    }

    /**
     * 비어 있는 부위를 순서대로 채운다.
     *
     * 현재 단계에서는 일반 스킬 점수 기준 상위 armor를 사용한다.
     * 최종 set skill 유효성은 이후 별도 검증 단계에서 다시 확인한다.
     */
    private void fillMissingParts(
            SetSkillBundle currentBundle,
            List<EquipCategory> missingParts,
            int partIndex,
            RecommendRequest request,
            List<SetSkillBundle> result
    ) {
        if (partIndex == missingParts.size()) {
            result.add(currentBundle);
            return;
        }

        if (result.size() >= 10) {
            return;
        }

        EquipCategory targetPart = missingParts.get(partIndex);
        List<ArmorCandidate> candidates = getTopArmorCandidatesForPart(targetPart, request);

        for (ArmorCandidate candidate : candidates) {
            Armor armor = candidate.getArmor();

            if (currentBundle.containsPart(armor.getCategory())) {
                continue;
            }

            SetSkillBundle nextBundle = currentBundle.copy();
            nextBundle.addArmor(armor);

            fillMissingParts(
                    nextBundle,
                    missingParts,
                    partIndex + 1,
                    request,
                    result
            );

            if (result.size() >= 10) {
                return;
            }
        }
    }

    /**
     * 특정 부위 armor 후보 중 일반 스킬 요청과 잘 맞는 armor를 점수화
     */
    private List<ArmorCandidate> getTopArmorCandidatesForPart(
            EquipCategory part,
            RecommendRequest request
    ) {
        return armorRepository.findByCategory(part).stream()
                .map(armor -> new ArmorCandidate(armor, calculateArmorScore(armor, request)))
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .limit(5)
                .toList();
    }

    /**
     * armor 하나가 사용자의 일반 스킬 요청에 얼마나 잘 맞는지 점수 계산
     */
    private int calculateArmorScore(Armor armor, RecommendRequest request) {
        List<SelectedNormalSkillRequest> normalSkills = request.getNormalSkills();

        if (normalSkills == null || normalSkills.isEmpty()) {
            return 0;
        }

        List<EquipSkill> armorSkills = equipSkillRepository.findByEquipTypeAndEquipId(
                EquipType.ARMOR,
                armor.getId()
        );

        int score = 0;

        for (SelectedNormalSkillRequest requestedSkill : normalSkills) {
            for (EquipSkill armorSkill : armorSkills) {
                if (armorSkill.getSkill().getId().equals(requestedSkill.getSkillId())) {
                    score += armorSkill.getSkillLevel() * 10;
                }
            }
        }

        return score;
    }

    /**
     * 완성된 armor bundle들에 대해, 요청 스킬과 잘 맞는 charm 후보를 붙인다.
     *
     * 현재 MVP 기준:
     * - 각 bundle마다 상위 charm 몇 개만 사용
     * - charm이 없어도 후보 하나는 만들기 위해 null 허용
     */
    private List<RecommendCandidate> attachCharmCandidates(
            List<SetSkillBundle> bundles,
            RecommendRequest request
    ) {
        List<RecommendCandidate> result = new ArrayList<>();
        List<Charm> topCharms = getTopCharmCandidates(request);

        for (SetSkillBundle bundle : bundles) {
            if (topCharms.isEmpty()) {
                result.add(new RecommendCandidate(bundle, null));
                continue;
            }

            for (Charm charm : topCharms) {
                result.add(new RecommendCandidate(bundle, charm));

                if (result.size() >= 30) {
                    return result;
                }
            }
        }

        return result;
    }

    /**
     * 요청 일반 스킬과 잘 맞는 charm 상위 후보 조회
     */
    private List<Charm> getTopCharmCandidates(RecommendRequest request) {
        return charmRepository.findAll().stream()
                .map(charm -> new CharmScore(charm, calculateCharmScore(charm, request)))
                .sorted((a, b) -> Integer.compare(b.score(), a.score()))
                .limit(5)
                .map(CharmScore::charm)
                .toList();
    }

    /**
     * charm 하나가 사용자의 일반 스킬 요청에 얼마나 잘 맞는지 점수 계산
     */
    private int calculateCharmScore(Charm charm, RecommendRequest request) {
        List<SelectedNormalSkillRequest> normalSkills = request.getNormalSkills();

        if (normalSkills == null || normalSkills.isEmpty()) {
            return 0;
        }

        List<EquipSkill> charmSkills = equipSkillRepository.findByEquipTypeAndEquipId(
                EquipType.CHARM,
                charm.getId()
        );

        int score = 0;

        for (SelectedNormalSkillRequest requestedSkill : normalSkills) {
            for (EquipSkill charmSkill : charmSkills) {
                if (charmSkill.getSkill().getId().equals(requestedSkill.getSkillId())) {
                    score += charmSkill.getSkillLevel() * 15;
                }
            }
        }

        return score;
    }

    /**
     * 최종 candidate 기준으로 사용자가 선택한 모든 set skill 조건을 다시 검증한다.
     *
     * 중요:
     * - buildSetSkillBundles()에서 한 번 만족했다고 끝내지 않는다.
     * - armor 5부위가 모두 완성된 뒤, 실제 최종 armor 조합 기준으로 다시 계산한다.
     */
    private List<RecommendCandidate> filterCandidatesBySelectedSetSkillValidation(
            List<RecommendCandidate> candidates,
            RecommendRequest request
    ) {
        List<SelectedSetSkillRequest> setSkills = request.getSetSkills();

        if (setSkills == null || setSkills.isEmpty()) {
            return candidates;
        }

        Map<String, List<Armor>> setSkillArmorMap = new HashMap<>();
        for (SelectedSetSkillRequest setSkillRequest : setSkills) {
            setSkillArmorMap.put(
                    setSkillRequest.getSetSkillId(),
                    armorRepository.findAllBySetSkillId(setSkillRequest.getSetSkillId())
            );
        }

        return candidates.stream()
                .filter(candidate -> satisfiesAllSelectedSetSkills(candidate, setSkills, setSkillArmorMap))
                .toList();
    }

    /**
     * candidate가 사용자가 선택한 모든 set skill 조건을 만족하는지 검사
     */
    private boolean satisfiesAllSelectedSetSkills(
            RecommendCandidate candidate,
            List<SelectedSetSkillRequest> setSkills,
            Map<String, List<Armor>> setSkillArmorMap
    ) {
        Set<String> equippedArmorIds = candidate.getArmorBundle().getArmors().stream()
                .map(Armor::getId)
                .collect(Collectors.toSet());

        for (SelectedSetSkillRequest setSkillRequest : setSkills) {
            List<Armor> matchedArmors = setSkillArmorMap.getOrDefault(
                    setSkillRequest.getSetSkillId(),
                    Collections.emptyList()
            );

            int actualCount = 0;
            for (Armor armor : matchedArmors) {
                if (equippedArmorIds.contains(armor.getId())) {
                    actualCount++;
                }
            }

            if (actualCount < setSkillRequest.getRequiredCount()) {
                return false;
            }
        }

        return true;
    }

    /**
     * armor + charm이 제공하는 현재 일반 스킬 총합 계산
     */
    private Map<String, Integer> calculateCurrentSkillLevels(RecommendCandidate candidate) {
        Map<String, Integer> currentSkillLevels = new HashMap<>();

        for (Armor armor : candidate.getArmorBundle().getArmors()) {
            List<EquipSkill> armorSkills = equipSkillRepository.findByEquipTypeAndEquipId(
                    EquipType.ARMOR,
                    armor.getId()
            );

            for (EquipSkill armorSkill : armorSkills) {
                String skillId = armorSkill.getSkill().getId();
                int skillLevel = armorSkill.getSkillLevel();
                currentSkillLevels.merge(skillId, skillLevel, Integer::sum);
            }
        }

        Charm charm = candidate.getCharm();
        if (charm != null) {
            List<EquipSkill> charmSkills = equipSkillRepository.findByEquipTypeAndEquipId(
                    EquipType.CHARM,
                    charm.getId()
            );

            for (EquipSkill charmSkill : charmSkills) {
                String skillId = charmSkill.getSkill().getId();
                int skillLevel = charmSkill.getSkillLevel();
                currentSkillLevels.merge(skillId, skillLevel, Integer::sum);
            }
        }

        return currentSkillLevels;
    }

    /**
     * 요청 일반 스킬 대비 현재 candidate의 부족 레벨 계산
     */
    private List<SkillGap> calculateSkillGaps(RecommendCandidate candidate, RecommendRequest request) {
        List<SelectedNormalSkillRequest> normalSkills = request.getNormalSkills();

        if (normalSkills == null || normalSkills.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Integer> currentSkillLevels = calculateCurrentSkillLevels(candidate);
        List<SkillGap> gaps = new ArrayList<>();

        for (SelectedNormalSkillRequest requestedSkill : normalSkills) {
            String skillId = requestedSkill.getSkillId();
            int targetLevel = requestedSkill.getTargetLevel();
            int currentLevel = currentSkillLevels.getOrDefault(skillId, 0);
            int remainingLevel = Math.max(0, targetLevel - currentLevel);

            gaps.add(new SkillGap(
                    skillId,
                    skillId,
                    targetLevel,
                    currentLevel,
                    remainingLevel
            ));
        }

        return gaps;
    }

    /**
     * 현재 bundle의 실제 사용 가능한 슬롯 개수 계산
     *
     * slot1Lv / slot2Lv / slot3Lv는 각 슬롯 칸의 레벨값으로 본다.
     * - 1이면 1슬롯 1개
     * - 2면 2슬롯 1개
     * - 3이면 3슬롯 1개
     */
    private SlotCounts calculateAvailableSlotCounts(SetSkillBundle bundle) {
        int slot1Count = 0;
        int slot2Count = 0;
        int slot3Count = 0;

        for (Armor armor : bundle.getArmors()) {
            int[] slots = {
                    armor.getSlot1Lv(),
                    armor.getSlot2Lv(),
                    armor.getSlot3Lv()
            };

            for (int slotLevel : slots) {
                if (slotLevel == 1) {
                    slot1Count++;
                } else if (slotLevel == 2) {
                    slot2Count++;
                } else if (slotLevel == 3) {
                    slot3Count++;
                }
            }
        }

        return new SlotCounts(slot1Count, slot2Count, slot3Count);
    }

    /**
     * 부족한 일반 스킬을 장식주 슬롯 수요로 변환
     */
    private SlotDemand calculateSlotDemand(RecommendCandidate candidate, RecommendRequest request) {
        List<SkillGap> gaps = calculateSkillGaps(candidate, request);
        Map<String, Integer> decorationSlotLevelMap = getDecorationSlotLevelMap();

        int needSlot1 = 0;
        int needSlot2 = 0;
        int needSlot3 = 0;

        for (SkillGap gap : gaps) {
            int remaining = gap.getRemainingLevel();

            if (remaining <= 0) {
                continue;
            }

            Integer requiredSlotLevel = decorationSlotLevelMap.get(gap.getSkillId());

            if (requiredSlotLevel == null) {
                return new SlotDemand(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            }

            if (requiredSlotLevel == 1) {
                needSlot1 += remaining;
            } else if (requiredSlotLevel == 2) {
                needSlot2 += remaining;
            } else if (requiredSlotLevel == 3) {
                needSlot3 += remaining;
            }
        }

        return new SlotDemand(needSlot1, needSlot2, needSlot3);
    }

    /**
     * 스킬별 장식주 최소 슬롯 레벨 조회
     *
     * 예:
     * 어떤 스킬이 2슬롯 장식주와 3슬롯 장식주 둘 다 있으면
     * 최소 요구 슬롯인 2를 기록한다.
     */
    private Map<String, Integer> getDecorationSlotLevelMap() {
        List<EquipSkill> decorationSkills = equipSkillRepository.findByEquipType(EquipType.DECORATION);

        Set<String> decorationIds = decorationSkills.stream()
                .map(EquipSkill::getEquipId)
                .collect(Collectors.toSet());

        Map<String, Decoration> decorationMap = decorationRepository.findAllById(decorationIds).stream()
                .collect(Collectors.toMap(Decoration::getId, decoration -> decoration));

        Map<String, Integer> skillMinSlotMap = new HashMap<>();

        for (EquipSkill equipSkill : decorationSkills) {
            String skillId = equipSkill.getSkill().getId();
            String equipId = equipSkill.getEquipId();
            Decoration decoration = decorationMap.get(equipId);

            if (decoration == null) {
                continue;
            }

            int slotLevel = decoration.getSlotLevel();
            skillMinSlotMap.merge(skillId, slotLevel, Math::min);
        }

        return skillMinSlotMap;
    }

    /**
     * 스킬별 대표 장식주 조회
     *
     * 현재 MVP 기준:
     * - 각 스킬에 대해 최소 슬롯으로 장착 가능한 장식주 1개를 대표 후보로 선택한다.
     */
    private Map<String, DecorationCandidate> getBestDecorationCandidateMap() {
        List<EquipSkill> decorationSkills = equipSkillRepository.findByEquipType(EquipType.DECORATION);

        Set<String> decorationIds = decorationSkills.stream()
                .map(EquipSkill::getEquipId)
                .collect(Collectors.toSet());

        Map<String, Decoration> decorationMap = decorationRepository.findAllById(decorationIds).stream()
                .collect(Collectors.toMap(Decoration::getId, decoration -> decoration));

        Map<String, DecorationCandidate> result = new HashMap<>();

        for (EquipSkill equipSkill : decorationSkills) {
            Decoration decoration = decorationMap.get(equipSkill.getEquipId());
            if (decoration == null) {
                continue;
            }

            String skillId = equipSkill.getSkill().getId();
            DecorationCandidate candidate = new DecorationCandidate(
                    decoration.getId(),
                    decoration.getName(),
                    skillId,
                    equipSkill.getSkill().getName(),
                    equipSkill.getSkillLevel(),
                    decoration.getSlotLevel()
            );

            DecorationCandidate current = result.get(skillId);

            if (current == null || candidate.slotLevel() < current.slotLevel()) {
                result.put(skillId, candidate);
            }
        }

        return result;
    }

    /**
     * 현재 candidate가 장식주로 실제 충족 가능한지 판정
     *
     * 판정 조건:
     * 1) need3 <= slot3
     * 2) need2 + need3 <= slot2 + slot3
     * 3) need1 + need2 + need3 <= slot1 + slot2 + slot3
     */
    private boolean canSatisfyWithDecorations(RecommendCandidate candidate, RecommendRequest request) {
        SlotCounts slots = calculateAvailableSlotCounts(candidate.getArmorBundle());
        SlotDemand demand = calculateSlotDemand(candidate, request);

        int need1 = demand.getNeedSlot1();
        int need2 = demand.getNeedSlot2();
        int need3 = demand.getNeedSlot3();

        int slot1 = slots.getSlot1Count();
        int slot2 = slots.getSlot2Count();
        int slot3 = slots.getSlot3Count();

        if (need3 > slot3) {
            return false;
        }

        if (need2 + need3 > slot2 + slot3) {
            return false;
        }

        return need1 + need2 + need3 <= slot1 + slot2 + slot3;
    }

    /**
     * set skill 검증까지 통과한 candidate 중 장식주로 실제 완성 가능한 후보만 남긴다.
     */
    private List<RecommendCandidate> filterCandidatesByDecorationFeasibility(
            List<RecommendCandidate> candidates,
            RecommendRequest request
    ) {
        return candidates.stream()
                .filter(candidate -> canSatisfyWithDecorations(candidate, request))
                .limit(10)
                .toList();
    }

    /**
     * 완성 candidate를 카드 응답으로 변환
     *
     * 현재는 장식주 배치 후 남는 슬롯 여유가 많은 후보를 우선으로 정렬한다.
     */
    private List<RecommendCardResponse> buildRecommendCards(
            List<RecommendCandidate> candidates,
            RecommendRequest request
    ) {
        return candidates.stream()
                .sorted((a, b) -> Integer.compare(
                        calculateRemainingSlotScore(b, request),
                        calculateRemainingSlotScore(a, request)
                ))
                .limit(10)
                .map(candidate -> toRecommendCardResponse(candidate, request))
                .toList();
    }
    /**
     * 후보의 남는 슬롯 여유를 점수화한다.
     *
     * 가중치:
     * - 3슬롯 여유 > 2슬롯 여유 > 1슬롯 여유
     *
     * 현재는 단순 정렬용 점수로 사용한다.
     */
    private int calculateRemainingSlotScore(RecommendCandidate candidate, RecommendRequest request) {
        SlotCounts totalSlots = getTotalAvailableSlots(candidate);
        SlotDemand demand = calculateSlotDemand(candidate, request);

        int remainSlot1 = Math.max(0, totalSlots.getSlot1Count() - demand.getNeedSlot1());
        int remainSlot2 = Math.max(0, totalSlots.getSlot2Count() - demand.getNeedSlot2());
        int remainSlot3 = Math.max(0, totalSlots.getSlot3Count() - demand.getNeedSlot3());

        return remainSlot1 + (remainSlot2 * 2) + (remainSlot3 * 3);
    }

    /**
     * RecommendCandidate 하나를 카드 응답 하나로 변환
     *
     * 현재 카드 상세에는 아래 정보가 포함된다.
     * - 장비 목록
     * - 장식주 배치 결과
     * - 최종 스킬 목록
     * - 방어력 / 원소 저항
     */
    private RecommendCardResponse toRecommendCardResponse(
            RecommendCandidate candidate,
            RecommendRequest request
    ) {
        List<EquipDetailResponse> equips = buildEquipDetails(candidate);
        List<DecorationDetailResponse> decorations = buildDecorationDetails(candidate, request);
        List<FinalSkillResponse> finalSkills = buildFinalSkills(candidate, decorations);
        DefenseStatResponse stats = calculateDefenseStats(candidate);

        RecommendDetailResponse detail = buildRecommendDetail(
                equips,
                decorations,
                finalSkills,
                stats
        );

        return RecommendCardResponse.builder()
                .title(makeCardTitle(candidate, request))
                .detail(detail)
                .build();
    }

    /**
     * armor 목록을 EquipDetailResponse 목록으로 변환
     *
     * 표시 순서:
     * 머리 -> 몸통 -> 팔 -> 허리 -> 다리 -> 호석
     */
    private List<EquipDetailResponse> buildEquipDetails(RecommendCandidate candidate) {
        List<Armor> sortedArmors = candidate.getArmorBundle().getArmors().stream()
                .sorted((a, b) -> Integer.compare(getEquipOrder(a.getCategory()), getEquipOrder(b.getCategory())))
                .toList();

        List<EquipDetailResponse> equips = new ArrayList<>();

        for (Armor armor : sortedArmors) {
            equips.add(EquipDetailResponse.builder()
                    .part(toPartLabel(armor.getCategory()))
                    .name(armor.getName())
                    .build());
        }

        Charm charm = candidate.getCharm();
        if (charm != null) {
            equips.add(EquipDetailResponse.builder()
                    .part("호석")
                    .name(charm.getName())
                    .build());
        }

        return equips;
    }

    private int getEquipOrder(EquipCategory category) {
        return switch (category) {
            case HEAD -> 1;
            case BODY -> 2;
            case ARM -> 3;
            case WAIST -> 4;
            case LEG -> 5;
            default -> 99;
        };
    }

    private String toPartLabel(EquipCategory category) {
        return switch (category) {
            case HEAD -> "머리";
            case BODY -> "몸통";
            case ARM -> "팔";
            case WAIST -> "허리";
            case LEG -> "다리";
            default -> category.name();
        };
    }

    /**
     * 카드 상세 영역 생성
     *
     * 현재 포함 정보:
     * - equips: 장비/호석 목록
     * - decorations: 실제 장식주 배치 결과
     * - finalSkills: armor + charm + decoration 합산 최종 스킬
     * - stats: 방어력 및 5속성 저항
     */
    private RecommendDetailResponse buildRecommendDetail(
            List<EquipDetailResponse> equips,
            List<DecorationDetailResponse> decorations,
            List<FinalSkillResponse> finalSkills,
            DefenseStatResponse stats
    ) {
        return RecommendDetailResponse.builder()
                .equips(equips)
                .decorations(decorations)
                .finalSkills(finalSkills)
                .stats(stats)
                .build();
    }

    /**
     * 카드 제목 생성
     *
     * 현재는 장식주 배치 후 남는 슬롯 정보만 표시한다.
     * (세트 스킬은 이미 필터 단계에서 보장되므로 별도 표시하지 않는다)
     */
    private String makeCardTitle(RecommendCandidate candidate, RecommendRequest request) {
        return buildRemainingSlotSummary(candidate, request);
    }

    /**
     * armor의 실제 슬롯을 장식주 배치용 슬롯 목록으로 변환한다.
     *
     * 처리 순서:
     * - armor를 표시 순서대로 정렬
     * - 각 armor의 slot1Lv / slot2Lv / slot3Lv를 실제 슬롯 1칸으로 변환
     * - 높은 슬롯부터 먼저 사용하도록 내림차순 정렬
     */
    private List<AvailableSlot> buildAvailableSlots(RecommendCandidate candidate) {
        List<Armor> sortedArmors = candidate.getArmorBundle().getArmors().stream()
                .sorted((a, b) -> Integer.compare(getEquipOrder(a.getCategory()), getEquipOrder(b.getCategory())))
                .toList();

        List<AvailableSlot> slots = new ArrayList<>();

        for (Armor armor : sortedArmors) {
            String part = toPartLabel(armor.getCategory());

            addSlotIfPresent(slots, part, armor.getSlot1Lv());
            addSlotIfPresent(slots, part, armor.getSlot2Lv());
            addSlotIfPresent(slots, part, armor.getSlot3Lv());
        }

        slots.sort((a, b) -> Integer.compare(b.getSlotLevel(), a.getSlotLevel()));
        return slots;
    }

    /**
     * slotLevel이 실제 슬롯 하나를 의미할 때만 배치 가능 슬롯 목록에 추가한다.
     */
    private void addSlotIfPresent(List<AvailableSlot> slots, String part, int slotLevel) {
        if (slotLevel >= 1 && slotLevel <= 3) {
            slots.add(new AvailableSlot(part, slotLevel));
        }
    }

    /**
     * 요청 스킬 부족분을 실제 장식주 배치 결과로 변환한다.
     *
     * 현재 규칙:
     * - 부족한 스킬만 대상으로 한다.
     * - 높은 슬롯 장식주부터 먼저 배치한다.
     * - 실제 사용 가능한 armor 슬롯에만 배치한다.
     */
    private List<DecorationDetailResponse> buildDecorationDetails(
            RecommendCandidate candidate,
            RecommendRequest request
    ) {
        List<SkillGap> gaps = calculateSkillGaps(candidate, request);
        Map<String, DecorationCandidate> decorationMap = getBestDecorationCandidateMap();
        List<AvailableSlot> availableSlots = buildAvailableSlots(candidate);

        List<DecorationPlacement> placements = new ArrayList<>();

        List<SkillGap> sortedGaps = gaps.stream()
                .filter(gap -> gap.getRemainingLevel() > 0)
                .sorted((a, b) -> {
                    DecorationCandidate da = decorationMap.get(a.getSkillId());
                    DecorationCandidate db = decorationMap.get(b.getSkillId());

                    int aSlot = da == null ? -1 : da.slotLevel();
                    int bSlot = db == null ? -1 : db.slotLevel();

                    return Integer.compare(bSlot, aSlot);
                })
                .toList();

        for (SkillGap gap : sortedGaps) {
            DecorationCandidate decoration = decorationMap.get(gap.getSkillId());

            if (decoration == null) {
                continue;
            }

            int remaining = gap.getRemainingLevel();

            for (int i = 0; i < remaining; i++) {
                AvailableSlot slot = findUsableSlot(availableSlots, decoration.slotLevel());

                if (slot == null) {
                    break;
                }

                slot.use();

                placements.add(new DecorationPlacement(
                        slot.getPart(),
                        decoration.decorationName(),
                        decoration.skillName(),
                        1,
                        decoration.slotLevel()
                ));
            }
        }

        return placements.stream()
                .collect(Collectors.groupingBy(
                        p -> p.decorationName() + "|" + p.skillName() + "|" + p.slotLevel(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .values().stream()
                .map(group -> {
                    DecorationPlacement first = group.get(0);

                    return DecorationDetailResponse.builder()
                            .part("여러 부위")
                            .name(first.decorationName())
                            .skillName(first.skillName())
                            .level(first.level())
                            .slotLevel(first.slotLevel())
                            .count(group.size())
                            .build();
                })
                .toList();
    }

    /**
     * 요구 슬롯 레벨 이상을 수용할 수 있는 첫 번째 미사용 슬롯을 반환한다.
     */
    private AvailableSlot findUsableSlot(List<AvailableSlot> slots, int requiredSlotLevel) {
        for (AvailableSlot slot : slots) {
            if (!slot.isUsed() && slot.getSlotLevel() >= requiredSlotLevel) {
                return slot;
            }
        }
        return null;
    }

    /**
     * 최종 스킬 목록 생성
     *
     * 합산 대상:
     * - armor 기본 스킬
     * - charm 기본 스킬
     * - 실제 배치된 decoration 스킬
     *
     * 현재는 동일 스킬명 기준으로 레벨을 합산한다.
     */
    private List<FinalSkillResponse> buildFinalSkills(
            RecommendCandidate candidate,
            List<DecorationDetailResponse> decorations
    ) {
        Map<String, Integer> skillLevels = new HashMap<>();

        for (Armor armor : candidate.getArmorBundle().getArmors()) {
            List<EquipSkill> armorSkills = equipSkillRepository.findByEquipTypeAndEquipId(
                    EquipType.ARMOR,
                    armor.getId()
            );

            for (EquipSkill armorSkill : armorSkills) {
                skillLevels.merge(
                        armorSkill.getSkill().getName(),
                        armorSkill.getSkillLevel(),
                        Integer::sum
                );
            }
        }

        Charm charm = candidate.getCharm();
        if (charm != null) {
            List<EquipSkill> charmSkills = equipSkillRepository.findByEquipTypeAndEquipId(
                    EquipType.CHARM,
                    charm.getId()
            );

            for (EquipSkill charmSkill : charmSkills) {
                skillLevels.merge(
                        charmSkill.getSkill().getName(),
                        charmSkill.getSkillLevel(),
                        Integer::sum
                );
            }
        }

        for (DecorationDetailResponse decoration : decorations) {
            skillLevels.merge(
                    decoration.getSkillName(),
                    decoration.getLevel(),
                    Integer::sum
            );
        }

        return skillLevels.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(entry -> FinalSkillResponse.builder()
                        .skillName(entry.getKey())
                        .level(entry.getValue())
                        .build())
                .toList();
    }

    /**
     * 최종 추천 후보의 방어 스탯 계산
     *
     * 계산 대상:
     * - defense: armor 5부위의 물리 방어력 합
     * - fire/water/thunder/ice/dragon: armor의 elementals JSON 합산
     *
     * 주의:
     * - charm / decoration은 방어 스탯 계산에서 제외한다.
     */
    private DefenseStatResponse calculateDefenseStats(RecommendCandidate candidate) {
        int defense = 0;
        int fireRes = 0;
        int waterRes = 0;
        int thunderRes = 0;
        int iceRes = 0;
        int dragonRes = 0;

        for (Armor armor : candidate.getArmorBundle().getArmors()) {
            defense += armor.getDefense();

            Map<String, Integer> elementalMap = parseElementalResistances(armor.getElementals());

            fireRes += elementalMap.getOrDefault("fire", 0);
            waterRes += elementalMap.getOrDefault("water", 0);
            thunderRes += elementalMap.getOrDefault("thunder", 0);
            iceRes += elementalMap.getOrDefault("ice", 0);
            dragonRes += elementalMap.getOrDefault("dragon", 0);
        }

        return DefenseStatResponse.builder()
                .defense(defense)
                .fireRes(fireRes)
                .waterRes(waterRes)
                .thunderRes(thunderRes)
                .iceRes(iceRes)
                .dragonRes(dragonRes)
                .build();
    }

    /**
     * armor.elementals JSON 문자열을 파싱하여 속성 저항 맵으로 변환
     *
     * 지원 형태 예시:
     * 1) 배열 형태
     * [
     *   {"element_type":"fire","value":2},
     *   {"element_type":"water","value":-1}
     * ]
     *
     * 2) object 내부 elementals 배열
     * {"elementals":[...]}
     *
     * 3) 단순 object 형태
     * {"fire":2,"water":-1,"thunder":0,"ice":1,"dragon":0}
     *
     * 파싱 실패 시에는 빈 맵을 반환한다.
     */
    private Map<String, Integer> parseElementalResistances(String elementalsJson) {
        Map<String, Integer> result = new HashMap<>();

        if (elementalsJson == null || elementalsJson.isBlank()) {
            return result;
        }

        try {
            JsonNode root = objectMapper.readTree(elementalsJson);

            if (root.isArray()) {
                for (JsonNode node : root) {
                    mergeElementalValue(result, node);
                }
                return result;
            }

            if (root.isObject()) {
                if (root.has("elementals") && root.get("elementals").isArray()) {
                    for (JsonNode node : root.get("elementals")) {
                        mergeElementalValue(result, node);
                    }
                    return result;
                }

                mergeDirectElementalField(result, root, "fire");
                mergeDirectElementalField(result, root, "water");
                mergeDirectElementalField(result, root, "thunder");
                mergeDirectElementalField(result, root, "ice");
                mergeDirectElementalField(result, root, "dragon");
            }
        } catch (Exception e) {
            return Collections.emptyMap();
        }

        return result;
    }

    /**
     * elementals 배열 내부 원소 저항 1건을 누적한다.
     */
    private void mergeElementalValue(Map<String, Integer> result, JsonNode node) {
        if (node == null || !node.isObject()) {
            return;
        }

        String elementType = normalizeElementType(node);
        if (elementType == null) {
            return;
        }

        int value = extractElementalValue(node);
        result.merge(elementType, value, Integer::sum);
    }

    /**
     * 단순 object 형태의 원소 저항 필드를 누적한다.
     */
    private void mergeDirectElementalField(Map<String, Integer> result, JsonNode root, String key) {
        if (root.has(key) && root.get(key).canConvertToInt()) {
            result.merge(key, root.get(key).asInt(), Integer::sum);
        }
    }

    /**
     * 원소 타입명을 내부 표준 키로 정규화한다.
     *
     * 반환 키:
     * - fire
     * - water
     * - thunder
     * - ice
     * - dragon
     */
    private String normalizeElementType(JsonNode node) {
        String rawType = null;

        if (node.has("element_type")) {
            rawType = node.get("element_type").asText();
        } else if (node.has("elementType")) {
            rawType = node.get("elementType").asText();
        } else if (node.has("type")) {
            rawType = node.get("type").asText();
        } else if (node.has("name")) {
            rawType = node.get("name").asText();
        }

        if (rawType == null) {
            return null;
        }

        return switch (rawType.trim().toLowerCase()) {
            case "fire", "불", "화" -> "fire";
            case "water", "물", "수" -> "water";
            case "thunder", "lightning", "번개", "뇌" -> "thunder";
            case "ice", "얼음", "빙" -> "ice";
            case "dragon", "용" -> "dragon";
            default -> null;
        };
    }

    /**
     * 원소 저항 수치를 추출한다.
     *
     * 지원 필드:
     * - value
     * - resistance
     *
     * raw 데이터에 negative 플래그가 있으면 음수 처리도 반영한다.
     */
    private int extractElementalValue(JsonNode node) {
        int value = 0;

        if (node.has("value") && node.get("value").canConvertToInt()) {
            value = node.get("value").asInt();
        } else if (node.has("resistance") && node.get("resistance").canConvertToInt()) {
            value = node.get("resistance").asInt();
        }

        if (node.has("negative") && node.get("negative").asBoolean(false)) {
            value = -Math.abs(value);
        }

        return value;
    }

    private record CharmScore(Charm charm, int score) {}

    /**
     * 현재 후보의 전체 슬롯 수 계산
     *
     * 반환값:
     * - slot1: 1슬롯 개수
     * - slot2: 2슬롯 개수
     * - slot3: 3슬롯 개수
     */
    private SlotCounts getTotalAvailableSlots(RecommendCandidate candidate) {
        return calculateAvailableSlotCounts(candidate.getArmorBundle());
    }
    /**
     * 장식주 배치 후 남는 슬롯 정보를 문자열로 반환한다.

     */
    private String buildRemainingSlotSummary(RecommendCandidate candidate, RecommendRequest request) {
        SlotCounts totalSlots = getTotalAvailableSlots(candidate);
        SlotDemand demand = calculateSlotDemand(candidate, request);

        int remainSlot1 = Math.max(0, totalSlots.getSlot1Count() - demand.getNeedSlot1());
        int remainSlot2 = Math.max(0, totalSlots.getSlot2Count() - demand.getNeedSlot2());
        int remainSlot3 = Math.max(0, totalSlots.getSlot3Count() - demand.getNeedSlot3());

        return "슬롯 여유: "
                + "1[" + remainSlot1 + "] "
                + "2[" + remainSlot2 + "] "
                + "3[" + remainSlot3 + "]";
    }
}