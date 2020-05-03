package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.entities.Event;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {

	@Autowired
	private VenueService venueService;
	@Autowired
	private EventService eventService;

	@RequestMapping(method = RequestMethod.GET)
	public Resources<Resource<Venue>> getAllVenues() {

		return venueToResource(venueService.findAll());
	}
	
	// Shows all events that are at the specified venue
	@RequestMapping(value = "/{id}/events", method = RequestMethod.GET)
	public Resources<Resource<Event>> getEventsForVenue(@PathVariable("id") long id) {
		
		// Get all events at that venue
		List<Event> events = eventService.findAllByVenue(venueService.findById(id).get());
		
		List<Resource<Event>> resources = new ArrayList<Resource<Event>>();
		for (Event event : events) {
			resources.add(eventToResource(event));
		}
		return new Resources<Resource<Event>>(resources);
	}
	
	
	// Shows next 3 events that are at the specified venue
	@RequestMapping(value = "/{id}/next3events", method = RequestMethod.GET)
	public Resources<Resource<Event>> getNext3Events(@PathVariable("id") long id) {

		// Get next 3 events at that venue
		List<Event> events = eventService.findAllByVenue(venueService.findById(id).get());
		List<Event> resultEvents = new ArrayList<Event>();

		for(Event e : events) {
			if(resultEvents.size() < 3)
				resultEvents.add(e);
			else break;
		}
		
		List<Resource<Event>> resources = new ArrayList<Resource<Event>>();
		for (Event event : resultEvents) {
			resources.add(eventToResource(event));
		}
		return new Resources<Resource<Event>>(resources);
	}
	
	
	private Resource<Event> eventToResource(Event event) {
		Link selfLink = linkTo(EventsControllerApi.class).slash(event.getId()).withSelfRel();
		Link venueLink = linkTo(EventsControllerApi.class).slash(event.getId()).slash("venue").withRel("venue");
		
		return new Resource<Event>(event, selfLink, venueLink);
	}

	private Resource<Venue> venueToResource(Venue venue) {
		Link selfLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).withSelfRel();
		Link venueLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).withRel("venue");
		Link eventsLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).slash("events").withRel("events");
		Link next3eventsLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).slash("next3events").withRel("next3events");
		
		return new Resource<Venue>(venue, selfLink, venueLink, eventsLink, next3eventsLink);
	}

	private Resources<Resource<Venue>> venueToResource(Iterable<Venue> venues) {
		Link selfLink = linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel();

		List<Resource<Venue>> resources = new ArrayList<Resource<Venue>>();
		for (Venue venue : venues) {
			resources.add(venueToResource(venue));
		}

		return new Resources<Resource<Venue>>(resources, selfLink);
	}
}
