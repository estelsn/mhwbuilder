package io.MHWilds.mhwbuilder.util.entityenums;


public enum EquipCategory { //BuildEquip, Armor
    WEAPON(0, "무기"),
    HEAD(1, "머리"),
    BODY(2, "몸통"),
    ARM(3, "팔"),
    WAIST(4, "허리"),
    LEG(5, "다리"),
    CHARM(6, "호석");

    private final int code;
    private final String label;

    EquipCategory(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }
    public String getLabel() {
        return label;
    }
    public static EquipCategory fromEquipCode(int code) {
        for (EquipCategory category : values()) {
            if (category.code == code) {
                return category;
            }
        }
        throw new IllegalArgumentException("알 수 없는 장비 카테고리 코드: " + code);
    }

}