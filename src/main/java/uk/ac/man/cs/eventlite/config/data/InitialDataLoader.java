package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Component
@Profile({ "default", "test" })
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {

		if (eventService.count() > 0) {
			log.info("Database already populated. Skipping data initialization.");
			return;
		}else if (eventService.count() == 0) {
			Venue testVenue = new Venue();
			Event testEvent = new Event();
			testVenue.setName("Kilburn 1.8");
			testVenue.setId(1);
			testVenue.setCapacity(1000);			
			
			testEvent.setId(1);
			testEvent.setName("Test Event");
			testEvent.setDate(LocalDate.now());
			testEvent.setTime(LocalTime.now());
			testEvent.setVenue(1);
			
			venueService.save(testVenue);
			eventService.save(testEvent);
						
			log.info("Added event (" + testEvent.getId() + ") to the database.");
			return;
		}
		// Build and save initial models here.
	}
}
