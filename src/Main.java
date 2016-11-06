

public class Main {
    public static void main(String[] args) {
    	Markov.loadWordClasses();
    	Markov.loadTemplate();
    	Markov.populateMap();
    	Markov.generateDialogues(10000,3,15);
    	Markov.printDialoguesToFile();
    }
}
