package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;

import uk.ac.man.cs.eventlite.entities.Event;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	private final static Logger log = LoggerFactory.getLogger(EventsController.class);
	private long idOfUpdatedEvent = 0;
	
	@Autowired 
	private VenueService venueService;
	
	@Autowired
	private EventService eventService;

	@RequestMapping(method = RequestMethod.GET)
	public String getAllEvents(Model model) {

		model.addAttribute("events", eventService.findAll());
				
		return "events/index";
	}
	
	@RequestMapping(value="/update", method=RequestMethod.GET)
	public String updateEvent(Model model, @RequestParam String id) {
		log.info("Update method called");
		log.info("id: " + id);
		
		idOfUpdatedEvent = Long.parseLong(id);
		
		Optional<Event> event = eventService.findById(idOfUpdatedEvent);
		if (event.isPresent()) {
			model.addAttribute("eventToUpdate", event.get());
		}
		
		model.addAttribute("venues", venueService.findAll());
		
		return "events/update";
	}
	
	@RequestMapping(value="/update", method=RequestMethod.POST)
	public String saveUpdatedEvent(@ModelAttribute("updatedEvent") Event eventToUpdate, BindingResult errors, Model model) {
		eventService.deleteById(idOfUpdatedEvent); // Delete old event
		eventService.save(eventToUpdate); // Save new event
		return "events/updated"; // Go to updated.html page
	}
}
