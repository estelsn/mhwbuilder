package io.MHWilds.mhwbuilder.crawler.equipment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class RawSlotDto {
    private int count;
    private List<Integer> values; // [2,2,"-"] → 숫자만 필터링 추천
}