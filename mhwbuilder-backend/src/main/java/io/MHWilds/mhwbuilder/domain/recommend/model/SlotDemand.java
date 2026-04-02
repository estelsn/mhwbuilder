package io.MHWilds.mhwbuilder.domain.recommend.model;

public class SlotDemand {

    private final int needSlot1;
    private final int needSlot2;
    private final int needSlot3;

    public SlotDemand(int needSlot1, int needSlot2, int needSlot3) {
        this.needSlot1 = needSlot1;
        this.needSlot2 = needSlot2;
        this.needSlot3 = needSlot3;
    }

    public int getNeedSlot1() {
        return needSlot1;
    }

    public int getNeedSlot2() {
        return needSlot2;
    }

    public int getNeedSlot3() {
        return needSlot3;
    }
}