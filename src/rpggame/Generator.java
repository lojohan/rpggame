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
		abort = true;
		restorePrevious();
		eraseBackup();
	}
	
	public static void setFilePaths(String outputPath, String outputName, String backupName) {
		Generator.outputPath = outputPath;
		Generator.outputName = outputName;
		Generator.backupName = backupName;
	}
	
	public static boolean backupPrevious() {
		Path source = FileSystems.getDefault().getPath(outputPath, outputName);
		Path target = FileSystems.getDefault().getPath(outputPath, backupName);
		try {
			Files.copy(source, target, COPY_ATTRIBUTES);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean eraseBackup() {
		Path target = FileSystems.getDefault().getPath(outputPath, backupName);
		try {
			Files.delete(target);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean restorePrevious() {
		Path target = FileSystems.getDefault().getPath(outputPath, outputName);
		Path source = FileSystems.getDefault().getPath(outputPath, backupName);
		try {
			Files.copy(source, target, REPLACE_EXISTING);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
