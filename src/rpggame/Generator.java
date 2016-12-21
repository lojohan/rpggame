package rpggame;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.*;

import java.io.IOException;
import java.nio.file.FileSystems;

public abstract class  Generator {
	
	protected static boolean abort = false;
	
	protected static String outputPath;
	
	protected static String backupName;
	
	protected static String outputName;

	protected static Gui gui;
	
	public static void init() {
		abort = false;
		backupPrevious();
	}
	
	public static void abort() {
		gui.appendToTextArea(gui.output, "Aborting generation...\n");
		abort = true;
		restorePrevious();
		eraseBackup();
		gui.appendToTextArea(gui.output, "Aborted generation!\n");
	}
	
	public static void setFilePaths(String outputPath, String outputName, String backupName) {
		Generator.outputPath = outputPath;
		Generator.outputName = outputName;
		Generator.backupName = backupName;
	}
	
	public static boolean backupPrevious() {
		gui.appendToTextArea(gui.output, "Backing up "+outputName+" to "+backupName+"...\n");
		Path source = FileSystems.getDefault().getPath(outputPath, outputName);
		Path target = FileSystems.getDefault().getPath(outputPath, backupName);
		try {
			Files.copy(source, target, COPY_ATTRIBUTES);
			gui.appendToTextArea(gui.output, "Backed up "+outputName+" to "+backupName+"!\n");
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			gui.appendToTextArea(gui.output, e.getMessage());
			return false;
		}
	}
	
	public static boolean eraseBackup() {
		gui.appendToTextArea(gui.output, "Erasing backup file: "+backupName+"...\n");
		Path target = FileSystems.getDefault().getPath(outputPath, backupName);
		try {
			Files.delete(target);
			gui.appendToTextArea(gui.output, "Erased backup file: "+backupName+"!\n");
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			gui.appendToTextArea(gui.output, e.getMessage());
			return false;
		}
	}
	
	public static boolean restorePrevious() {
		gui.appendToTextArea(gui.output, "Restoring "+outputName+" from "+backupName+"...\n");
		Path target = FileSystems.getDefault().getPath(outputPath, outputName);
		Path source = FileSystems.getDefault().getPath(outputPath, backupName);
		try {
			Files.copy(source, target, REPLACE_EXISTING);
			gui.appendToTextArea(gui.output, "Restored "+outputName+" from "+backupName+"!\n");
			return true;
		} catch (IOException e) {
			gui.appendToTextArea(gui.output, e.getMessage());
			return false;
		}
	}
	
	public static void onComplete() {
		eraseBackup();
	}
	
	public static void addGui(Gui gui) {
		Generator.gui = gui;
	}
}
