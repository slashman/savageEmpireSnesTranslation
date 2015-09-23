package net.slashie.translations.sfc.savageEmpire;

import java.util.HashMap;
import java.util.Map;

public class Bits {
	public final static Map<String, String> KNOWN_BITS = new HashMap<String, String>();
	static {
		String[] data = new String[]{
			"07", "KURAK_SOMETHING",
			"0B", "YOLARU_SOMETHING",
			"11", "BARRAB_SOMETHING",
			"15", "DISKIKI_SOMETHING",
			
			"81", "ASKED_DOKREI_ABOUT_UNION",
			
			"86", "KNOWS_KURAK_NEED",
			"87", "AIERA_SAVED_KURAK_JOINED_UNION",
			
			"8A", "KNOWS_YOLARU_NEED",
			"8B", "YOLARU_JOINED_UNION",
			
			"90", "KNOWS_BARRAB_NEED",
			"91", "BARRAB_JOINED_UNION",
			
			"94", "KNOWS_DISKIKI_NEED",
			"95", "DISKIKI_JOINED_UNION",
			
			"9A", "KNOWS_DOKREY",
			
			"9E", "KNOWS_FRITZ",
			"9F", "FRITZ_TALKED_ABOUT_BRAIN",
			"A0", "FRITZ_ALREADY_TOLD_LONG_STORY",
			
			"AA", "KNOWS_INARA",
			"AB", "INARA_UNUSED?",
			"AC", "INARA_EXPLAINED_ALREADY_WHY_PINDIRO_JOINS_UNION",
			
			"AD", "INTANYA_CONVERSATION_RESET",
			
			"C5", "KNOWS_MOSAGAN"
		};
		for (int i = 0; i < data.length; i+=2){
			KNOWN_BITS.put(data[i], data[i+1]);
		}
	}
}
