package com.company.booking.view.booking;

import com.company.booking.entity.Booking;
import com.company.booking.entity.User;
import com.company.booking.service.BookingValidationService;
import com.company.booking.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "bookings/:id", layout = MainView.class)
@ViewController(id = "Booking.detail")
@ViewDescriptor(path = "booking-detail-view.xml")
@EditedEntityContainer("bookingDc")
public class BookingDetailView extends StandardDetailView<Booking> {

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Booking> event) {
        Booking booking = event.getEntity();
        final User user = (User) currentAuthentication.getUser();
        booking.setUser(user);
    }

    @Autowired
    private BookingValidationService bookingValidationService;

    // Shows the booking rules as form errors; BookingEventListener still guards every save.
    @Subscribe
    public void onValidation(final ValidationEvent event) {
        bookingValidationService.validate(getEditedEntity()).forEach(event.getErrors()::add);
    }
}
