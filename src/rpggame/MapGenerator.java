package rpggame;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import rpggame.Zone.Dir;


public class MapGenerator {
	static HashMap<String,String> entities = new HashMap<>();
	static ArrayList<String> entityStrings = new ArrayList<>();
	
	// Generate tiles in a specific zone
	public static void generateTiles(int startX, int startY, int sizeX, int sizeY) {
		// placeholder		
	}
	
	public static void generateEdgeTiles(Zone zone) {
		for(Dir dir : Dir.values()) {
			ArrayList<IntegerPair> tmp = zone.getEdge(dir);
			for(IntegerPair ip : tmp) {
				String pos = ip.x+","+ip.y;
				entities.put(pos, "Tile");
				entityStrings.add("Tile;;"+ pos +";1;7;");
			}
		}
	}
	
	public static void generate(int sizeX, int sizeY, int maximumDepth) {
		int currentDepth = 0;
    	MapGenerator.generateMap(0,0,sizeX, sizeY, currentDepth, maximumDepth,0,0);
    	MapGenerator.printToMap();
	}
	
	public static void generateMap(int startX, int startY, int sizeX, int sizeY, int currentDepth, int maximumDepth, int prevExitX, int prevExitY) {
		if(currentDepth <= maximumDepth) {
			
			entityStrings.add("Zone;"+NameGenerator.generateRandomPlaceName()+";"+startX+","+startY+";"+sizeX+","+sizeY+";");
			Zone currentZone = new Zone(startX, startY, sizeX, sizeY);
			
	    	MapGenerator.generateEdgeTiles(currentZone);
	    	MapGenerator.generateNonPlayerEntities(currentZone, 2);
	    	MapGenerator.generatePlayer(currentZone, currentDepth);
	    	
	    	int newStartX = startX-sizeX;
	    	generateMap(newStartX,0, sizeX, sizeY, currentDepth+1,maximumDepth, startX, 5);
	    	
			clearExit(currentDepth, prevExitX, prevExitY);
	    	
		}
	}

	private static void clearExit(int currentDepth, int prevExitX, int prevExitY) {
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
	
	// generate entities in specific zone
	public static void generateNonPlayerEntities(Zone zone, double entityDensity) {
		// placeholder
		int count = 0;
		int numberOfEntities = (int) (zone.sizeX*zone.sizeY*entityDensity/100);
		
		while(count < numberOfEntities) {
			
			final Random rn = new Random();
			int randX = zone.x + 1 + rn.nextInt(zone.sizeX-1);
			int randY = zone.y + 1 + rn.nextInt(zone.sizeY-1);
			
			String pos = randX+","+randY;
			if(!entities.containsKey(pos)) {
				entities.put(pos, "NPC");
				entityStrings.add("NPC;"+NameGenerator.generateRandomName()+";"+pos+";2;2;battle;randomAI;displayDialogue(0);");
				count++;
			}
		}
	}
	
	public static void generatePlayer(Zone zone, int currentDepth) {
		if(currentDepth == 0) {
			// placeholder
			boolean playerAdded = false;
			final Random rn = new Random();
			
			while(!playerAdded) {
				int randX = zone.x +1 + rn.nextInt(zone.sizeX-1);
				int randY = zone.y +1 + rn.nextInt(zone.sizeY-1);
				
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
