import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameGenerator {
	static ArrayList<String> names = new ArrayList<>();
	static ArrayList<String> placePrefixes = new ArrayList<>();
	static ArrayList<String> placeSuffixes = new ArrayList<>();
	static String templateText = "";
	
	static String generateRandomName() {
		final Random rn = new Random();
		int rand = rn.nextInt(names.size());
		
		return names.get(rand);
	}
	
	static String generateRandomPlaceName() {
		
		final Random rn = new Random();
		final StringBuilder sb = new StringBuilder();
		int rand = rn.nextInt(100);
		
		if(rand <= 50) {
			int rand1 = rn.nextInt(placePrefixes.size());
			sb.append(placePrefixes.get(rand1)+" "+ generateRandomName() );
		}		
		else {
			int rand2 = rn.nextInt(placeSuffixes.size());
			sb.append(generateRandomName()+" "+placeSuffixes.get(rand2) );
		}
			
		return sb.toString();
	}
	
	static void loadPossibleNames() {
		HashSet<String> wordClasses = loadWordClasses();
		HashSet<String> allWords = loadAllWords();
		
		for(String word : allWords) {
			if(!wordClasses.contains(word)) {
				final String firstLetter = word.substring(0, 1);
				final String firstWord = word.replaceFirst("\\w", firstLetter.toUpperCase());
				names.add(firstWord);
			}
		}
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
	
	static HashSet<String> loadWordClasses() {
		HashSet<String> wordClasses = new HashSet<String>();
		for (String clazz : new String[]{"verbs", "adjectives", "nouns", "adverbs", "prepositions", "conjunctions", "pronouns", "punctuations"}) {
			try {
				BufferedReader in = new BufferedReader(new FileReader("templates/wordclasses/" + clazz + ".txt"));
				String line;
				while ((line = in.readLine()) != null) {
					String word = line.trim();
					if (word.isEmpty()) continue;
						wordClasses.add(word);
				}
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return wordClasses;
	}
	
	static HashSet<String> loadAllWords() {
		HashSet<String> words = new HashSet<String>();
		
		final Pattern p = Pattern.compile("(\\w|')+|[.:,!?-]");
		
		Matcher m = p.matcher(templateText);
		
		while(m.find()) {
			String word = m.group();
			words.add(word);
		}
		
		return words;
	}
	
	static void loadPrefixes() {
		try {
			BufferedReader in = new BufferedReader(new FileReader("templates/fixes/prefixes.txt"));
			String line;
			while ((line = in.readLine()) != null) {
				String word = line.trim();
				if (word.isEmpty()) continue;
					placePrefixes.add(word);
		}
		in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void loadSuffixes() {
		try {
			BufferedReader in = new BufferedReader(new FileReader("templates/fixes/suffixes.txt"));
			String line;
			while ((line = in.readLine()) != null) {
				String word = line.trim();
				if (word.isEmpty()) continue;
					placeSuffixes.add(word);
		}
		in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
