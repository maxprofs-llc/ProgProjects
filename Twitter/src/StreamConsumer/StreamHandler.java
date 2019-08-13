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
	
	public ConcurrentLinkedDeque<Tweet> tweets = new ConcurrentLinkedDeque<Tweet>();
	public ConcurrentLinkedDeque<Tweet> processedTweets = new ConcurrentLinkedDeque<Tweet>();

	public StreamHandler() {
		// create a pool with max 4 threads
		// executing at once, this can and
		// should be changed in the future...
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
     
        TwitterStream twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
        
        // thread pool
        // executor = Executors.newWorkStealingPool();
        // queue_size = new AtomicInteger(0);
        
//        listener = new StreamListener(this);
        Listener listener = new Listener();
        twitterStream.addListener(listener);
        
        // query filter
         FilterQuery tweetFilterQuery = new FilterQuery();
         tweetFilterQuery.track(new String[]{":)", ":("}); // OR on keywords
        
        // note that not all tweets have location metadata set.
        twitterStream.filter(tweetFilterQuery);
//        twitterStream.sample();
        
	}
	
	private static int index = 0;
	
	private void readKeysFromFile() {
		try (Stream<String> lines = Files.lines(Paths.get(relativeFilePath_apiKeys), Charset.defaultCharset())) {
			//System.out.println(lines.count());
			lines.forEachOrdered(
				line -> {
					apiKeys[index] = line;
					index++;
				}
			);
		} catch (IOException e) {
			System.out.println("Error reading api keys!");
			e.printStackTrace();
		}
	}

}
