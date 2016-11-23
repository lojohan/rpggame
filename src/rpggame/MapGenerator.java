package rpggame;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import rpggame.Zone.Dir;

public class MapGenerator {
	static final boolean DEBUG = false;

	static HashMap<String, String> entities = new HashMap<>(10000000);
	static ArrayList<String> entityStrings = new ArrayList<>();
	static ArrayList<Zone> createdZones = new ArrayList<>();
	static ArrayList<IntegerPair> exitsToClear = new ArrayList<>();
	static ArrayList<IntegerPair> nextToExits = new ArrayList<>();
	static int numberOfCreatedExits = 0;

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

	public static void generateEdgeTiles(Zone zone, boolean visible) {
		for (ArrayList<IntegerPair> edge : getAllEdges(zone)) {
			for (IntegerPair ip : edge) {
				generateSolidTile(ip.x, ip.y, visible);
			}
		}
	}

	public static IntegerPair getSizeForZone(String name) {
		IntegerPair size = new IntegerPair(6, 30);
		String[] words = name.split(" ");

		for (String word : words) {
			if (sizeMap.containsKey(word)) {
				size = sizeMap.get(word);
				break;
			}
			String noS = word.replaceFirst("s$", "");
			if (sizeMap.containsKey(noS)) {
				size = sizeMap.get(noS);
				break;
			}
		}

		return size;
	}

	public static void generateSolidTile(int x, int y, boolean visible) {
		String pos = x + "," + y;
		//if (!entities.containsKey(pos)) {
			entities.put(pos, "Tile");
			if(visible)
				entityStrings.add("Tile;;" + pos + ";1;1;7;");
			else
				entityStrings.add(0,"Tile;;" + pos + ";1;6;3;");
		//}
	}

	public static void generate(int sizeX, int sizeY, int maximumDepth, double entityDensity) {
		int currentDepth = 0;
		System.out.println("Before clearing exits:");
		MapGenerator.generateMap(50, 50, sizeX, sizeY, currentDepth, maximumDepth, 0, 0, entityDensity, null, null);
		clearExits();
		clearNextToExits();
		MapGenerator.printToMap();

		System.out.println("After clearing exits:");
		for (Zone zone : createdZones) {
			checkNumbersAfterClearing(zone);
		}
		System.out.println("The following tiles are duplicates");
		if (DEBUG) {
			ArrayList<String> duplicates = getDuplicateTilesInList();
			for (String duplicate : duplicates) {
				System.out.println(duplicate);
			}
		}
	}

	public static void generateLabyrinth(int x, int y, int sizeX, int sizeY) {
		HashMap<IntegerPair, IntegerPair> labyrinthTiles = new HashMap<>();

		// fill with prelim tiles
		for (int i = 0; i < sizeX - 1; i++) {
			for (int j = 0; j < sizeY - 1; j++) {
				if (i % 2 == 0 && j % 2 == 0) {
					labyrinthTiles.put(new IntegerPair(i, j), new IntegerPair(0, 0));
				} else {
					labyrinthTiles.put(new IntegerPair(i, j), new IntegerPair(1, 0));
				}
			}
		}

		recursiveLabyrinth(labyrinthTiles, 0, 0, sizeX - 1, sizeY - 1);

		// generate tiles
		for (IntegerPair ip : labyrinthTiles.keySet()) {
			IntegerPair tile = labyrinthTiles.get(ip);
			if (tile.x == 1) {
				generateSolidTile(ip.x + x + 1, ip.y + y + 1, true);
			}
		}
	}

	public static void recursiveLabyrinth(HashMap<IntegerPair, IntegerPair> labyrinth, int currentX, int currentY,
			int sizeX, int sizeY) {
		int newX = 0;
		int newY = 0;

		// ArrayList<Dir> dirs = new TreeSet(Arrays.asList(Dir.values()));

		List<Dir> dirs = (List<Dir>) Arrays.asList(Dir.values());
		Collections.shuffle(dirs);
		for (Dir dir : dirs) {
			IntegerPair newPos = new IntegerPair(currentX + newX, currentY + newY);

			newX = 0;
			newY = 0;
			switch (dir) {
			case NORTH:
				newY = -2;
				break;
			case EAST:
				newX = 2;
				break;
			case SOUTH:
				newY = 2;
				break;
			case WEST:
				newX = -2;
				break;
			}

			newPos.x = currentX + newX;
			newPos.y = currentY + newY;

			boolean withinMapBounds = currentX + newX >= 0 && currentX + newX < sizeX && currentY + newY >= 0
					&& currentY + newY < sizeY;

			if (!withinMapBounds)
				continue; // next dir

			boolean visited = labyrinth.get(newPos).y == 1;

			if (visited)
				continue; // next dir

			if (labyrinth.containsKey(newPos)) {
				IntegerPair wall = new IntegerPair(currentX + newX / 2, currentY + newY / 2);
				labyrinth.put(wall, new IntegerPair(0, 0));
				labyrinth.get(newPos).y = 1;
				recursiveLabyrinth(labyrinth, newPos.x, newPos.y, sizeX, sizeY);
			} else {
				throw new AssertionError("is a bug");
			}
		}
	}

