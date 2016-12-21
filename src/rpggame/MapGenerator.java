package rpggame;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MapGenerator {
	static final boolean DEBUG = false;
	
	public static int worldID = 0;
	
	public static int numberofNPCs = 0;
	
	public static int numberofZones = 0;
	
	public static int numberofSublevels = 0;
	
	public static Gui gui;
	
	// a map containing possible sizes for different kinds of zones.
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
	
	// a list of all created worlds.
	public static ArrayList<World> worlds = new ArrayList<>();
	
	/**
	 * Clears previous map from file, generates a new one, and then prints it to the file.
	 * @param maximumDepth - the maximumDepth of the recursion.
	 */
	public static void generate(int maximumDepth) {
		gui.writeToTextArea(gui.output, "");
		gui.writeToTextArea(gui.output, "Generating map...\n");
		
		reset();
		
		World mainWorld = new World(worldID,worldID, maximumDepth);
		mainWorld.generate(0,null,null);
		printToFile();
		
		gui.appendToTextArea(gui.output, "\nMap Generated!\n");
		gui.appendToTextArea(gui.output, "	"+worlds.size()+" worlds generated!\n");
		gui.appendToTextArea(gui.output, "	"+numberofZones+" zones generated!\n");
		gui.appendToTextArea(gui.output, "	"+numberofSublevels+" sublevels generated!\n");
		gui.appendToTextArea(gui.output, "	"+numberofNPCs+" NPCs generated!\n");
		
	}
	
	public static void reset() {
		clearMapFile();
		worldID = 0;
		worlds.clear();
		numberofNPCs = 0;
		numberofZones = 0;
		numberofSublevels = 0;
	}
	
	public static void addGui(Gui gui) {
		MapGenerator.gui = gui;
	}
	
	/**
	 * Clears the generated map-file.
	 */
	public static void clearMapFile() {
		try {
			RandomAccessFile rf = new RandomAccessFile("output/maps/randommap.txt", "rw");
			rf.setLength(0);
			rf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Prints to the map-file.
	 */
	// TODO: print in proper so that drawing in the game is not messed up.
	public static void printToFile() {
		PrintWriter pw;
		try {
			pw = new PrintWriter(new FileOutputStream("output/maps/randommap.txt", true));
			
		
			for(World world : worlds) {
				pw.println(world.id);
				
				printZones(world, pw);
				
				printEntities(world, pw);

				pw.println();
				pw.println();
				pw.println();
				pw.println();
				pw.println();
			}
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void printZones(World world, PrintWriter pw) {
		for(Zone zone : world.zones) {
			for(String zoneString : zone.zone) {
				pw.println(zoneString);
			}
		}
	}
	
	private static void printEntities(World world, PrintWriter pw) {
		
		Set<IntegerPair> allEntities = world.entities.keySet();
		for(IntegerPair entitiesOnPoint : allEntities) {
			ArrayList<String> entityStrings = world.entities.get(entitiesOnPoint);
			for(String entity : entityStrings) {		
				pw.println(entity);
			}
		}
	}
	
	// class representing a world
	static class World {
		int id;
		int returnid;
		int maximumDepth  = 0;
		HashMap<IntegerPair, ArrayList<String> > entities = new HashMap<>();	
		ArrayList<Zone> zones = new ArrayList<>();
		
		public World(int id, int returnid, int maximumDepth) {
			this.id = id;
			this.maximumDepth = maximumDepth;
		}
		
		public void addToWorldList() {
			worlds.add(this);
		}
		
		public void addZoneToWorld(Zone zone) {
			this.zones.add(zone);
		}
		
		/**
		 * recursive function to generate worlds and add them WorldList
		 * @param currentDepth
		 * @param edgeForEntrance
		 * @param prevExit
		 * @return
		 */
		public boolean generate(int currentDepth, Edge edgeForEntrance, IntegerPair prevExit) {
			boolean couldGenerateThisZone = false;
			
			if(currentDepth > maximumDepth) return couldGenerateThisZone;
			
			String name = NameGenerator.generateRandomPlaceName();
			Zone currentZone = new Zone(name,zones,this);
			
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
				numberofZones++;
				
				addZoneToWorld(currentZone);
				
				gui.appendToTextAreaIfVerbose(gui.output, "Generating Zone: "+currentZone.name+"...\n");
				
				gui.appendToTextAreaIfVerbose(gui.output, "Generating Rectangles...\n");
				currentZone.generateRandomRectangles(2, 5, 5, 10, 10);
				gui.appendToTextAreaIfVerbose(gui.output, "Done Generating Rectangles!\n");
				
				if(currentDepth != 0) {
					gui.appendToTextAreaIfVerbose(gui.output, "Setting Zone friendly...\n");
					currentZone.randomFriendly(0.1);
					gui.appendToTextAreaIfVerbose(gui.output, "Done setting zone friendly: "+currentZone.friendly+"\n");
				}
				
				gui.appendToTextAreaIfVerbose(gui.output, "Adding entrance to zone: "+currentZone.name+"...\n");
				addEntranceToZone(currentZone, prevExit);
				gui.appendToTextAreaIfVerbose(gui.output, "Added entrance to zone: "+currentZone.name+"!\n");
				
				doOnlyFirstTime(currentZone, currentDepth);
				
				recurseToNextZone(currentZone, currentDepth, 8);
				
				gui.appendToTextAreaIfVerbose(gui.output, "Generating scenery for zone: "+currentZone.name+"...\n");
				generateScenery(currentZone, currentDepth);
				gui.appendToTextAreaIfVerbose(gui.output, "Generated scenery for zone: "+currentZone.name+"!\n");
				
				gui.appendToTextAreaIfVerbose(gui.output, "Generating NPCs for zone: "+currentZone.name+"...\n");
				numberofNPCs += generateNPCs(currentZone, currentDepth);
				gui.appendToTextAreaIfVerbose(gui.output, "Generated NPCs for zone: "+currentZone.name+"!\n");
				
				gui.appendToTextAreaIfVerbose(gui.output, "Adding entities to map...\n");
				putEntityMap(currentZone);
				gui.appendToTextAreaIfVerbose(gui.output, "Added entities to map!\n");
				
				gui.appendToTextAreaIfVerbose(gui.output, "Adding zone: "+currentZone.name+" to map...\n");
				currentZone.addZones();
				gui.appendToTextAreaIfVerbose(gui.output, "Added zone: "+currentZone.name+" to map!\n");
				
				gui.appendToTextAreaIfVerbose(gui.output, "Done Generating Zone: "+currentZone.name+"!\n");
			}
			
			return couldGenerateThisZone;
		}
		
		public static void incrementWorldID() {
			worldID += 1;
		}
		
		public static int getWorldID() {
			return worldID;
		}
		
		private void addEntranceToZone(Zone zone, IntegerPair entrance) {
			if (entrance != null) {
				zone.addExit(entrance);
			}
		}
		
		public int generateNPCs(Zone zone, int currentDepth) {
			return zone.generateNPCs(0.03).size();
		}
		
		/**
		 * Generates scenery for the zone, both solid and non-solid.
		 * @param zone
		 * @param currentDepth
		 */
		public void generateScenery(Zone zone, int currentDepth) {
			gui.appendToTextAreaIfVerbose(gui.output, "Generating blocking scenery for zone: "+zone.name+"...\n");
			zone.generateBlockingScenery();
			gui.appendToTextAreaIfVerbose(gui.output, "Generated blocking scenery for zone: "+zone.name+"!\n");
			
			gui.appendToTextAreaIfVerbose(gui.output, "Generating non-blocking scenery for zone: "+zone.name+"...\n");
			zone.generateNonBlockingScenery();
			gui.appendToTextAreaIfVerbose(gui.output, "Generated non-blocking scenery for zone: "+zone.name+"!\n");
		}
		
		/**
		 * Things that should only be done for the first zone in a particular world.
		 * @param zone
		 * @param currentDepth
		 */
		private void doOnlyFirstTime(Zone zone, int currentDepth) {
			if(currentDepth == 0) {
				zone.generatePlayer();
				addToWorldList();
				zone.setFriendly(true);
			}
		}
		
		/**
		 * Calls on the generate function to generate the next zone.
		 * @param currentZone
		 * @param currentDepth
		 */
		private void recurseToNextZone(Zone currentZone, int currentDepth, int tries) {
			Random rand = new Random();
			int rando = 1+rand.nextInt(tries);
			for(int i = 0; i < rando; i++) { 
				// TODO: this edge should only be the edge which it would have in common with the next rectangle:
				Edge exitEdge = currentZone.getRandomEdgeForExit();
				IntegerPair exit = currentZone.getRandomPointForExitOnEdge(exitEdge);
				
				if(generate(currentDepth + 1, exitEdge, exit)) {
					addEntranceToZone(currentZone, exit);
				}
			}
			
		}
		
		public boolean canAddZone(Zone zone) {
			if(zones.isEmpty()) {
				return true;
			}
			
			for(Zone zoneInWorld : this.zones) {
				if(zone.overlaps(zoneInWorld)) return false;
			}
			return true;
		}
		
		public void putEntityMap(Zone zone) {
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
