package net.slashie.translations.sfc.savageEmpire;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.GoogleAPI;
import com.google.api.GoogleAPIException;
import com.google.api.translate.Language;
import com.google.api.translate.Translate;

public class ScriptExtractor {
	private static final boolean TRANSLATE = true;
	private final static int CONVERSATION_ID = 2; // 0 to 35 
	enum Mode { DISASSEMBLE, EXTRACT_SCRIPT};
	private static final Mode MODE = Mode.DISASSEMBLE;
	
	private static final int BANK_SIZE = 0x8000;
	private static final int CHARSET_TABLE_OFFSET = 0x118000;
	
	//private static final int DIALOG_OPTIONS = 0x12CF4;
	//private static final int DIALOG_OPTIONS = 0x14BAF;
	private static final int DIALOG_OPTIONS = 0x147C6;
	
	private final static Map<String, String> KNOWN_FLAGS = new HashMap<String, String>();
	static {
		KNOWN_FLAGS.put("10A9", "INTANYA_INTRO_DONE");
	}
	
	private final static Map<String, String> PORTRAITS = new HashMap<String, String>();
	static {
		PORTRAITS.put("F8", "RAFKIN");
	}
	
	private final static List<String> DIALOG_NAMES = new ArrayList<String>();
	static{
		DIALOG_NAMES.add("Eodon Exploration Record by Jimmy");
		DIALOG_NAMES.add("Victory");
		DIALOG_NAMES.add("Aroron. Chief of the Kurak");
		DIALOG_NAMES.add("?");
		DIALOG_NAMES.add("?");
		DIALOG_NAMES.add("?");
		DIALOG_NAMES.add("?");
		DIALOG_NAMES.add("Dokurei (Dokrey)");
		DIALOG_NAMES.add("Fritz von Hun Traben (He probably sells bullets for emeralds?)");
		DIALOG_NAMES.add("?");
		DIALOG_NAMES.add("?");
		DIALOG_NAMES.add("?");
		DIALOG_NAMES.add("?");
		DIALOG_NAMES.add("?");
		DIALOG_NAMES.add("OK - Inara. Pindiro");

	}

	public static void main(String[] args) throws IOException, GoogleAPIException {
		new ScriptExtractor().readFile("savage.smc");
	}

	private RandomAccessFile romfile;
	private List<byte[]> dialogOptions;
	private List<Character> charsetList;
	
	private byte currentDialogScriptBank;
	private List<String> currentDialogOptions;
	private long currentWaitForInput;

