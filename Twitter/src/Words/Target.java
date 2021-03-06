package Words;

import AbstractWordClasses.AbsWord;

public class Target extends AbsWord {

	public Target(String source) {
		super(source, null);
		super.processedText = source.substring(1);
		super.tag = "TRG";
	}
	
	public static boolean isType(String s) {
		if (s.charAt(0) == '@') return true;
		return false;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}
}
