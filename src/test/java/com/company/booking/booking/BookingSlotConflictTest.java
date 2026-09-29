package com.company.booking.booking;

import com.company.booking.entity.*;
import com.company.booking.service.BookingValidationException;
import com.company.booking.service.BookingValidationService;
import com.company.booking.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
class BookingSlotConflictTest {

    private static final LocalDateTime T10 = LocalDateTime.of(2030, 3, 5, 10, 0);
    private static final LocalDateTime T1030 = T10.plusMinutes(30);
    private static final LocalDateTime T11 = T10.plusHours(1);
    private static final LocalDateTime T1130 = T11.plusMinutes(30);
    private static final LocalDateTime T12 = T10.plusHours(2);

    @Autowired
    DataManager dataManager;

    @Autowired
    BookingValidationService bookingValidationService;

    @Autowired
    DatatypeFormatter datatypeFormatter;

    User user;
    Building building;
    Room roomA;
    Room roomB;
    Desk desk;
    final List<Booking> savedBookings = new ArrayList<>();

    @BeforeEach
    void setUp() {
        String suffix = String.valueOf(System.nanoTime());

        user = dataManager.create(User.class);
        user.setUsername("slot-test-" + suffix);
        user = dataManager.save(user);

        building = dataManager.create(Building.class);
        building.setName("Slot building " + suffix);
        building = dataManager.save(building);

        Floor floor = dataManager.create(Floor.class);
        floor.setBuilding(building);
        floor.setNumber(1);
        floor = dataManager.save(floor);

        roomA = saveRoom(floor, "Room A " + suffix);
        roomB = saveRoom(floor, "Room B " + suffix);

        desk = dataManager.create(Desk.class);
        desk.setFloor(floor);
        desk.setCode("D-" + suffix);
        desk = dataManager.save(desk);
    }

    @Test
    void overlappingRoomBookingIsRejectedWithRoomAndInterval() {
        save(newBooking(roomA, null, T10, T11));

        assertThatThrownBy(() -> save(newBooking(roomA, null, T1030, T1130)))
                .satisfies(e -> assertThat(validationMessage(e))
                        .contains(roomA.getName())
                        .contains(datatypeFormatter.formatLocalDateTime(T10))
                        .contains(datatypeFormatter.formatLocalDateTime(T11)));
    }

    @Test
    void adjacentBookingIsSaved() {
        save(newBooking(roomA, null, T10, T11));

        save(newBooking(roomA, null, T11, T12));

        assertThat(savedBookings).hasSize(2);
    }

    @Test
    void overlapWithCancelledBookingIsSaved() {
        Booking cancelled = newBooking(roomA, null, T10, T11);
        cancelled.setStatus(BookingStatus.CANCELLED);
        save(cancelled);

        save(newBooking(roomA, null, T1030, T1130));

        assertThat(savedBookings).hasSize(2);
    }

    @Test
    void sameIntervalOnDifferentRoomIsSaved() {
        save(newBooking(roomA, null, T10, T11));

        save(newBooking(roomB, null, T10, T11));

        assertThat(savedBookings).hasSize(2);
    }

    @Test
    void roomAndDeskBookingsOverSameIntervalAreSaved() {
        save(newBooking(roomA, null, T10, T11));

        save(newBooking(null, desk, T10, T11));

        assertThat(savedBookings).hasSize(2);
    }

    @Test
    void overlappingDeskBookingIsRejected() {
        save(newBooking(null, desk, T10, T11));

        assertThatThrownBy(() -> save(newBooking(null, desk, T1030, T1130)))
                .satisfies(e -> assertThat(validationMessage(e)).contains(desk.getCode()));
    }

    @Test
    void resavingExistingBookingDoesNotConflictWithItself() {
        Booking booking = save(newBooking(roomA, null, T10, T11));

        Booking loaded = dataManager.load(Id.of(booking)).one();
        loaded.setPurpose("Changed purpose");
        dataManager.save(loaded);

        assertThat(dataManager.load(Id.of(booking)).one().getPurpose()).isEqualTo("Changed purpose");
    }

    @Test
    void cancelledBookingIsSavedButRestoringItIsRejected() {
        save(newBooking(roomA, null, T10, T11));
        Booking cancelled = newBooking(roomA, null, T1030, T1130);
        cancelled.setStatus(BookingStatus.CANCELLED);
        Booking savedCancelled = save(cancelled);

        Booking restored = dataManager.load(Id.of(savedCancelled)).one();
        restored.setStatus(BookingStatus.CONFIRMED);

        assertThatThrownBy(() -> dataManager.save(restored))
                .satisfies(e -> assertThat(validationMessage(e)).contains(roomA.getName()));
        assertThat(dataManager.load(Id.of(savedCancelled)).one().getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void containedAndCoveringBookingsAreRejected() {
        save(newBooking(roomA, null, T1030, T11));

        assertThatThrownBy(() -> save(newBooking(roomA, null, T1030.plusMinutes(10), T1030.plusMinutes(20))))
                .satisfies(e -> assertThat(validationMessage(e)).isNotEmpty());
        assertThatThrownBy(() -> save(newBooking(roomA, null, T10, T12)))
                .satisfies(e -> assertThat(validationMessage(e)).isNotEmpty());
    }

    @Test
    void validateSkipsTimeRulesWhenStartIsMissing() {
        save(newBooking(roomA, null, T10, T11));
        Booking unsaved = newBooking(roomA, null, null, T11);

        assertThat(bookingValidationService.validate(unsaved)).isEmpty();
    }

    private Room saveRoom(Floor floor, String name) {
        Room room = dataManager.create(Room.class);
        room.setFloor(floor);
        room.setName(name);
        room.setCapacity(4);
        return dataManager.save(room);
    }

    private Booking newBooking(Room room, Desk desk, LocalDateTime startAt, LocalDateTime endAt) {
        Booking booking = dataManager.create(Booking.class);
        booking.setUser(user);
        booking.setRoom(room);
        booking.setDesk(desk);
        booking.setStartAt(startAt);
        booking.setEndAt(endAt);
        return booking;
    }

    private Booking save(Booking booking) {
        Booking saved = dataManager.save(booking);
        savedBookings.add(saved);
        return saved;
    }

    // The listener's exception may reach the caller directly or wrapped by the persistence layer.
    private static String validationMessage(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof BookingValidationException) {
                return t.getMessage();
            }
        }
        throw new AssertionError("No BookingValidationException in the cause chain", e);
    }

    @AfterEach
    void tearDown() {
        // Bookings first: their foreign keys to room/desk/user have no cascade.
        savedBookings.forEach(b -> dataManager.load(Id.of(b)).optional().ifPresent(dataManager::remove));
        // Floor, rooms and desk are removed by the ON DELETE CASCADE foreign keys.
        dataManager.load(Id.of(building)).optional().ifPresent(dataManager::remove);
        dataManager.load(Id.of(user)).optional().ifPresent(dataManager::remove);
    }
}