	public static void generateMap(int startX, int startY, int sizeX, int sizeY, int currentDepth, int maximumDepth,
			int prevExitX, int prevExitY, double entityDensity, Dir excludeDir, String zoneName) {
		if (currentDepth <= maximumDepth) {

			boolean canCreateZone = true;
			if (createdZones.isEmpty())
				canCreateZone = true;

			for (Zone createdZone : createdZones) {
				if (zoneOverLaps(createdZone, startX, startY, sizeX, sizeY)) {
					canCreateZone = false;
				}
			}

			if (canCreateZone) {
				int friendly = 0;
				if (isAreaFriendly(10) || currentDepth == 0)
					friendly = 1;
				if (zoneName == null)
					zoneName = NameGenerator.generateRandomPlaceName();
				entityStrings.add("Zone;" + zoneName + ";" + startX + "," + startY + ";" + (startX + sizeX) + ","
						+ (startY + sizeY) + ";" + friendly + ";");
				Zone currentZone = new Zone(startX, startY, sizeX, sizeY);
				createdZones.add(currentZone);
				boolean visibleEdge = true;

				for (Dir direction : Dir.values()) {
					if (direction != excludeDir) {
						String nextZoneName = NameGenerator.generateRandomPlaceName();
						IntegerPair nextSizeBounds = getSizeForZone(nextZoneName);
						IntegerPair nextSize = getNextSize(nextSizeBounds.x, nextSizeBounds.x, nextSizeBounds.y,
								nextSizeBounds.y);
						IntegerPair nextCoords = getNewStartCoords(currentZone, nextSize.x, nextSize.y, direction);

						IntegerPair exitPoint = getRandomPointOnEdge(currentZone.getEdge(direction),
								getLimit(currentZone, nextSize.x, nextSize.y, direction));

						if (currentDepth != maximumDepth) {
							IntegerPair nextToExitPoint1 = new IntegerPair(exitPoint.x, exitPoint.y);
							IntegerPair nextToExitPoint2 = new IntegerPair(exitPoint.x, exitPoint.y);

							switch (direction) {
							case NORTH:
								nextToExitPoint1.x = exitPoint.x + 1;
								nextToExitPoint2.x = exitPoint.x - 1;
								break;
							case EAST:
								nextToExitPoint1.y = exitPoint.y - 1;
								nextToExitPoint2.y = exitPoint.y + 1;
								break;
							case SOUTH:
								nextToExitPoint1.x = exitPoint.x - 1;
								nextToExitPoint2.x = exitPoint.x + 1;
								break;
							case WEST:
								nextToExitPoint1.y = exitPoint.y + 1;
								nextToExitPoint2.y = exitPoint.y - 1;
								break;
							}

							nextToExits.add(nextToExitPoint1);
							nextToExits.add(nextToExitPoint2);
						}

						if (currentDepth != maximumDepth) {
							generateMap(nextCoords.x, nextCoords.y, nextSize.x, nextSize.y, currentDepth + 1,
									maximumDepth, exitPoint.x, exitPoint.y, entityDensity, getExcludeDir(direction),
									nextZoneName);
						}
					}

				}

				if ((zoneName.contains("Labyrinth") || zoneName.contains("Cave")) && friendly == 0) {
					MapGenerator.generateLabyrinth(currentZone.x, currentZone.y, currentZone.sizeX, currentZone.sizeY);
				} else if (zoneName.contains("Forest")) {
					MapGenerator.generateForest(currentZone, 10);
					MapGenerator.generateGrass(currentZone);
					visibleEdge = false;
				} else if (zoneName.contains("River")) {
					MapGenerator.generateWater(currentZone, 7);
					MapGenerator.generateGrass(currentZone);
					visibleEdge = false;
				} else if (zoneName.contains("Ocean") || zoneName.contains("Sea")) {
					MapGenerator.generateWater(currentZone, 15);
					MapGenerator.generateGrass(currentZone);
					visibleEdge = false;
				} else if (zoneName.contains("Field") ) {
					MapGenerator.generateGrass(currentZone);
					visibleEdge = false;
				} else if (zoneName.contains("Village") || zoneName.contains("City") || zoneName.contains("Town")) {
					MapGenerator.generateHouse(currentZone, 8);
				}
				
				MapGenerator.generateEdgeTiles(currentZone,visibleEdge);

				//MapGenerator.generateNonPlayerEntities(currentZone, entityDensity, friendly);

				MapGenerator.generatePlayer(currentZone, currentDepth);

				IntegerPair nextExit = new IntegerPair(prevExitX, prevExitY);

				if (!exitsToClear.contains(nextExit)) {
					exitsToClear.add(nextExit);
					numberOfCreatedExits++;
				}
				checkNumbersBeforeClearing(currentZone);

			}
		}

	}

