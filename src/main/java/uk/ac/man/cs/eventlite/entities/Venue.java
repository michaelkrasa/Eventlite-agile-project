package uk.ac.man.cs.eventlite.entities;


import java.util.List;
import java.util.function.BooleanSupplier;

//import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
//import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.ac.man.cs.eventlite.config.data.InitialDataLoader;


@Entity
@Table(name = "venues")
public class Venue {
	
	private final static Logger log = LoggerFactory.getLogger(Venue.class);
	
	@Id
	@GeneratedValue
	private long id;

	private String name;

	private int capacity;
	
	private double latitude;
	
	private double longitude;
	
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
	
	public void setLocation(String locationString) {
		MapboxGeocoding locationQuery = MapboxGeocoding.builder()
			.accessToken("pk.eyJ1IjoiajMzMDM2YWoiLCJhIjoiY2s4eGhsMjdoMDk0dzNscDVidXd0OGc3dyJ9.wDg8cWDqltVSBrXxf8R9mg")
			.query(locationString)
			.build();
		
		locationQuery.enqueueCall(new Callback<GeocodingResponse>() {
			@Override
			public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
		 
				List<CarmenFeature> results = response.body().features();
		 
				if (results.size() > 0) {
		 
				  // Log the first results Point.
				  Point location = results.get(0).center();
				  log.info("[Geocode request] : onResponse: " + location.toString());
				  
				  latitude = location.latitude();
				  longitude = location.longitude();
				  
				  log.info("[Geocode request] : lat,long : " + latitude + ", " + longitude);
		 
				} else {
		 
				  // No result for your request were found.
				  log.info("[Geocode request] : onResponse: No result found");
		 
				}
			}
		 
			@Override
			public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
				throwable.printStackTrace();
			}
		});

	}

	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
}
