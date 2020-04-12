package uk.ac.man.cs.eventlite.dao;

import java.util.List;
import java.util.Optional;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public <S extends Event> S save(S event);
	
	public Iterable<Event> findAllByNameContainingIgnoreCase(String name);
	
	public void deleteById(Long ID); 
	
	public Optional<Event> findById(Long ID);
	
	public List<Event> findAllByVenue(Venue venue);
	
}
