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
	
	static final double learningPercentage = 0.5;
	
	static ArrayList<ProcessedTweet> annotatedTweets = new ArrayList<ProcessedTweet>(130000);
	static ArrayList<ProcessedTweet> learningdataset = new ArrayList<ProcessedTweet>((int)(annotatedTweets.size()*learningPercentage*1.2));
	static ArrayList<ProcessedTweet> testingdataset = new ArrayList<ProcessedTweet>((int)(annotatedTweets.size()*(1-learningPercentage)*1.2));
	
	public static final Random r = new Random();
	
	public static void main(String[] args) {
		
		datasetPath = 
//				"datasets/processedDatasetEnglishTweetsOnly.txt";
//				"output/tweets.txt";
				"datasets/manually_GO_et_al_testSet.txt";

		
//		special();
		
		readAndCreateDatasets();

		threeWayLearning();
		
//		twoWayLearning();
		
		testDataset(false);
		
//		testFmeasure(false);
		
		
//		testAgain();
		
		

//		readAndCreateDatasets();
		
//		DataVisualizer vis = new DataVisualizer(annotatedTweets);
	}
	
	public static void testFmeasure(boolean twoway) {
		System.out.println("\n--- F1 measure ---");
		int nnegative = 0, npositive = 0, nneutral = 0;
		for (ProcessedTweet t : testingdataset) {
			if (t.annotatedSentiment == 1) npositive++;
			else if (t.annotatedSentiment == 0) nneutral++;
			else nnegative++;
		}
		System.out.println("\t|-> Number of positive tweets: " + npositive);
		System.out.println("\t|-> Number of neutral tweets: " + nneutral);
		System.out.println("\t|-> Number of negative tweets: " + nnegative);
		
		if (twoway) {
			double TP = 0, TN = 0;
			double FP = 0, FN = 0;
			
			for (ProcessedTweet t : testingdataset)
			{
				// True Positives (TP)
				// False Positives (FP)
				if (t.annotatedSentiment == 1) {
					if (t.annotatedSentiment == t.tweet.getSentimentTwoWay()) TP++;
					else FP++;
				}
				
				// True Negatives (TN)
				// False Negatives (FN) 
				else {
					if (t.annotatedSentiment == t.tweet.getSentimentTwoWay()) TN++;
					else FN++;
				}
			}
			
			// accuracy
			double accuracy = (TP+TN) / (TP+FP+FN+TN);
			double precision = TP / (TP+FP);
			double recall = TP / (TP+FN);
			
			double f1 = 2*(recall * precision) / (recall + precision);
			
			DecimalFormat format = new DecimalFormat("#.####");
			
			System.out.println("\t|");
			System.out.println("\t|-> Accuracy: " + format.format(accuracy));
			System.out.println("\t|-> Precision: " + format.format(precision));
			System.out.println("\t|-> Recall: " + format.format(recall));
			System.out.println("\t|-> F1-measure: " + format.format(f1));
		} else {
			// 3way
			System.out.println("\t|-> Not implemented for 3-way");
		}
	}

	public static void testDataset(boolean twoway) {
		System.out.println("\n--- TESTING PHASE ---");
		System.out.println("\t|-> Size of testing dataset: " + testingdataset.size());
		
		if (twoway)
			System.out.print("\t|-> Testing with threshold: (" + Result.format.format(Tweet.threshold) + "))...");
		else
			System.out.print("\t|-> Testing with thresholds: ("+
					Result.format.format(Tweet.positiveThreshold)+", "+Result.format.format(Tweet.threshold)+", "+
					Result.format.format(Tweet.negativeThreshold)+"))...");
				
		// get sentiment and see if it matches
		int correctlyClassified = 0;
		double accuracy;
		for (ProcessedTweet t : testingdataset) {
			int sentiment = (twoway)? t.tweet.getSentimentTwoWay() :t.tweet.getSentimentThreeWay();
			if (sentiment == t.annotatedSentiment) correctlyClassified++;
			else {
//				System.out.println("\nmine, theirs, tweet:");
//				System.out.println(sentiment + ", " + t.annotatedSentiment + ", " + t.tweet.getCleanSource());
			}
		}
		
		accuracy = (double) correctlyClassified / testingdataset.size();
		
		System.out.println(" done.");
		
		System.out.println("\t|-> Fraction of correctly classified: " + correctlyClassified + "/" +testingdataset.size());
		System.out.println("\t|-> Accuracy -> " + Result.format.format(accuracy));
	}
	
	private static void twoWayLearning() {
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
	
	private static void threeWayLearning() {
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
				
				//za file go_et_all_test
				// sentiment,tweet
				String[] tokens = line.split(",");
				String sentiment = tokens[0];
				String tweet = "";
				
//				if (sentiment.equals("0"))  return;
				
				int ind = (datasetPath.contains("processedDatasetEnglishTweetsOnly"))? 2 : 1;
				for (int i=ind; i<tokens.length; i++) tweet += ", " + tokens[i];
				
				// might want to remove these due to bias
//				tweet.replace(":)", "");
//				tweet.replace(":(", "");
				
				//System.out.println("tweet, senti: " + sentiment + " - " + tweet);
				
				Tweet tw = new Tweet(tweet, null);
				tw.processTweet();
				
//				if (new Random().nextDouble() < 0.1) {
//					System.out.println("\nsrc -> " + tweet);
//					System.out.println(tw.toString() + "\n");
//				}
				
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
