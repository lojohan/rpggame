package rpggame;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DialogueGenerator extends Generator{
	
	static String templateText = "";
	static HashMap<String, HashMap<String,Integer>> wordOccurrences = new HashMap<>();
	static HashMap<String, HashMap<String,Integer>> classOccurrences = new HashMap<>();
	
	static ArrayList<String> dialogues = new ArrayList<>();
	
	static HashMap<String, String> _wordClasses = new HashMap<>();
	static HashMap<String, List<String>> _wordClasses_reverse = new HashMap<>();
	
	final static int ResolutionNormalization = 10000000;
	
	static ArrayList<String> wordsToChooseFrom = new ArrayList<>();
	private static int wordsNotInLists;
	private static int totalWordsCount;
	
	public static Gui gui;
	
	public static void addGui(Gui gui) {
		DialogueGenerator.gui = gui;
	}
	
	public static void resetDialogues() {
		setFilePaths("output/","RandomDialogues.txt","RandomDialoguesBackup.txt");
		init();
		dialogues.clear();
	}
	
	public static void loadTemplate() {
		try {
			BufferedReader in = new BufferedReader(new FileReader("templates/template.txt"));
			StringBuilder sb = new StringBuilder(templateText);
			while(in.ready()) {
				sb.append((char)in.read());
			}
			templateText = sb.toString();
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void loadWordClasses() {
		for (String clazz : new String[]{"verbs", "adjectives", "nouns", "adverbs", "prepositions", "conjunctions", "pronouns", "punctuations"}) {
			_wordClasses_reverse.put(clazz, new ArrayList<>());
			try {
				BufferedReader in = new BufferedReader(new FileReader("templates/wordclasses/" + clazz + ".txt"));
				String line;
				while ((line = in.readLine()) != null) {
					String word = line.trim();
					if (word.isEmpty()) continue;
					_wordClasses.put(word, clazz);
				}
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void populateMap() {
		
		final Pattern p = Pattern.compile("(\\w|')+|[.:,!?-]");
		
		HashSet<String> notInListSet = new HashSet<>();
		
		Matcher m = p.matcher(templateText);
		
		int nextIndex = 0;
		while(m.find(nextIndex)) {
			String next = m.group();
			
			/* Check the following word,, and add an occurrence count. */
		    nextIndex = m.end();
			if (m.find(nextIndex)) {
				String following = m.group();
				
				HashMap<String, Integer> occ = wordOccurrences.get(next);
				if (occ == null) {
					occ = new HashMap<String,Integer>();
					wordOccurrences.put(next, occ);
				}
				if(!occ.containsKey(following))
					occ.put(following, 1);
				else
					occ.put(following, occ.get(following) + 1);
				
				// do the same but for word class
				occ = classOccurrences.get(next);
				if (occ == null) {
					occ = new HashMap<String,Integer>();
					classOccurrences.put(next, occ);
				}
				final String followingClass = getWordClassNormalized(following);
				if(followingClass != null) {
					if(!occ.containsKey(followingClass))
						occ.put(followingClass, 1);
					else
						occ.put(followingClass, occ.get(followingClass) + 1);
				}
			}
			
			wordsToChooseFrom.add(next);
			putWordClassNormalized(next);
			
			if (hasWordClassNormalized(next)) {
				notInListSet.add(next);
			}
		}
		totalWordsCount = wordOccurrences.size();
		
		wordsNotInLists = notInListSet.size();
		System.out.println("Stats: words not in lists: " + wordsNotInLists + ", total wordCount:" + totalWordsCount + " ("+(100*(float)wordsNotInLists/totalWordsCount)+"%)");
		normalizeOccurrences();
	}
	
	static void normalizeOccurrences() {
		
		for(String word : wordOccurrences.keySet()) {
			int numberOfOccs = 0;
			HashMap<String, Integer> nextwordstats = wordOccurrences.get(word);
			for(Integer count : nextwordstats.values()) {
				numberOfOccs += count;
			}
			
			int sumWordCount = 0;
			
			for (Entry<String, Integer> entry : nextwordstats.entrySet()) {
				int n = (int)((double)entry.getValue() / (double)numberOfOccs * ResolutionNormalization);
				sumWordCount += n;
				entry.setValue(n);
			}
			
			if (sumWordCount != ResolutionNormalization) {
				assert false;
			}
		}
		// do the same for clazzzzes
		for(String word : classOccurrences.keySet()) {
			int numberOfOccs = 0;
			HashMap<String, Integer> nextwordstats = classOccurrences.get(word);
			for(Integer count : nextwordstats.values()) {
				numberOfOccs += count;
			}
			
			int sumClassCount = 0;
			
			for (Entry<String, Integer> entry : nextwordstats.entrySet()) {
				int n = (int)((double)entry.getValue() / (double)numberOfOccs * ResolutionNormalization);
				sumClassCount += n;
				entry.setValue(n);
			}
			
			if (sumClassCount != ResolutionNormalization) {
				assert false;
			}
		}
	}
	
	static boolean hasWordClassNormalized(String word) {
		String w = word.toLowerCase();
		return _wordClasses.containsKey(w);
	}
	
	static String getWordClassNormalized(String word) {
		String w = word.toLowerCase();
		String clazz = _wordClasses.get(w);
		return clazz;
	}
	
	static List<String> getWordsOfClass(String clazz) {
		List<String> words = _wordClasses_reverse.get(clazz);
		return words;
	}
	
	static void putWordClassNormalized(String word) {
		final String wordClassNormalized = getWordClassNormalized(word);
		if (wordClassNormalized != null)
			_wordClasses_reverse.get(wordClassNormalized).add(word);
	}
	
	public static String generate(int minimumWords, int maximumWords) {
		final Random rn = new Random();
		final StringBuilder sb = new StringBuilder();
		String lastWord = "";
		int rand;
		
		// Choose first word
		do {
			rand = rn.nextInt(wordsToChooseFrom.size());
			lastWord = wordsToChooseFrom.get(rand);
		} while (lastWord.matches("\\W"));
		
		final String firstLetter = lastWord.substring(0, 1);
		final String firstWord = lastWord.replaceFirst("\\w", firstLetter.toUpperCase());
		sb.append(firstWord);		
		int numberofwords = 0;
		
		while(!lastWord.equals(".") && !lastWord.equals("?")  && !lastWord.equals("!")) {
			
			// get next word...
			String nextWord = ""; // in case the following algos goes wrong...
			int rand1 = rn.nextInt(100);
			if (rand1 < 90) {
				final HashMap<String,Integer> nextwordstats = wordOccurrences.get(lastWord);
				
				rand = rn.nextInt(ResolutionNormalization);
				
				int count = 0;
				
				for(String word : nextwordstats.keySet()) {
					count += nextwordstats.get(word);
					if(rand < count) {
						nextWord = word;
						break;
					}
				}
				if (nextWord.equals("")) {
					assert false;
				}
			} else {
				final HashMap<String,Integer> nextwordclassstats = classOccurrences.get(lastWord);
				int rand2 = rn.nextInt(ResolutionNormalization);
				
				int count = 0;
				String nextWordClass = "adverbs";
				
				for(String clazz : nextwordclassstats.keySet()) {
					count += nextwordclassstats.get(clazz);
					if(rand2 < count) {
						nextWordClass = clazz;
						break;
					}
				}
				
				List<String> wordsOfClass = getWordsOfClass(nextWordClass);
				int rand3 = rn.nextInt(wordsOfClass.size());
				nextWord = wordsOfClass.get(rand3);
				
				if (nextWord.equals("")) {
					assert false;
				}
			}
			
			if(nextWord.isEmpty() || nextWord.matches("[.:,!?-]"))
				sb.append(nextWord);
			else
				sb.append(" "+nextWord);
			
			if(numberofwords == 19) {
				sb.append("...");
			}
			
			if (!nextWord.isEmpty()) {
				lastWord = nextWord;
				numberofwords++;
			}
		}
		if(numberofwords <= maximumWords && numberofwords >= minimumWords)
			return sb.toString();
		else
			return null;
	}
	
	public static boolean generateDialogues(int numberOfDialogues, int minimumWords, int maximumWords) {
		while(dialogues.size() < numberOfDialogues && !abort) {
			String s = generate(minimumWords,maximumWords);
			if(s != null) {
				dialogues.add(s);
			}
			gui.writeToTextArea(gui.output, "Generating "+numberOfDialogues+" random dialogue strings...\n");
			gui.appendToTextArea(gui.output, "Progress: "+(100*dialogues.size()/(float)numberOfDialogues)+"%\n");
		}
		onComplete();
		return true;
	}
	
	public static void printDialoguesToFile() {
		try {
			PrintWriter pw = new PrintWriter(new FileOutputStream("output/RandomDialogues.txt"));
			for(int i = 0; i < dialogues.size(); i++) {
				pw.println(String.valueOf(i+1)+":");
				pw.println(dialogues.get(i));
				pw.flush();
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}


