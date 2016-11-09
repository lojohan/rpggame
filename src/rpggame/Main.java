package rpggame;


public class Main {
    public static void main(String[] args) {
    	
    	DialogueGenerator.loadWordClasses();
    	DialogueGenerator.loadTemplate();
    	DialogueGenerator.populateMap();
    	DialogueGenerator.generateDialogues(10000,3,15);
    	DialogueGenerator.printDialoguesToFile();
    	
    	for (int i = 0 ; i < 10; i++) {
	    	System.out.println("A random name: " + NameGenerator.generateRandomName());
	    	System.out.println("A random place: " + NameGenerator.generateRandomPlaceName());
    	}
    	
    	MapGenerator.generate(16, 20, 100, 0.03);
    }
}