	private void readFile(String filename) throws IOException, GoogleAPIException {
		boolean ALL_JAPANESE = false;
		this.romfile = new RandomAccessFile(filename, "r");
		// Load the 125 conversations
		// For each conversation, load its charset, then go to the dialog code area and extract the used characters
		byte[] charsetPointer = new byte[3];
		byte[] fontOffset = new byte[2];
		byte[] dialogScriptAddress = new byte[2];
		
		//outpl("-- DIALOG OPTIONS");
		romfile.seek(DIALOG_OPTIONS);
		this.dialogOptions = new ArrayList<byte[]>();
		for (int i = 0; i < 200; i++){
			byte codeByte = romfile.readByte();
			int numberOfBytes = unsign(codeByte);
			byte[] dialogOption = new byte[numberOfBytes];
			romfile.read(dialogOption);
			dialogOptions.add(dialogOption);
			/*outp(i+": ");
			outp(dialogOption);
			outpl("");*/
		}
		
		//for (int i = 0; i < 125; i++){
		nextDialog: for (int i = CONVERSATION_ID; i < CONVERSATION_ID+1; i++){
			currentWaitForInput = 0;
			currentDialogScriptBank = 0;
			currentDialogOptions = null;
			outpl_both("-- DIALOG "+(i+1));
			if (i < DIALOG_NAMES.size())
				outpl_both("-- "+DIALOG_NAMES.get(i));
			romfile.seek(CHARSET_TABLE_OFFSET+4*i);
			romfile.read(charsetPointer);
			long charsetOffset = calculatePointer(charsetPointer);
			romfile.seek(charsetOffset);
			outp("-- CHARSET");
			romfile.read(fontOffset);
			charsetList = new ArrayList<Character>();
			// Read offset until FF FF is found (Should verify if that's a correct termination sequence)
			boolean substract20h = false;
			while (!(fontOffset[0] == -1 && fontOffset[1] == -1)){
				long fonttableOffset = calculateOffset(fontOffset[0], fontOffset[1]);
				long fonttableIndex = fonttableOffset / 3;
				char fontTableCharacter = getCharacterByIndex(fonttableIndex);
				if (fontTableCharacter == '誰'){ //TODO: Find out why this happens
					/*
					 * In your EUC charset for Dialog 16, the values from “誰” onward seem to be set to 
					 * 20h bytes higher than they should be. So for example, where you have “ぐかが”, 
					 * it should say “誰かが”. It ended up making the second half of the Japanese text look scrambled.
					 */
					// substract20h = true;
				}
				if (substract20h){
					fontTableCharacter = getCharacterByIndex(fonttableIndex - 0x20);
				}
				outp(fontTableCharacter+"");
				charsetList.add(fontTableCharacter);
				romfile.read(fontOffset);
			}
			outpl("");
			
			/*
			outpl("DIALOG_OPTIONS");
			for (int k = 0; k < dialogOptions.size(); k++){
				byte[] dialogOption = dialogOptions.get(k);
				outp(k+": ");
				printJapanese(dialogOption);
				outpl("");
			} */
			
			romfile.seek(0x0C0000+2*i);
			romfile.read(dialogScriptAddress);
			romfile.seek(0x0C00FA+1*i);
			currentDialogScriptBank = romfile.readByte();
			long dialogCodeOffset = calculatePointer(new byte[]{dialogScriptAddress[0], dialogScriptAddress[1],currentDialogScriptBank});
			romfile.seek(dialogCodeOffset);
			boolean textMode = false;
			String japaneseBuffer = "";
			int previousCommand = 0;
			boolean loadedDialog = false;
			for (int j = 0; j < 4000; j++){
				byte codeByte = romfile.readByte();
				int command = unsign(codeByte);
				if (command != (byte)0x05 && previousCommand == (byte)0x05){
					outpl_both("");
					textMode = false;
					if (TRANSLATE){
						outpl_both("-- ENGRISH: "+toEnglish(japaneseBuffer));
					}
				}
				if (!textMode){
					String filePointer = transformLong(romfile.getFilePointer()-1); 
					String comment = InjectedComments.COMMENTS.get(filePointer);
					if (comment != null){
						outp_both("-- IF ");
						String[] commentFragments = comment.split(" ");
						for (String commentFragment: commentFragments){
							if (commentFragment.equals("&&")){
								outp_both(" && ");
								continue;
							}
							if (commentFragment.startsWith("!")){
								outp_both("!");
								commentFragment = commentFragment.substring(1);
							}
							if (Bits.KNOWN_BITS.get(commentFragment) != null){
								outp_both(Bits.KNOWN_BITS.get(commentFragment));
							} else {
								outp_both(commentFragment);
							}
						}
						outpl_both("");
					}
					outp(filePointer+":   ");
				}
				if (command == (byte)0x05 && previousCommand != (byte)0x05){
					String filePointer = transformLong(romfile.getFilePointer()-1); 
					outpScript(filePointer+":   ");
				}
				int charIndex = -1;
				switch (command){
				case 0x05:
					if (!textMode){
						outp("PRINT ");
						japaneseBuffer = "";
					}
					textMode = true;
					codeByte = romfile.readByte();
					charIndex = unsign(codeByte);
					if (charIndex > 0 && charIndex < charsetList.size()){
						Character c = charsetList.get(charIndex);
						outp_both(c+"");
						japaneseBuffer += c;
					} else {
						outp_both("?");
					}
					break;
				case 0x07:
					outp("LOAD_PORTRAIT ");
					String portraitCode = transform2(romfile.readByte());
					String portrait = PORTRAITS.get(portraitCode);
					outpl(portrait != null ? portrait : portraitCode);
					break;
				case 0x01:
					outpl("LINE_BREAK");
					break;
				case 0x8C: //Jump
					outpl("JUMP8C "+addressInROM());
					break;
				case 0x8D: // Long Jump
					outpl("XJUMP "+longAddressInROM());
					break;
				case 0x88: // Blank RAM
					outpl("(BLANK)");
					break;
				case 0x9A: 
					outp("SET BIT [");
					//byteSequenceFinishingWith00();
					byte readByte = 0x00;
					while ((readByte = romfile.readByte()) != (byte)0x00){
						String bitName = Bits.KNOWN_BITS.get(transform2(readByte));
						outp(bitName != null ? bitName: transform2(readByte));
					}
					
					outpl("]");
					break;
				case 0x9B: 
					outp("IF NOT BIT [");
					readByte = 0x00;
					while ((readByte = romfile.readByte()) != (byte)0x00){
						String bitName = Bits.KNOWN_BITS.get(transform2(readByte));
						outp(bitName != null ? bitName: transform2(readByte));
					}
					outpl("] DO: ");
					break;
				case 0x94: // Jump if
					outpl("IF "+address()+" JUMP "+addressInROM());
					break;
				case 0x96:
					outpl("JUMP96 "+addressInROM());
					break;
				case 0xAB: // Skip bytes
					outpl("SKIP "+transform2(romfile.readByte()));
					break;
				case 0xB6: // Conditional Dialog
					outp("IF NOT SELECTED DIALOG [");
					outpScript("DIALOG ");
					byte b1 = romfile.readByte();
					byte b2 = romfile.readByte();
					int dialogIndex = unsign(b1) - 61;
					// b2 not used
					if (dialogIndex > 0 && dialogIndex < dialogOptions.size()){
						outp_both("(");
						String japanese = printJapanese(dialogOptions.get(dialogIndex), false);
						currentDialogOptions.add(japanese);
						if (TRANSLATE){
							outp_both(" - "+toEnglish(japanese));
						}
						outp_both(") ");
					} else {
						outp_both("(?) ");
					}
					outpl(" DO: ");
					outpScript("\n");
					break;
				case 0xB8: // Load dialog in memory
					/*
					 * Store values aabb, ccdd, etc. in memory starting at $7E:FC7C. 
					 * Will continue to read until it finds a pair of bytes that has the 7th bit set. 
					 * (i.e. returns a non-zero number when ANDed with #$4000). 
					 * 
					 * This is for loading dialogue options into memory
					 */
					/*
					 * Will try using this as termination sequence for dialogs. Still not sure
					 */
					if (loadedDialog){
						loadedDialog = false;
						continue nextDialog;
					} else {
						loadedDialog = true;
						outp("LOAD_DIALOG_OPTIONS ");
						currentDialogOptions = new ArrayList<String>();
						this.loadDialog();
					}
					break;
				case 0xB9: // Continue loading dialog into memory
					/*
					 * Same as above, but does not initialize address index back to zero and instead 
					 * continues where the above left off.
					 */
					outp("LOAD_MORE_DIALOG_OPTIONS ");
					this.loadDialog();
					break;
				case 0xBE: // Display dialog options
					/*
					 * Display or hide dialogue options. If the highest bit is set (8x), then display it. 
					 * If not set, then hide the option. These are indexed relative to the order they were 
					 * loaded in by a B8 or B9 command.
					 */
					outp("SET_DIALOG_OPTIONS ");
					while ((readByte = romfile.readByte()) != (byte)0x00){
						String dialogOption = transform(readByte);
						if (dialogOption.charAt(0) == '8'){
							outp("SHOW ");
						} else {
							outp("HIDE ");
						}
						int index = Integer.parseInt(dialogOption.charAt(1)+"", 16);
						if (index > currentDialogOptions.size()){
							outp(index+" (????) ");
						} else {
							outp(index+" ("+currentDialogOptions.get(index-1));
							if (TRANSLATE){
								outp(" - "+toEnglish(currentDialogOptions.get(index-1)));
							}
							outp(") ");
						}
					}
					
					outpl("");
					break;
				case 0xC0: // Set flag in RAM
					/*
					 * Set a flag of value xxyy in RAM around $7E:FE78.
					 */
					String address = address();
					String knownName = KNOWN_FLAGS.get(address);
					outpl("SET "+(knownName != null ? knownName : address));
					break;
				case 0xC2:
					/* Check if any flags in $7E:FE78 are greater than xxyy? And then OR xxyy with $8000 and check again. */
					outpl("FLAGS_GREATER_THAN "+address());
					break;
				case 0xC4:
					address = address();
					knownName = KNOWN_FLAGS.get(address);
					outpl("IF NOT FLAG "+ (knownName != null ? knownName : address)+" DO: ");
					break;
				case 0xC9:
					outpl("WAIT_FOR_INPUT");
					this.currentWaitForInput = romfile.getFilePointer()-1;
					break;
				case 0xCA:
					romfile.readByte();
					outpl_both("IF NOT PROMPT THEN "+addressInROM());
					break;
				default:
					if (ALL_JAPANESE){
						charIndex = unsign(codeByte);
						if (charIndex > 0 && charIndex < charsetList.size()){
							Character c = charsetList.get(charIndex);
							outp(c+"");
							japaneseBuffer += c;
						} else {
							outp("?");
						}
					} else {
						outpl("*"+transform2(codeByte)+"*");
					}
				}
				previousCommand = command;
			}
			outp("\n---\n");
		}
		romfile.close();
		
	}

