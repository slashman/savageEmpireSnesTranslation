package net.slashie.translations.sfc.savageEmpire;

import java.util.HashMap;
import java.util.Map;

public class Dialogs {
	public final static Map<String, String> DIALOGS = new HashMap<String, String>();
	static {
		String[] data = new String[]{
			"1", "Eodon Exploration Record by Jimmy",
			"2", "Victory",
			"4", "Aroron. Chief of the Kurak",
			"8", "Dokurei (Dokrey)",
			"9", "Fritz von Hun Traben (He probably sells bullets for emeralds?)",
			"15", "Inara, Chief of the Pindiro (Done)",
			"16", "Intanya from the Kurak (Done)",
			"21", "Mosagan from the Yolaru (Done)",
			"22", "Hakuru? (Pretty confusing this one)",
			"24", "Pakusakutamaku"
		};
		for (int i = 0; i < data.length; i+=2){
			DIALOGS.put(data[i], data[i+1]);
		}
	}
}
