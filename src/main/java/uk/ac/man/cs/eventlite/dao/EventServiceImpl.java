package uk.ac.man.cs.eventlite.dao;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import uk.ac.man.cs.eventlite.entities.Event;

@Service
public class EventServiceImpl implements EventService {
	
	@Autowired
	private EventRepository eventRepository;

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	private final static String DATA = "data/events.json";

	@Override
	public long count() {
		return eventRepository.count();
	}
	
	@Override 
	public Iterable<Event> findAll() { 
		
		return eventRepository.findAll(Sort.by("date", "time")); 
	}
	
	@Override
	public <S extends Event> S save(S event) {
		return(eventRepository.save(event));
	}
	
	@Override
	public Optional<Event> findById(Long eventId) {
		return eventRepository.findById(eventId);
	}
	
}
	  
