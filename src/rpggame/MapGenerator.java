package rpggame;
import java.awt.Rectangle;
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
	static ArrayList<Zone> createdZones = new ArrayList<>();
	static ArrayList<IntegerPair> exitsToClear = new ArrayList<>();
	
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
    	MapGenerator.generateMap(50,50,sizeX, sizeY, currentDepth, maximumDepth,0,0,Dir.EAST);
    	clearExits();
    	MapGenerator.printToMap();
	}
	
	public static void generateMap(int startX, int startY, int sizeX, int sizeY, int currentDepth, int maximumDepth, int prevExitX, int prevExitY, Dir exclude) {
		if(currentDepth <= maximumDepth) {
			
			boolean canCreateZone = true;
			
			for(Zone createdZone : createdZones) {
				if(zoneOverLaps(createdZone, startX,startY,sizeX,sizeY)) {
					canCreateZone = false;
				}
			}
			
			if(canCreateZone) {
				entityStrings.add("Zone;"+NameGenerator.generateRandomPlaceName()+";"+startX+","+startY+";"+(startX+sizeX)+","+(startY+sizeY)+";");
				Zone currentZone = new Zone(startX, startY, sizeX, sizeY);
				createdZones.add(currentZone);
				//Dir exitDir = Dir.WEST;
				Dir exitDir = Zone.getRandomDirectionExcl(exclude);
				
				//used to not have the next zone be the same as the current one.
				Dir excludeDir = getExcludeDir(exitDir);
				
				
				IntegerPair nextSize = getNextSize(5,5,30,30);
				IntegerPair nextCoords = getNewStartCoords(currentZone,nextSize.x,nextSize.y,exitDir);
				
				IntegerPair exitPoint = getRandomPointOnEdge(currentZone.getEdge(exitDir),
						getLimit(currentZone,nextSize.x,nextSize.y,exitDir));
				
		    	MapGenerator.generateEdgeTiles(currentZone);
		    	MapGenerator.generateNonPlayerEntities(currentZone, 2);
		    	MapGenerator.generatePlayer(currentZone, currentDepth);
		    	
		    	if(currentDepth != maximumDepth) {
		    		generateMap(nextCoords.x,nextCoords.y, nextSize.x, nextSize.y, currentDepth+1,maximumDepth, exitPoint.x, exitPoint.y, excludeDir);
		    	}
		    	
		    	exitsToClear.add(new IntegerPair(prevExitX,prevExitY));
			}
	    	
		}
	}
	
	private static int getLimit(Zone zone,  int sizeX, int sizeY, Dir dir) {
		switch(dir) {
		case NORTH:
			return zone.sizeY+sizeY-2;
		case EAST:
			return zone.sizeX+sizeX-2;
		case SOUTH:
			return zone.sizeY+sizeY-2;
		case WEST:
			return zone.sizeX+sizeX-2;
		default:
			return 0;
		}
		
		
	}
	
	private static boolean zoneOverLaps(Zone zone, int startX, int startY, int sizeX, int sizeY) {
		Rectangle r1 = new Rectangle(zone.x, zone.y, zone.sizeX, zone.sizeY);
		Rectangle r2 = new Rectangle(startX, startY, sizeX, sizeY);
		return r1.intersects(r2);
	}
	
	private static IntegerPair getNewStartCoords(Zone currentZone,int nextZoneSizeX, int nextZoneSizeY, Dir dir) {
		IntegerPair ip;
		switch(dir) {
		case NORTH:
			ip = new IntegerPair(currentZone.x - nextZoneSizeX, currentZone.y);
			break;
		case EAST:
			ip = new IntegerPair(currentZone.x, currentZone.y + currentZone.sizeY);
			break;
		case SOUTH:
			ip = new IntegerPair(currentZone.x + currentZone.sizeX, currentZone.y);
			break;
		case WEST:
			ip = new IntegerPair(currentZone.x, currentZone.y - nextZoneSizeY);
			break;
		default:
			return null;
		}
		return ip;
	}
	
	private static IntegerPair getNextSize(int xlimlower, int ylimlower, int xlimupper, int ylimupper) {
		final Random rn = new Random();
		int rand1 = xlimlower + rn.nextInt(xlimupper - xlimlower);
		int rand2 = ylimlower + rn.nextInt(ylimupper - ylimlower);
		
		return new IntegerPair(rand1,rand2);
	}
	
	private static IntegerPair getRandomPointOnEdge(ArrayList<IntegerPair> edge) {
		final Random rn = new Random();
		int rand = 1 + rn.nextInt(edge.size()-2);
		return edge.get(rand);
	}
	
	private static IntegerPair getRandomPointOnEdge(ArrayList<IntegerPair> edge, int limit) {
		final Random rn = new Random();
		int rand = 0;
		if(edge.size() <= limit) {
			rand = 1 + rn.nextInt(edge.size()-2);
		} else {
			rand = 1 + rn.nextInt(limit-1);
		}
		return edge.get(rand);
	}
	
	private static Dir getExcludeDir(Dir dir) {
		switch(dir) {
		case NORTH:
			return Dir.SOUTH;
		case EAST:
			return Dir.WEST;
		case SOUTH:
			return Dir.NORTH;
		case WEST:
			return Dir.WEST;
		default:
			return null;
		}
	}

	private static void clearExit(int currentDepth, int prevExitX, int prevExitY) {
		if(currentDepth > 0) {
			String exitCoords = prevExitX+","+prevExitY;
			entities.remove(exitCoords);
			
			Iterator<String> it = entityStrings.iterator();
			
			
			while(it.hasNext()) {
				String entity = it.next();
				if(entity.startsWith("Tile;;"+exitCoords)) {
					it.remove();
				}
			}
			
		}
	}
	
	private static void clearExits() {
		
		for(IntegerPair exit : exitsToClear) {
		String exitCoords = exit.x+","+exit.y;
		entities.remove(exitCoords);
		
		Iterator<String> it = entityStrings.iterator();
		
		
		while(it.hasNext()) {
			String entity = it.next();
			if(entity.startsWith("Tile;;"+exitCoords)) {
				it.remove();
			}
			}
		}
	}
	
	// generate entities in specific zone
	public static void generateNonPlayerEntities(Zone zone, double entityDensity) {
		// placeholder
		int count = 0;
		int numberOfEntities = (int) ( (zone.sizeX-1)*(zone.sizeY-1)*entityDensity/100);
		
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
