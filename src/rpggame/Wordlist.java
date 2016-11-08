package rpggame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Wordlist {
	public static final String[] WORD_CLASSES = new String[] { "verbs", "adjectives", "nouns", "adverbs",
			"prepositions", "conjunctions", "pronouns", "punctuations" };
	public final static Pattern wordPattern = Pattern.compile("(\\w|')+|[.:,!?-]");

	final private ArrayList<String> names = new ArrayList<>();
	final private ArrayList<String> placePrefixes = new ArrayList<>();
	final private ArrayList<String> placeSuffixes = new ArrayList<>();
	final private Map<String, TreeSet<String>> wordsByClass = new HashMap<>();
	final private TreeSet<String> templateWords = new TreeSet<>();

	final private Random rn = new Random();

	private static Wordlist instance;

	private Wordlist() {

		for (String clazz : WORD_CLASSES) {
			wordsByClass.put(clazz, new TreeSet<String>());
		}

		loadWordClasses();
		loadTemplateWords();
		loadPrefixes();
		loadSuffixes();
		
		loadPossibleNames();
	}
	
	public static Wordlist getInstance() {
		if (instance == null) instance = new Wordlist();
		return instance;
	}

	public static String capitalize(String word) {
		String firstLetter = word.substring(0, 1);
		String firstWord = word.replaceFirst("\\w", firstLetter.toUpperCase());
		return firstWord;
	}

	public String getRandomAdjective() {
		return getRandomOfClass("adjectives");
	}
	
	public String getRandomTemplateAdjective() {
		return getRandomOfClassTemplate("adjectives");
	}

	public String getRandomVerb() {
		return getRandomOfClass("verbs");
	}
	
	public String getRandomTemplateVerb() {
		return getRandomOfClassTemplate("verbs");
	}

	public String getRandomNoun() {
		return getRandomOfClass("nouns");
	}
	
	public String getRandomTemplateNoun() {
		return getRandomOfClassTemplate("nouns");
	}

	public String getRandomAdverb() {
		return getRandomOfClass("adverbs");
	}
	
	public String getRandomTemplateAdverb() {
		return getRandomOfClassTemplate("adverbs");
	}

	public String getRandomOfClass(String clazz) {
		TreeSet<String> w = wordsByClass.get(clazz);
		return getRandomSetItem(w); // w.get(rn.nextInt(w.size()));
	}
	
	public String getRandomOfClassTemplate(String clazz) {
		TreeSet<String> classWords = wordsByClass.get(clazz);
		String w = null;
		do {
			w = getRandomSetItem(templateWords);
		} while (!classWords.contains(w));
		return w; // w.get(rn.nextInt(w.size()));
	}

	public String getRandomName() {
		int rand = rn.nextInt(names.size());
		return names.get(rand);
	}
	
	public String getRandomPlacePrefix() {
		int rand1 = rn.nextInt(placePrefixes.size());
		return placePrefixes.get(rand1);
	}
	
	public String getRandomPlaceSuffix() {
		int rand1 = rn.nextInt(placeSuffixes.size());
		return placeSuffixes.get(rand1);
	}

	public boolean isWordlistWord(String word) {
		for (TreeSet<String> words : wordsByClass.values()) {
			if (words.contains(word))
				return true;
		}
		return false;
	}

	public boolean isTemplateWord(String word) {
		return templateWords.contains(word);
	}

	private void loadPossibleNames() {
		for (String word : templateWords) {
			if (word.matches("\\d+")) continue;
			if (!word.matches(".*\\w+.*")) continue;
			if (!isWordlistWord(word.toLowerCase().replaceAll("'s$", "").replaceAll("s'$", "s"))) {
				final String firstLetter = word.substring(0, 1);
				final String firstWord = word.replaceFirst("\\w", firstLetter.toUpperCase());
				names.add(firstWord);
			}
		}
	}

	private void loadWordClasses() {
		for (String clazz : WORD_CLASSES) {
			final Set<String> words = wordsByClass.get(clazz);
			try {
				BufferedReader in = new BufferedReader(new FileReader("templates/wordclasses/" + clazz + ".txt"));
				String line;
				while ((line = in.readLine()) != null) {
					String word = line.trim();
					if (word.isEmpty())
						continue;

					words.add(word);
				}
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void loadTemplateWords() {
		try {
			BufferedReader in = new BufferedReader(new FileReader("templates/template.txt"));
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty())
					continue;

				Matcher m = wordPattern.matcher(line);

				while (m.find()) {
					String word = m.group();
					templateWords.add(word.toLowerCase());
				}
			}
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadPrefixes() {
		try {
			BufferedReader in = new BufferedReader(new FileReader("templates/fixes/prefixes.txt"));
			String line;
			while ((line = in.readLine()) != null) {
				String word = line.trim();
				if (word.isEmpty())
					continue;
				placePrefixes.add(word);
			}
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadSuffixes() {
		try {
			BufferedReader in = new BufferedReader(new FileReader("templates/fixes/suffixes.txt"));
			String line;
			while ((line = in.readLine()) != null) {
				String word = line.trim();
				if (word.isEmpty())
					continue;
				placeSuffixes.add(word);
			}
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getRandomSetItem(Set<String> set) {
		int rand = rn.nextInt(set.size());
		return set.stream().skip(rand).findFirst().get();
	}
}
