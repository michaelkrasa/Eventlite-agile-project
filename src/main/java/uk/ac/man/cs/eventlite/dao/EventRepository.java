package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventRepository extends CrudRepository<Event, Long>{

	Iterable<Event> findAll(Sort by);
	
	Optional<Event> findById(long id);
	
}