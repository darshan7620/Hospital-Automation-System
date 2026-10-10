package com.medicore.appointment;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    NO_SHOW;

    private static final Map<AppointmentStatus, Set<AppointmentStatus>> ALLOWED_TRANSITIONS = Map.of(
            SCHEDULED, EnumSet.of(CONFIRMED, COMPLETED, CANCELLED, NO_SHOW),
            CONFIRMED, EnumSet.of(COMPLETED, CANCELLED, NO_SHOW),
            COMPLETED, EnumSet.noneOf(AppointmentStatus.class),
            CANCELLED, EnumSet.noneOf(AppointmentStatus.class),
            NO_SHOW, EnumSet.noneOf(AppointmentStatus.class));

    public boolean canTransitionTo(AppointmentStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public boolean isTerminal() {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).isEmpty();
    }

    public boolean isActive() {
        return this == SCHEDULED || this == CONFIRMED;
    }
}
