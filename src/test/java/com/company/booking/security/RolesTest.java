package com.company.booking.security;

import com.company.booking.entity.*;
import com.company.booking.service.BookingValidationException;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.Id;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.core.security.UserRepository;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Entity-level and row-level behaviour of the Employee, Office Manager and Own bookings roles.
 * Fixtures are written as the system user; every check runs as a real user with role assignments.
 */
@SpringBootTest
@ActiveProfiles("test")
class RolesTest {

    private static final LocalDateTime T10 = LocalDateTime.of(2030, 4, 8, 10, 0);

    @Autowired
    DataManager dataManager;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Autowired
    UserRepository userRepository;

    String emp1;
    String emp2;
    String manager;
    Building building;
    Room room;
    final List<Object> createdAmenities = new ArrayList<>();
    final List<Object> createdBuildings = new ArrayList<>();

    @BeforeEach
    void setUp() {
        String suffix = String.valueOf(System.nanoTime());
        emp1 = "roles-emp1-" + suffix;
        emp2 = "roles-emp2-" + suffix;
        manager = "roles-mgr-" + suffix;

        systemAuthenticator.runWithSystem(() -> {
            createUser(emp1, List.of(UiMinimalRole.CODE, EmployeeRole.CODE), List.of(OwnBookingsRole.CODE));
            createUser(emp2, List.of(UiMinimalRole.CODE, EmployeeRole.CODE), List.of(OwnBookingsRole.CODE));
            createUser(manager, List.of(UiMinimalRole.CODE, OfficeManagerRole.CODE), List.of());

            building = dataManager.create(Building.class);
            building.setName("Roles building " + suffix);
            building = dataManager.save(building);

            Floor floor = dataManager.create(Floor.class);
            floor.setBuilding(building);
            floor.setNumber(1);
            floor = dataManager.save(floor);

            room = dataManager.create(Room.class);
            room.setFloor(floor);
            room.setName("Roles room " + suffix);
            room.setCapacity(4);
            room = dataManager.save(room);
        });
    }

    @Test
    void employeeSeesOnlyOwnBookings() {
        Booking own = asSystem(() -> saveBooking(emp1, T10, T10.plusHours(1)));
        Booking foreign = asSystem(() -> saveBooking(emp2, T10.plusHours(2), T10.plusHours(3)));

        List<Booking> visible = systemAuthenticator.withUser(emp1, this::loadRoomBookings);
        boolean foreignFound = systemAuthenticator.withUser(emp1, () ->
                dataManager.load(Booking.class).id(foreign.getId()).optional().isPresent());

        assertThat(visible).extracting(Booking::getId).containsExactly(own.getId());
        assertThat(foreignFound).isFalse();
    }