	private static void generateForest(Zone zone, int forestDensity) {
		// placeholder
		int count = 0;
		int numberOfEntities = (int) ((zone.sizeX - 4) * (zone.sizeY - 4) * forestDensity / 100);

		while (count < numberOfEntities) {

			final Random rn = new Random();
			int randX = zone.x + 2 + rn.nextInt(zone.sizeX - 2);
			int randY = zone.y + 2 + rn.nextInt(zone.sizeY - 2);

			String pos = randX + "," + randY;
			if (!entities.containsKey(pos)) {
				entities.put(pos, "Tile");
				entityStrings.add("Tile;;" + pos + ";1;4;2;");
				count++;
			}
		}
	}
	
	private static void generateHouse(Zone zone, int houseDensity) {
		// placeholder
		int count = 0;
		int numberOfEntities = (int) ((zone.sizeX - 4) * (zone.sizeY - 4) * houseDensity / 100);

		while (count < numberOfEntities) {

			final Random rn = new Random();
			int randX = zone.x + 2 + rn.nextInt(zone.sizeX - 2);
			int randY = zone.y + 2 + rn.nextInt(zone.sizeY - 2);

			String pos = randX + "," + randY;
			if (!entities.containsKey(pos)) {
				entities.put(pos, "Tile");
				entityStrings.add("Tile;;" + pos + ";1;8;7;enterLevel(0 30 30)");
				count++;
			}
		}
	}

	private static void generateWater(Zone zone, int waterDensity) {
		// placeholder
		int count = 0;
		int numberOfEntities = (int) ((zone.sizeX - 4) * (zone.sizeY - 4) * waterDensity / 100);

		while (count < numberOfEntities) {

			final Random rn = new Random();
			int randX = zone.x + 2 + rn.nextInt(zone.sizeX - 2);
			int randY = zone.y + 2 + rn.nextInt(zone.sizeY - 2);

			String pos = randX + "," + randY;
			if (!entities.containsKey(pos)) {
				entities.put(pos, "Tile");
				entityStrings.add("Tile;;" + pos + ";1;6;6;");
				count++;
			}
		}
	}
	
	private static void generateGrass(Zone zone) {
		for(int i = 1; i < zone.sizeX; i++) {
			for(int j = 1; j < zone.sizeY; j++) {
				String pos = (i+zone.x) + "," + (j+zone.y);
				if (!entities.containsKey(pos)) {
					//entities.put(pos, "Tile");
					entityStrings.add("Tile;grass;" + pos + ";0;6;2;");
				}
			}
		}
	}

	private static void checkNumbersBeforeClearing(Zone currentZone) {
		if (DEBUG) {
			for (ArrayList<IntegerPair> edge : getAllEdges(currentZone)) {
				int exitcount = 0;
				int tilesonedgemap = 0;
				int tilesonedgestr = 0;

				ArrayList<String> edgeStrs = new ArrayList<>();

				for (IntegerPair pointonedge : edge) {
					for (IntegerPair exit : exitsToClear) {
						if (exit.x == pointonedge.x && exit.y == pointonedge.y) {
							exitcount++;
						}
					}
					if (entities.get((pointonedge.x + "," + pointonedge.y)).equals("Tile")) {
						tilesonedgemap++;
					}
					for (String entity : entityStrings) {
						if (entity.startsWith("Tile;;" + pointonedge.x + "," + pointonedge.y + ";")) {
							tilesonedgestr++;
							edgeStrs.add(entity);
						}
					}

				}
				if (exitcount > 1) {
					System.out.println("Warning: edge contains multiple exits.");
				}
				if (tilesonedgemap != edge.size()) {
					System.out.println(
							"Expected " + edge.size() + " tiles on this edge from map. Found: " + tilesonedgemap);
				}
				if (tilesonedgestr != edge.size()) {
					System.out.println(
							"Expected " + edge.size() + " tiles on this edge from list. Found: " + tilesonedgestr);
				}
				if (tilesonedgestr != tilesonedgemap) {
					System.out.println("Generated a different number of tiles than expected");
					if (tilesonedgestr < tilesonedgemap)
						System.out.println("Fewer tiles generated than found in map. Generated: " + tilesonedgestr
								+ ", Expected: " + tilesonedgemap);
					if (tilesonedgestr > tilesonedgemap)
						System.out.println("More tiles generated than found in map. Generated: " + tilesonedgestr
								+ ", Expected: " + tilesonedgemap);
				}
				if (tilesonedgestr == tilesonedgemap) {
					// System.out.println("Generated a correct number of
					// tiles");
				}
			}
			if (numberOfCreatedExits != exitsToClear.size()) {
				System.out
						.println("Created " + numberOfCreatedExits + " but cleared " + exitsToClear.size() + " tiles.");
			}
		}
	}

