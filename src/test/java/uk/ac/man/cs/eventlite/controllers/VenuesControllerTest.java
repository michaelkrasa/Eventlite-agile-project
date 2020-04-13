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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
public class VenuesControllerTest {
	
	private final static String BAD_ROLE = "USER";

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
	private VenuesController venuesController;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(venuesController).apply(springSecurity(springSecurityFilterChain))
				.build();
	}
	
	@Test
	public void deleteVenueWhileNoVenue() throws Exception {	
		// data needed for test
		long ID = (long)1;
		Optional<Venue> testVenue = Optional.empty();
		
		// mocked methods
		when(venueService.findById(ID)).thenReturn(testVenue);
		doNothing().when(venueService).deleteById(ID);

		// assertion checks
		assertFalse(venueService.findById(ID).isPresent());
		
		// performing functions being tested
		mvc.perform(get("/venues/delete_venue?ID=" + ID)
			.accept(MediaType.TEXT_HTML))
			.andExpect(status().isFound())
			.andExpect(view().name("redirect:/venues"))
			.andExpect(handler().methodName("deleteVenueByID"));	
			
		// verifying 
		verify(venueService, VerificationModeFactory.times(2)).findById(ID);
		verify(venueService, VerificationModeFactory.times(0)).deleteById(ID);			
	}
	
	@Test
	public void deleteVenueWithVenueWithEvent() throws Exception {
		// data needed for test
		Venue v = new Venue();
		v.setName("testVenue");
		v.setCapacity(80);
		long ID = (long)1;
		v.setId(ID);	
		Optional<Venue> testVenue = Optional.of(v);
		Event e = new Event();
		e.setName("testEvent");
		e.setTime(null);
		e.setDate(null);
		e.setVenue(v);
		e.setId(ID);	
		Optional<Event> testEvent = Optional.of(e);
		
		// mocked methods
		when(venueService.findById(ID)).thenReturn(testVenue);
		doNothing().when(venueService).deleteById(ID);
	
		// assertion checks
		assertEquals(venueService.findById(ID).get(), testVenue.get());
		assertEquals(eventService.findById(ID).get(), testEvent.get());	
			
		// performing functions being tested
		mvc.perform(get("/venues/delete_venue?ID=" + ID)
			.accept(MediaType.TEXT_HTML))
			.andExpect(status().isFound())
			.andExpect(view().name("redirect:/venues"))
			.andExpect(handler().methodName("deleteVenueByID"));
		
		// verifying 
		verify(eventService, VerificationModeFactory.times(1)).findById(ID);
		verify(venueService, VerificationModeFactory.times(3)).findById(ID);
		verify(venueService, VerificationModeFactory.times(1)).deleteById(ID);				
	}
	
	@Test
	public void deleteVenueWithVenueWithNoEvent() throws Exception {
		// data needed for test
		Venue v = new Venue();
		v.setName("testVenue");
		v.setCapacity(80);
		long ID = (long)1;
		v.setId(ID);	
		Optional<Venue> testVenue = Optional.of(v);	
		Optional<Event> testEvent = Optional.empty();
		
		// mocked methods
		when(venueService.findById(ID)).thenReturn(testVenue);
		doNothing().when(venueService).deleteById(ID);
	
		// assertion checks	
		assertEquals(venueService.findById(ID).get(), testVenue.get());
		assertFalse(eventService.findById(ID).isPresent());
			
		// performing functions being tested
		mvc.perform(get("/venues/delete_venue?ID=" + ID)
			.accept(MediaType.TEXT_HTML))
			.andExpect(status().isFound())
			.andExpect(view().name("redirect:/venues"))
			.andExpect(handler().methodName("deleteVenueByID"));
		
		// verifying 
		verify(eventService, VerificationModeFactory.times(1)).findById(ID);
		verify(venueService, VerificationModeFactory.times(3)).findById(ID);
		verify(venueService, VerificationModeFactory.times(0)).deleteById(ID);
	}
		
}
