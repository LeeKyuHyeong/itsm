package com.itsm.core.domain.inspection;

import com.itsm.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_inspection_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    @Column(name = "inspection_id", nullable = false)
    private Long inspectionId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "result_value", columnDefinition = "TEXT")
    private String resultValue;

    @Column(name = "is_normal", nullable = false, columnDefinition = "char(1)")
    private String isNormal;

    @Column(name = "remark", length = 500)
    private String remark;

    @Builder
    public InspectionResult(Long inspectionId, Long itemId, String resultValue,
                            String isNormal, String remark) {
        this.inspectionId = inspectionId;
        this.itemId = itemId;
        this.resultValue = resultValue;
        this.isNormal = isNormal != null ? isNormal : "Y";
        this.remark = remark;
    }

    public void update(String resultValue, String isNormal, String remark) {
        this.resultValue = resultValue;
        this.isNormal = isNormal;
        this.remark = remark;
    }
}
