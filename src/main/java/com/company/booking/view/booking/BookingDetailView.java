package com.company.booking.view.booking;

import com.company.booking.entity.Booking;
import com.company.booking.entity.Floor;
import com.company.booking.service.BookingValidationService;
import com.company.booking.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorageLocator;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.image.JmixImage;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "bookings/:id", layout = MainView.class)
@ViewController(id = "Booking.detail")
@ViewDescriptor(path = "booking-detail-view.xml")
@EditedEntityContainer("bookingDc")
public class BookingDetailView extends StandardDetailView<Booking> {

    @Autowired
    private BookingValidationService bookingValidationService;
    @Autowired
    private FileStorageLocator fileStorageLocator;
    @Autowired
    private Downloader downloader;

    @ViewComponent
    private MessageBundle messageBundle;
    @ViewComponent
    private Span floorPlanHint;
    @ViewComponent
    private JmixImage<?> planImage;
    @ViewComponent
    private JmixButton downloadPlanButton;

    // Shows the booking rules as form errors; BookingEventListener still guards every save.
    @Subscribe
    public void onValidation(final ValidationEvent event) {
        bookingValidationService.validate(getEditedEntity()).forEach(event.getErrors()::add);
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        updateFloorPlan();
    }

    @Subscribe(id = "bookingDc", target = Target.DATA_CONTAINER)
    public void onBookingDcItemPropertyChange(final InstanceContainer.ItemPropertyChangeEvent<Booking> event) {
        if ("room".equals(event.getProperty()) || "desk".equals(event.getProperty())) {
            updateFloorPlan();
        }
    }

    @Subscribe(id = "downloadPlanButton", subject = "clickListener")
    public void onDownloadPlanButtonClick(final ClickEvent<JmixButton> event) {
        FileRef plan = currentPlan();
        if (plan != null) {
            downloader.download(plan);
        }
    }

    private void updateFloorPlan() {
        Floor floor = selectedFloor();
        FileRef plan = floor != null ? floor.getPlan() : null;

        if (plan == null) {
            floorPlanHint.setText(messageBundle.getMessage(floor == null ? "floorPlanNoPlace" : "floorPlanMissing"));
        } else {
            planImage.setSrc((DownloadHandler) UiComponentUtils.createResource(plan, fileStorageLocator));
            planImage.setAlt(plan.getFileName());
        }
        floorPlanHint.setVisible(plan == null);
        planImage.setVisible(plan != null);
        downloadPlanButton.setVisible(plan != null);
    }

    @Nullable
    private FileRef currentPlan() {
        Floor floor = selectedFloor();
        return floor != null ? floor.getPlan() : null;
    }

    // The room or desk comes with its floor: from the fetch plan, or from the Room/Desk lookup.
    @Nullable
    private Floor selectedFloor() {
        Booking booking = getEditedEntity();
        if (booking.getRoom() != null) {
            return booking.getRoom().getFloor();
        }
        if (booking.getDesk() != null) {
            return booking.getDesk().getFloor();
        }
        return null;
    }
}
