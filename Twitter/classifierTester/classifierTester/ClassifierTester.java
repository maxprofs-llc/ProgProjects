package classifierTester;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Stream;

import Tokenizer.Classifier;
import Tokenizer.Tweet;


public class ClassifierTester {

	static String datasetPath;
	
	static final double learningPercentage = 0.66;
	
	static ArrayList<ProcessedTweet> annotatedTweets = new ArrayList<ProcessedTweet>(130000);
	static ArrayList<ProcessedTweet> learningdataset = new ArrayList<ProcessedTweet>((int)(annotatedTweets.size()*learningPercentage*1.2));
	static ArrayList<ProcessedTweet> testingdataset = new ArrayList<ProcessedTweet>((int)(annotatedTweets.size()*(1-learningPercentage)*1.2));
	
	public static final Random r = new Random();
	
	public static void main(String[] args) {
		
		// for when testing 2-way classification
		// on first dataset (datasets/englishtweetsonly.txt or something like that)
		// when doing this comment readAndCreateDatasets(); in 2way() method
		// keep the line that sets the datasetpath beucase i was lazy when writing
		// this class - TODO fix that
//		special();

//		threeWay();
		
//		twoWay();
		
//		testDataset();
		
		
		
		
//		testAgain();
		
		
		datasetPath = //"datasets/processedDatasetEnglishTweetsOnly.txt";
				"output/tweets.txt";
		readAndCreateDatasets();
		
		DataVisualizer vis = new DataVisualizer(annotatedTweets);
	}

	public static void testDataset() {
		System.out.println("\n--- TESTING PHASE ---");
		System.out.println("\t|-> Size of testing dataset: " + testingdataset.size());
		System.out.print("\t|-> Testing with thresholds: ("+Tweet.positiveThreshold+", "+Tweet.threshold+", "+Tweet.negativeThreshold+"))...");
		
		// get sentiment and see if it matches
		int correctlyClassified = 0;
		double accuracy;
		for (ProcessedTweet t : testingdataset) {
			int sentiment = (datasetPath.contains("Only"))? t.tweet.getSentimentThreeWay() : t.tweet.getSentimentTwoWay();
			if (sentiment == t.annotatedSentiment) correctlyClassified++;
		}
		
		accuracy = (double) correctlyClassified / testingdataset.size();
		
		System.out.println(" done.");
		
		System.out.println("\t|-> Fraction of correctly classified: " + correctlyClassified + "/" +testingdataset.size());
		System.out.println("\t|-> Accuracy -> " + Result.format.format(accuracy));
		
		// guessing
//		correctlyClassified = 0;
//		for (ProcessedTweet t : testingdataset) {
//			if (t.)
//		}
//		
//		int p = 0, n = 0, neg = 0;
//		for (ProcessedTweet t : annotatedTweets) 
//			if (t.annotatedSentiment == 1) 
//				p++; 
//			else if (t.annotatedSentiment == 0) n++;
//			else neg++;
//		
//		System.out.println("poz, neu, neg -> " + p + ", " + n + ", " + neg);
	}
	
	private static void twoWay() {
		datasetPath = "output/tweets.txt";
//		readAndCreateDatasets();
		
		System.out.println("\n--- LEARNING PHASE ---");
		
		int correctlyClassified = 0;
		double increaseValue = 0.05;
		ArrayList<Result> results = new ArrayList<Result>(1000);
		
		// learning
		System.out.println("\t|-> Defnining threshold... ");
		int output = 0;
		for (Tweet.threshold=-3; Tweet.threshold<3; Tweet.threshold+=increaseValue) {
			for (int i=0; i<learningdataset.size(); i++) {
				ProcessedTweet tweet = learningdataset.get(i);
				int sentiment = tweet.tweet.getSentimentTwoWay();
				if (tweet.annotatedSentiment == sentiment) {
					correctlyClassified++;
				}
			}
			double accuracy = (double) correctlyClassified / (double) learningdataset.size();
			
			if (output % 10 == 0) {
				System.out.println("\t|\t|-> (threshold, accuracy, fraction) -> (" + Result.format.format(Tweet.threshold) + ", "
						+ Result.format.format(accuracy) + ", " + correctlyClassified + "/" + learningdataset.size() +")");
			}
			results.add(new Result(accuracy, correctlyClassified, Tweet.threshold, -99));
			correctlyClassified = 0;
			output++;
		}
		System.out.println("\t|");
		
		// sorting
		System.out.println("\t|-> Numer of results: " + results.size());
		System.out.print("\t|-> Sorting list of results... ");
		results.sort((r1, r2) -> -Double.compare(r1.accuracy, r2.accuracy));
		System.out.println("done.");
		
		System.out.println("\t|-> Top 5 best performing thresholds:");
		for (int i=0; i<5; i++) System.out.println("\t|\t|-> " + results.get(i).toString());
		System.out.println("\t|");
		
		System.out.println("\t|-> Top 5 worst performing thresholds:");
		for (int i=results.size()-1; i>=(results.size()-5); i--) System.out.println("\t|\t|-> " + results.get(i).toString());
		
		// set the treshold to the best value (is saved at positivelimit)
		Tweet.threshold = results.get(0).positivelimit;
		Tweet.positiveThreshold = -99;
		Tweet.negativeThreshold = -99;

		System.out.println("\t|");
		System.out.println("\t|-> Best threshold -> " + Result.format.format(Tweet.threshold));
	}
	
