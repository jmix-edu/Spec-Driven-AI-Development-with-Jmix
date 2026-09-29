package com.company.booking.service;

import com.company.booking.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.security.Authenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Seeds a demo office (one building with floors, rooms, desks and an amenity catalog)
 * into an empty database.
 */
@Service
public class DemoDataService {

    private static final Logger log = LoggerFactory.getLogger(DemoDataService.class);

    private static final String ADMIN_USERNAME = "admin";

    private final DataManager dataManager;

    public DemoDataService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Seeds the demo data if the database holds nothing but the {@code admin} user.
     * Runs under the system user, so it can be called from startup code with no authenticated user.
     *
     * @return {@code true} if data was seeded, {@code false} if the database was not empty
     */
    @Authenticated
    public boolean seedIfEmpty() {
        if (!isEmpty()) {
            log.info("Demo data skipped: the database is not empty");
            return false;
        }
        dataManager.save(buildDemoOffice());
        log.info("Demo data seeded");
        return true;
    }

    private boolean isEmpty() {
        boolean onlyAdmin = dataManager.load(User.class)
                .query("select u from User u where u.username <> :admin")
                .parameter("admin", ADMIN_USERNAME)
                .maxResults(1)
                .list()
                .isEmpty();
        return onlyAdmin && Stream.of(Building.class, Floor.class, Room.class, Desk.class, Amenity.class, Booking.class)
                .allMatch(this::hasNoRows);
    }

    private boolean hasNoRows(Class<?> entityClass) {
        String entityName = entityClass.getSimpleName();
        return dataManager.load(entityClass)
                .query("select e from " + entityName + " e")
                .maxResults(1)
                .list()
                .isEmpty();
    }

    private SaveContext buildDemoOffice() {
        // Saved entities are not used afterwards, so skip reloading them.
        SaveContext context = new SaveContext().setDiscardSaved(true);

        Map<String, Amenity> amenities = Stream.of("Projector", "Whiteboard", "Video conferencing", "TV screen")
                .collect(Collectors.toMap(name -> name, name -> {
                    Amenity amenity = dataManager.create(Amenity.class);
                    amenity.setName(name);
                    context.saving(amenity);
                    return amenity;
                }));

        Building building = dataManager.create(Building.class);
        building.setName("Main office");
        building.setAddress("1 Example Street");
        context.saving(building);

        Floor first = floor(context, building, 1, "Ground floor");
        room(context, first, "Orion", 8,
                List.of(amenities.get("Projector"), amenities.get("Whiteboard"), amenities.get("Video conferencing")));
        room(context, first, "Lyra", 4, List.of(amenities.get("Whiteboard")));
        desks(context, first, "1-A-", 4);

        Floor second = floor(context, building, 2, "Open space");
        room(context, second, "Vega", 12,
                List.of(amenities.get("Projector"), amenities.get("Video conferencing"), amenities.get("TV screen")));
        room(context, second, "Pegasus", 6, List.of(amenities.get("TV screen")));
        desks(context, second, "2-A-", 6);

        return context;
    }

    private Floor floor(SaveContext context, Building building, int number, String name) {
        Floor floor = dataManager.create(Floor.class);
        floor.setBuilding(building);
        floor.setNumber(number);
        floor.setName(name);
        context.saving(floor);
        return floor;
    }

    private void room(SaveContext context, Floor floor, String name, int capacity, List<Amenity> amenities) {
        Room room = dataManager.create(Room.class);
        room.setFloor(floor);
        room.setName(name);
        room.setCapacity(capacity);
        room.setAmenities(amenities);
        context.saving(room);
    }

    private void desks(SaveContext context, Floor floor, String codePrefix, int count) {
        for (int i = 1; i <= count; i++) {
            Desk desk = dataManager.create(Desk.class);
            desk.setFloor(floor);
            desk.setCode(codePrefix + String.format("%02d", i));
            context.saving(desk);
        }
    }
}
