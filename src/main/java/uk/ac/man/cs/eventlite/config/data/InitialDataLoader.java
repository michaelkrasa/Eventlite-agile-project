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
			log.info("[Database seeding] : Started");
			
			Venue testVenue = new Venue();
			Venue testVenue2 = new Venue();
			
			testVenue.setName("Kilburn G23");
			testVenue.setCapacity(80);
			// set location to Kilburn
			testVenue.setAddress1("Kilburn Building");
			testVenue.setCity("Manchester");
			testVenue.updateLocation();
			
			testVenue2.setName("Kilburn LF31");
			testVenue2.setCapacity(60);
			// set location to Kilburn
			testVenue2.setAddress1("Kilburn Building");
			testVenue2.setCity("Manchester");
			testVenue2.updateLocation();
						
			venueService.save(testVenue);
			venueService.save(testVenue2);
			
			log.info("Added venue (" + testVenue.getId() + ") to the database.");
			log.info("Added venue (" + testVenue2.getId() + ") to the database.");
			
			Event testEvent = new Event();
			testEvent.setName("COMP23412 Showcase, group G");
			testEvent.setDate(LocalDate.of(2020,05,11));
			testEvent.setTime(LocalTime.of(15,00));
			testEvent.setVenue(testVenue2);
			testEvent.setDescription("Showcase of team project for group G");
			eventService.save(testEvent);
			log.info("Added event (" + testEvent.getId() + ") to the database.");
			
			Event testEvent2 = new Event();
			testEvent2.setName("COMP23412 Showcase, group H");
			testEvent2.setDate(LocalDate.of(2020,05,05));
			testEvent2.setTime(LocalTime.of(10,00));
			testEvent2.setVenue(testVenue);
			testEvent2.setDescription("Showcase of team project for group H");
			eventService.save(testEvent2);
			log.info("Added event (" + testEvent2.getId() + ") to the database.");
			
			Event testEvent3 = new Event();
			testEvent3.setName("COMP23412 Showcase, group F");
			testEvent3.setDate(LocalDate.of(2020,05,07));
			testEvent3.setTime(LocalTime.of(11,00));
			testEvent3.setVenue(testVenue2);
			testEvent3.setDescription("Showcase of team project for group F");
			eventService.save(testEvent3);
			log.info("Added event (" + testEvent3.getId() + ") to the database.");
			
			Event testEvent4 = new Event();
			testEvent4.setName("COMP23412 Showcase, group E");
			testEvent4.setDate(LocalDate.of(2020,05,07));
			testEvent4.setTime(LocalTime.of(10,00));
			testEvent4.setVenue(testVenue);
			testEvent4.setDescription("A sensible description");
			eventService.save(testEvent4);
			log.info("Added event (" + testEvent4.getId() + ") to the database.");
			
			Event testEvent5 = new Event();
			testEvent5.setName("COMP23412 Showcase, group Past 1");
			testEvent5.setDate(LocalDate.of(2018,05,07));
			testEvent5.setTime(LocalTime.of(14,00));
			testEvent5.setVenue(testVenue2);
			testEvent5.setDescription("Showcase of team project for a past event");
			eventService.save(testEvent5);
			log.info("Added event (" + testEvent5.getId() + ") to the database.");
			
			Event testEvent6 = new Event();
			testEvent6.setName("COMP23412 Showcase, group Past 2");
			testEvent6.setDate(LocalDate.of(2018,05,07));
			testEvent6.setTime(LocalTime.of(10,00));
			testEvent6.setVenue(testVenue);
			testEvent6.setDescription("Showcase of team project for another past event");
			eventService.save(testEvent6);			
			log.info("Added event (" + testEvent6.getId() + ") to the database.");
			
			Event testEvent7 = new Event();
			testEvent7.setName("COMP23412 Showcase, group Past 3");
			testEvent7.setDate(LocalDate.of(2018,03,07));
			testEvent7.setTime(LocalTime.of(20,30));
			testEvent7.setVenue(testVenue2);
			testEvent7.setDescription("Showcase of team project for yet another past event");
			eventService.save(testEvent7);
			log.info("Added event (" + testEvent7.getId() + ") to the database.");
			
			Event testEvent8 = new Event();
			testEvent8.setName("COMP23412 Showcase, group I");
			testEvent8.setDate(LocalDate.of(2020,05,07));
			testEvent8.setTime(LocalTime.of(10,00));
			testEvent8.setVenue(testVenue);
			testEvent8.setDescription("Showcase of team project for group I");
			eventService.save(testEvent8);
			
			log.info("Added event (" + testEvent8.getId() + ") to the database.");
			
			log.info("[Database seeding] : Finished");
			
		}
		// Build and save initial models here.
	}
}