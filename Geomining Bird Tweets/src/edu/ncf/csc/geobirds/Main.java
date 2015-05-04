package edu.ncf.csc.geobirds;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class Main {
	static Twitter twitter = TwitterFactory.getSingleton();

	public static void main(String[] args) throws TwitterException, IOException {
		BufferedReader in = new BufferedReader(new FileReader("commonnames.txt"));

		System.out.println("===============================\n" + "Starting Egyptian Goose Geomine\n      Built by the NCCSC\n" + "===============================");
		ArrayList<BirdTweet> tweets = new ArrayList<BirdTweet>();
		for (String line; (line = in.readLine()) != null;) {
			tweets.addAll(doSearch(line));
		}
		in.close();
		System.out.println();
		tweets = removeDuplicates(tweets);

		int records = 0;
		PrintWriter print = new PrintWriter("egyptiangoosetweets.csv");
		for (BirdTweet bt : tweets) {
			if (bt.tweet.getGeoLocation() != null) {
				print.println(records + "," + bt);
				records++;
				System.out.println(bt);
			}
		}
		System.out.println("\nFound a grand total of " + tweets.size() + " tweets");

		print.flush();
		print.close();
	}

	public static ArrayList<BirdTweet> doSearch(String q) {
		System.out.println("\nSearching for: " + q);
		int maxNumberOfTweets = 2048;
		Query query = new Query(q);
		query.setSince("2012-01-01");

		long lastID = Long.MAX_VALUE;
		ArrayList<BirdTweet> tweets = new ArrayList<BirdTweet>();
		while (tweets.size() < maxNumberOfTweets) {
			if (maxNumberOfTweets - tweets.size() > 100)
				query.setCount(100);
			else
				query.setCount(maxNumberOfTweets - tweets.size());
			try {
				QueryResult result = twitter.search(query);
				if (result.getTweets().size() == 0)
					break;
				tweets.addAll(convertToBirdTweets(result.getTweets(), query));
				System.out.println("Gathered " + result.getTweets().size() + " tweets...");
				for (BirdTweet t : tweets)
					if (t.tweet.getId() < lastID)
						lastID = t.tweet.getId();
			}

			catch (TwitterException te) {
				System.out.println("Couldn't connect: " + te);
			}
			query.setMaxId(lastID - 1);
		}

		System.out.println("Found a total of " + tweets.size() + " tweets");

		return tweets;
	}

	public static ArrayList<BirdTweet> convertToBirdTweets(List<Status> sts, Query q) {
		ArrayList<BirdTweet> bts = new ArrayList<BirdTweet>();
		for (Status s : sts) {
			bts.add(new BirdTweet(s, q));
		}
		return bts;
	}

	public static ArrayList<BirdTweet> removeDuplicates(ArrayList<BirdTweet> list) {
		ArrayList<Long> seen = new ArrayList<Long>();
		ArrayList<BirdTweet> no_dupes = new ArrayList<BirdTweet>();
		for (BirdTweet bt : list) {
			if (!seen.contains((Long) bt.tweet.getId())) {
				seen.add((Long) bt.tweet.getId());
				no_dupes.add(bt);
			}
		}
		return no_dupes;
	}

	static class BirdTweet {
		Status tweet;
		String query;

		public BirdTweet(Status s, Query q) {
			tweet = s;
			query = q.getQuery();
		}

		public String toString() {
			GeoLocation geodata = tweet.getGeoLocation();
			String lat = geodata == null ? "NA" : String.valueOf(geodata.getLatitude());
			String lon = geodata == null ? "NA" : String.valueOf(geodata.getLongitude());
			return tweet.getId() + "," + query + "," + lat + "," + lon + "," + tweet.getCreatedAt();
		}
	}
}
