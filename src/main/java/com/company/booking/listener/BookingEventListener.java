package com.company.booking.listener;

import com.company.booking.entity.Booking;
import com.company.booking.service.BookingValidationService;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Guards every save of a {@link Booking} with {@link BookingValidationService}.
 * <p>
 * Runs before commit, so an exception rejects the save for UI and programmatic callers alike.
 * {@code EntityChangedEvent} is used instead of {@code EntitySavingEvent} because the latter
 * cannot read unloaded references of an updated booking.
 */
@Component
public class BookingEventListener {

    private final DataManager dataManager;
    private final BookingValidationService bookingValidationService;

    public BookingEventListener(DataManager dataManager, BookingValidationService bookingValidationService) {
        this.dataManager = dataManager;
        this.bookingValidationService = bookingValidationService;
    }

    @EventListener
    public void onBookingChanged(EntityChangedEvent<Booking> event) {
        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            return;
        }
        // Unconstrained: row-level roles must not hide the booking being guarded.
        Booking booking = dataManager.unconstrained().load(event.getEntityId())
                .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE)
                        .add("room", FetchPlan.INSTANCE_NAME)
                        .add("desk", FetchPlan.INSTANCE_NAME))
                .one();

        bookingValidationService.checkValid(booking);
    }
}
