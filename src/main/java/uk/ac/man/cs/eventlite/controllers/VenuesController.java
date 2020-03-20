package uk.ac.man.cs.eventlite.controllers;

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

	@RequestMapping(method = RequestMethod.GET)
	public String getAllVenues(Model model) {

		model.addAttribute("venues", venueService.findAll());
		
		log.error("venues laoded");
				
		return "venues/index";
	}
	
	@RequestMapping(value = "/{venueId}", method = RequestMethod.GET)
    public String getVenueById(@PathVariable String venueId, Model model) {
		
		Optional<Venue> venue = venueService.findById(Long.parseLong(venueId));
		
		if(venue.isEmpty()) {
			return getAllVenues(model);
		}
		
		model.addAttribute("venue", venue.get());
		
		return "venues/view";
	}
}
