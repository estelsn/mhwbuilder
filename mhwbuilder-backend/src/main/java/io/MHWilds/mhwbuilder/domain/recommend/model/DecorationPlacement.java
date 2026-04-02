package io.MHWilds.mhwbuilder.domain.recommend.model;

/**
 * 실제 장식주 배치 결과 1건
 *
 * 예:
 * - 머리 / 공격주 / 공격 / 1레벨 / 2슬롯
 */
public record DecorationPlacement(
        String part,
        String decorationName,
        String skillName,
        int level,
        int slotLevel
) {
}