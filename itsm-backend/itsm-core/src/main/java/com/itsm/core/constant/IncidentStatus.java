package com.itsm.core.constant;

import java.util.List;

public final class IncidentStatus {

    private IncidentStatus() {}

    public static final String RECEIVED = "RECEIVED";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String CLOSED = "CLOSED";
    public static final String REJECTED = "REJECTED";

    public static final List<String> ALL = List.of(RECEIVED, IN_PROGRESS, COMPLETED, CLOSED, REJECTED);
}
