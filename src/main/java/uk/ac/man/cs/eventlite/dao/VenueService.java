package uk.ac.man.cs.eventlite.dao;
import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueService {

	public long count();

	public Iterable<Venue> findAll();
	
	public <V extends Venue> V save(V venue);

	public Optional<Venue> findById(Long ID);

	public void deleteById(Long ID); 

}