    @Test
    void employeeCreatesUpdatesAndCancelsOwnBookingButCannotDeleteIt() {
        Booking saved = systemAuthenticator.withUser(emp1, () -> {
            Booking booking = newBooking(loadUser(emp1), T10, T10.plusHours(1));
            Booking created = dataManager.save(booking);

            Booking loaded = loadBookingWithUser(created);
            loaded.setPurpose("Planning");
            loaded = dataManager.save(loaded);

            loaded.setStatus(BookingStatus.CANCELLED);
            return dataManager.save(loaded);
        });

        assertThat(saved.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(saved.getPurpose()).isEqualTo("Planning");
        assertDenied(() -> systemAuthenticator.runWithUser(emp1, () ->
                dataManager.remove(loadBookingWithUser(saved))));
        assertThat(asSystem(() -> dataManager.load(Id.of(saved)).optional().isPresent())).isTrue();
    }

    @Test
    void employeeCannotCreateOrReassignBookingForAnotherUser() {
        User other = asSystem(() -> loadUser(emp2));

        assertDenied(() -> systemAuthenticator.runWithUser(emp1, () ->
                dataManager.save(newBooking(other, T10, T10.plusHours(1)))));

        Booking own = asSystem(() -> saveBooking(emp1, T10.plusHours(2), T10.plusHours(3)));
        assertDenied(() -> systemAuthenticator.runWithUser(emp1, () -> {
            Booking loaded = loadBookingWithUser(own);
            loaded.setUser(other);
            dataManager.save(loaded);
        }));
    }

    @Test
    void employeeCannotChangeAnotherEmployeesBooking() {
        Booking foreign = asSystem(() -> saveBooking(emp2, T10, T10.plusHours(1)));
        Booking instance = asSystem(() -> loadBookingWithUser(foreign));

        assertDenied(() -> systemAuthenticator.runWithUser(emp1, () -> {
            instance.setPurpose("Hijacked");
            dataManager.save(instance);
        }));
        assertThat(asSystem(() -> dataManager.load(Id.of(foreign)).one().getPurpose())).isNull();
    }

    @Test
    void employeeBookingConflictsWithInvisibleBooking() {
        asSystem(() -> saveBooking(emp2, T10, T10.plusHours(1)));

        assertThatThrownBy(() -> systemAuthenticator.runWithUser(emp1, () ->
                dataManager.save(newBooking(loadUser(emp1), T10.plusMinutes(30), T10.plusMinutes(90)))))
                .satisfies(e -> assertThat(causeChainHas(e, BookingValidationException.class)).isTrue());
    }

    @Test
    void employeeSeesOnlyThemselvesAmongUsers() {
        List<User> users = systemAuthenticator.withUser(emp1, () -> dataManager.load(User.class).all().list());

        assertThat(users).extracting(User::getUsername).containsExactly(emp1);
    }

    @Test
    void employeeCannotCreateReferenceData() {
        assertDenied(() -> systemAuthenticator.runWithUser(emp1, () -> {
            Building b = dataManager.create(Building.class);
            b.setName("Employee building " + System.nanoTime());
            createdBuildings.add(dataManager.save(b));
        }));
        assertDenied(() -> systemAuthenticator.runWithUser(emp1, () -> {
            Amenity a = dataManager.create(Amenity.class);
            a.setName("Employee amenity " + System.nanoTime());
            createdAmenities.add(dataManager.save(a));
        }));
    }

    @Test
    void managerSeesAllBookingsButCannotChangeThem() {
        Booking b1 = asSystem(() -> saveBooking(emp1, T10, T10.plusHours(1)));
        Booking b2 = asSystem(() -> saveBooking(emp2, T10.plusHours(2), T10.plusHours(3)));

        List<Booking> visible = systemAuthenticator.withUser(manager, this::loadRoomBookings);
        assertThat(visible).extracting(Booking::getId).containsExactlyInAnyOrder(b1.getId(), b2.getId());

        User emp1User = asSystem(() -> loadUser(emp1));
        assertDenied(() -> systemAuthenticator.runWithUser(manager, () ->
                dataManager.save(newBooking(emp1User, T10.plusHours(4), T10.plusHours(5)))));
        assertDenied(() -> systemAuthenticator.runWithUser(manager, () -> {
            Booking loaded = loadBookingWithUser(b1);
            loaded.setPurpose("Changed by manager");
            dataManager.save(loaded);
        }));
        assertDenied(() -> systemAuthenticator.runWithUser(manager, () ->
                dataManager.remove(loadBookingWithUser(b2))));
    }

    @Test
    void managerManagesOfficeStructureAndAmenities() {
        systemAuthenticator.runWithUser(manager, () -> {
            Amenity amenity = dataManager.create(Amenity.class);
            amenity.setName("Manager amenity " + System.nanoTime());
            amenity = dataManager.save(amenity);
            createdAmenities.add(amenity);

            Building b = dataManager.create(Building.class);
            b.setName("Manager building " + System.nanoTime());
            b = dataManager.save(b);
            createdBuildings.add(b);

            Floor floor = dataManager.create(Floor.class);
            floor.setBuilding(b);
            floor.setNumber(3);
            floor = dataManager.save(floor);

            Room r = dataManager.create(Room.class);
            r.setFloor(floor);
            r.setName("Manager room");
            r.setCapacity(2);
            r.setAmenities(List.of(amenity));
            dataManager.save(r);

            Desk desk = dataManager.create(Desk.class);
            desk.setFloor(floor);
            desk.setCode("M-01");
            dataManager.save(desk);

            Building loaded = dataManager.load(Id.of(b)).one();
            loaded.setAddress("Updated by manager");
            loaded = dataManager.save(loaded);

            dataManager.remove(loaded);
            Amenity loadedAmenity = dataManager.load(Id.of(amenity)).one();
            dataManager.remove(loadedAmenity);
        });
    }

    @Test
    void employeeResolvesThroughUserRepositoryWithEmployeeRole() {
        List<String> authorities = systemAuthenticator.withUser(emp1, () ->
                userRepository.loadUserByUsername(emp1).getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList());

        assertThat(authorities).anyMatch(a -> a.endsWith(EmployeeRole.CODE));
    }

    private void createUser(String username, List<String> resourceRoles, List<String> rowLevelRoles) {
        User user = dataManager.create(User.class);
        user.setUsername(username);
        user.setPassword("{noop}" + username);
        user.setActive(true);
        dataManager.save(user);
        resourceRoles.forEach(code -> assignRole(username, code, RoleAssignmentRoleType.RESOURCE));
        rowLevelRoles.forEach(code -> assignRole(username, code, RoleAssignmentRoleType.ROW_LEVEL));
    }

    private void assignRole(String username, String roleCode, String roleType) {
        RoleAssignmentEntity assignment = dataManager.create(RoleAssignmentEntity.class);
        assignment.setUsername(username);
        assignment.setRoleCode(roleCode);
        assignment.setRoleType(roleType);
        dataManager.save(assignment);
    }

    private User loadUser(String username) {
        return dataManager.load(User.class)
                .query("select u from User u where u.username = :username")
                .parameter("username", username)
                .one();
    }

    private Booking newBooking(User user, LocalDateTime startAt, LocalDateTime endAt) {
        Booking booking = dataManager.create(Booking.class);
        booking.setUser(user);
        booking.setRoom(room);
        booking.setStartAt(startAt);
        booking.setEndAt(endAt);
        return booking;
    }

    private Booking saveBooking(String username, LocalDateTime startAt, LocalDateTime endAt) {
        return dataManager.save(newBooking(loadUser(username), startAt, endAt));
    }

    // The row-level predicate reads booking.user, so load it with the booking.
    private Booking loadBookingWithUser(Booking booking) {
        return dataManager.load(Booking.class)
                .id(booking.getId())
                .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE)
                        .add("user", FetchPlan.BASE)
                        .add("room", FetchPlan.INSTANCE_NAME))
                .one();
    }

    private List<Booking> loadRoomBookings() {
        return dataManager.load(Booking.class)
                .query("select b from Booking b where b.room = :room")
                .parameter("room", room)
                .list();
    }

    private <T> T asSystem(SystemAuthenticator.AuthenticatedOperation<T> operation) {
        return systemAuthenticator.withSystem(operation);
    }

    private static void assertDenied(Runnable action) {
        assertThatThrownBy(action::run)
                .satisfies(e -> assertThat(causeChainHas(e, AccessDeniedException.class))
                        .as("AccessDeniedException in the cause chain of %s", e)
                        .isTrue());
    }

    private static boolean causeChainHas(Throwable e, Class<? extends Throwable> type) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (type.isInstance(t)) {
                return true;
            }
        }
        return false;
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.runWithSystem(() -> {
            // Bookings first: their foreign keys to room and user have no cascade.
            dataManager.load(Booking.class)
                    .query("select b from Booking b where b.user.username in :usernames")
                    .parameter("usernames", List.of(emp1, emp2, manager))
                    .list()
                    .forEach(dataManager::remove);
            // Floors, rooms and desks are removed by the ON DELETE CASCADE foreign keys.
            createdBuildings.forEach(b -> dataManager.load(Id.of(b)).optional().ifPresent(dataManager::remove));
            dataManager.load(Id.of(building)).optional().ifPresent(dataManager::remove);
            createdAmenities.forEach(a -> dataManager.load(Id.of(a)).optional().ifPresent(dataManager::remove));
            dataManager.load(RoleAssignmentEntity.class)
                    .query("select r from sec_RoleAssignmentEntity r where r.username in :usernames")
                    .parameter("usernames", List.of(emp1, emp2, manager))
                    .list()
                    .forEach(dataManager::remove);
            dataManager.load(User.class)
                    .query("select u from User u where u.username in :usernames")
                    .parameter("usernames", List.of(emp1, emp2, manager))
                    .list()
                    .forEach(dataManager::remove);
        });
    }
}
