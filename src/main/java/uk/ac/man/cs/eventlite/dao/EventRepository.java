package uk.ac.man.cs.eventlite.dao;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventRepository extends CrudRepository<Event, Long>{

	Iterable<Event> findAll(Sort by);

	Iterable<Event> findAllByNameContainingIgnoreCase(String name, Sort by);
	
	Optional<Event> findById(long id);

	List<Event> findAllByVenue(Venue venue, Sort by);
	
	public Iterable<Event> findTop3ByDateGreaterThanEqualOrderByDateAscTimeAscNameAsc(LocalDate date);
	
}