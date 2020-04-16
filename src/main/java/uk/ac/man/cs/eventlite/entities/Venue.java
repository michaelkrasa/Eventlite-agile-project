package uk.ac.man.cs.eventlite.entities;

//import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
//import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "venues")
public class Venue {
	
	@Id
	@GeneratedValue
	private long id;
	
	@NotNull(message = "Venues must have a name")
	@Size(max = 256)
	private String name;
	
	@NotNull(message = "Venues must have a capacity")
	@Min(value = 0, message = "Capacity must be positive")
	private int capacity;
	
	@NotEmpty(message = "Venues must have a street address")
	@Size(max = 300)
	private String streetAddress;
	
	@NotEmpty(message = "Venues must have a postcode")
	private String postcode;
	
//	@OneToMany(mappedBy = "venues")
//	private Set<Event> events;

	public Venue() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}
	
	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
}
