package io.MHWilds.mhwbuilder.domain.recommend.model;

import lombok.Getter;

/**
 * 장식주 배치를 위해 armor의 실제 슬롯 한 칸을 표현하는 모델
 *
 * 예:
 * - 몸통의 2슬롯 1칸
 * - 허리의 1슬롯 1칸
 */
@Getter
public class AvailableSlot {

    private final String part;
    private final int slotLevel;
    private boolean used;

    public AvailableSlot(String part, int slotLevel) {
        this.part = part;
        this.slotLevel = slotLevel;
        this.used = false;
    }

    public void use() {
        this.used = true;
    }
}