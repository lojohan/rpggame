package rpggame;


public class Main {
    public static void main(String[] args) {
    	/*
    	DialogueGenerator.loadWordClasses();
    	DialogueGenerator.loadTemplate();
    	DialogueGenerator.populateMap();
    	DialogueGenerator.generateDialogues(10000,3,15);
    	DialogueGenerator.printDialoguesToFile();
    	
    	for (int i = 0 ; i < 10; i++) {
	    	System.out.println("A random name: " + NameGenerator.generateRandomName());
	    	System.out.println("A random place: " + NameGenerator.generateRandomPlaceName());
    	}
    	*/
    	
    	//MapGenerator.clearMapFile();
    	
    	//MapGenerator.generate(16, 20, 1, 0.03);
    	MapGenerator2.generate(10);
    }
}
