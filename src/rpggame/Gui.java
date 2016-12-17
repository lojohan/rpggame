package rpggame;

import javax.swing.JFrame;

public class Gui extends JFrame {
	public Gui() {
		super("Gui");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void makeVisible() {
		this.pack();
		this.setVisible(true);
	}
	
}
