package com.company.booking.listener;

import com.company.booking.service.DemoDataService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Seeds demo data when the application starts. Only in the {@code dev} profile: production
 * and integration tests never get demo data.
 */
@Profile("dev")
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
