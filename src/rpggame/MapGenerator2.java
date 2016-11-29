package rpggame;

import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class MapGenerator2 {
	static final boolean DEBUG = false;
	
	@SuppressWarnings("serial")
	static final HashMap<String, IntegerPair> sizeMap = new HashMap<String, IntegerPair>() {
		{
			put("Village", new IntegerPair(6, 12));
			put("Town", new IntegerPair(10, 20));
			put("City", new IntegerPair(16, 26));
			put("Cave", new IntegerPair(8, 18));
			put("Cavern", new IntegerPair(8, 18));
			put("Field", new IntegerPair(8, 18));
			put("Forest", new IntegerPair(8, 22));
			put("Mount", new IntegerPair(12, 22));
			put("Mountain", new IntegerPair(12, 22));
			put("River", new IntegerPair(6, 12));
			put("Domain", new IntegerPair(20, 30));
			put("Land", new IntegerPair(20, 30));
			put("Sea", new IntegerPair(16, 26));
			put("Ocean", new IntegerPair(20, 40));
		}
	};
	
	public static ArrayList<World> worlds = new ArrayList<>();
	
	public static void generate(int maximumDepth) {
		clearMapFile();
		World mainWorld = new World(0,maximumDepth);
		mainWorld.generate(0,null);
		printToFile();
	}
	
	public static void clearMapFile() {
		try {
			
			PrintWriter pw = new PrintWriter(new FileOutputStream("output/maps/randommap.txt"));
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void printToFile() {
		PrintWriter pw;
		try {
			pw = new PrintWriter(new FileOutputStream("output/maps/randommap.txt", true));
			
		
			for(World world : worlds) {
				pw.println(world.id);
				
				for(Zone2 zone : world.zones) {
					for(String zoneString : zone.zone) {
						pw.println(zoneString);
					}
				}
				Set<IntegerPair> allEntities = world.entities.keySet();
				for(IntegerPair entitiesOnPoint : allEntities) {
					ArrayList<String> entityStrings = world.entities.get(entitiesOnPoint);
					for(String entity : entityStrings) {		
						pw.println(entity);
					}
				}
				pw.println();
				pw.println();
				pw.println();
				pw.println();
				pw.println();
			}
			
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static class World {
		int id;
		int maximumDepth  = 0;
		HashMap<IntegerPair, ArrayList<String> > entities = new HashMap<>();	
		ArrayList<Zone2> zones = new ArrayList<>();
		
		public World(int id, int maximumDepth) {
			this.id = id;
			this.maximumDepth = maximumDepth;
		}
		
		public void addToWorldList() {
			worlds.add(this);
		}
		
		// recursive function to generate worlds and add them WorldList
		public boolean generate(int currentDepth, IntegerPair prevExit) {
			boolean couldGenerateThisZone = false;
			
			if(currentDepth > maximumDepth) return couldGenerateThisZone;
			
			// placeholder
			String name = NameGenerator.generateRandomPlaceName();
			Zone2 currentZone = new Zone2(name,zones);
			
			if(prevExit != null) {
				currentZone.exits.add(prevExit);
			}

			
			// TODO: does not guarantee that the exits are connected
			if(couldGenerateThisZone = currentZone.generateFirstRectangle(0, 0, 10, 10)) {
				zones.add(currentZone);
				//currentZone.generateExits(4);
				currentZone.generateRandomRectangles(5, 5, 5, 10, 10);
				currentZone.addZones();
				IntegerPair potentialExit = currentZone.getPotentialExit();
				
				// recurse to next zone.
				if(generate(currentDepth + 1, potentialExit)) {
					currentZone.exits.add(potentialExit);
				}
				
				currentZone.generateWallTiles();
				if(currentDepth == 0) {
					currentZone.addPlayer(new IntegerPair(1,1), "Anton");
					addToWorldList();
				}
				
				currentZone.setFriendly(true);
				putEntityMap(currentZone);
			}
			
			return couldGenerateThisZone;
		}
		
		
		public boolean canAddZone(Zone2 zone) {
			if(zones.isEmpty()) {
				return true;
			}
			
			for(Zone2 zoneInWorld : this.zones) {
				if(zone.overlaps(zoneInWorld)) return false;
			}
			return true;
		}
		
		private void putEntityMap(Zone2 zone) {
			for(IntegerPair key : zone.entities.keySet()) {
				if(this.entities.containsKey(key)) {
					for(String entity : zone.entities.get(key)) {
						if(!this.entities.get(key).contains(entity)) {
							this.entities.get(key).add(entity);
						}
					}
				} else {
					this.entities.put(key, zone.entities.get(key));
				}
			}
		}
	}
}
