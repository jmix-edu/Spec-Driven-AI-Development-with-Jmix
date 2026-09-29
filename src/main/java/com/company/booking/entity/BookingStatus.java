package com.company.booking.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.jspecify.annotations.Nullable;

public enum BookingStatus implements EnumClass<String> {

    CONFIRMED("CONFIRMED"),
    CANCELLED("CANCELLED");

    private final String id;

    BookingStatus(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static BookingStatus fromId(String id) {
        for (BookingStatus value : BookingStatus.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