	private static void checkNumbersAfterClearing(Zone currentZone) {
		if (DEBUG) {
			for (ArrayList<IntegerPair> edge : getAllEdges(currentZone)) {
				int exitcount = 0;
				int tilesonedgemap = 0;
				int tilesonedgestr = 0;
				for (IntegerPair pointonedge : edge) {
					for (IntegerPair exit : exitsToClear) {
						if (exit.x == pointonedge.x && exit.y == pointonedge.y) {
							// System.out.println("Found exit on edge");
							exitcount++;
						}
					}
					if (entities.containsKey((pointonedge.x + "," + pointonedge.y))) {
						tilesonedgemap++;
					}
					for (String entity : entityStrings) {
						if (entity.startsWith("Tile;;" + pointonedge.x + "," + pointonedge.y + ";")) {
							tilesonedgestr++;
						}
					}

				}
				if (tilesonedgestr != tilesonedgemap) {
					System.out.println("Generated a different number of tiles than expected");
					if (tilesonedgestr < tilesonedgemap)
						System.out.println("Fewer tiles generated than found in map. Generated: " + tilesonedgestr
								+ ", Expected: " + tilesonedgemap);
					if (tilesonedgestr > tilesonedgemap) {
						System.out.println("More tiles generated than found in map. Generated: " + tilesonedgestr
								+ ", Expected: " + tilesonedgemap);
					}

				} else if (exitcount != (edge.size() - tilesonedgemap)) {
					System.out.println("Warning: edge contains multiple exits. (1)");
				} else if (exitcount != (edge.size() - tilesonedgestr)) {
					System.out.println("Warning: edge contains multiple exits. (2)");
				}
				if (tilesonedgemap != edge.size() - exitcount) {
					System.out.println(
							"Expected " + (edge.size() - exitcount) + " tiles on this edge. Found: " + tilesonedgemap);
				}
				if (numberOfCreatedExits != exitsToClear.size()) {
					System.out.println(
							"Created " + numberOfCreatedExits + " but cleared " + exitsToClear.size() + " tiles.");
				}
			}
		}
	}

	private static ArrayList<String> getDuplicateTilesInList() {
		ArrayList<String> duplicate = new ArrayList<>();
		int l = entityStrings.size();
		for (int i = 0; i < l; i++) {
			for (int j = 0; j < l; j++) {
				if (i != j && entityStrings.get(i).equals(entityStrings.get(j))) {
					duplicate.add(entityStrings.get(i));
				}
			}
		}

		return duplicate;
	}

	private static ArrayList<ArrayList<IntegerPair>> getAllEdges(Zone zone) {
		ArrayList<ArrayList<IntegerPair>> allEdges = new ArrayList<>();

		for (Dir direction : Dir.values()) {
			allEdges.add(zone.getEdge(direction));
		}

		return allEdges;
	}

	private static int getLimit(Zone zone, int sizeX, int sizeY, Dir dir) {
		switch (dir) {
		case NORTH:
			return sizeY;
		case EAST:
			return sizeX;
		case SOUTH:
			return sizeY;
		case WEST:
			return sizeX;
		default:
			return 0;
		}

	}

	private static boolean zoneOverLaps(Zone zone, int startX, int startY, int sizeX, int sizeY) {
		Rectangle r1 = new Rectangle(zone.x, zone.y, zone.sizeX, zone.sizeY);
		Rectangle r2 = new Rectangle(startX, startY, sizeX, sizeY);
		// return (r1.intersects(r2) || r1.contains(r2) || r1.equals(r2));
		return (r1.intersects(r2));
	}

