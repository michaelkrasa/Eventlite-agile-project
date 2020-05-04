package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;

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
@DirtiesContext
@ActiveProfiles("test")
public class HomePageControllerTest {
	
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
	private HomePageController homePageController;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(homePageController).apply(springSecurity(springSecurityFilterChain))
				.build();
	}
	
	@Test
	public void getIndexWhenNoVenues() throws Exception {
		when(eventService.find3Upcoming()).thenReturn(Collections.<Event> emptyList());
		when(venueService.findTop3Venues()).thenReturn(Collections.<Venue> emptyList());

		mvc.perform(get("/").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("index")).andExpect(handler().methodName("getIndexAttributes"));

		verify(eventService).find3Upcoming();
		verify(venueService).findTop3Venues();
		verifyZeroInteractions(event);
		verifyZeroInteractions(venue);
	}
	
	@Test
	public void getIndexWithVenuesNoEvents() throws Exception {
		when(eventService.find3Upcoming()).thenReturn(Collections.<Event> emptyList());
		when(venueService.findTop3Venues()).thenReturn(Collections.<Venue> singletonList(venue));

		mvc.perform(get("/").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("index")).andExpect(handler().methodName("getIndexAttributes"));

		verify(eventService).find3Upcoming();
		verify(venueService).findTop3Venues();
		verifyZeroInteractions(event);
		verifyZeroInteractions(venue);
	}
	
	@Test
	public void getIndexWithVenuesAndEvents() throws Exception {
		when(eventService.find3Upcoming()).thenReturn(Collections.<Event> singletonList(event));
		when(venueService.findTop3Venues()).thenReturn(Collections.<Venue> singletonList(venue));

		mvc.perform(get("/").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("index")).andExpect(handler().methodName("getIndexAttributes"));

		verify(eventService).find3Upcoming();
		verify(venueService).findTop3Venues();
		verifyZeroInteractions(event);
		verifyZeroInteractions(venue);
	}
//
}
