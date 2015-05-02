package edu.ncf.csc.geobirds;

import java.util.ArrayList;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class Main {
	public static void main(String[] args) throws TwitterException {
		Twitter twitter = TwitterFactory.getSingleton();

		int numberOfTweets = 2048;
		Query query = new Query("eagle");
		query.setSince("2012-01-01");

		long lastID = Long.MAX_VALUE;
		ArrayList<Status> tweets = new ArrayList<Status>();
		while (tweets.size() < numberOfTweets) {
			if (numberOfTweets - tweets.size() > 100)
				query.setCount(100);
			else
				query.setCount(numberOfTweets - tweets.size());
			try {
				QueryResult result = twitter.search(query);
				tweets.addAll(result.getTweets());
				System.out.println("Gathered " + tweets.size() + " tweets");
				for (Status t : tweets)
					if (t.getId() < lastID)
						lastID = t.getId();

			}

			catch (TwitterException te) {
				System.out.println("Couldn't connect: " + te);
			}
			query.setMaxId(lastID - 1);
		}

		int sample = 0;
		for (Status s : tweets) {
			GeoLocation geodata = s.getGeoLocation();
			if (geodata == null || geodata.getLatitude() == 0 || geodata.getLongitude() == 0)
				continue;
			String lat = String.valueOf(geodata.getLatitude());
			String lon = String.valueOf(geodata.getLongitude());
			System.out.println(sample++ + " " + query.getQuery() + " " + lat + ", " + lon);
		}
	}
}
