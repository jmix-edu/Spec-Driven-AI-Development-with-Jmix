package com.company.booking.listener;

import com.company.booking.service.DemoDataService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Seeds demo data when the application starts. Disabled under the {@code test} profile,
 * so integration tests run against their own fixtures only.
 */
@Profile("!test")
@Component
public class DemoDataStartupListener {

    private final DemoDataService demoDataService;

    public DemoDataStartupListener(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        demoDataService.seedIfEmpty();
    }
}
