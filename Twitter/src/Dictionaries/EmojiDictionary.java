package Dictionaries;

import java.io.IOException;

import AbstractWordClasses.AbsMeasurableWord;
import Words.Emoji;

public class EmojiDictionary extends AbsDictionary {
	
	public EmojiDictionary(String relativeFilePath) throws IOException {
		super();
		buildHashtable(relativeFilePath, "emojis", 1, 1000);
		checkIntegrity();
	}
	
	private void checkIntegrity() {
		// does not contain space or -
		String strangeChars = "'�!\"#$%&/()=?*Њ�Ǝ����:;_~�^���`�����+*\"<>-�����\\…��";
		super.checkIntegrity(strangeChars);
	}
	
	@Override
	public void processLine(String line) {
		// data structure: codepoint,description,sentiment
		String[] tokens = line.split(",");
		hashTable.put(tokens[0], new Emoji(tokens[0], tokens[1], tokens[2]));
	}

	public AbsMeasurableWord getEntry(String key) {
		return (AbsMeasurableWord) hashTable.get(key);
	}
}
