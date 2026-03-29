package io.MHWilds.mhwbuilder.util.entityenums;

import lombok.Getter;

@Getter
public enum GearSkillType {
    //Decoration
    WEAPON(1, "무기용"),
    ARMOR(2, "방어구용");

    private final int code;
    private final String label;

    GearSkillType(int code, String label){
        this.code = code;
        this.label = label;
    }

    public static GearSkillType fromCode(int code){
        for(GearSkillType gsType : values()){
            if(gsType.code == code){
                return gsType;
            }
        }
        throw new IllegalArgumentException("분류를 알 수 없는 장식품 " + code);
    }

}
