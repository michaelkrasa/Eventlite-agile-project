package uk.ac.man.cs.eventlite.dao;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class VenueServiceImpl implements VenueService {
	
	@Autowired
	private VenueRepository venueRepository;
	
	private final static Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

	private final static String DATA = "data/venues.json";

	@Override
	public long count() {
		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findAll();
	}
	
	@Override
	public <V extends Venue> V save(V venue) {
		return(venueRepository.save(venue));
	}

	@Override
	public Optional<Venue> findById(Long ID) {
		return venueRepository.findById(ID);
	}
	
	@Override 
	public void deleteById(Long ID) {
		venueRepository.deleteById(ID);
	} 
	
	@Override
	public Iterable<Venue> findAllByNameContainingIgnoreCase(String name){
		return venueRepository.findAllByNameContainingIgnoreCase(name, Sort.by("name"));
	}
	
	@Override
	public List<Venue> findTop3Venues(){
		if (count() < 3) {
			return venueRepository.findTop3ByNameAsc();
		}
		else {
			return venueRepository.findTop3ByNameAsc().subList(0, 3);
		}
	}

}
