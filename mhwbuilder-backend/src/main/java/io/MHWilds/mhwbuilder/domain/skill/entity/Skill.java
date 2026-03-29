package io.MHWilds.mhwbuilder.domain.skill.entity;

import io.MHWilds.mhwbuilder.util.KsuidUtil;
import io.MHWilds.mhwbuilder.util.entityenums.SkillCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "skill")
@Getter
@Setter
@NoArgsConstructor    // JPA 필수 기본 생성자
public class Skill { //그룹 스킬, 시리즈 스킬 제외한 모든 스킬 모음

    @Id
    @Column(nullable = false)
    private String id;

    @Column(name = "external_code", nullable = false, unique = true)
    private int code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillCategory category; //스킬타입. 공, 방, 등등등

    @Column(nullable = false, unique = true)
    private String name; //스킬 이름

    @Column(nullable = false)
    private String description; //설명

    @ElementCollection//값 타입 컬렉션(list, set 매핑)s
    @CollectionTable( //컬렉션 데이터 저장 테이블 정의
            name = "skill_level_description",
            joinColumns = @JoinColumn(name = "skill_id") //pk 연동
    )
    @Column(nullable = false)
    @OrderColumn(name = "level_order")//정렬
    private List<String> levels; //레벨당 설명

    @Column(name = "max_level", nullable = false)
    private int maxLevel; //최대 레벨

    private String version; //추가 버전.

    @PrePersist
    private void onCreate() {
        if (this.id == null){
            this.id = KsuidUtil.generate("skill");
        }
    }
}
