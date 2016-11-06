import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class MapGenerator {
	static HashMap<String,String> entities = new HashMap<>();
	static ArrayList<String> entityStrings = new ArrayList<>();
	
	final static int SizeOfZone = 20;
	
	// Generate tiles in a specific zone
	public static void generateTiles() {
		// placeholder
		for(int i = 0; i < SizeOfZone; i++) {
			String pos1 = "0,"+i;
			entities.put(pos1,"Tile");
			entityStrings.add("Tile;;"+ pos1 +";1;7;");
			
			String pos2 = SizeOfZone+","+i;
			entities.put(pos2,"Tile");
			entityStrings.add("Tile;;"+ pos2 +";1;7;");
			
			String pos3 = i+",0";
			entities.put(pos3,"Tile");
			entityStrings.add("Tile;;"+ pos3 +";1;7;");
			
			String pos4 = i+","+SizeOfZone;
			entities.put(pos4,"Tile");
			entityStrings.add("Tile;;"+ pos4 +";1;7;");
		}
	}
	
	// generate zone, size is determined by randomness
	public static void generateZone() {
		entityStrings.add("Zone;"+NameGenerator.generateRandomPlaceName()+";0,0;"+(SizeOfZone-1)+","+(SizeOfZone-1)+";");
	}
	
	// generate entities in specific zone
	public static void generateNonPlayerEntities() {
		// placeholder
		for(int i = 1; i < SizeOfZone/2; i++) {
			String pos = i*2+","+i;
			entities.put(pos, "NPC");
			entityStrings.add("NPC;"+NameGenerator.generateRandomName()+";"+pos+";2;2;battle;randomAI;displayDialogue(0);");
		}
	}
	
	public static void generatePlayer() {
		// placeholder
		String pos = "4,5";
		entities.put(pos, "Player");
		entityStrings.add("Player;"+NameGenerator.generateRandomName()+";"+pos+";3;2;;playerControl;");
	}
	
	// prints all the generated entities to randommap.txt
	public static void printToMap() {
		try {
			PrintWriter pw = new PrintWriter(new FileOutputStream("output/maps/randommap.txt"));
			for(int i = 0; i < entityStrings.size(); i++) {
				pw.println(entityStrings.get(i));
				pw.flush();
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
