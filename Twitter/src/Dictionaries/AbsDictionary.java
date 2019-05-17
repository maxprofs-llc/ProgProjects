package Dictionaries;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.stream.Stream;

import Words.IWord;

abstract class AbsDictionary implements IDictionary {
	
	Hashtable<String, INode> hashTable;

	public AbsDictionary() {
	
	}
	
	protected void buildHashtable(String relativeFilePath, String dictionaryName, int skipToLine, int hashtableSize) throws IOException {
		System.out.println("Creating dictionary of " + dictionaryName + "...");
		System.out.println("\t|-> Creating hashtable...");
		
		// hashtable, key -> word, value -> node(word and statistics)
		hashTable = new Hashtable<String, INode>(hashtableSize);
		
		Stream<String> linesToRead;
		try (Stream<String> lines = Files.lines(Paths.get(relativeFilePath), Charset.defaultCharset())) {
			linesToRead = lines.skip(skipToLine);
			linesToRead.forEachOrdered(
				line -> processLine(line)
			);
		}
		
		System.out.println("\t|-> Done.");
		System.out.println("\t|-> Number of entries: " + hashTable.size());
	}
	
	public void processLine(String line) {
		// each dictionary that extends this
		// abstract class must have this 
		// function overwritten, is used by
		// the buildHashtable function
	}
	
	protected void checkIntegrity() {
		System.out.println("\t|-> Checking integrity...");
		hashTable.forEach((word, node) -> {
			if (!node.checkIntegrity()) {
				System.out.println("\t|\t|-> Maybe a bad node: " + node.toString());
			}
		});
		System.out.println("\t\\-> Done.");
		System.out.println();
	}

	@Override
	public boolean contains(String key) {
		return hashTable.containsKey(key);
	}

	@Override
	public IWord getEntry(String key) {
		return (IWord) hashTable.get(key);
	}

}