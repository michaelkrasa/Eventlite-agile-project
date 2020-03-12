package uk.ac.man.cs.eventlite.controllers;

import java.util.Optional;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;

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
	
	@Autowired 
	private VenueService venueService;

	@RequestMapping(method = RequestMethod.GET)
	public String getAllEvents(Model model) {

		model.addAttribute("events", eventService.findAll());
		
		log.error("events laoded");
				
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
	
	
	/////////////////////////// ADD EVENT ////////////////////////////////////////////////////
	
	// GET request made by button, taking user to page "event/new" to input event details
	@RequestMapping(value = "/new", method = RequestMethod.GET)
	public String newEvent(Model model) {
		
		if (!model.containsAttribute("event")) {
			model.addAttribute("event", new Event());
		}
		
		// Send "venues" as response parameter to events/new (for venue dropdown)
		if (!model.containsAttribute("venues")) {
			model.addAttribute("venues", venueService.findAll());
		}
		
		
		return "events/new";
	}
	
	// POST request made when submitting form on "event/new", to create the new event
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createEvent(@RequestBody @Valid @ModelAttribute Event event,
			BindingResult errors, Model model, RedirectAttributes redirectAttrs) {
		
		// If form has errors, stay on event/new (stay on form)
		if (errors.hasErrors()) {
			model.addAttribute("event", event);
			model.addAttribute("venues", venueService.findAll()); // Reload venues 
			return "events/new";
		}	
		
		// If no errors, save the event
		eventService.save(event);
		redirectAttrs.addFlashAttribute("ok_message", "New event added.");
		
		// Go back to /events
		return "redirect:/events";
	}


	@RequestMapping(value = "/foundEvents", method = RequestMethod.GET)
	public String getAllByName(@RequestParam (value = "search", required = false) String name, Model model) {
		model.addAttribute("search", eventService.findAllByNameContainingIgnoreCase(name));
		
		return "events/index";
	}
	
}