	private String printJapanese(byte[] dialogOption, boolean disassembleOnly) {
		String japanese = "";
		for (int j = 0; j < dialogOption.length; j++){
			byte dialogByte = dialogOption[j];
			int command = unsign(dialogByte);
			if (command == 0x05){
				j++;
				int charIndex = unsign(dialogOption[j]);
				if (charIndex > 0 && charIndex < charsetList.size()){
					Character c = charsetList.get(charIndex);
					if (disassembleOnly)
						outp(c+"");
					else
						outp_both(c+"");
					japanese += c;
				} else {
					if (disassembleOnly)
						outp("?");
					else
						outp_both("?");
				}
			} else {
				if (disassembleOnly)
					outp("*"+transform2(dialogByte)+"*");
				else
					outp_both("*"+transform2(dialogByte)+"*");
			}
		}
		return japanese;
	}

	/**
	 * Transforms a font table index into a character using ShiftJIS mapping
	 * TODO: Handle special characters (Like the arrow keys?) 
	 */
	private char getCharacterByIndex(long index) throws UnsupportedEncodingException{
		index += 0x8140;
		byte b1 = (byte) (index & 0xFF);
		byte b2 = (byte) ((index >> 8) & 0xFF);
		return new String(new byte[]{b2, b1}, "Shift_JIS").charAt(0);
	}
	
