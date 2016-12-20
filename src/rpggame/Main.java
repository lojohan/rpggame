package rpggame;


public class Main {
    public static void main(String[] args) {
    	
    	DialogueGenerator.loadWordClasses();
    	DialogueGenerator.loadTemplate();
    	DialogueGenerator.populateMap();
    	
    	Gui gui = new Gui();
    	gui.makeVisible();
    	
    }
}
