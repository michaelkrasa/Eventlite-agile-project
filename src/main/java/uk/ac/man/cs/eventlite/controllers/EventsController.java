package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {
	
	private final static Logger log = LoggerFactory.getLogger(EventsController.class);
	
	@Autowired
	private EventService eventService;

	@RequestMapping(method = RequestMethod.GET)
	public String getAllEvents(Model model) {
		
		List<Event> futureEvents = new ArrayList<Event>();
		List<Event> pastEvents = new ArrayList<Event>();
		
		Iterable<Event> allEvents = eventService.findAll();
		for(Event e: allEvents) {
			if(e.getDate().compareTo(LocalDate.now())<0) pastEvents.add(e);
			else futureEvents.add(e);
		}
		
		// past events in reverse chronological order
		Collections.reverse(pastEvents);

		model.addAttribute("events", allEvents);
		model.addAttribute("future_events", futureEvents);
		model.addAttribute("past_events", pastEvents);
		
				
		return "events/index";
	}
		
	@RequestMapping(value = "/{eventId}", method = RequestMethod.GET)
    public String getEventById(@PathVariable String eventId, Model model) {
		
		Optional<Event> event = eventService.findById(Long.parseLong(eventId));
		
		if(event.isEmpty()) {
			return getAllEvents(model);
		}
		
		model.addAttribute("event", event.get());
		
		return "events/view";
	}
	
	@RequestMapping(method = RequestMethod.PUT)
	public Optional<Event> findById(Long ID) {
		return eventService.findById(ID);	
	}
	
	// DELETE request made when deleting form on "event" - to delete an event
	@RequestMapping(value ="delete_event", method = RequestMethod.GET)
	public String deleteEvent(Long ID) { 	
		// check to find if ID exists
		if (eventService.findById(ID).isPresent()) {
			eventService.deleteById(ID);
		}	
		// Go back to the current page
		return "/events"; 

	}

	@RequestMapping(value = "/foundEvents", method = RequestMethod.GET)
	public String getAllByName(@RequestParam (value = "search", required = false) String name, Model model) {
		
		List<Event> futureEvents = new ArrayList<Event>();
		List<Event> pastEvents = new ArrayList<Event>();
		
		Iterable<Event> allEvents = eventService.findAllByNameContainingIgnoreCase(name);
		for(Event e: allEvents) {
			if(e.getDate().compareTo(LocalDate.now())<0) pastEvents.add(e);
			else futureEvents.add(e);
		}
		
		// past events in reverse chronological order
		Collections.reverse(pastEvents);
		
		model.addAttribute("search", allEvents);
		model.addAttribute("search_future", futureEvents);
		model.addAttribute("search_past", pastEvents);
		
		return "events/index";
	}
	
}
