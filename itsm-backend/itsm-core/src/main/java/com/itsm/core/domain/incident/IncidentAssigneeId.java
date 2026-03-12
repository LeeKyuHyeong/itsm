package com.itsm.core.domain.incident;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class IncidentAssigneeId implements Serializable {
    private Long incidentId;
    private Long userId;
}
