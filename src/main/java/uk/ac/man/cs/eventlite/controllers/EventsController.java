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

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import uk.ac.man.cs.eventlite.dao.EventService;
//import uk.ac.man.cs.eventlite.dao.TwitterService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.utils.TwitterUtils;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	private final static Logger log = LoggerFactory.getLogger(EventsController.class);
	private long idOfEventToUpdate = 0;
	
	@Autowired
	private EventService eventService;
	
	@Autowired 
	private VenueService venueService;
	
	@Autowired
	private TwitterUtils twitterUtils;
	
//	@Autowired
//	private TwitterService twitterService;

	@RequestMapping(method = RequestMethod.GET)
	public String getAllEvents(Model model) throws TwitterException {
		
		log.info("events loaded");

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
		
		getPastTweets(model);
				
		return "events/index";
	}
		
	@RequestMapping(value = "/{eventId}", method = RequestMethod.GET)
    public String getEventById(@PathVariable String eventId, Model model) throws TwitterException {
		
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
		// Go back to the current page
		return "redirect:/events"; 
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
	
	/////////////////////////// UPDATE EVENT ////////////////////////////////////////////////////
	@RequestMapping(value="/update", method = RequestMethod.GET)
	public String updateEvent(Model model, @RequestParam String id) {
		log.info("Update method called");
		log.info("id: " + id);
		
		idOfEventToUpdate = Long.parseLong(id);
		
		Optional<Event> event = eventService.findById(idOfEventToUpdate);
		if (event.isPresent()) {
			model.addAttribute("event", event.get());
		}
		
		model.addAttribute("venues", venueService.findAll());
		
		return "events/update";
	}
	
	@RequestMapping(value="/update", method=RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String saveUpdatedEvent(@RequestBody @Valid @ModelAttribute Event event, 
			BindingResult errors, Model model, RedirectAttributes redirectAttrs) {
		
		System.out.println("-------------------");
		System.out.println(errors);
		
		// If form has errors, stay on event/update (stay on form)
		if (errors.hasErrors()) {
			model.addAttribute("event", event);
			model.addAttribute("venues", venueService.findAll()); // Reload venues 
			return "events/update";
		}
		
		eventService.deleteById(idOfEventToUpdate); // Delete old event
		
		eventService.save(event); // Save new event
		redirectAttrs.addFlashAttribute("ok_message", "Event updated.");
		
		return "redirect:/events"; // Go back to /events
	}
	
	@RequestMapping(value = "/foundEvents", method = RequestMethod.GET)
	public String getAllByName(@RequestParam (value = "search", required = false) String name, Model model) throws TwitterException {
		
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
		
		getPastTweets(model);
		
		return "events/index";
	}
	
	@RequestMapping(value = "/{eventId}/tweeted", method = RequestMethod.GET)
	public RedirectView updateStatus(@RequestParam (value = "tweet", required = false) String tweet, RedirectAttributes redir, @PathVariable String eventId) throws TwitterException {
		
		if (!tweet.isEmpty()) {	
			log.info("Tweet sent - " + tweet);
			Twitter twitter = twitterUtils.getTwitterInstance();
			twitter.updateStatus(tweet);
			RedirectView redirectView = new RedirectView("/events/" + eventId,true);
			redir.addFlashAttribute("tweet_success", tweet);
			return redirectView;
		}
		else {
			RedirectView redirectView = new RedirectView("/events/" + eventId,true);
			redir.addFlashAttribute("tweet_fail", "Please type a message to tweet");
			return redirectView;
		}

		
	}
	
	
	public void getPastTweets(Model model) throws TwitterException {
		log.info("Past tweets loaded");
		
		Twitter twitter = twitterUtils.getTwitterInstance();
		
		List<Status> statuses = twitter.getHomeTimeline();
		if (statuses.size() > 5) statuses = statuses.subList(0, 5);
		
		model.addAttribute("past_tweets", statuses);
	}
	
}
