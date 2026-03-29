package io.MHWilds.mhwbuilder.domain.admin.entity;

import io.MHWilds.mhwbuilder.util.KsuidUtil;
import io.MHWilds.mhwbuilder.util.entityenums.LogStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "update_log")
@Getter
@Setter
@NoArgsConstructor
public class UpdateLog {

    @Id
    private String id; // PK

    @Column(nullable = false)
    private LocalDateTime runAt; // 실행 시각

    @Column(nullable = false)
    private String type; // 업데이트 종류

    @Enumerated(EnumType.STRING)
    @Column
    private LogStatus status; // 상태

    @Column(nullable = false)
    private int affectedCount = 0; // 영향받은 항목 수

    @Lob
    private String message; // 상세 메시지

    private Long durationMS; // 수행 시간(ms)

    @PrePersist
    private void onCreate(){
        if(this.id == null){
            this.id = KsuidUtil.generate("updateLog");
        }
    }
}
