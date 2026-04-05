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

    private static final int MAX_SET_BUNDLE_COUNT = 50;
    private static final int MAX_COMPLETED_BUNDLE_COUNT = 20;
    private static final int MAX_FILLED_BUNDLE_PER_BASE = 10;
    private static final int MAX_CANDIDATE_COUNT = 30;
    private static final int MAX_CARD_COUNT = 10;

    private static final int ARMOR_SKILL_WEIGHT = 10;
    private static final int CHARM_SKILL_WEIGHT = 15;

    private static final String NO_RESULT_MESSAGE = "조건을 만족하는 세팅이 없습니다.";

    private final ArmorRepository armorRepository;
    private final EquipSkillRepository equipSkillRepository;
    private final CharmRepository charmRepository;
    private final DecorationRepository decorationRepository;
    private final ObjectMapper objectMapper;

    @Override
    public RecommendResultResponse recommend(RecommendRequest request) {
        List<SetSkillBundle> setSkillBundles = buildSetSkillBundles(request);
        List<SetSkillBundle> completedBundles = completeBundlesWithRemainingParts(setSkillBundles, request);
        List<RecommendCandidate> candidates = attachCharmCandidates(completedBundles, request);
        List<RecommendCandidate> validatedCandidates = filterCandidatesBySelectedSetSkillValidation(candidates, request);
        List<RecommendCandidate> feasibleCandidates = filterCandidatesByDecorationFeasibility(validatedCandidates, request);
        List<RecommendCardResponse> cards = buildRecommendCards(feasibleCandidates, request);

        if (cards.isEmpty()) {
            throw new IllegalArgumentException(NO_RESULT_MESSAGE);
        }

        return RecommendResultResponse.builder()
                .cards(cards)
                .build();
    }

    /** 선택한 세트 스킬 조건을 만족하는 기본 방어구 묶음을 만든다. */
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

    /** 세트 스킬 조건을 순서대로 만족시키며 조합을 확장한다. */
    private void buildSetSkillBundlesRecursive(
            List<SelectedSetSkillRequest> setSkills,
            int setSkillIndex,
            SetSkillBundle currentBundle,
            List<SetSkillBundle> result,
            Map<String, List<Armor>> setSkillArmorMap
    ) {
        if (result.size() >= MAX_SET_BUNDLE_COUNT) {
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

    /** 현재 세트 스킬에서 부족한 개수만큼만 방어구를 추가한다. */
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
        if (result.size() >= MAX_SET_BUNDLE_COUNT) {
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

    /** 현재 번들 안에서 해당 세트 스킬에 기여하는 방어구 수를 센다. */
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

    /** 비어 있는 부위를 채워 5부위 방어구를 완성한다. */
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

            if (result.size() >= MAX_COMPLETED_BUNDLE_COUNT) {
                return result.stream().limit(MAX_COMPLETED_BUNDLE_COUNT).toList();
            }
        }

        return result.stream().limit(MAX_COMPLETED_BUNDLE_COUNT).toList();
    }

    /** 아직 채워지지 않은 방어구 부위를 반환한다. */
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

    /** 비어 있는 부위를 순서대로 채운다. */
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

        if (result.size() >= MAX_FILLED_BUNDLE_PER_BASE) {
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

            if (result.size() >= MAX_FILLED_BUNDLE_PER_BASE) {
                return;
            }
        }
    }

    /** 부위별 상위 방어구 후보를 점수순으로 가져온다. */
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

    /** 방어구의 일반 스킬 기여도를 계산한다. */
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
                    score += armorSkill.getSkillLevel() * ARMOR_SKILL_WEIGHT;
                }
            }
        }

        return score;
    }

    /** 완성된 방어구 조합마다 호석 후보를 붙인다. */
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

                if (result.size() >= MAX_CANDIDATE_COUNT) {
                    return result;
                }
            }
        }

        return result;
    }

    /** 일반 스킬 기준으로 상위 호석 후보를 조회한다. */
    private List<Charm> getTopCharmCandidates(RecommendRequest request) {
        return charmRepository.findAll().stream()
                .map(charm -> new CharmScore(charm, calculateCharmScore(charm, request)))
                .sorted((a, b) -> Integer.compare(b.score(), a.score()))
                .limit(5)
                .map(CharmScore::charm)
                .toList();
    }

    /** 호석의 일반 스킬 기여도를 계산한다. */
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
                    score += charmSkill.getSkillLevel() * CHARM_SKILL_WEIGHT;
                }
            }
        }

        return score;
    }

    /** 최종 장비 기준으로 세트 스킬 조건을 다시 검증한다. */
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

    /** 후보가 모든 세트 스킬 조건을 만족하는지 확인한다. */
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

    /** 현재 장비 조합이 제공하는 일반 스킬 레벨을 합산한다. */
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

    /** 요청 대비 부족한 일반 스킬 레벨을 계산한다. */
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

    /** 방어구 번들의 슬롯 개수를 집계한다. */
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

    /** 부족한 스킬을 슬롯 수요로 변환한다. */
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

    /** 스킬별 최소 장식주 슬롯 레벨을 구한다. */
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

    /** 스킬별 대표 장식주를 고른다. */
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

    /** 현재 후보가 장식주로 마무리 가능한지 검사한다. */
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

    /** 장식주 배치가 가능한 후보만 남긴다. */
    private List<RecommendCandidate> filterCandidatesByDecorationFeasibility(
            List<RecommendCandidate> candidates,
            RecommendRequest request
    ) {
        return candidates.stream()
                .filter(candidate -> canSatisfyWithDecorations(candidate, request))
                .limit(MAX_CARD_COUNT)
                .toList();
    }

    /** 후보를 카드 응답으로 변환한다. */
    private List<RecommendCardResponse> buildRecommendCards(
            List<RecommendCandidate> candidates,
            RecommendRequest request
    ) {
        return candidates.stream()
                .sorted((a, b) -> Integer.compare(
                        calculateRemainingSlotScore(b, request),
                        calculateRemainingSlotScore(a, request)
                ))
                .limit(MAX_CARD_COUNT)
                .map(candidate -> toRecommendCardResponse(candidate, request))
                .toList();
    }

    /** 남는 슬롯 여유를 점수화한다. */
    private int calculateRemainingSlotScore(RecommendCandidate candidate, RecommendRequest request) {
        SlotCounts totalSlots = getTotalAvailableSlots(candidate);
        SlotDemand demand = calculateSlotDemand(candidate, request);

        int remainSlot1 = Math.max(0, totalSlots.getSlot1Count() - demand.getNeedSlot1());
        int remainSlot2 = Math.max(0, totalSlots.getSlot2Count() - demand.getNeedSlot2());
        int remainSlot3 = Math.max(0, totalSlots.getSlot3Count() - demand.getNeedSlot3());

        return remainSlot1 + (remainSlot2 * 2) + (remainSlot3 * 3);
    }

    /** 추천 후보 하나를 카드 응답으로 변환한다. */
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

    /** 장비 목록을 응답 형식으로 변환한다. */
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

    /** 카드 상세 정보를 조립한다. */
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

    /** 카드 제목을 만든다. */
    private String makeCardTitle(RecommendCandidate candidate, RecommendRequest request) {
        return buildRemainingSlotSummary(candidate, request);
    }

    /** 장착 가능한 슬롯 목록을 만든다. */
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

    /** 유효한 슬롯만 목록에 추가한다. */
    private void addSlotIfPresent(List<AvailableSlot> slots, String part, int slotLevel) {
        if (slotLevel >= 1 && slotLevel <= 3) {
            slots.add(new AvailableSlot(part, slotLevel));
        }
    }

    /** 부족한 스킬을 실제 장식주 배치 결과로 변환한다. */
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

    /** 요구 슬롯 이상을 수용할 수 있는 첫 번째 빈 슬롯을 찾는다. */
    private AvailableSlot findUsableSlot(List<AvailableSlot> slots, int requiredSlotLevel) {
        for (AvailableSlot slot : slots) {
            if (!slot.isUsed() && slot.getSlotLevel() >= requiredSlotLevel) {
                return slot;
            }
        }
        return null;
    }

    /** 최종 스킬 목록을 만든다. */
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

    /** 방어력과 속성 저항을 계산한다. */
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

    /** elementals JSON을 내부 저항 맵으로 변환한다. */
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

    /** 배열형 원소 저항 값을 누적한다. */
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

    /** 객체형 원소 저항 값을 누적한다. */
    private void mergeDirectElementalField(Map<String, Integer> result, JsonNode root, String key) {
        if (root.has(key) && root.get(key).canConvertToInt()) {
            result.merge(key, root.get(key).asInt(), Integer::sum);
        }
    }

    /** 원소 타입명을 내부 키로 정규화한다. */
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

    /** 원소 저항 수치를 추출한다. */
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

    /** 후보의 전체 슬롯 수를 반환한다. */
    private SlotCounts getTotalAvailableSlots(RecommendCandidate candidate) {
        return calculateAvailableSlotCounts(candidate.getArmorBundle());
    }

    /** 장식주 배치 후 남는 슬롯 요약 문자열을 만든다. */
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