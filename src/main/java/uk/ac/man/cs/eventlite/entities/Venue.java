package uk.ac.man.cs.eventlite.entities;


import java.util.List;


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


@Entity
@Table(name = "venues")
public class Venue {
	
	private final static Logger log = LoggerFactory.getLogger(Venue.class);
	
	@Id
	@GeneratedValue
	private long id;

	private String name;

	private int capacity;
	
	private String address1 = "";
	private String address2 = "";
	private String city;
	private String postcode ;
	
	private String locationString; // used to check if its been updated to avoid unnecesary api Calls
	
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
	
	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public void updateLocation() {
		
		String inputLocationString = getLocationString();
		
		MapboxGeocoding locationQuery = MapboxGeocoding.builder()
			.accessToken("pk.eyJ1IjoiajMzMDM2YWoiLCJhIjoiY2s4eGhsMjdoMDk0dzNscDVidXd0OGc3dyJ9.wDg8cWDqltVSBrXxf8R9mg")
			.query(inputLocationString)//.country(string)
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
				  locationString = inputLocationString;
				  
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
		
		// wait 1 sec for asynchronous response
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	// geocode format: {house number} {street} {city} {state} {zip}
	// e.g. {123} {Main St} {Swindon} {SN2 2DQ}	
	public String getLocationString() {
		String location = address1;
		
		if(address2 != null && !address2.contentEquals(""))
			location += " " + address2;
		
		if(city != null && !city.contentEquals(""))
			location += " " + city;
		
		if(postcode != null && !postcode.contentEquals(""))
			location += " " + postcode;
		
		return location;
	}
	
	public void setLocationFields(String address1, String address2, String city, String postcode) {
		this.address1 = address1;
		this.address2 = address2;
		this.city = city;
		this.postcode = postcode;
	}
	
}