	private static IntegerPair getNewStartCoords(Zone currentZone, int nextZoneSizeX, int nextZoneSizeY, Dir dir) {
		IntegerPair ip;
		switch (dir) {
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
		int rand1 = xlimlower + 2 * rn.nextInt((xlimupper - xlimlower) / 2);
		int rand2 = ylimlower + 2 * rn.nextInt((ylimupper - ylimlower) / 2);

		return new IntegerPair(rand1, rand2);
	}

	private static boolean isAreaFriendly(int proportionOfFriendlyAreas) {
		final Random rn = new Random();
		int rand = rn.nextInt(100);
		if (rand < proportionOfFriendlyAreas)
			return true;
		return false;
	}

	private static IntegerPair getRandomPointOnEdge(ArrayList<IntegerPair> edge, int limit) {
		final Random rn = new Random();
		int rand = 0;
		if (edge.size() <= limit || limit <= 0) {
			rand = 1 + rn.nextInt(edge.size() - 2);
		} else {
			rand = 1 + rn.nextInt(limit - 1);
		}
		return edge.get(rand);
	}

	private static Dir getExcludeDir(Dir dir) {
		switch (dir) {
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

	private static void clearExits() {

		for (IntegerPair exit : exitsToClear) {
			String exitCoords = exit.x + "," + exit.y;
			entities.remove(exitCoords);

			Iterator<String> it = entityStrings.iterator();

			while (it.hasNext()) {
				String entity = it.next();
				if (entity.startsWith("Tile;;" + exitCoords + ";")) {
					it.remove();
				}
			}
			entityStrings.add(0,"Tile;grass;" + exitCoords + ";0;6;2;");
		}
	}

	private static void clearNextToExits() {

		for (IntegerPair exit : nextToExits) {
			String exitCoords = exit.x + "," + exit.y;
			if (entities.get(exitCoords) != null && entities.get(exitCoords).equals("Tile"))
				entities.remove(exitCoords);

			Iterator<String> it = entityStrings.iterator();

			while (it.hasNext()) {
				String entity = it.next();
				if (entity.startsWith("Tile;;" + exitCoords + ";")) {
					it.remove();
				}
			}
		}
	}

	// generate entities in specific zone
	public static void generateNonPlayerEntities(Zone zone, double entityDensity, int friendly) {
		// placeholder
		int count = 0;
		// int numberOfEntities = (int) ((zone.sizeX - 2) * (zone.sizeY - 2) *
		// entityDensity / 100f);

		int zoneSize = (zone.sizeX - 2) * (zone.sizeY - 2);
		while (count < zoneSize) {

			final Random rn = new Random();

			double makeEntity = rn.nextDouble();
			if (makeEntity < entityDensity) {
				while (true) {
					int randX = zone.x + 2 + rn.nextInt(zone.sizeX - 2);
					int randY = zone.y + 2 + rn.nextInt(zone.sizeY - 2);
	
					String pos = randX + "," + randY;
					if (!entities.containsKey(pos)) {
						entities.put(pos, "NPC");
						if (friendly == 0)
							entityStrings.add("NPC;" + NameGenerator.generateRandomName() + ";" + pos
									+ ";1;2;1;battle;randomAI;displayDialogue(0);");
						else
							entityStrings.add("NPC;" + NameGenerator.generateRandomName() + ";" + pos
									+ ";1;2;2;;randomAI;displayDialogue(0);");
						
						break;
					}
				}
			}
			count++;
		}
	}

	public static void generatePlayer(Zone zone, int currentDepth) {
		if (currentDepth == 0) {
			// placeholder
			boolean playerAdded = false;
			final Random rn = new Random();

			while (!playerAdded) {
				int randX = zone.x + 1 + rn.nextInt(zone.sizeX - 1);
				int randY = zone.y + 1 + rn.nextInt(zone.sizeY - 1);

				String pos = randX + "," + randY;
				if (!entities.containsKey(pos)) {
					entities.put(pos, "Player");
					entityStrings
							.add("Player;" + NameGenerator.generateRandomName() + ";" + pos + ";1;3;6;;playerControl;");
					playerAdded = true;
				}
			}
		}
	}

	// prints all the generated entities to randommap.txt
	public static void printToMap() {
		try {
			PrintWriter pw = new PrintWriter(new FileOutputStream("output/maps/randommap.txt"));
			// place holder, should print appropriate level id
			pw.println("0");
			
			for (int i = 0; i < entityStrings.size(); i++) {
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
