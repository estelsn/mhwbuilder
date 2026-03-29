package io.MHWilds.mhwbuilder.domain.user.entity;


import io.MHWilds.mhwbuilder.domain.buildset.entity.BuildSet;
import io.MHWilds.mhwbuilder.domain.buildset.entity.BuildSetScrap;
import io.MHWilds.mhwbuilder.util.KsuidUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="mhuser")
@Setter
@Getter
@NoArgsConstructor
public class MHUser { //사용자 정보. UserSocialAccount와 연동

    @Id
    @Column(nullable = false)
    private String id; //식별자

    @Column(nullable = false, unique = true)
    private String name; //닉네임

    @Column(unique = true)
    private String email; //이메일. 소셜로그인 하면 없을 수 있음

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; //생성일

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; //수정일

    @OneToMany(mappedBy = "mhUser", //FK 컬럼이 존재하는 테이블의 해당 필드명. JPA 레벨 작동이므로  @Column(name= ) 이랑은 관계없음.
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE) //MHUser 데이터 삭제(=회원탈퇴) 시 연관 데이터 다 삭제
    private List<BuildSet> buildSets = new ArrayList<>();

    @OneToMany(mappedBy = "mhUser",
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE,
            orphanRemoval = true) //리스트에서 데이터를 지우면 BuildSet에서 해당 데이터 같이 지워짐. 반대는 안되니까 BuildSet 삭제시 리스트삭제로 해결할 것.
    private List<BuildSetScrap> scraps = new ArrayList<>();


    @PrePersist //자동으로 현재시각 저장. 서비스에서 호출 없이도 insert 직전 실행됨.
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.id == null){
            this.id = KsuidUtil.generate("mhUser");
        }
    }

    @PreUpdate //수정시 갱신
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
