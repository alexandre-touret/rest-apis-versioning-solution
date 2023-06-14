package info.touret.bookstore.spring.maintenance.controller;

import info.touret.bookstore.spring.book.generated.controller.MaintenanceApi;
import info.touret.bookstore.spring.book.generated.dto.MaintenanceDto;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Manages maintenance mode flag by using Spring boot standard functionalities
 * <p>
 * These classes interact with Actuator and its Readiness Probe.
 *
 * @see ApplicationEventPublisher
 * @see org.springframework.boot.availability.ApplicationAvailability
 * @see ReadinessState
 */
@RestController()
public class MaintenanceController implements MaintenanceApi {


    public static final String API_MAINTENANCE_URI = "/maintenance";
    private final ApplicationEventPublisher eventPublisher;

    private final ApplicationAvailability availability;

    public MaintenanceController(ApplicationEventPublisher eventPublisher, ApplicationAvailability applicationAvailability) {
        this.eventPublisher = eventPublisher;
        this.availability = applicationAvailability;
    }

    @Override
    public ResponseEntity<MaintenanceDto> retreiveInMaintenance() {
        var lastChangeEvent = availability.getLastChangeEvent(ReadinessState.class);
        var maintenanceDto = new MaintenanceDto();
        maintenanceDto.setInMaintenance(lastChangeEvent.getState().equals(ReadinessState.REFUSING_TRAFFIC));
        maintenanceDto.setFrom(OffsetDateTime.ofInstant(new Date(lastChangeEvent.getTimestamp()).toInstant(), ZoneId.systemDefault()));
        return ResponseEntity.ok(maintenanceDto);
    }

    @Override
    public ResponseEntity<Void> initInMaintenance(String inMaintenance) {
        AvailabilityChangeEvent.publish(eventPublisher, this, Boolean.valueOf(inMaintenance) ? ReadinessState.REFUSING_TRAFFIC : ReadinessState.ACCEPTING_TRAFFIC);
        return ResponseEntity.noContent().build();
    }
}
