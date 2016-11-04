import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

public class Markov {
	
	static String templateText = "";
	static HashMap<String,Integer> wordOccurences = new HashMap<>();
	static Vector<String> wordsToChooseFrom = new Vector<>();
	
	public static void loadTemplate() {
		try {
			BufferedReader in = new BufferedReader(new FileReader("template.txt"));
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
	
	public static void populateMap() {

		StringTokenizer st = new StringTokenizer(templateText);
		
		while(st.hasMoreTokens()) {
			String next = st.nextToken();
			if(wordOccurences.containsKey(next)) {
				wordOccurences.put(next, wordOccurences.get(next) + 1 );
			} else {
				wordOccurences.put(next, 1);
			}
			wordsToChooseFrom.addElement(next);
		}
	}
	
	public static void generate() {
		populateMap();
		
		
		Random rn = new Random();
		String randomPhrase = "";
		StringBuilder sb = new StringBuilder(randomPhrase);
		
		while(!randomPhrase.endsWith(". ") && !randomPhrase.endsWith("? ")  && !randomPhrase.endsWith("! ")) {
			int rand = rn.nextInt(wordsToChooseFrom.size() - 1 );
			sb.append(wordsToChooseFrom.get(rand));
			randomPhrase += sb.toString()+" ";
		}
		
		System.out.println(randomPhrase);
	}
}
