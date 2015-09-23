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
		
		// Dialog 4
		"159A3F", "85 && 87",
		"159A84", "85 && !87 && 86",
		"159AE3", "85 && !87 && !86",
		"159B11", "!85",
		"159B94", "87",
		"159BF5", "!87",
		"159CAE", "87",
		"159CE7", "!87",
		"159E25", "87",
		"159E6A", "!87",
		"15A2D9", "81",
		"15A337", "87",
		"15A370", "!87 && 86",
		"15A395", "!87 && !86",
		"15A3D5", "87",
		"15A3A5", "NOT PROMPT",
		"15A3CF", "YES PROMPT",
		
		// Dialog 5
		"15A74A", "88 && 8B",
		"15A789", "88 && !8B && 89",
		"15A7DE", "88 && !8B && !89",
		"15A805", "!88",
		"15AA0E", "8B",
		"15AA67", "!8B && 8A",
		"15AAD6", "!8B && !8A",
		"15AC9E", "8B",
		
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
		
		// Dialog 16
		"160D34", "AD",
		"160D4D", "!AD",
		"160F68", "87",
		"160FBB", "!87",
		
		// Dialog 21
		"163CF9", "C5",
		"163D22", "!C5",
		};
		for (int i = 0; i < data.length; i+=2){
			COMMENTS.put(data[i], data[i+1]);
		}
	}
	
}
