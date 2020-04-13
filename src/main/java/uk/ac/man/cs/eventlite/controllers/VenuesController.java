package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

	private final static Logger log = LoggerFactory.getLogger(VenuesController.class);
	
	@Autowired
	private VenueService venueService;
	
	@Autowired
	private EventService eventService;

	@RequestMapping(method = RequestMethod.GET)
	public String getAllVenues(Model model) {

		model.addAttribute("venues", venueService.findAll());
				
		return "venues/index";
	}
	
	@RequestMapping(value = "/{venueId}", method = RequestMethod.GET)
    public String getVenueById(@PathVariable String venueId, Model model) {
		
		Optional<Venue> venue = venueService.findById(Long.parseLong(venueId));
		
		if(venue.isEmpty()) {
			return getAllVenues(model);
		}
		
		model.addAttribute("venue", venue.get());
		
		
		List<Event> events = eventService.findAllByVenue(venue.get());
		for(Event e: events) {
			if(e.getDate().compareTo(LocalDate.now())<0) {
				events.remove(e); // remove events before today
			}
			else
				break;	// in order last to first, as soon as one is after today, no more need to be removed
		}
		
		model.addAttribute("events", events);
		
		return "venues/view";
	}
	

	@RequestMapping(method = RequestMethod.PUT)
	public Optional<Venue> findById(Long ID) {
		return venueService.findById(ID);	
	}
	
	@RequestMapping(value ="delete_venue", method = RequestMethod.GET) 
	public String deleteVenueByID(Long ID) { 	
		// check to find if venue with ID exists
		if (venueService.findById(ID).isPresent()) {
			// check if the venue has events
			if (eventService.findAllByVenue(venueService.findById(ID).get()).isEmpty()) {
				venueService.deleteById(ID);
				log.error("venue " + ID + " deleted");
			}
		}	
		// Go back to the current page
		return "redirect:/venues"; 
	}
	

	@RequestMapping(value = "/foundVenues", method = RequestMethod.GET)
	public String getAllByName(@RequestParam (value = "search", required = false) String name, Model model) {
		
		Iterable<Venue> allVenues = venueService.findAllByNameContainingIgnoreCase(name);
		
		model.addAttribute("search", allVenues);
		
		return "venues/index";
	}

}
