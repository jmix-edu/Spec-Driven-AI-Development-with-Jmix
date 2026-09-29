package com.company.booking.service;

import com.company.booking.entity.Booking;
import com.company.booking.entity.BookingStatus;
import com.company.booking.entity.Desk;
import com.company.booking.entity.Room;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Booking rules:
 * <ol>
 *     <li>R1: {@code endAt} is strictly after {@code startAt};</li>
 *     <li>R2: exactly one of {@code room} and {@code desk} is set;</li>
 *     <li>R3: a non-cancelled booking does not overlap {@code [startAt, endAt)} with another
 *     non-cancelled booking of the same room or desk.</li>
 * </ol>
 * The conflict query ignores row-level security: bookings hidden from the current user still occupy their slot.
 */
@Service
public class BookingValidationService {

    private final DataManager dataManager;
    private final Messages messages;
    private final DatatypeFormatter datatypeFormatter;

    public BookingValidationService(DataManager dataManager, Messages messages, DatatypeFormatter datatypeFormatter) {
        this.dataManager = dataManager;
        this.messages = messages;
        this.datatypeFormatter = datatypeFormatter;
    }

    /**
     * Returns a localized message for every failed rule, or an empty list when the booking is valid.
     * {@code room} and {@code desk} must be loaded or {@code null}.
     */
    public List<String> validate(Booking booking) {
        List<String> errors = new ArrayList<>();

        boolean timesPresent = booking.getStartAt() != null && booking.getEndAt() != null;
        boolean timesValid = timesPresent && booking.getStartAt().isBefore(booking.getEndAt());
        if (timesPresent && !timesValid) {
            errors.add(messages.getMessage(BookingValidationService.class, "bookingStartBeforeEnd"));
        }

        boolean targetValid = (booking.getRoom() == null) != (booking.getDesk() == null);
        if (!targetValid) {
            errors.add(messages.getMessage(BookingValidationService.class, "bookingTargetRequired"));
        }

        if (timesValid && targetValid && booking.getStatus() != BookingStatus.CANCELLED) {
            findConflictMessage(booking).ifPresent(errors::add);
        }
        return errors;
    }

    /**
     * Throws {@link BookingValidationException} with all failed-rule messages if the booking is invalid.
     */
    public void checkValid(Booking booking) {
        List<String> errors = validate(booking);
        if (!errors.isEmpty()) {
            throw new BookingValidationException(String.join("\n", errors));
        }
    }

    private Optional<String> findConflictMessage(Booking booking) {
        Room room = booking.getRoom();
        String placeCondition = room != null ? "b.room = :place" : "b.desk = :place";
        Object place = room != null ? room : booking.getDesk();

        Optional<Booking> conflict = dataManager.unconstrained().load(Booking.class)
                .query("select b from Booking b"
                        + " where b.id <> :id"
                        + " and b.status <> :cancelled"
                        + " and " + placeCondition
                        + " and b.startAt < :endAt"
                        + " and b.endAt > :startAt"
                        + " order by b.startAt")
                .parameter("id", booking.getId())
                .parameter("cancelled", BookingStatus.CANCELLED.getId())
                .parameter("place", place)
                .parameter("startAt", booking.getStartAt())
                .parameter("endAt", booking.getEndAt())
                .maxResults(1)
                .optional();

        return conflict.map(other -> {
            String from = datatypeFormatter.formatLocalDateTime(other.getStartAt());
            String to = datatypeFormatter.formatLocalDateTime(other.getEndAt());
            if (room != null) {
                return messages.formatMessage(BookingValidationService.class, "roomSlotTaken", room.getName(), from, to);
            }
            Desk desk = booking.getDesk();
            return messages.formatMessage(BookingValidationService.class, "deskSlotTaken", desk.getCode(), from, to);
        });
    }
}