	private static void threeWay() {
		// read entire dataset
		datasetPath = "datasets/processedDatasetEnglishTweetsOnly.txt";
		readAndCreateDatasets();
		
		System.out.println("\n--- LEARNING PHASE ---");
		
		int correctlyClassified = 0;
		double increaseValue = 0.05;
		Tweet.positiveThreshold = -3;
		Tweet.negativeThreshold = -3;
		ArrayList<Result> results = new ArrayList<Result>(1000);

		// learning
		System.out.println("\t|-> Defnining thresholds... ");
		int output = 0;
		for ( ; Tweet.positiveThreshold < 3; Tweet.positiveThreshold+=increaseValue) {
			for (Tweet.negativeThreshold=-3 ; Tweet.negativeThreshold < 3; Tweet.negativeThreshold+=increaseValue) {
				
				for (int i=0; i<learningdataset.size(); i++) {
					ProcessedTweet tweet = learningdataset.get(i);
					int sentiment = tweet.tweet.getSentimentThreeWay();
					if (tweet.annotatedSentiment == sentiment) {
						correctlyClassified++;
					}
				}
				double accuracy = (double) correctlyClassified / (double) learningdataset.size();
				
				if (output % 100 == 0) {
					System.out.println("\t|\t|-> ((pos, neg), accuracy, fraction) -> ((" + Result.format.format(Tweet.positiveThreshold) + ", "
							+ Result.format.format(Tweet.negativeThreshold) + "), " + Result.format.format(accuracy) + ", "
							+ correctlyClassified + "/" + learningdataset.size() +")");
				}
				
				results.add(new Result(accuracy, correctlyClassified, Tweet.positiveThreshold, Tweet.negativeThreshold));
				correctlyClassified = 0;
				output++;
			}
		}
		System.out.println("\t|");
		
		// sorting
		System.out.println("\t|-> Numer of results: " + results.size());
		System.out.print("\t|-> Sorting list of results... ");
		results.sort((r1, r2) -> -Double.compare(r1.accuracy, r2.accuracy));
		System.out.println("done.");
		
		System.out.println("\t|-> Top 5 best performing thresholds:");
		for (int i=0; i<5; i++) System.out.println("\t|\t|-> " + results.get(i).toString());
		System.out.println("\t|");
		
		System.out.println("\t|-> Top 5 worst performing thresholds:");
		for (int i=results.size()-1; i>=(results.size()-5); i--) System.out.println("\t|\t|-> " + results.get(i).toString());

		// set the tresholds to the best values
		Tweet.positiveThreshold = results.get(0).positivelimit;
		Tweet.negativeThreshold = results.get(0).negativelimit;
		Tweet.threshold = -99;
		
		System.out.println("\t|");
		System.out.println("\t|-> Best thresholds: (pos, neg) -> (" + Result.format.format(Tweet.positiveThreshold)
			+ ", " + Result.format.format(Tweet.negativeThreshold) + ")");
	}

