package rpggame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class Gui extends JFrame implements ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JPanel mapGenerationPanel = new JPanel(new GridBagLayout());
	
	GridBagConstraints c = new GridBagConstraints();
	
	JButton mapGenerationButton;
	
	JButton dialogueGenerationButton;
	
	JButton abortButton;
	
	TextField recursionDepth;
	
	TextField numberOfDialogues;
	
	JTextArea output;
	
	JScrollPane outputPane;
	
	boolean verbose = true;
	
	Runnable run1 = new GenerateMap();
	
	Runnable run2 = new GenerateDialogue();
	
	Thread thread = new Thread();
	
	public Gui() {
		super("Gui");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		initButtons();
		
		initTextFields();
		
		this.add(mapGenerationPanel);
		
		this.setSize(400, 400);
		
		this.setResizable(false);
		
		MapGenerator.addGui(this);
		
		DialogueGenerator.addGui(this);
	}
	
	public void initButtons() {
		mapGenerationButton = new JButton("Generate random map");
		
		mapGenerationPanel.setBounds(800, 800, 200, 200);
		mapGenerationButton.setBounds(60, 400, 220, 30);
		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		mapGenerationPanel.add(mapGenerationButton, c);
		
		mapGenerationButton.addActionListener(this);
		
		dialogueGenerationButton = new JButton("Generate random dialogues");
		
		dialogueGenerationButton.setBounds(60, 400, 220, 30);
		
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		mapGenerationPanel.add(dialogueGenerationButton, c);
		
		dialogueGenerationButton.addActionListener(this);
		
		abortButton = new JButton("Abort current process");
		
		abortButton.setBounds(60, 400, 220, 30);
		
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		mapGenerationPanel.add(abortButton, c);
		
		abortButton.addActionListener(this);
		
	}
	
	public void initTextFields() {
		recursionDepth = new TextField(10);
		
		recursionDepth.setEditable(true);
		
		recursionDepth.setBounds(60, 400, 220, 30);
		
		numberOfDialogues = new TextField(10);
		
		numberOfDialogues.setEditable(true);
		
		numberOfDialogues.setBounds(60, 400, 220, 30);
		
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		mapGenerationPanel.add(recursionDepth, c);
		
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		mapGenerationPanel.add(numberOfDialogues, c);
		
		output = new JTextArea(16, 58);
		
		output.setEditable(false);
		
		outputPane = new JScrollPane(output);
		
		outputPane.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		
		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		mapGenerationPanel.add(outputPane, c);
		
		
	}
	
	public void makeVisible() {
		this.pack();
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
		case "Generate random map":
			if(this.thread != null) {
				if(!this.thread.isAlive()) {
					thread = new Thread(run1);
					thread.start();
				} else {
					showThreadBusy(this);
				}
			} else {
				thread = new Thread(run1);
				thread.start();
			}
			break;
		case "Generate random dialogues":
			if(this.thread != null) {
				if(!this.thread.isAlive()) {
					thread = new Thread(run2);
					thread.start();
				} else {
					showThreadBusy(this);
				}
			} else {
				thread = new Thread(run2);
				thread.start();
			}
			break;
		case "Abort current process":
			terminateThread();
			break;
		}
	}

	private boolean generateRandomMap() {
		String rec = this.recursionDepth.getText();
		int depth = 0;
		try {
			depth = Integer.parseInt(rec);
			if(depth > -1) {
				MapGenerator.generate(depth);
				return true;
			} else {
				this.writeToTextArea(this.output, "Input must be a number larger than or equal to 0!");
			}
		} catch(NumberFormatException ex) {
			this.writeToTextArea(this.output, "Input must be a number larger than or equal to 0!");
			ex.getStackTrace();
			this.terminateThread();
			return false;
		}
		this.terminateThread();
		return false;
	}
	
	public boolean generateRandomDialogues() {
		String num = this.numberOfDialogues.getText();
		int count = 0;
		try {
			count = Integer.parseInt(num);
			if(count > -1) {
				DialogueGenerator.resetDialogues();
		    	DialogueGenerator.generateDialogues(count,3,15);
		    	this.appendToTextArea(this.output, "Generated "+count+" random dialogue strings!\n");
		    	
		    	this.appendToTextArea(this.output, "Printing dialogue strings to file...\n");
		    	DialogueGenerator.printDialoguesToFile();
		    	this.appendToTextArea(this.output, "Printed dialogue strings to file!\n");
		    	return true;
			} else {
				this.writeToTextArea(this.output, "Input must be a number larger than or equal to 0!");
			}
		} catch(NumberFormatException ex) {
			this.writeToTextArea(this.output, "Input must be a number larger than or equal to 0!");
			ex.getStackTrace();
			this.terminateThread();
			return false;
		}
		this.terminateThread();
		return false;
	}
	
	public void showThreadBusy(JFrame parent) {
		final JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		writeToTextArea(textArea, "Thread currently busy. "
				+ "Please wait until current process is done and try again.");
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(350,150));
		
		JOptionPane.showMessageDialog(parent, scrollPane, "An Error Has Occurred", JOptionPane.ERROR_MESSAGE);

	}
	
	public void writeToTextArea(JTextArea ta, String s) {
		ta.setText(s);
		output.setCaretPosition(output.getDocument().getLength());
		this.output.repaint();
	}
	
	public void appendToTextArea(JTextArea ta, String s) {
		ta.append(s);
		output.setCaretPosition(output.getDocument().getLength());
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
	
	// TODO: this does not work. make fix plz.
	public void terminateThread() {
		this.thread = null;
		DialogueGenerator.abort();
		MapGenerator.abort();
	}
	
	class GenerateDialogue implements Runnable{

		@Override
		public void run() {
			Thread thisThread = Thread.currentThread();
			boolean first = true;
			boolean done = false;
			while(thread == thisThread) {
				if(first) {
					done = generateRandomDialogues();
					first = false;
				}
				if(done) thread = null;
			}
		}
		
	}
	
	class GenerateMap implements Runnable{

		@Override
		public void run() {
			Thread thisThread = Thread.currentThread();
			boolean first = true;
			boolean done = false;
			while(thread == thisThread) {
				if(first) {
					done = generateRandomMap();
					first = false;
				}
				if(done) thread = null;
			}
		}
		
	}
}
