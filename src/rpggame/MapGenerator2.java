package rpggame;

import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
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
		mainWorld.generate(0,null,null);
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
		public boolean generate(int currentDepth, Edge edgeForEntrance, IntegerPair prevExit) {
			boolean couldGenerateThisZone = false;
			
			if(currentDepth > maximumDepth) return couldGenerateThisZone;
			
			String name = NameGenerator.generateRandomPlaceName();
			Zone2 currentZone = new Zone2(name,zones);
			
			if(prevExit != null) {
				// TODO : should be reworked so that walls of zones do not touch
				currentZone.exits.add(prevExit);
			}
			
			// get rid of this!!!!!!!!
			IntegerPair stupidCoordsGetRidOf;
			IntegerPair stupidSizeGetRidOf;
			if(edgeForEntrance != null) {
				stupidCoordsGetRidOf = new IntegerPair(edgeForEntrance.getCorners().get(0));
				stupidSizeGetRidOf = new IntegerPair(edgeForEntrance.size()-1,edgeForEntrance.size()-1);
			}
			else {
				stupidCoordsGetRidOf = new IntegerPair(0,0);
				stupidSizeGetRidOf = new IntegerPair(10,10);
			}
				
			
			// TODO: does not properly determine size of the new zone or guarantee that exit leads to it.
			if(couldGenerateThisZone = currentZone.generateFirstRectangle(stupidCoordsGetRidOf.x, stupidCoordsGetRidOf.y, 
					stupidSizeGetRidOf.x, stupidSizeGetRidOf.y, edgeForEntrance)) {
				zones.add(currentZone);
				currentZone.generateRandomRectangles(2, 5, 5, 10, 10);
				currentZone.addZones();
				
				// recurse to next zone.
				recurseToNextZone(currentZone, currentDepth);
				
				generateScenery(currentZone, currentDepth);
				
				generatePlayer(currentZone, currentDepth);
				
				currentZone.setFriendly(true);
				putEntityMap(currentZone);
			}
			
			return couldGenerateThisZone;
		}
		
		private void generateScenery(Zone2 zone, int currentDepth) {
			zone.generateBlockingScenery();
			//zone.generateNonBlockingScenery();
		}
		
		private void generatePlayer(Zone2 zone, int currentDepth) {
			if(currentDepth == 0) {
				zone.addPlayer(new IntegerPair(1,1), "Anton");
				addToWorldList();
			}
		}
		
		private void recurseToNextZone(Zone2 currentZone, int currentDepth) {
			Random rand = new Random();
			int rando = 1+rand.nextInt(4);
			for(int i = 0; i < rando; i++) { 
				// TODO: this edge should only be the edge which it would have in common with the next rectangle:
				Edge exitEdge = currentZone.getRandomEdgeForExit();
				IntegerPair exit = currentZone.getRandomPointForExitOnEdge(exitEdge);
				
				if(generate(currentDepth + 1, exitEdge, exit)) {
					currentZone.exits.add(exit);
				}
			}
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
