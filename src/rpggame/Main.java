package rpggame;


public class Main {
    public static void main(String[] args) {
    	
    	DialogueGenerator.loadWordClasses();
    	DialogueGenerator.loadTemplate();
    	DialogueGenerator.populateMap();
    	DialogueGenerator.generateDialogues(10000,3,15);
    	DialogueGenerator.printDialoguesToFile();
    	
    	//MapGenerator.generate(50);
    	
    	Gui gui = new Gui();
    	gui.makeVisible();
    	
    }
}
