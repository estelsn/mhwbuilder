package io.MHWilds.mhwbuilder.domain.recommend.model;

public class SlotCounts {

    private final int slot1Count;
    private final int slot2Count;
    private final int slot3Count;

    public SlotCounts(int slot1Count, int slot2Count, int slot3Count) {
        this.slot1Count = slot1Count;
        this.slot2Count = slot2Count;
        this.slot3Count = slot3Count;
    }

    public int getSlot1Count() {
        return slot1Count;
    }

    public int getSlot2Count() {
        return slot2Count;
    }

    public int getSlot3Count() {
        return slot3Count;
    }
}