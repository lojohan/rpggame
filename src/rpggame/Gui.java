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
	
	TextField recursionDepth;
	
	JTextArea output;
	
	JScrollPane outputPane;
	
	public Gui() {
		super("Gui");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		initButtons();
		
		initTextFields();
		
		this.add(mapGenerationPanel);
		
		this.setSize(400, 400);
		
		MapGenerator.addGui(this);
	}
	
	public void initButtons() {
		mapGenerationButton = new JButton("Generate random map");
		
		mapGenerationPanel.setBounds(800, 800, 200, 200);
		mapGenerationButton.setBounds(60, 400, 220, 30);
		
		mapGenerationPanel.add(mapGenerationButton);
		
		mapGenerationButton.addActionListener(this);
		
	}
	
	public void initTextFields() {
		recursionDepth = new TextField(10);
		
		recursionDepth.setEditable(true);
		
		recursionDepth.setBounds(60, 400, 220, 30);
		
		mapGenerationPanel.add(recursionDepth);
		
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
		case "Generate random map":
			String rec = this.recursionDepth.getText();
			int depth = 0;
			try {
				depth = Integer.parseInt(rec);
				MapGenerator.generate(depth);
			} catch(NumberFormatException ex) {
				ex.getStackTrace();
			}
			break;
		}
	}
	
	public void writeToTextArea(JTextArea ta, String s) {
		ta.setText(s);
		this.update(this.getGraphics());
	}
	
	public void appendToTextArea(JTextArea ta, String s) {
		ta.append(s);
		this.update(this.getGraphics());
	}
}
