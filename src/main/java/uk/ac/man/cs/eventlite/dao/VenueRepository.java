package uk.ac.man.cs.eventlite.dao;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;

import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueRepository extends CrudRepository<Venue, Long> {

	Iterable<Venue> findAllByNameContainingIgnoreCase(String name, Sort by);

	@Query("SELECT v FROM Venue v, Event e WHERE e.venue = v.id GROUP BY v.id ORDER BY COUNT(e.id) DESC")
    public List<Venue> findTop3ByNameAsc();
	
}
