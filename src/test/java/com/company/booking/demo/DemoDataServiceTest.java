package com.company.booking.demo;

import com.company.booking.entity.Building;
import com.company.booking.service.DemoDataService;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Calls the seeder the way the startup listener does: with no authenticated user.
 */
@SpringBootTest
@ActiveProfiles("test")
class DemoDataServiceTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Autowired
    DemoDataService demoDataService;

    Building existing;

    @BeforeEach
    void setUp() {
        // Setup only is authenticated: guarantees a non-empty database.
        existing = systemAuthenticator.withSystem(() -> {
            Building building = dataManager.create(Building.class);
            building.setName("Seed test building " + System.nanoTime());
            return dataManager.save(building);
        });
    }

    @Test
    void doesNotSeedNonEmptyDatabaseAndRunsWithoutAuthentication() {
        long buildingsBefore = countBuildings();

        boolean seeded = demoDataService.seedIfEmpty();

        assertThat(seeded).isFalse();
        assertThat(countBuildings()).isEqualTo(buildingsBefore);
    }

    private long countBuildings() {
        return systemAuthenticator.withSystem(() ->
                (long) dataManager.load(Building.class).all().list().size());
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.runWithSystem(() ->
                dataManager.load(Id.of(existing)).optional().ifPresent(dataManager::remove));
    }
}
