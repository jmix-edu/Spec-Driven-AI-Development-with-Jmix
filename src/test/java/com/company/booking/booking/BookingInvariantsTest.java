package com.company.booking.booking;

import com.company.booking.entity.*;
import com.company.booking.service.BookingValidationException;
import com.company.booking.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
class BookingInvariantsTest {

    private static final LocalDateTime START = LocalDateTime.of(2030, 1, 10, 10, 0);

    @Autowired
    DataManager dataManager;

    User user;
    Building building;
    Room room;
    Desk desk;
    Booking savedBooking;

    @BeforeEach
    void setUp() {
        String suffix = String.valueOf(System.nanoTime());

        user = dataManager.create(User.class);
        user.setUsername("booking-test-" + suffix);
        user = dataManager.save(user);

        building = dataManager.create(Building.class);
        building.setName("Test building " + suffix);
        building = dataManager.save(building);

        Floor floor = dataManager.create(Floor.class);
        floor.setBuilding(building);
        floor.setNumber(1);
        floor = dataManager.save(floor);

        room = dataManager.create(Room.class);
        room.setFloor(floor);
        room.setName("Room " + suffix);
        room.setCapacity(6);
        room = dataManager.save(room);

        desk = dataManager.create(Desk.class);
        desk.setFloor(floor);
        desk.setCode("D-" + suffix);
        desk = dataManager.save(desk);
    }

    @Test
    void roomBookingIsSavedWithConfirmedStatusByDefault() {
        Booking booking = newBooking(room, null, START, START.plusHours(1));

        savedBooking = dataManager.save(booking);

        Booking loaded = dataManager.load(Id.of(savedBooking)).one();
        assertThat(loaded.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void bookingWithRoomAndDeskIsRejected() {
        Booking booking = newBooking(room, desk, START, START.plusHours(1));

        assertRejected(booking);
        assertNotPersisted(booking);
    }

    @Test
    void bookingWithoutRoomAndDeskIsRejected() {
        Booking booking = newBooking(null, null, START, START.plusHours(1));

        assertRejected(booking);
        assertNotPersisted(booking);
    }

    @Test
    void bookingEndingBeforeStartIsRejected() {
        Booking booking = newBooking(null, desk, START, START);

        assertRejected(booking);
        assertNotPersisted(booking);
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

    private void assertRejected(Booking booking) {
        // The listener's exception may reach the caller directly or wrapped by the persistence layer.
        assertThatThrownBy(() -> dataManager.save(booking))
                .satisfies(e -> assertThat(causeChainHas(e, BookingValidationException.class)).isTrue());
    }

    private static boolean causeChainHas(Throwable e, Class<? extends Throwable> type) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (type.isInstance(t)) {
                return true;
            }
        }
        return false;
    }

    private void assertNotPersisted(Booking booking) {
        assertThat(dataManager.load(Booking.class).id(booking.getId()).optional()).isEmpty();
    }

    @AfterEach
    void tearDown() {
        // Children before parents: the booking references room/desk and user.
        if (savedBooking != null) {
            dataManager.load(Id.of(savedBooking)).optional().ifPresent(dataManager::remove);
        }
        // Floor, room and desk are removed by the ON DELETE CASCADE foreign keys.
        dataManager.load(Id.of(building)).optional().ifPresent(dataManager::remove);
        dataManager.load(Id.of(user)).optional().ifPresent(dataManager::remove);
    }
}
