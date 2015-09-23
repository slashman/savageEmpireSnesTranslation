package net.slashie.translations.sfc.savageEmpire;

import java.util.HashMap;
import java.util.Map;

public class InjectedComments {
	public final static Map<String, String> COMMENTS = new HashMap<String, String>();
	static {
		String[] data = new String[]{
		"1580E0", "!E2 && E3",
		"15812A", "!E2 && 63",
		"158183", "E2 && B1",
		
		// Dialog 9
		"15D75A", "9E",
		"15D791", "!9E",
		"15DAA0", "A0",
		"15DB65", "9F",
		"15DBE3", "!9F",
		// Dialog 15
		"16076A", "81",
		"160779", "AA && AC",
		"1607A0", "AA && !AC",
		"1607C6", "!AA",
		"160B27", "AC",
		"160B6C", "!AC",
		"160C59", "AC",
		"160C8F", "!AC",
		
		// Dialog 21
		"163CF9", "C5",
		"163D22", "!C5",
		};
		for (int i = 0; i < data.length; i+=2){
			COMMENTS.put(data[i], data[i+1]);
		}
	}
	
}
