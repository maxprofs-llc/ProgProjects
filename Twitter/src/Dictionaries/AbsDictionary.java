package Dictionaries;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;

import AbstractWordClasses.AbsWord;

abstract class AbsDictionary implements IDictionary {
	
	HashMap<String, AbsWord> hashTable;
	
	public AbsDictionary() { }
	
	protected void buildHashtable(String relativeFilePath, String dictionaryName, int skipToLine, int hashtableSize) throws IOException {
		System.out.println("Creating dictionary of " + dictionaryName + "...");
		System.out.println("\t|-> Creating hashtable...");
		
		// hashtable:
		// key		-> string,
		// value	-> node(word and statistics)
		hashTable = new HashMap<String, AbsWord>(hashtableSize);
		
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
	
	public abstract void processLine(String line);

	protected void checkIntegrity(String strangeChars) {
		System.out.println("\t|-> Checking integrity...");
		hashTable.forEach((word, node) -> {
			if (!node.checkIntegrity()) {
				System.out.println("\t|\t|-> Maybe a bad node: " + node.toString());
			}
			String s = node.getSourceText();
			char[] chars = s.toCharArray();
			
			loop:
			for (int i=0; i<strangeChars.length(); i++) {
				for (int j=0; j<chars.length; j++) {
					if (chars[j] == strangeChars.charAt(i)) {
						System.out.println("\t|\t|-> Maybe a bad node (suspect char: '"+ chars[j]+"'): "+ node.toString());
						break loop;
					}
				}
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
	public AbsWord getEntry(String key) {
		return hashTable.get(key);
	}
	
	public HashMap<String, AbsWord> getHashmap() {
		return hashTable;
	}

}