	/**
	 * Given two bytes, calculates an int offset
	 */
	private int calculateOffset(byte b1, byte b2){
		return ((b2 & 0xff) << 8) + (b1 & 0xff);
	}
	
	/**
	 * Unsigns a byte to use it as index or something
	 */
	private int unsign(byte b1){
		return b1 & 0xff;
	}
	
	/**
	 * Transform a LOROM SNES Pointer into an offset on the ROM
	 */
	private long calculatePointer(byte[] pointer) {
		byte bank = pointer[2];
		int address = calculateOffset(pointer[0], pointer[1]);
		return (bank & 0x7F) * BANK_SIZE + (address & 0x7FFF);
	}

	// Console output
	private String transform(byte b){
		return String.format("%02X ", b);
	}
	
	private String transform2(byte b){
		return String.format("%02X", b);
	}
	
	private String transformLong(long longo){
		return String.format("%02X", longo);
	}
	
	private void outp(String string) {
		if (MODE == Mode.DISASSEMBLE)
			System.out.print(string);
	}
	
	private void outpScript(String string) {
		if (MODE == Mode.EXTRACT_SCRIPT)
			System.out.print(string);
	}

	private void outpl(String string) {
		if (MODE == Mode.DISASSEMBLE)
			System.out.println(string);
	}
	
	private void outp_both(String string) {
		System.out.print(string);
	}

	private void outpl_both(String string) {
		System.out.println(string);
	}
	
	private void outp(byte[] bytes) {
		for (int i = 0; i < bytes.length; i++){
			outp(transform(bytes[i])+" ");
		}
	}
	
	private String toEnglish(String japanese) throws GoogleAPIException{
	    GoogleAPI.setHttpReferrer("http://blog.slashie.net");
	    GoogleAPI.setKey("AIzaSyCFVYTOEI08D9XH4nz_mE_nwuekJ1hU2EA");
	    return Translate.DEFAULT.execute(japanese, Language.JAPANESE, Language.ENGLISH);
	}
	
	private void loadDialog() throws IOException, GoogleAPIException{
		byte b1 = (byte)0x00;
		byte b2 = (byte)0x00;
		int safe = 1000;
		while (true){
			b1 = romfile.readByte();
			b2 = romfile.readByte();
			if ((b2 & (byte)0x40) != 0) {
				break;
			}
			String address = transform2(b1);
			address = transform2(b2) + address;
			outp(address);
			// Ignore b2? probably initial status
			int dialogIndex = unsign(b1) - 61;
			if (dialogIndex > 0 && dialogIndex < dialogOptions.size()){
				outp("(");
				String japanese = printJapanese(dialogOptions.get(dialogIndex), true);
				currentDialogOptions.add(japanese);
				if (TRANSLATE){
					// outp(" - "+toEnglish(japanese));
				}
				outp(") ");
			} else {
				outp("(?) ");
			}
			safe--;
			if (safe == 0){
				outpl("OVERFLOW!!");
				System.exit(-1);
				break;
			}
		}
		outpl("");
	}
	
	private String address() throws IOException{
		String address = transform2(romfile.readByte());
		return transform2(romfile.readByte()) + address;
	}
	
	private String addressInROM() throws IOException{
		byte b1 = romfile.readByte();
		byte b2 = romfile.readByte();
		long dialogCodeOffset = calculatePointer(new byte[]{b1, b2,currentDialogScriptBank});
		if (currentWaitForInput == dialogCodeOffset){
			return "WAIT_FOR_INPUT";
		} else 
			return transformLong(dialogCodeOffset);
	}
	
	private String longAddressInROM() throws IOException{
		byte b1 = romfile.readByte();
		byte b2 = romfile.readByte();
		byte b3 = romfile.readByte();
		long dialogCodeOffset = calculatePointer(new byte[]{b1, b2, b3});
		return transformLong(dialogCodeOffset);
	}
}
