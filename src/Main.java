

public class Main {
    public static void main(String[] args) {
    	
    	DialogueGenerator.loadWordClasses();
    	DialogueGenerator.loadTemplate();
    	DialogueGenerator.populateMap();
    	DialogueGenerator.generateDialogues(10000,3,15);
    	DialogueGenerator.printDialoguesToFile();
    	
    	
    	NameGenerator.loadTemplate();
    	NameGenerator.loadPossibleNames();
    	NameGenerator.loadPrefixes();
    	NameGenerator.loadSuffixes();
    	System.out.println(NameGenerator.generateRandomName());
    	System.out.println(NameGenerator.generateRandomPlaceName());
    	
    	MapGenerator.generateTiles();
    	MapGenerator.generateNonPlayerEntities();
    	MapGenerator.generatePlayer();
    	MapGenerator.generateZone();
    	MapGenerator.printToMap();
    }
}