	public static void readAndCreateDatasets() {
		System.out.println("--- DATASET READING PROCESS ---");
		System.out.print("\t|-> Reading and processing dataset... ");
		try (Stream<String> lines = Files.lines(Paths.get(datasetPath), Charset.defaultCharset())) {
			lines.forEachOrdered(line -> {
				// za file processedDatasetEnglishTweetsOnly
				// sentiment,tweetID,tweetText
				
				// za file tweets.txt
				// sentiment,tweetText
				String[] tokens = line.split(",");
				String sentiment = tokens[0];
				String tweet = "";
				
				if (sentiment.equals("-1"))  return;
				
				int ind = (datasetPath.contains("processedDatasetEnglishTweetsOnly"))? 2 : 1;
				for (int i=ind; i<tokens.length; i++) tweet += tokens[i];
				
				Tweet tw = new Tweet(tweet, null);
				tw.processTweet();
				
				annotatedTweets.add(new ProcessedTweet(tw, Integer.parseInt(sentiment)));
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(" done.");
		System.out.println("\t|-> Original dataset size: " + annotatedTweets.size());
		
		// create learning dataset
		System.out.print("\t|-> Creating learning and testing dataset... ");
		
		annotatedTweets.forEach(t -> {
			if (r.nextFloat() <= learningPercentage) 
				learningdataset.add(t);
			else
				testingdataset.add(t);
		});
		System.out.println(" done.");
		
		System.out.println("\t|-> Learning dataset size: " + learningdataset.size());
		System.out.println("\t|-> Testing  dataset size: " + testingdataset.size());
		
		int x = testingdataset.size() + learningdataset.size();
		int y = annotatedTweets.size();
		if (x != y) throw new Error("ERROR: Something went wrong when creating datasets: " + x + " != " + y + "\n");
	}
	
	// copy of method read and create datasets
	public static void special() {
		System.out.println("--- DATASET READING PROCESS ---");
		System.out.print("\t|-> Reading and processing dataset... ");
		datasetPath = "datasets/processedDatasetEnglishTweetsOnly.txt";
		try (Stream<String> lines = Files.lines(Paths.get(datasetPath), Charset.defaultCharset())) {
			lines.forEachOrdered(line -> {
				// za file processedDatasetEnglishTweetsOnly
				// sentiment,tweetID,tweetText
				
				// za file tweets.txt
				// sentiment,tweetText
				String[] tokens = line.split(",");
				String sentiment = tokens[0];
				String tweet = "";
				
				if (sentiment.equals("0")) return; // throw neutral tweets out
				
				int ind = (datasetPath.contains("processedDatasetEnglishTweetsOnly"))? 2 : 1;
				for (int i=ind; i<tokens.length; i++) tweet += tokens[i];
				
				Tweet tw = new Tweet(tweet, null);
				tw.processTweet();
				
				annotatedTweets.add(new ProcessedTweet(tw, Integer.parseInt(sentiment)));
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(" done.");
		System.out.println("\t|-> Original dataset size: " + annotatedTweets.size());
		
		// create learning dataset
		System.out.print("\t|-> Creating learning and testing dataset... ");
		
		annotatedTweets.forEach(t -> {
			if (r.nextFloat() <= learningPercentage) 
				learningdataset.add(t);
			else
				testingdataset.add(t);
		});
		System.out.println(" done.");
		
		System.out.println("\t|-> Learning dataset size: " + learningdataset.size());
		System.out.println("\t|-> Testing  dataset size: " + testingdataset.size());
		
		int x = testingdataset.size() + learningdataset.size();
		int y = annotatedTweets.size();
		if (x != y) throw new Error("ERROR: Something went wrong when creating datasets: " + x + " != " + y + "\n");
		
		for (ProcessedTweet t : annotatedTweets) if (t.annotatedSentiment == 0)
			throw new Error("ERROR: Something went wrong when creating datasets: neutral tweet found");
	}
}

class Result {
	double accuracy;
	int correctlyClassified;
	
	double positivelimit, negativelimit;
	
	static DecimalFormat format = new DecimalFormat("#.####");
	
	public Result(double a, int cc, double posth, double negth) {
		accuracy = a; correctlyClassified = cc;
		negativelimit = negth; positivelimit = posth;
	}
	
	public String toString() {
		return "(pos, neg) -> (" + format.format(positivelimit) + ", " + format.format(negativelimit) + "). Accuracy: " + format.format(accuracy);
	}
}

class ProcessedTweet {
	Tweet tweet;
	int annotatedSentiment;
	
	public ProcessedTweet(Tweet tweet, int annotatedSentiment) {
		this.tweet = tweet;
		this.annotatedSentiment = annotatedSentiment;
	}
}

//class TweetInstance {
//	
//	int sentiment;
//	String text;
//	
//	public TweetInstance(String text, String sentiment) {
//		this.text = text;
//		this.sentiment = Integer.parseInt(sentiment);
//	}
//}
