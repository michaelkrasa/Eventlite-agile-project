package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.man.cs.eventlite.testutil.MessageConverterUtil.getMessageConverters;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerApiTest {

	private MockMvc mvc;

	@Autowired
	private Filter springSecurityFilterChain;

	@Mock
	private VenueService venueService;
	@Mock
	private EventService eventService;

	@InjectMocks
	private VenuesControllerApi venuesController;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(venuesController).apply(springSecurity(springSecurityFilterChain))
				.setMessageConverters(getMessageConverters()).build();
	}

	@Test
	public void getIndexWhenNoVenues() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());

		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")));

		verify(venueService).findAll();
	}

	@Test
	public void getIndexWithVenues() throws Exception {
		Venue v = new Venue();
		v.setId(0);
		v.setName("Venue");
		v.setCapacity(100);	
		
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(v));

		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")))
				.andExpect(jsonPath("$._embedded.venues.length()", equalTo(1)))
				.andExpect(jsonPath("$._embedded.venues[0]._links.venue.href", endsWith("venues/0")))
				.andExpect(jsonPath("$._embedded.venues[0]._links.events.href", endsWith("venues/0/events")))
				.andExpect(jsonPath("$._embedded.venues[0]._links.next3events.href", endsWith("venues/0/next3events")));

		verify(venueService).findAll();
	}
	
	@Test
	public void getAllEventsForVenue() throws Exception {
		long venueId = 0;
		Venue v = new Venue();
		v.setId(venueId);
		v.setName("Venue");
		v.setCapacity(100);		
		
		Event e = new Event();
		e.setId(0);
		e.setName("Event");
		e.setDate(LocalDate.now());
		e.setTime(LocalTime.now());
		e.setVenue(v);
		
		when(venueService.findById(venueId)).thenReturn(Optional.of(v));
		when(eventService.findAllByVenue(v)).thenReturn(Collections.<Event>singletonList(e));
		
		mvc.perform(get("/api/venues/0/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
			.andExpect(handler().methodName("getEventsForVenue")).andExpect(jsonPath("$.length()", equalTo(1)))
			.andExpect(jsonPath("$._embedded.events.length()", equalTo(1)));
		
		verify(venueService).findById(venueId);
		verify(eventService).findAllByVenue(v);
	}
	
	@Test
	public void getNext3Events() throws Exception {
		long venueId = 0;
		int num = 1;

		Venue v = new Venue();
		v.setId(venueId);
		v.setName("Venue");
		v.setCapacity(100);

		Event e1 = new Event();
		Event e2 = new Event();
		Event e3 = new Event();

		List<Event> events = new ArrayList<Event>();
		events.add(e1);
		events.add(e2);
		events.add(e3);

		for(Event e : events) {
			e.setId(num);
			e.setName("Event " + num);
			e.setDate(LocalDate.now());
			e.setTime(LocalTime.now());
			e.setVenue(v);
			num++;
		}

		when(venueService.findById(venueId)).thenReturn(Optional.of(v));
		when(eventService.findAllByVenue(v)).thenReturn(events);

		mvc.perform(get("/api/venues/0/next3events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
			.andExpect(handler().methodName("getNext3Events")).andExpect(jsonPath("$.length()", equalTo(1)))
			.andExpect(jsonPath("$._embedded.events.length()", equalTo(3)));

		verify(venueService).findById(venueId);
		verify(eventService).findAllByVenue(v);

	}
}
