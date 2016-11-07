import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class MapGenerator {
	static HashMap<String,String> entities = new HashMap<>();
	static ArrayList<String> entityStrings = new ArrayList<>();
	
	// Generate tiles in a specific zone
	public static void generateTiles(int startX, int startY, int sizeX, int sizeY) {
		// placeholder
		for(int i = startY; i < sizeY; i++) {
			String pos1 = startX+","+i;
			entities.put(pos1,"Tile");
			entityStrings.add("Tile;;"+ pos1 +";1;7;");
			
			String pos2 = (startX + sizeX)+","+i;
			entities.put(pos2,"Tile");
			entityStrings.add("Tile;;"+ pos2 +";1;7;");
		}
		for(int i = startX; i < sizeX; i++) {
			String pos3 = i+","+startY;
			entities.put(pos3,"Tile");
			entityStrings.add("Tile;;"+ pos3 +";1;7;");
			
			String pos4 = i+","+(startY + sizeY);
			entities.put(pos4,"Tile");
			entityStrings.add("Tile;;"+ pos4 +";1;7;");
		}
		
	}
	
	public static void generateMap(int sizeX, int sizeY, int maximumDepth) {
		int currentDepth = 0;
    	MapGenerator.generateZone(0,0,sizeX, sizeY, currentDepth, maximumDepth,0,0);
    	MapGenerator.printToMap();
	}
	
	// generate zone, size is determined by randomness
	public static void generateZone(int startX, int startY, int sizeX, int sizeY, int currentDepth, int maximumDepth, int prevExitX, int prevExitY) {
		if(currentDepth <= maximumDepth) {
			
			entityStrings.add("Zone;"+NameGenerator.generateRandomPlaceName()+";"+startX+","+startY+";"+sizeX+","+sizeY+";");
	    	MapGenerator.generateTiles(startX, startY, sizeX, sizeY);
	    	MapGenerator.generateNonPlayerEntities(startX, startY, sizeX, sizeY, 2);
	    	MapGenerator.generatePlayer(sizeX, sizeY, currentDepth);
	    	generateZone(-sizeX,0, sizeX, sizeY, currentDepth+1,maximumDepth, 0, 5);
	    	
			if(currentDepth > 0) {
				String exitCoords = prevExitX+","+prevExitY;
				entities.remove(exitCoords);
				
				Iterator<String> it = entityStrings.iterator();
				
				while(it.hasNext()) {
					String entity = it.next();
					if(entity.contains("Tile;;"+exitCoords)) {
						it.remove();
					}
				}
			}
	    	
		}
	}
	
	// generate entities in specific zone
	public static void generateNonPlayerEntities(int startX, int startY, int sizeX, int sizeY, double entityDensity) {
		// placeholder
		int count = 0;
		int numberOfEntities = (int) (sizeX*sizeY*entityDensity/100);
		
		while(count < numberOfEntities) {
			
			final Random rn = new Random();
			int randX = startX + 1 + rn.nextInt(sizeX-1);
			int randY = startY + 1 + rn.nextInt(sizeY-1);
			
			String pos = randX+","+randY;
			if(!entities.containsKey(pos)) {
				entities.put(pos, "NPC");
				entityStrings.add("NPC;"+NameGenerator.generateRandomName()+";"+pos+";2;2;battle;randomAI;displayDialogue(0);");
				count++;
			}
		}
	}
	
	public static void generatePlayer(int sizeX, int sizeY, int currentDepth) {
		if(currentDepth == 0) {
			// placeholder
			boolean playerAdded = false;
			final Random rn = new Random();
			
			while(!playerAdded) {
				int randX = 1 + rn.nextInt(sizeX-1);
				int randY = 1 + rn.nextInt(sizeY-1);
				
				String pos = randX+","+randY;
				if(!entities.containsKey(pos)) {
					entities.put(pos, "Player");
					entityStrings.add("Player;"+NameGenerator.generateRandomName()+";"+pos+";3;6;;playerControl;");
					playerAdded = true;
				}
			}
		}
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
