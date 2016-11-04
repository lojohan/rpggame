import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Markov {
	
	static String templateText = "";
	static HashMap<String, HashMap<String,Integer> > wordOccurences = new HashMap<>();
	static ArrayList<String> wordsToChooseFrom = new ArrayList<>();
	
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
		
		final Pattern p = Pattern.compile("(\\w|')+|[.:,!?-]");
		
		Matcher m = p.matcher(templateText);
		int nextIndex = 0;
		while(m.find(nextIndex)) {
			String next = m.group();
			
		    nextIndex = m.end();
			if (m.find(nextIndex)) {
				String following = m.group();
				
				HashMap<String, Integer> occ = wordOccurences.get(next);
				if (occ == null) {
					occ = new HashMap<String,Integer>();
					wordOccurences.put(next, occ);
				}
				if(!occ.containsKey(following))
					occ.put(following, 1);
				else
					occ.put(following, occ.get(following) + 1);
			}
			
			wordsToChooseFrom.add(next);
		}
	}
	
	public static String generate() {
		populateMap();
		Random rn = new Random();
		StringBuilder sb = new StringBuilder();
		String lastWord = "";
		int rand = 0;
		do {
			rand = rn.nextInt(wordsToChooseFrom.size());
			lastWord = wordsToChooseFrom.get(rand);
		} while (lastWord.matches("\\W"));
		String firstLetter = lastWord.substring(0, 1);
		String firstWord = lastWord.replaceFirst("\\w", firstLetter.toUpperCase());
		sb.append(firstWord);
		
		while(!lastWord.equals(".") && !lastWord.equals("?")  && !lastWord.equals("!")) {
			
			// get next word...
			HashMap<String,Integer> tmpmap = wordOccurences.get(lastWord);
			
			int numberOfOccs = 0;
			
			for(Integer count : tmpmap.values()) {
				numberOfOccs += count;
			}
			
			rand = rn.nextInt(numberOfOccs);
			
			int count = 0;
			
			for(String word : tmpmap.keySet()) {
				count += tmpmap.get(word);
				if(rand <= count) {
					lastWord = word;
					break;
				}
			}
			
			if(lastWord.isEmpty() || lastWord.matches("[.:,!?-]"))
				sb.append(lastWord);
			else
				sb.append(" "+lastWord);
		}
		return sb.toString();
	}
	
	public static void generateDialogues(int numberOfDialogues) {
		try {
			PrintWriter pw = new PrintWriter(new FileOutputStream("dialgoues.txt"));
			for(int i = 0; i < numberOfDialogues; i++) {
				pw.println(String.valueOf(i+1)+":");
				pw.println(generate());
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
