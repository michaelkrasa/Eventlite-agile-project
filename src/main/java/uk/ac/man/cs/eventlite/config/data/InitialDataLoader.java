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
		
		if (venueService.count() == 0) {
			Venue testVenue = new Venue();
			testVenue.setName("Kilburn 1.8");
			testVenue.setId(1);
			testVenue.setCapacity(1000);	
			venueService.save(testVenue);
			log.info("Added venue (" + testVenue.getId() + ") to the database.");
		}

		if (eventService.count() > 0) {
			log.info("Database already populated. Skipping data initialization.");
			return;
		}else if (eventService.count() == 0) {
			Event testEvent = new Event();
			testEvent.setId(2);
			testEvent.setName("COMP23412 Showcase, group G");
			testEvent.setDate(LocalDate.of(2020,05,11));
			testEvent.setTime(LocalTime.of(15,00));
			testEvent.setVenue(1);
			eventService.save(testEvent);
			log.info("Added event (" + testEvent.getId() + ") to the database.");
			
			Event testEvent2 = new Event();
			testEvent2.setId(3);
			testEvent2.setName("COMP23412 Showcase, group H");
			testEvent2.setDate(LocalDate.of(2020,05,05));
			testEvent2.setTime(LocalTime.of(10,00));
			testEvent2.setVenue(1);
			eventService.save(testEvent2);
			log.info("Added event (" + testEvent2.getId() + ") to the database.");
			
			Event testEvent3 = new Event();
			testEvent3.setId(4);
			testEvent3.setName("COMP23412 Showcase, group F");
			testEvent3.setDate(LocalDate.of(2020,05,07));
			testEvent3.setTime(LocalTime.of(11,00));
			testEvent3.setVenue(1);
			eventService.save(testEvent3);
			log.info("Added event (" + testEvent3.getId() + ") to the database.");
			
		}
		// Build and save initial models here.
	}
}
