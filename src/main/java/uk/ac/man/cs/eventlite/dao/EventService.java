package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public <S extends Event> S save(S event);
	
	public void deleteById(Long ID); 
	
	public Optional<Event> findById(Long ID);
	
}
