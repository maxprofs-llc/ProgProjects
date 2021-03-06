package StreamConsumer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Stream;

import Tokenizer.Tweet;
import datasetCollector2.Listener;
import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class StreamHandler {
	
	String relativeFilePath_apiKeys = "apikeys/keys.txt";
	String[] apiKeys = new String[4];
	
	//public AtomicInteger queue_size;
	public ThreadPoolExecutor executor;
	//public BlockingQueue queue = new ArrayBlockingQueue(10000);
	public StreamListener listener;
	
	TwitterStream twitterStream;
	
	public ConcurrentLinkedDeque<Tweet> tweets = new ConcurrentLinkedDeque<Tweet>();
	public ConcurrentLinkedDeque<Tweet> processedTweets = new ConcurrentLinkedDeque<Tweet>();
	
	String[] queries;

	public StreamHandler() {
		// create a pool with max 4 threads
		// executing at once, this can and
		// should be changed in the future...
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
		readKeysFromFile();
		openAndListenStream();
	}
	
	public StreamHandler(ArrayList<String> queries, String apiKeysPath) {
		relativeFilePath_apiKeys = (apiKeysPath != null)? apiKeysPath : relativeFilePath_apiKeys;
		this.queries = (queries != null && !queries.isEmpty())? queries.toArray(new String[] {}) : null;
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
		readKeysFromFile();
		openAndListenStream();
	}

	
	public void openAndListenStream() {
		// Twitter4J setup
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey(apiKeys[0])
                .setOAuthConsumerSecret(apiKeys[1])
                .setOAuthAccessToken(apiKeys[2])
                .setOAuthAccessTokenSecret(apiKeys[3])
                .setTweetModeExtended(true)
        		.setJSONStoreEnabled(true);
     
        twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
        
        // thread pool
        // executor = Executors.newWorkStealingPool();
        // queue_size = new AtomicInteger(0);
        

        listener = new StreamListener(this);
        // Listner used when collecting dataset
        // Listener listener = new Listener();
        twitterStream.addListener(listener);
        
        // query filter stream
        if (queries != null) {
            FilterQuery tweetFilterQuery = new FilterQuery();
            tweetFilterQuery.track(queries); // OR on keywords
            twitterStream.filter(tweetFilterQuery);
            System.out.println("\t-> Stream with query started.");
            for (String s : queries) System.out.print(s + ", ");
        }
        
        // regular stream
        else {
        	System.out.println("\t-> Regular stream started.");
        	twitterStream.sample();
        }
	}
	
	public void stopStream() {
		twitterStream.removeListener(listener);
		twitterStream.clearListeners(); // ?
		twitterStream.cleanUp();
		twitterStream = null; // ?
	}
	
	private static int index = 0;
	
	private void readKeysFromFile() {
		try (Stream<String> lines = Files.lines(Paths.get(relativeFilePath_apiKeys), Charset.defaultCharset())) {
			//System.out.println(lines.count());
			lines.forEachOrdered(
				line -> {
					System.out.println(line);
					apiKeys[index] = line;
					index++;
				}
			);
		} catch (IOException e) {
			System.out.println("Error reading api keys!");
			e.printStackTrace();
		}
		index = 0;
	}

}
