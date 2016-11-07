package rpggame;

import java.util.Random;

public class NameGenerator {
	
	static String generateRandomName() {
		return Wordlist.getInstance().getRandomName();
	}

	static String generateRandomPlaceName() {

		final Random rn = new Random();
		final StringBuilder sb = new StringBuilder();
		int rand = rn.nextInt(100);
		
		Wordlist wlist = Wordlist.getInstance();

		if (rand <= 50) {
			// prefix
			String prefix = wlist.getRandomPlacePrefix() + " ";
			// s?
			if (rn.nextInt(100) < 30) {
				prefix = prefix.replaceFirst(" of", "s of");
			}
			sb.append(prefix);
			
			// adjective
			if (rn.nextInt(100) < 33) {
				sb.append(Wordlist.capitalize(wlist.getRandomTemplateAdjective()) + " ");
			}
			
			// name
			sb.append(generateRandomName());
		} else {
			// adjective
			if (rn.nextInt(100) < 33) {
				sb.append(Wordlist.capitalize(wlist.getRandomTemplateAdjective()) + " ");
			}
			// name
			sb.append(generateRandomName());
			
			// suffix
			sb.append(" " +  wlist.getRandomPlaceSuffix());
			
			// s?
			if (rn.nextInt(100) < 30) {
				sb.append('s');
			}
		}

		return sb.toString();
	}
}
