package uk.ac.man.cs.eventlite.controllers;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

	@RequestMapping(method = RequestMethod.GET)
	public String getAllEvents(Model model) {

		model.addAttribute("events", eventService.findAll());
				
		return "events/index";
	}
	
	@RequestMapping(method = RequestMethod.PUT)
	public Optional<Event> findById(Long ID) {
		return eventService.findById(ID);	
	}
	
	// DELETE request made when deleting form on "event", to delete an event
	@RequestMapping(value ="delete_event", method = RequestMethod.GET) // delete
	public String deleteEvent(Long ID) { // , RedirectAttributes redirectAttrs ) {
		
		// check to find if ID exists
		if (eventService.findById(ID).isPresent()) {
			eventService.deleteById(ID);
			// redirectAttrs.addFlashAttribute("ok_message", "Event Deleted.");
		}
		
		// Go back to the current page
		return "redirect:/events";             // redirect:/events
	}

}
