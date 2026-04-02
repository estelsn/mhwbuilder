package io.MHWilds.mhwbuilder.domain.recommend.model;

/**
 * 특정 스킬을 채우기 위해 사용할 대표 장식주 정보
 *
 * 현재 MVP 기준:
 * - 스킬별 최소 슬롯 장식주 1개를 대표 후보로 사용한다.
 * - 추후 여러 장식주 조합 최적화가 필요하면 이 모델을 확장하면 된다.
 */
public record DecorationCandidate(
        String decorationId,
        String decorationName,
        String skillId,
        String skillName,
        int skillLevel,
        int slotLevel
) {
}