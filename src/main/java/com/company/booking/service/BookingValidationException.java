package com.company.booking.service;

/**
 * Thrown when a {@link com.company.booking.entity.Booking} violates a data invariant.
 * The message is localized.
 */
public class BookingValidationException extends RuntimeException {

    public BookingValidationException(String message) {
        super(message);
    }
}
