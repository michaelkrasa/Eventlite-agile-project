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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
	public void getIndexWhenNoVenues() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event> emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		verify(venueService).findAll();
		verifyZeroInteractions(event);
		verifyZeroInteractions(venue);
	}

	@Test
	public void getIndexWithVenues() throws Exception {
		
		when(venueService.findAll()).thenReturn(Collections.<Venue> singletonList(venue));

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		verify(venueService).findAll();
		verifyZeroInteractions(event);
		verifyZeroInteractions(venue);
	}
	
	@Test
	public void getIndexWithNoVenuesSearch() throws Exception {
		
		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());
		
		mvc.perform(get("/venues/foundVenues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
		.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllByName"));

		verify(venueService).findAllByNameContainingIgnoreCase(null);
		verifyZeroInteractions(venue);
	}
	
	@Test
	public void getIndexWithVenuesSearch() throws Exception {
		
		when(venueService.findAll()).thenReturn(Collections.<Venue> singletonList(venue));

		mvc.perform(get("/venues/foundVenues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllByName"));

		verify(venueService).findAllByNameContainingIgnoreCase(null);
		verifyZeroInteractions(venue);
	}
			
	@Test
	public void getVenueFromID() throws Exception {
		
		venue.setId(100L);
		
		// optional event
		Optional<Venue> optV = Optional.of(venue);
		
		// mock method
		when(venueService.findById(100L)).thenReturn(optV);
				
		
		// call the correct mvc function
		mvc.perform(get("/venues/100").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
		.andExpect(view().name("venues/view")).andExpect(handler().methodName("getVenueById"));
		
		
		// check that method is called correctly, 2 times. one for assertequals, one for mvc.perform
		verify(venueService).findById(100L);
	}
	
	@Test
	public void getNullEventFromID() throws Exception {
		when(venueService.findById(0L)).thenReturn(Optional.empty());
		
		// routes to index as event 0 doesnt exist
		mvc.perform(get("/venues/0").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getVenueById"));
		
		verify(venueService).findById(0L);
	}
	
	@Test void newVenuePage() throws Exception {
		// Load new venues page
		mvc.perform(get("/venues/new")
			        .accept(MediaType.TEXT_HTML))
		            .andExpect(status().is(200))
		            .andExpect(view().name("venues/new"));
	}
	
	
	@Test void newVenue() throws Exception {
		ArgumentCaptor<Venue> arg = ArgumentCaptor.forClass(Venue.class);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
		params.add("name", "Kilburn Building");
		params.add("address1", "Kilburn Building University of Manchester");
		params.add("address2", "Oxford Rd");
		params.add("city", "Manchester");
		params.add("postcode", "M13 9PL");
		params.add("capacity", "20");
		
		mvc.perform(MockMvcRequestBuilders.post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.params(params)
				.accept(MediaType.TEXT_HTML)
				.with(csrf()))
		.andExpect(status().is3xxRedirection()).andExpect(content().string(""))
		.andExpect(view().name("redirect:/venues")).andExpect(model().hasNoErrors())
		.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeExists("ok_message"));

		verify(venueService).save(arg.capture());
		System.out.println("=============================");
		System.out.println(arg.getValue().getName());
		System.out.println("=============================");
		assertThat("Kilburn Building", equalTo(arg.getValue().getName()));
		assertThat("Kilburn Building University of Manchester", equalTo(arg.getValue().getAddress1()));
		assertThat("Oxford Rd", equalTo(arg.getValue().getAddress2()));
		assertThat("Manchester", equalTo(arg.getValue().getCity()));
		assertThat("M13 9PL", equalTo(arg.getValue().getPostcode()));
		assertThat(20, equalTo(arg.getValue().getCapacity()));
		
//		// Test no authentication
//		mvc.perform(MockMvcRequestBuilders.post("/venues").contentType(MediaType.APPLICATION_FORM_URLENCODED)
//				.params(params)
//				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
//		.andExpect(header().string("Location", endsWith("/sign-in")));
//
//		verify(eventService, never()).save(event);
//		

	}
	
	@Test
	public void addVenueWithNoAuthentication() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
		params.add("name", "Kilburn Building");
		params.add("address1", "Kilburn Building University of Manchester");
		params.add("address2", "Oxford Rd");
		params.add("city", "Manchester");
		params.add("postcode", "M13 9PL");
		params.add("capacity", "20");
				
		mvc.perform(MockMvcRequestBuilders.post("/venues").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.params(params)
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
		.andExpect(header().string("Location", endsWith("/sign-in")));

		verify(venueService, never()).save(venue);
	}
	
	
	@Test
	public void addVenueWithNoName() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
		params.add("address1", "Kilburn Building University of Manchester");
		params.add("address2", "Oxford Rd");
		params.add("city", "Manchester");
		params.add("postcode", "M13 9PL");
		params.add("capacity", "20");
		
		mvc.perform(MockMvcRequestBuilders.post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.params(params).accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
		.andExpect(view().name("venues/new"))
		.andExpect(model().attributeHasFieldErrors("venue", "name"))
		.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void addVenueWithNoAddress1() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
		params.add("name", "Kilburn Building");
		params.add("address1", "");
		params.add("address2", "Oxford Rd");
		params.add("city", "Manchester");
		params.add("postcode", "M13 9PL");
		params.add("capacity", "20");
		
		mvc.perform(MockMvcRequestBuilders.post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.params(params).accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
		.andExpect(view().name("venues/new"))
		.andExpect(model().attributeHasFieldErrors("venue", "address1"))
		.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(venue);
	}

	@Test
	public void addVenueWithNoPostcode() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
		params.add("name", "Kilburn Building");
		params.add("address1", "Kilburn Building University of Manchester");
		params.add("address2", "Oxford Rd");
		params.add("city", "Manchester");
		params.add("postcode", "");
		params.add("capacity", "20");
		
		mvc.perform(MockMvcRequestBuilders.post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.params(params).accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
		.andExpect(view().name("venues/new"))
		.andExpect(model().attributeHasFieldErrors("venue", "postcode"))
		.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void addVenueWithNoCapacity() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
		params.add("name", "Kilburn Building");
		params.add("address1", "");
		params.add("address2", "Oxford Rd");
		params.add("city", "Manchester");
		params.add("postcode", "M13 9PL");
		params.add("capacity", "");
		
		mvc.perform(MockMvcRequestBuilders.post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.params(params).accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
		.andExpect(view().name("venues/new"))
		.andExpect(model().attributeHasFieldErrors("venue", "capacity"))
		.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void addVenueLongName() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
		params.add("name", "Test event test event test event test event test event test event "
				+ "test event test event test event test event test event test event"
				+ "test event test event test event test event test event test event"
				+ "test event test event test event test event test event test event");
		params.add("address1", "Kilburn Building University of Manchester");
		params.add("address2", "Oxford Rd");
		params.add("city", "Manchester");
		params.add("postcode", "M13 9PL");
		
		mvc.perform(MockMvcRequestBuilders.post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.params(params)
				.accept(MediaType.TEXT_HTML)
				.with(csrf()))
		.andExpect(status().isOk()).andExpect(view().name("venues/new"))
		.andExpect(model().attributeHasFieldErrors("venue", "name"))
		.andExpect(handler().methodName("createVenue")).andExpect(flash().attributeCount(0));

		verify(venueService, never()).save(venue);
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
	//*
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
		List<Event> events = Arrays.asList(new Event[]{e}); 
		
		// mocked methods
		when(venueService.findById(ID)).thenReturn(testVenue);
		when(eventService.findById(ID)).thenReturn(testEvent);
		when(eventService.findAllByVenue(v)).thenReturn(Arrays.asList(new Event[]{e})); 
		doNothing().when(venueService).deleteById(ID);
			
		// assertion checks
		assertEquals(venueService.findById(ID).get(), testVenue.get());
		assertEquals(eventService.findAllByVenue(v), events);	
			
		// performing functions being tested
		mvc.perform(get("/venues/delete_venue?ID=" + ID)
			.accept(MediaType.TEXT_HTML))
			.andExpect(status().isFound())
			.andExpect(view().name("redirect:/venues"))
			.andExpect(handler().methodName("deleteVenueByID"));
		
		// verifying 
		verify(eventService, VerificationModeFactory.times(2)).findAllByVenue(v);
		verify(venueService, VerificationModeFactory.times(3)).findById(ID);
		verify(venueService, VerificationModeFactory.times(0)).deleteById(ID);				
	} //*/
	
	@Test
	public void deleteVenueWithVenueWithNoEvent() throws Exception {
		// data needed for test
		Venue v = new Venue();
		v.setName("testVenue");
		v.setCapacity(80);
		long ID = (long)1;
		v.setId(ID);	
		Optional<Venue> testVenue = Optional.of(v);	
		List<Event> events = Collections.emptyList();
		
		// mocked methods
		when(venueService.findById(ID)).thenReturn(testVenue);
		doNothing().when(venueService).deleteById(ID);
		when(eventService.findAllByVenue(v)).thenReturn(Collections.emptyList()); 
	
		// assertion checks	
		assertEquals(venueService.findById(ID).get(), testVenue.get());
		assertEquals(eventService.findAllByVenue(v), events);
		
		// performing functions being tested
		mvc.perform(get("/venues/delete_venue?ID=" + ID)
			.accept(MediaType.TEXT_HTML))
			.andExpect(status().isFound())
			.andExpect(view().name("redirect:/venues"))
			.andExpect(handler().methodName("deleteVenueByID"));
		
		// verifying 
		verify(eventService, VerificationModeFactory.times(2)).findAllByVenue(v);
		verify(venueService, VerificationModeFactory.times(3)).findById(ID);
		verify(venueService, VerificationModeFactory.times(1)).deleteById(ID);
	}
	
	@Test
	public void updateVenueWhereVenueExists() throws Exception {
		
		Venue v = new Venue();
		v.setName("testVenue");
		v.setCapacity(80);
		long ID = (long)1;
		v.setId(ID);	
		Optional<Venue> testVenue = Optional.of(v);	
		
		when(venueService.findById(ID)).thenReturn(testVenue);
		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());
		
		mvc.perform(get("/venues/update?id=" + ID)
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/update"))
				.andExpect(model().attributeExists("venueToUpdate"))
				.andExpect(handler().methodName("updateVenue"));
		
		verify(venueService, VerificationModeFactory.times(1)).findById(ID);
		
	}
	
	@Test
	public void updateVenueWhereNoVenue() throws Exception {
		long ID = (long)1;
		Optional<Venue> testVenue = Optional.empty();
		
		when(venueService.findById(ID)).thenReturn(testVenue);
		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());
		
		mvc.perform(get("/venues/update?id=" + ID)
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/update"))
				.andExpect(model().attributeDoesNotExist("venueToUpdate"))
				.andExpect(handler().methodName("updateVenue"));
		
		verify(venueService, VerificationModeFactory.times(1)).findById(ID);
	}
	
	// test that adding a Venue's location with a string, converts that into a coordinate to be stored.
	@Test
	public void mapboxAddLocation() {
		Venue testVenue = new Venue();
		
		// double defaults at 0
		assertTrue(testVenue.getLatitude() == 0 && testVenue.getLongitude() == 0);
		
		// update location
		testVenue.setAddress1("Kilburn Building");
		testVenue.setCity("Manchester");
		testVenue.updateLocation();
		
		// 53.467524, -2.233915 are the lat, long of Kilburn ( results of the first call of this and verified in google maps)
		assertTrue(testVenue.getLatitude() == 53.467524 && testVenue.getLongitude() == -2.233915);
		assertTrue(testVenue.getLocationString().contentEquals("Kilburn Building Manchester"));
	}
}

