package uk.ac.man.cs.eventlite.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import javax.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.verification.VerificationMode;
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
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerTest {

	private MockMvc mvc;

	@Autowired
	private Filter springSecurityFilterChain;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@Mock
	private EventService eventService;

	@Mock
	private VenueService venueService;

	@InjectMocks
	private EventsController eventsController;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(eventsController).apply(springSecurity(springSecurityFilterChain))
				.build();
	}

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event> emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
		verifyZeroInteractions(event);
		verifyZeroInteractions(venue);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event> singletonList(event));
		when(venueService.findAll()).thenReturn(Collections.<Venue> singletonList(venue));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
		verifyZeroInteractions(event);
		verifyZeroInteractions(venue);
	}
	
	@Test
	public void getEventFromID() throws Exception {
		
		// create venue
		Venue v= new Venue();
		v.setName("Kilburn G23");
		v.setCapacity(80);
		
		// create event
		Event e = new Event();
		e.setName("test");
		e.setTime(LocalTime.now());
		e.setDate(LocalDate.now());
		e.setVenue(v);
		e.setId(100L);
		
		// optional event
		Optional<Event> optE = Optional.of(e);
		
		// mock method
		when(eventService.findById(e.getId())).thenReturn(optE);
		
		// call method to check it returns correct event
		assertEquals(eventService.findById(100L).get(), optE.get());
		
		
		// call the correct mvc function
		mvc.perform(get("/events/100").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
		.andExpect(view().name("events/view")).andExpect(handler().methodName("getEventById"));
		
		
		// check that method is called correctly, 2 times. one for assertequals, one for mvc.perform
		verify(eventService, VerificationModeFactory.times(2)).findById(e.getId());
	}
	
	@Test
	public void getNullEventFromID() throws Exception {
		when(eventService.findById(0L)).thenReturn(Optional.empty());
		
		// routes to index as event 0 doesnt exist
		mvc.perform(get("/events/0").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getEventById"));
		
		verify(eventService).findById(0L);
	}
	
	@Test
	public void deleteEventWithEvent() throws Exception { 
		// data needed for test
		Event e = new Event();
		e.setName("testEvent");
		e.setTime(null);
		e.setDate(null);
		e.setVenue(null);
		long ID = (long)1;
		e.setId(ID);	
		Optional<Event> testEvent = Optional.of(e);
		
		// mocked methods
		when(eventService.findById(ID)).thenReturn(testEvent);
		doNothing().when(eventService).deleteById(ID);
		
		// assertion checks
		assertEquals(eventService.findById(ID).get(), testEvent.get());
		
		// performing functions being tested
		mvc.perform(get("/events/delete_event?ID=" + ID)
			.accept(MediaType.TEXT_HTML))
			.andExpect(status().isFound())
			.andExpect(view().name("redirect:/events"))
			.andExpect(handler().methodName("deleteEventByID"));	
			
		// verifying 
		verify(eventService, VerificationModeFactory.times(2)).findById(ID);
		verify(eventService, VerificationModeFactory.times(1)).deleteById(ID);	
	}
	
	
	@Test
	public void deleteEventWhileNoEvent() throws Exception { 
		// data needed for test
		/*
		Event e = new Event();
		e.setName("testEvent");
		e.setTime(null);
		e.setDate(null);
		e.setVenue(null);
		long ID = (long)1;
		e.setId(ID);	*/
		long ID = (long)1;
		Optional<Event> testEvent = Optional.empty(); //Optional.of(event);
		
		// mocked methods
		when(eventService.findById(ID)).thenReturn(testEvent);
		doNothing().when(eventService).deleteById(ID);
		
		// assertion checks
		//assertFalse(  testEvent.isPresent());
		assertFalse(eventService.findById(ID).isPresent());
		
		// performing functions being tested
		mvc.perform(get("/events/delete_event?ID=" + ID)
			.accept(MediaType.TEXT_HTML))
			.andExpect(status().isFound())
			.andExpect(view().name("redirect:/events"))
			.andExpect(handler().methodName("deleteEventByID"));	
			
		// verifying 
		verify(eventService, VerificationModeFactory.times(2)).findById(ID);
		verify(eventService, VerificationModeFactory.times(0)).deleteById(ID);	
	}
	
}
