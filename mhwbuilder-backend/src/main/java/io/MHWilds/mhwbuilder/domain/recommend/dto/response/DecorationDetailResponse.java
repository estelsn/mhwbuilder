package io.MHWilds.mhwbuilder.domain.recommend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DecorationDetailResponse {
    private String part;       // 어느 부위에 넣었는지
    private String name;       // 장식주 이름
    private String skillName;  // 채워주는 스킬명
    private int level;         // 몇 레벨 채웠는지
    private int slotLevel;     // 필요한 슬롯 레벨
    private int count;
}