package io.MHWilds.mhwbuilder.domain.buildset.entity;

import io.MHWilds.mhwbuilder.domain.user.entity.MHUser;
import io.MHWilds.mhwbuilder.util.KsuidUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "build_set")
@Getter
@Setter
@NoArgsConstructor
public class BuildSet {

    @Id
    @Column(nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mh_user_id")
    private MHUser mhUser; //사용자 연동

    @Column(nullable = false)
    private String title; //제목

    @Lob
    @Column
    private String memo; //메모, 설명 등

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0; //좋아요 카운트

    @Column(name = "public_use", nullable = false)
    private boolean publicUse = false; //공유 여부

    @Column(name = "private_at")
    private LocalDateTime privateAt; //공유 비활성화 날짜

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_build_id")
    private BuildSet originalBuild; //원 빌드

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; //생성일

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; //수정일

    //하위 테이블 리스트
    @OneToMany(mappedBy = "buildSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BuildEquip> buildEquips = new ArrayList<>();

    @OneToMany(mappedBy = "buildSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BuildPart> buildParts = new ArrayList<>();

    @OneToMany(mappedBy = "buildSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BuildSetLike> buildSetLikes = new ArrayList<>();


    //연관관계 저장 메서드
    public void addBuildEquip(BuildEquip buildEquip) {
        this.buildEquips.add(buildEquip);
        buildEquip.setBuildSet(this);
    }

    public void addBuildPart(BuildPart buildPart) {
        this.buildParts.add(buildPart);
        buildPart.setBuildSet(this);
    }

    public void addBuildSetLike(BuildSetLike buildSetLike) {
        this.buildSetLikes.add(buildSetLike);
        buildSetLike.setBuildSet(this);
    }

    @PrePersist //자동으로 현재시각 저장
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if(this.id == null) {
            this.id = KsuidUtil.generate("buildSet");
        }
    }

    @PreUpdate //수정시 갱신
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
