package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

	private final static Logger log = LoggerFactory.getLogger(VenuesController.class);
	
	private long idOfUpdatedVenue = 0;
	
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
		Iterator<Event> iterator = events.iterator();
		while(iterator.hasNext()) {
			Event e = iterator.next();
			if(e.getDate().compareTo(LocalDate.now())<0) {
				iterator.remove(); // remove events before today
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
	
	// GET request made by button, taking user to page "venues/new" to input event details
	@RequestMapping(value = "/new", method = RequestMethod.GET)
	public String newEvent(Model model) {
		
		if (!model.containsAttribute("venue")) {
			model.addAttribute("venue", new Venue());
		}
				
		return "venues/new";
	}
	
	// POST request made when submitting form on "venues/new", to create the new event
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createEvent(@RequestBody @Valid @ModelAttribute Venue venue,
			BindingResult errors, Model model, RedirectAttributes redirectAttrs) {
		
		System.out.println("=============================");
		System.out.println(venue.getPostcode());
		System.out.println("=============================");
		
		// Set geolocation information
		venue.updateLocation();
		
		log.info("Location updated to: " + venue.getLocationString());
		log.info("Lat, Long updated to: " + venue.getLatitude() + ", " + venue.getLongitude());
		
		if(venue.getLongitude() == 0 && venue.getLatitude() == 0) {
			model.addAttribute("locationError", "Location is invalid");
			return "venues/new";
		}
		
		// If form has errors, stay on venues/new (stay on form)
		if (errors.hasErrors()) {
			model.addAttribute("venue", venue);
			return "venues/new";
		}
		
		// If no errors, save the venue
		venueService.save(venue);
		
		redirectAttrs.addFlashAttribute("ok_message", "New venue added.");
		
		// Go back to /venues
		return "redirect:/venues";
	}

	@RequestMapping(value="/update", method = RequestMethod.GET)
	public String updateVenue(Model model, @RequestParam String id) {
		log.info("Update method called");
		log.info("id: " + id);
		
		// Convert the id into a long and store in the class
		idOfUpdatedVenue = Long.parseLong(id);
		
		// Find the venueToUpdate and add it to model
		Optional<Venue> venue = venueService.findById(idOfUpdatedVenue);
		if (venue.isPresent()) {
			model.addAttribute("venueToUpdate", venue.get());
		}

		
		// Go to the update page
		return "venues/update";
	}
	
	@RequestMapping(value="/update", method=RequestMethod.POST)
	public String saveUpdatedVenue(@ModelAttribute("updatedVenue") Venue venueToUpdate, BindingResult errors, Model model) {
		
		// If form has errors, stay on event/new (stay on form)
		
		
		// Get the venue we want to update
		Optional<Venue> venue = venueService.findById(idOfUpdatedVenue);
		
		// only update if new address is different
		if(!venue.get().getLocationString().equals(venueToUpdate.getLocationString())) {
			venue.get().setLocationFields(venueToUpdate.getAddress1(), venueToUpdate.getAddress2(),
										  venueToUpdate.getCity(), venueToUpdate.getPostcode());
			
						
			// update to use new location fields
			venue.get().updateLocation();
			
			log.info("Location updated to: " + venue.get().getLocationString());
			log.info("Lat, Long updated to: " + venue.get().getLatitude() + ", " + venue.get().getLongitude());
			
			if(venue.get().getLongitude() == 0 && venue.get().getLatitude() == 0) {
				model.addAttribute("locationError", "Location is invalid");
				model.addAttribute("venueToUpdate", venueToUpdate);
				return "venues/update";
			}
		}
		
		if (errors.hasErrors()) { 
			model.addAllAttributes(errors.getModel());
			model.addAttribute("venueToUpdate", venueToUpdate);
			return "venues/update";
		}
		
		
		// Set the values of this venue to what the user inputted
		venue.get().setName(venueToUpdate.getName());
		venue.get().setCapacity(venueToUpdate.getCapacity());
		
		// Save it
		venueService.save(venue.get());
		// Go back to /venues
		return "redirect:/venues";
	}
	
}
