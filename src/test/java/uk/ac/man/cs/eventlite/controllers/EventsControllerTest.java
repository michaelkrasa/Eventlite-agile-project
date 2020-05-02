package uk.ac.man.cs.eventlite.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

import javax.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.servlet.view.RedirectView;

import twitter4j.Twitter;
import twitter4j.auth.AccessToken;
import uk.ac.man.cs.eventlite.config.Security;
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
	
	private final static String BAD_ROLE = "USER";

	private MockMvc mvc;

	@Autowired
	private Filter springSecurityFilterChain;

	@Mock
	private Event event;

	@Mock
	private Venue venue;
	
	@Mock
	private Twitter twitter;
	
	@Mock
	private Model model;

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
		// create event
		Event e = new Event();
		e.setDate(LocalDate.now());
		when(eventService.findAll()).thenReturn(Collections.<Event> singletonList(e));
		when(venueService.findAll()).thenReturn(Collections.<Venue> singletonList(venue));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
		verifyZeroInteractions(venue);
	}
	
	@Test
	public void getIndexWithNoEventsSearch() throws Exception {
		when(eventService.findAllByNameContainingIgnoreCase(null)).thenReturn(Collections.<Event> emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());
		
		mvc.perform(get("/events/foundEvents").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
		.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllByName"));

		verify(eventService).findAllByNameContainingIgnoreCase(null);
		verifyZeroInteractions(venue);
	}
	
	@Test
	public void getIndexWithEventsSearch() throws Exception {
		// create event
		Event e = new Event();
		e.setDate(LocalDate.now());
		e.setName("test");
		when(eventService.findAllByNameContainingIgnoreCase(null)).thenReturn(Collections.<Event> singletonList(e));
		when(venueService.findAll()).thenReturn(Collections.<Venue> singletonList(venue));


		mvc.perform(get("/events/foundEvents").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllByName"));

		verify(eventService).findAllByNameContainingIgnoreCase(null);
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
	
	public void getNewEvents() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/events/new")
		.accept(MediaType.TEXT_HTML))
		.andExpect(status().isOk()).andExpect(view().name("events/new"))
		.andExpect(handler().methodName("newEvent"));
	}
	
	@Test
	public void postEventNoAuth() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
		params.add("name", "Test event");
		params.add("date", "2020-04-04");
		params.add("time", "12:00");
		params.add("venue", "1");
				
		mvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.params(params)
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
		.andExpect(header().string("Location", endsWith("/sign-in")));

		verify(eventService, never()).save(event);
	}

	@Test
	public void postEventBadRole() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
		params.add("name", "Test event");
		params.add("date", "2020-04-04");
		params.add("time", "12:00");
		params.add("venue", "1");
		
		mvc.perform(MockMvcRequestBuilders.post("/events").with(user("Rob").roles(BAD_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(params)
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isForbidden());

		verify(eventService, never()).save(event);
	}

	@Test
	public void postEventNoCsrf() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
		params.add("name", "Test event");
		params.add("date", "2020-04-04");
		params.add("time", "12:00");
		params.add("venue", "1");
		
		mvc.perform(MockMvcRequestBuilders.post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(params)
				.accept(MediaType.TEXT_HTML)).andExpect(status().isForbidden());

		verify(eventService, never()).save(event);
	}

