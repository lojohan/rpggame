package rpggame;

import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class Gui extends JFrame implements ActionListener{
	
	JPanel mapGenerationPanel = new JPanel();
	
	JButton mapGenerationButton;
	
	JButton dialogueGenerationButton;
	
	TextField recursionDepth;
	
	TextField numberOfDialogues;
	
	JTextArea output;
	
	JScrollPane outputPane;
	
	boolean verbose = true;
	
	public Gui() {
		super("Gui");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		initButtons();
		
		initTextFields();
		
		this.add(mapGenerationPanel);
		
		this.setSize(400, 400);
		
		MapGenerator.addGui(this);
		
		DialogueGenerator.addGui(this);
	}
	
	public void initButtons() {
		mapGenerationButton = new JButton("Generate random map");
		
		mapGenerationPanel.setBounds(800, 800, 200, 200);
		mapGenerationButton.setBounds(60, 400, 220, 30);
		
		mapGenerationPanel.add(mapGenerationButton);
		
		mapGenerationButton.addActionListener(this);
		
		dialogueGenerationButton = new JButton("Generate random dialogues");
		
		dialogueGenerationButton.setBounds(60, 400, 220, 30);
		
		mapGenerationPanel.add(dialogueGenerationButton);
		
		dialogueGenerationButton.addActionListener(this);
		
	}
	
	public void initTextFields() {
		recursionDepth = new TextField(10);
		
		recursionDepth.setEditable(true);
		
		recursionDepth.setBounds(60, 400, 220, 30);
		
		numberOfDialogues = new TextField(10);
		
		numberOfDialogues.setEditable(true);
		
		numberOfDialogues.setBounds(60, 400, 220, 30);
		
		mapGenerationPanel.add(recursionDepth);
		
		mapGenerationPanel.add(numberOfDialogues);
		
		output = new JTextArea(16, 58);
		
		output.setEditable(false);
		
		outputPane = new JScrollPane(output);
		
		outputPane.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		
		mapGenerationPanel.add(outputPane);
		
		
	}
	
	public void makeVisible() {
		this.pack();
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
		case "Generate random map": {
			Runnable runnable = new GenerateMap();
			Thread thread = new Thread(runnable);
			thread.start();
			break;
		}
		case "Generate random dialogues": {
			Runnable runnable = new GenerateDialogue();
			Thread thread = new Thread(runnable);
			thread.start();
			break;
			}
		}
	}

	private void generateRandomMap() {
		String rec = this.recursionDepth.getText();
		int depth = 0;
		try {
			depth = Integer.parseInt(rec);
			if(depth > -1) {
				MapGenerator.generate(depth);
			} else {
				this.writeToTextArea(this.output, "Input must be at least 0!");
			}
		} catch(NumberFormatException ex) {
			ex.getStackTrace();
		}
	}
	
	public void generateRandomDialogues() {
		String num = this.numberOfDialogues.getText();
		int count = 0;
		try {
			count = Integer.parseInt(num);
			if(count > -1) {
		    	DialogueGenerator.generateDialogues(count,3,15);
		    	this.appendToTextArea(this.output, "Generated "+count+" random dialogue strings!\n");
		    	
		    	this.appendToTextArea(this.output, "Printing dialogue strings to file...\n");
		    	DialogueGenerator.printDialoguesToFile();
		    	this.appendToTextArea(this.output, "Printed dialogue strings to file!\n");
			} else {
				this.writeToTextArea(this.output, "Input must be at least 0!");
			}
		} catch(NumberFormatException ex) {
			ex.getStackTrace();
		}
	}
	
	public void writeToTextArea(JTextArea ta, String s) {
		ta.setText(s);
		//output.setCaretPosition(output.getText().length() - 1);
		this.output.repaint();
	}
	
	public void appendToTextArea(JTextArea ta, String s) {
		ta.append(s);
		//output.setCaretPosition(output.getText().length() - 1);
		this.output.repaint();
	}
	
	public void writeToTextAreaIfVerbose(JTextArea ta, String s) {
		if(verbose) {
			writeToTextArea(ta,s);
		}
	}
	
	public void appendToTextAreaIfVerbose(JTextArea ta, String s) {
		if(verbose) {
			appendToTextArea(ta,s);
		}
	}
	
	class GenerateDialogue implements Runnable{

		@Override
		public void run() {
			generateRandomDialogues();		
		}
		
	}
	
	class GenerateMap implements Runnable{

		@Override
		public void run() {
			generateRandomMap();
		}
		
	}
}
