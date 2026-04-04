package com.itsm.core.constant;

import java.util.List;

public final class ServiceRequestStatus {

    private ServiceRequestStatus() {}

    public static final String RECEIVED = "RECEIVED";
    public static final String ASSIGNED = "ASSIGNED";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String PENDING_COMPLETE = "PENDING_COMPLETE";
    public static final String CLOSED = "CLOSED";
    public static final String CANCELLED = "CANCELLED";
    public static final String REJECTED = "REJECTED";

    public static final List<String> ALL = List.of(RECEIVED, ASSIGNED, IN_PROGRESS, PENDING_COMPLETE, CLOSED, CANCELLED, REJECTED);
}
