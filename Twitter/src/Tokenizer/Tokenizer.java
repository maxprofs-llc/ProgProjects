package Tokenizer;
import java.math.BigDecimal;
import java.util.ArrayList;

import Dictionaries.DictionaryCollection;
import Words.AffectionWord;
import Words.Hashtag;
import Words.IWord;
import Words.Other;
import Words.Smiley;
import Words.StopWord;
import Words.Target;
import Words.URL;

public class Tokenizer {
	
	ArrayList<IWord> nodes;
	String sourceText;
	
	DictionaryCollection dictionary;
	
	int numOfNegativeWords = 0;
	int numOfNeutralWords = 0;
	int numOfPositiveWords = 0;
	
	int sumOfNegativeWords = 0;
	int sumOfNeutralWords = 0;
	int sumOfPositiveWords = 0;
	
	public Tokenizer(String source, DictionaryCollection dictionary) {
		sourceText = source;
		this.dictionary = dictionary;
		tokenizeToTree();
	}
	
	private void tokenizeToTree() {
		// init size of tree
		String[] tokens = sourceText.split(" ");
		nodes = new ArrayList<IWord>(tokens.length);
		
		// classify words
		for (String token : tokens) {
			if (token.length() == 0) continue;
			
			// preprocessing ????
			token = token.toLowerCase();
			token = token.replace(",", "");
			token = token.replace("/", "");
			token = token.replace("\\", "");
			token = token.replace("\t", "");
			
			// hashtags
			if (Hashtag.isType(token)) {
				nodes.add(new Hashtag(token, dictionary.whissell));
			}
			
			// smileys
			else if (dictionary.smileysContains(token)) {
				IWord word = dictionary.smileysGetEntry(token);
				nodes.add(new Smiley(
					word.getSourceWord(),
					word.getPleasantness()
				));
			}
			
			// @ tags
			else if (Target.isType(token)) {
				nodes.add(new Target(token, dictionary.whissell));
			}
			
			// urls
			else if (URL.isType(token)) {
				nodes.add(new URL(token));
			}
			
			// whissell words
			else if (dictionary.whissellContains(token)) {
				IWord word = dictionary.whissellGetEntry(token);
				nodes.add(new AffectionWord(
					word.getSourceWord(),
					word.getPleasantness(),
					word.getActivation(),
					word.getImagery()
				));
			}
			
			// stop words
			else if (dictionary.stopwordsContains(token)) {
				IWord word = dictionary.stopwordsGetEntry(token);
				nodes.add(new StopWord(
					word.getSourceWord()
				));
			}
			
			// other
			else {
				nodes.add(new Other(token));
			}
		}
	}
	
	public void classify() {
		// class ranges (according to pleasantness)
		// <AFT>:
		//		[1, 1.75]	-> negative
		//		(1.75, 2.25)-> neutral
		//		[2, 3]		-> positive
		
		// <SML>:	
		//		[-1, 0)		-> negative
		//		0			-> neutral
		//		(0, 1]		-> positive
		
		BigDecimal value;
		
		BigDecimal _175 = new BigDecimal(1.76);
		BigDecimal _2 = new BigDecimal(2);
		
		for (IWord w : nodes) {
			double pleasantness = w.getPleasantness();
			value = new BigDecimal(pleasantness);
			
			if (w instanceof AffectionWord) {
				if (value.compareTo(_175) == -1) {
//					System.out.println("-> neg: " + w.getSourceWord());
					numOfNegativeWords++;
					sumOfNegativeWords += pleasantness;
				} else if (value.compareTo(_2) == -1) {
//					System.out.println("-> neu: " + w.getSourceWord());
					numOfNeutralWords++;
					sumOfNeutralWords += pleasantness;
				} else {
//					System.out.println("-> pos: " + w.getSourceWord());
					numOfPositiveWords++;
					sumOfPositiveWords += pleasantness;
				}
			}
			
			else if (w instanceof Smiley) {
				 if (pleasantness == 0){
//					System.out.println("-> neu: " + w.getSourceWord());
					numOfNeutralWords++;
					sumOfNeutralWords += pleasantness;
				}
				 else if (pleasantness < 0) {
//					System.out.println("-> neg: " + w.getSourceWord());
					numOfNegativeWords++;
					sumOfNegativeWords += pleasantness;
				} else if (pleasantness > 0) {
//					System.out.println("-> pos: " + w.getSourceWord());
					numOfPositiveWords++;
					sumOfPositiveWords += pleasantness;
				}
			}
			
			else if (w instanceof Other){
				numOfNeutralWords++;
			}
 		}
	}
	
	
	public void print() {
		System.out.println("Tree of tweet:");
		System.out.println("\t|-> Source text: '" + sourceText + "'");
		System.out.println("\t|-> Tokens:");
		
		for (IWord w : nodes) {
			System.out.println("\t|\t|-> " + w.toString());
		}
		
		System.out.println("\t|-> Stats:");
		System.out.println("\t\t|-> Num of neg words: " + numOfNegativeWords);
		System.out.println("\t\t|-> Num of neu words: " + numOfNeutralWords);
		System.out.println("\t\t|-> Num of pos words: " + numOfPositiveWords);
		System.out.println("\t\t|-> Sum of neg words: " + sumOfNegativeWords);
		System.out.println("\t\t|-> Sum of neu words: " + sumOfNeutralWords);
		System.out.println("\t\t|-> Sum of pos words: " + sumOfPositiveWords);
	}
}