package uk.ac.man.cs.eventlite.controllers;

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

		model.addAttribute("events", eventService.findAll());
		
		log.error("events loaded");
				
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
	
	@RequestMapping(value ="delete_event", method = RequestMethod.GET) 
	public String deleteEventByID(Long ID) { 	
		// check to find if event with ID exists
		if (eventService.findById(ID).isPresent()) {
			eventService.deleteById(ID);
			log.error("event " + ID + " deleted");
		}		
		// Go back to the events page
		return "redirect:/events"; 
	}

	@RequestMapping(value = "/foundEvents", method = RequestMethod.GET)
	public String getAllByName(@RequestParam (value = "search", required = false) String name, Model model) {
		model.addAttribute("search", eventService.findAllByNameContainingIgnoreCase(name));
		
		return "events/index";
	}
	
}