//	@Test
//	public void postEvent() throws Exception {
//		ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
//		
//		MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
//		params.add("name", "Test event");
//		params.add("date", "2100-04-04");
//		params.add("time", "12:00");
//		params.add("venue", "1");
//
//		mvc.perform(MockMvcRequestBuilders.post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
//				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
//				.params(params)
//				.accept(MediaType.TEXT_HTML)
//				.with(csrf()))
//		.andExpect(status().is2xxSuccessful()).andExpect(content().string(""))
//		.andExpect(view().name("redirect:/events")).andExpect(model().hasNoErrors())
//		.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeExists("ok_message"));
//
//		verify(eventService).save(arg.capture());
//		assertThat("Test event", equalTo(arg.getValue().getName()));
//		assertThat("2020-04-04", equalTo(arg.getValue().getDate()));
//		assertThat("12:00", equalTo(arg.getValue().getTime()));
//		assertThat("1", equalTo(arg.getValue().getVenue()));
//	}

	@Test
	public void postBadEvent() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
		params.add("name", "Test event");
		params.add("date", "22-10-22222"); // This field is incorrect format purposefully 
		params.add("time", "12:00");
		params.add("venue", "1");
		
		mvc.perform(MockMvcRequestBuilders.post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.params(params).accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("events/new"))
		.andExpect(model().attributeHasFieldErrors("event", "date"))
		.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(event);
	}

	@Test
	public void postLongEventName() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
		params.add("name", "Test event test event test event test event test event test event "
				+ "test event test event test event test event test event test event"
				+ "test event test event test event test event test event test event"
				+ "test event test event test event test event test event test event");
		params.add("date", "2020-04-04");
		params.add("time", "12:00");
		params.add("venue", "1");
		
		mvc.perform(MockMvcRequestBuilders.post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.params(params).accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isOk()).andExpect(view().name("events/new"))
		.andExpect(model().attributeHasFieldErrors("event", "name"))
		.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(event);
	}

	@Test
	public void postEmptyEvent() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
		params.add("name", "");
		params.add("date", "2020-04-04");
		params.add("time", "12:00");
		params.add("venue", "1");
		
		mvc.perform(MockMvcRequestBuilders.post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.params(params).accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
		.andExpect(view().name("events/new"))
		.andExpect(model().attributeHasFieldErrors("event", "name"))
		.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(event);
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
	
	@Test
	public void updateEventWhereEventExists() throws Exception {
		
		Event e = new Event();
		e.setName("testEvent");
		e.setTime(null);
		e.setDate(null);
		e.setVenue(null);
		long ID = (long)1;
		e.setId(ID);	
		Optional<Event> testEvent = Optional.of(e);
		
		when(eventService.findById(ID)).thenReturn(testEvent);
		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());
		
		mvc.perform(get("/events/update?id=" + ID)
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/update"))
				.andExpect(model().attributeExists("event"))
				.andExpect(model().attributeExists("venues"))
				.andExpect(handler().methodName("updateEvent"));
		
		verify(eventService, VerificationModeFactory.times(1)).findById(ID);
		verify(venueService, VerificationModeFactory.times(1)).findAll();
		
	}
	
	@Test
	public void updateEventWhereNoEvent() throws Exception {
		long ID = (long)1;
		Optional<Event> testEvent = Optional.empty();
		
		when(eventService.findById(ID)).thenReturn(testEvent);
		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());
		
		mvc.perform(get("/events/update?id=" + ID)
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/update"))
				.andExpect(model().attributeDoesNotExist("event"))
				.andExpect(model().attributeExists("venues"))
				.andExpect(handler().methodName("updateEvent"));
		
		verify(eventService, VerificationModeFactory.times(1)).findById(ID);
		verify(venueService, VerificationModeFactory.times(1)).findAll();
	}
	
//	@Test
//	public void postNonEmptyTweet() throws Exception{
//
//		String tweet = "test tweet";
//		
//		Event e = new Event();
//		e.setName("testEvent");
//		e.setTime(null);
//		e.setDate(null);
//		e.setVenue(null);
//		long eventId = (long)1;
//		e.setId(eventId);	
//		Optional<Event> testEvent = Optional.of(e);
//		
////		RedirectAttributes redir = new RedirectAttributesModelMap();
////		
////		when(eventsController.updateStatus(tweet, redir, ID).thenReturn(null));
//		when(eventService.findById(eventId)).thenReturn(testEvent);
//		
//		mvc.perform(get("/events/{id}/tweeted", eventId).param("tweet", tweet).accept(MediaType.TEXT_HTML)).andExpect(status().is(302))
//			.andExpect(flash().attributeExists("tweet_success"))
//			.andExpect(model().attributeDoesNotExist("tweet_fail"))
//			.andExpect(handler().methodName("updateStatus"));
//		
//	}
//	
//	@Test
//	public void postEmptyTweet() throws Exception {
//		
//		String tweet = "";
//		
//		Event e = new Event();
//		e.setName("testEvent");
//		e.setTime(null);
//		e.setDate(null);
//		e.setVenue(null);
//		long eventId = (long)1;
//		e.setId(eventId);	
//		Optional<Event> testEvent = Optional.of(e);
//		
//		when(eventService.findById(eventId)).thenReturn(testEvent);
//		
//		mvc.perform(get("/events/{id}/tweeted", eventId).param("tweet", tweet).accept(MediaType.TEXT_HTML)).andExpect(status().is(302))
//			.andExpect(flash().attributeExists("tweet_fail"))
//			.andExpect(model().attributeDoesNotExist("tweet_success"))
//			.andExpect(handler().methodName("updateStatus"));
//		
//	}
	
}
