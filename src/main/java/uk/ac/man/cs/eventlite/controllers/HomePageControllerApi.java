package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping(value = "/api", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class HomePageControllerApi {

    @RequestMapping(method = RequestMethod.GET)
    public Resources getHomePageLinks() {

        Link eventsLink = linkTo(EventsControllerApi.class).withRel("events");
        Link venuesLink = linkTo(VenuesControllerApi.class).withRel("venues");

        return new Resources(new ArrayList(), eventsLink, venuesLink);
    }
}
