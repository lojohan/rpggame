

public class Main {
    public static void main(String[] args) {
    	
    	DialogueGenerator.loadWordClasses();
    	DialogueGenerator.loadTemplate();
    	DialogueGenerator.populateMap();
    	DialogueGenerator.generateDialogues(10000,3,15);
    	DialogueGenerator.printDialoguesToFile();
    	
    	NameGenerator.initNameGenerator();
    	System.out.println(NameGenerator.generateRandomName());
    	System.out.println(NameGenerator.generateRandomPlaceName());
    	
    	MapGenerator.generateMap(15, 20, 5);
    }
}
