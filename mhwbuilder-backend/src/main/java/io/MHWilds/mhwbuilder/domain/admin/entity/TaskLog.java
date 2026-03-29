package io.MHWilds.mhwbuilder.domain.admin.entity;

import io.MHWilds.mhwbuilder.util.KsuidUtil;
import io.MHWilds.mhwbuilder.util.entityenums.LogType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_log")
@Getter
@Setter
@NoArgsConstructor
public class TaskLog { //작업한 내용
    @Id
    @Column(nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogType type; // 작업 타입

    @Lob
    @Column(nullable = false)
    private String description; // 설명

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 작업일자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private MHAdmin admin; // 작업 관리자

    @PrePersist
    private void onCreate(){
        if(this.id == null){
            this.id = KsuidUtil.generate("tasklog");
        }
    }
}
