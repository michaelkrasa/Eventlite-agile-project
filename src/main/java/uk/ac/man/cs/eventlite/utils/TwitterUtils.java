package uk.ac.man.cs.eventlite.utils;

import org.springframework.stereotype.Component;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Component
public class TwitterUtils {
	
	public Twitter getTwitterInstance() {
		ConfigurationBuilder cb = new ConfigurationBuilder();	
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("S3XBupwKFKkDrViDlXBsnSp3H")
		  .setOAuthConsumerSecret("Y3ZdNVPDUyWZx85mnU2fEj5vTKfvVb234kKTjtIINhkBD9Rcl0")
		  .setOAuthAccessToken("1254555795431227394-Px2eytiI8IvU8x9YnKRgZwAKLl8Auk")
		  .setOAuthAccessTokenSecret("5SnhT8p2Cs2z7Ye4XisalmSghh1sTzcO9OtuvIxzoinb3");
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		return twitter;
	}


}
