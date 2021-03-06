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

import rpggame.ZoneObsolete.Dir;

public class MapGeneratorObsolete {
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
	
	public static int worldID = 0;
	
	public static void generate(int i, int j, int k, double d) {
		World mainWorld = new World(worldID++);
		mainWorld.generate(i, j, k, d);
		
	}

	static class World {
		int id;
		HashMap<String, String> entities = new HashMap<>(10000000);
		ArrayList<String> entityStrings = new ArrayList<>();
		ArrayList<ZoneObsolete> createdZones = new ArrayList<>();
		ArrayList<IntegerPair> exitsToClear = new ArrayList<>();
		ArrayList<IntegerPair> nextToExits = new ArrayList<>();
		ArrayList<IntegerPair> doNotClear = new ArrayList<>();
		int numberOfCreatedExits = 0;
		private ArrayList<World> worldsToPrintAfterThis = new ArrayList<>();
		
		public World(int id) {
			this.id = id;
		}

		public void generateEdgeTiles(ZoneObsolete zone, boolean visible) {
			for (ArrayList<IntegerPair> edge : getAllEdges(zone)) {
				for (IntegerPair ip : edge) {
					generateSolidTile(ip.x, ip.y, visible);
				}
			}
		}

		public IntegerPair getSizeForZone(String name) {
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

		public void generateSolidTile(int x, int y, boolean visible) {
			String pos = x + "," + y;
			// if (!entities.containsKey(pos)) {
			entities.put(pos, "Tile");
			if (visible)
				entityStrings.add("Tile;;" + pos + ";1;1;");
			else
				entityStrings.add(0, "Tile;;" + pos + ";1;6;");
			// }
		}

		public void generate(int sizeX, int sizeY, int maximumDepth, double entityDensity) {
			int currentDepth = 0;
			System.out.println("Before clearing exits:");
			generateMap(50, 50, sizeX, sizeY, currentDepth, maximumDepth,null, entityDensity, null, null);
			clearExits();
			clearNextToExits();
			printToMap();

			System.out.println("After clearing exits:");
			for (ZoneObsolete zone : createdZones) {
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

		public void generateLabyrinth(int x, int y, int sizeX, int sizeY) {
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

		public void recursiveLabyrinth(HashMap<IntegerPair, IntegerPair> labyrinth, int currentX, int currentY,
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

		public boolean generateMap(int startX, int startY, int sizeX, int sizeY, int currentDepth, int maximumDepth,
				IntegerPair prevExit, double entityDensity, Dir excludeDir, String zoneName) {
			
			if (currentDepth > maximumDepth) {
				return false;
			}

			boolean canCreateZone = true;
			if (createdZones.isEmpty())
				canCreateZone = true;

			for (ZoneObsolete createdZone : createdZones) {
				if (zoneOverLaps(createdZone, startX, startY, sizeX, sizeY)) {
					canCreateZone = false;
				}
			}
			
			if (!canCreateZone)
				return false;

			int friendly = 0;
			ZoneObsolete currentZone;
			if (isAreaFriendly(10) || currentDepth == 0)
				friendly = 1;
			if (zoneName == null)
				zoneName = NameGenerator.generateRandomPlaceName();
			entityStrings.add("Zone;" + zoneName + ";" + startX + "," + startY + ";" + (startX + sizeX) + ","
					+ (startY + sizeY) + ";" + friendly + ";");
			if(friendly == 1)
				currentZone = new ZoneObsolete(startX, startY, sizeX, sizeY,true);
			else
				currentZone = new ZoneObsolete(startX, startY, sizeX, sizeY,false);
			createdZones.add(currentZone);
			boolean visibleEdge = true;
			
			ArrayList<IntegerPair> exitsFromThisZone = new ArrayList<>();

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
						if(! ((zoneName.contains("Labyrinth") || zoneName.contains("Cave")) && friendly == 0))
							nextToExits.add(nextToExitPoint1);
						nextToExits.add(nextToExitPoint2);
					}

					if (currentDepth != maximumDepth) {
						boolean createdNext = generateMap(nextCoords.x, nextCoords.y, nextSize.x, nextSize.y, currentDepth + 1,
								maximumDepth, exitPoint, entityDensity, getExcludeDir(direction),
								nextZoneName);
						
						if (createdNext) {
							exitsFromThisZone.add(exitPoint);
						}
					}
				}

			}
			
			if(prevExit != null)
				exitsFromThisZone.add(prevExit);
			
			for(IntegerPair nextExit : exitsFromThisZone) {
				if (!exitsToClear.contains(nextExit)) {
					exitsToClear.add(nextExit);
					numberOfCreatedExits++;
				}
			}

			generateEdgeTiles(currentZone, visibleEdge);

			if ((zoneName.contains("Labyrinth") || zoneName.contains("Cave")) && friendly == 0) {
				//generateLabyrinth(currentZone.x, currentZone.y, currentZone.sizeX, currentZone.sizeY);
				generateCave(currentZone,exitsFromThisZone,zoneName);
			} else if (zoneName.contains("Forest")) {
				generateForest(currentZone, 10);
				generateGrass(currentZone);
				visibleEdge = false;
			} else if (zoneName.contains("River")) {
				generateWater(currentZone, 7);
				generateGrass(currentZone);
				visibleEdge = false;
			} else if (zoneName.contains("Ocean") || zoneName.contains("Sea")) {
				generateWater(currentZone, 15);
				generateGrass(currentZone);
				visibleEdge = false;
			} else if (zoneName.contains("Field")) {
				generateGrass(currentZone);
				visibleEdge = false;
			} else if (zoneName.contains("Village") || zoneName.contains("City") || zoneName.contains("Town")) {
				//generateHouse(currentZone, 8);
			}
			
			if(! ((zoneName.contains("Labyrinth") || zoneName.contains("Cave")) && friendly == 0))
				generateNonPlayerEntities(currentZone, entityDensity, friendly);

			generatePlayer(currentZone, currentDepth);
			
			checkNumbersBeforeClearing(currentZone);
			
			return true;
		}
		
		private void removeFromExitList(ArrayList<IntegerPair> notexits) {
			for(IntegerPair notexit : notexits) {
				if(entities.containsKey(notexit.x+","+notexit.y)) {
					Iterator<IntegerPair> it = exitsToClear.iterator();
					IntegerPair existingexit;
					while(it.hasNext()) {
						existingexit = it.next();
						if(notexit.equals(existingexit)) {
							it.remove();
							break;
						}
					}
				}
			}
		}

		private void generateForest(ZoneObsolete zone, int forestDensity) {
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
					entityStrings.add("Tile;;" + pos + ";1;5;");
					count++;
				}
			}
		}

		private void generateHouse(ZoneObsolete zone, int houseDensity) {
			// placeholder
			int count = 0;
			int numberOfHus = (int) ((zone.sizeX - 4) * (zone.sizeY - 4) * houseDensity / 100);

			while (count < numberOfHus) {

				final Random rn = new Random();
				int randX = zone.x + 2 + rn.nextInt(zone.sizeX - 2);
				int randY = zone.y + 2 + rn.nextInt(zone.sizeY - 2);

				String pos = randX + "," + randY;
				if (!entities.containsKey(pos)) {
					entities.put(pos, "Tile");
					entityStrings.add("Tile;;" + pos + ";1;3;enterLevel("+worldID+" 0 1)");
					count++;
				}
				World houseWorld = new World(worldID++);
				houseWorld.generateHouseWorld(10, 10, 1, this.id, randX + 1, randY, zone);
				houseWorld.printToMap();
				//worldsToPrintAfterThis.add(houseWorld);
			}
		}
		
		private void generateHouseWorld(int w, int h, int d, int returnWorldId, int returnX, int returnY,ZoneObsolete zone) {
			ZoneObsolete currentZone = new ZoneObsolete(0, 0, w, h, zone.friendly);
			generateEdgeTiles(currentZone,true);
			int friendly = 1;
			if(!zone.friendly) friendly = 0;
			generateNonPlayerEntities(currentZone,0.07,friendly);
			
			clearTile(0,1);
			entityStrings.add("Tile;;" + -1+","+1 + ";1;0;enterLevel(" + returnWorldId + " " + returnX + " " + returnY + ")");
			entityStrings.add("Zone;House of " +NameGenerator.generateRandomName()+ ";" + 0 + "," + 0 + ";" + w + ","
					+ h + ";" + friendly + ";");
			
		}
		
		private void generateCave(ZoneObsolete zone, ArrayList<IntegerPair> exits, String zoneName ) {
			String pos = "";
			
			for(int i = 1; i < zone.sizeX; i++) {
				for(int j = 1; j < zone.sizeY; j++) {
					pos = (zone.x+i)+","+(zone.y+j);
					entities.put(pos, "Tile");
					entityStrings.add("Tile;;" + pos + ";1;1;");
				}
			}
			
			for(IntegerPair exit : exits) {
				doNotClear.add(exit);
				clearTile(exit.x,exit.y);
				entities.put(exit.x+","+exit.y,"Tile");
				entityStrings.add("Tile;;" + exit.x+","+exit.y + ";1;3;enterLevel(" + worldID + " " + (exit.x-zone.x) + " " + (exit.y-zone.y) + ")");
			}
			World caveWorld = new World(worldID++);
			caveWorld.generateCaveWorld(zone.sizeX, zone.sizeY, 1, this.id, exits, zone, zoneName);
			caveWorld.printToMap();
		}
		
		private void generateCaveWorld(int w, int h, int d, int returnWorldId,ArrayList<IntegerPair> entrances,ZoneObsolete zone, String zoneName) {
			ZoneObsolete currentZone = new ZoneObsolete(0,0,w,h,zone.friendly);
			generateEdgeTiles(currentZone,true);
			generateLabyrinth(currentZone.x, currentZone.y, currentZone.sizeX, currentZone.sizeY);
			int friendly = 1;
			if(!zone.friendly) friendly = 0;
			
			generateNonPlayerEntities(currentZone,0.03,friendly);
			
			entityStrings.add("Zone;"+zoneName+ ";" + 0 + "," + 0 + ";" + w + ","
					+ h + ";" + friendly + ";");
			for(IntegerPair entrance : entrances) {
				clearTile(entrance.x - zone.x, entrance.y - zone.y);
				entityStrings.add("Tile;;" +(entrance.x - zone.x) +","+ (entrance.y - zone.y) + ";1;0;enterLevel(" + returnWorldId + " " + entrance.x + " " + entrance.y + ")");
			}
			
		}

		private void generateWater(ZoneObsolete zone, int waterDensity) {
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
					entityStrings.add("Tile;water;" + pos + ";1;9;");
					count++;
				}
			}
		}

		private void generateGrass(ZoneObsolete zone) {
			for (int i = 1; i < zone.sizeX; i++) {
				for (int j = 1; j < zone.sizeY; j++) {
					String pos = (i + zone.x) + "," + (j + zone.y);
					if (!entities.containsKey(pos)) {
						entityStrings.add("Tile;grass;" + pos + ";0;4;");
					}
				}
			}
		}

		private void checkNumbersBeforeClearing(ZoneObsolete currentZone) {
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
					System.out.println(
							"Created " + numberOfCreatedExits + " but cleared " + exitsToClear.size() + " tiles.");
				}
			}
		}

		private void checkNumbersAfterClearing(ZoneObsolete currentZone) {
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
						System.out.println("Expected " + (edge.size() - exitcount) + " tiles on this edge. Found: "
								+ tilesonedgemap);
					}
					if (numberOfCreatedExits != exitsToClear.size()) {
						System.out.println(
								"Created " + numberOfCreatedExits + " but cleared " + exitsToClear.size() + " tiles.");
					}
				}
			}
		}

		private ArrayList<String> getDuplicateTilesInList() {
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

		private ArrayList<ArrayList<IntegerPair>> getAllEdges(ZoneObsolete zone) {
			ArrayList<ArrayList<IntegerPair>> allEdges = new ArrayList<>();

			for (Dir direction : Dir.values()) {
				allEdges.add(zone.getEdge(direction));
			}

			return allEdges;
		}

		private int getLimit(ZoneObsolete zone, int sizeX, int sizeY, Dir dir) {
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

		private boolean zoneOverLaps(ZoneObsolete zone, int startX, int startY, int sizeX, int sizeY) {
			Rectangle r1 = new Rectangle(zone.x, zone.y, zone.sizeX, zone.sizeY);
			Rectangle r2 = new Rectangle(startX, startY, sizeX, sizeY);
			// return (r1.intersects(r2) || r1.contains(r2) || r1.equals(r2));
			return (r1.intersects(r2));
		}

		private IntegerPair getNewStartCoords(ZoneObsolete currentZone, int nextZoneSizeX, int nextZoneSizeY, Dir dir) {
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

		private IntegerPair getNextSize(int xlimlower, int ylimlower, int xlimupper, int ylimupper) {
			final Random rn = new Random();
			int rand1 = xlimlower + 2 * rn.nextInt((xlimupper - xlimlower) / 2);
			int rand2 = ylimlower + 2 * rn.nextInt((ylimupper - ylimlower) / 2);

			return new IntegerPair(rand1, rand2);
		}

		private boolean isAreaFriendly(int proportionOfFriendlyAreas) {
			final Random rn = new Random();
			int rand = rn.nextInt(100);
			if (rand < proportionOfFriendlyAreas)
				return true;
			return false;
		}

		private IntegerPair getRandomPointOnEdge(ArrayList<IntegerPair> edge, int limit) {
			final Random rn = new Random();
			int rand = 0;
			if (edge.size() <= limit || limit <= 0) {
				rand = 1 + rn.nextInt(edge.size() - 2);
			} else {
				rand = 1 + rn.nextInt(limit - 1);
			}
			return edge.get(rand);
		}

		private Dir getExcludeDir(Dir dir) {
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

		private void clearExits() {

			for (IntegerPair exit : exitsToClear) {
				if(!doNotClear.isEmpty()) {
					for(IntegerPair dont : doNotClear) {
						if(!dont.equals(exit)) {
							String exitCoords = exit.x + "," + exit.y;
							clearTile(exit.x,exit.y);
							entityStrings.add(0, "Tile;grass;" + exitCoords + ";0;4;");
						}
					}
				} else {
					String exitCoords = exit.x + "," + exit.y;
					clearTile(exit.x,exit.y);
					entityStrings.add(0, "Tile;grass;" + exitCoords + ";0;4;");
				}
			}
		}
		
		private void clearTile(int x, int y) {
			String exitCoords = x + "," + y;
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
		
		private void clearNextToExits() {

			for (IntegerPair exit : nextToExits) {
				clearTile(exit.x,exit.y);
			}
		}

		// generate entities in specific zone
		public void generateNonPlayerEntities(ZoneObsolete zone, double entityDensity, int friendly) {
			// placeholder
			int count = 0;
			// int numberOfEntities = (int) ((zone.sizeX - 2) * (zone.sizeY - 2)
			// *
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
										+ ";1;7;battle;randomAI(2000000000);displayDialogue(0);");
							else
								entityStrings.add("NPC;" + NameGenerator.generateRandomName() + ";" + pos
										+ ";1;8;;randomAI(2000000000);displayDialogue(0);");

							break;
						}
					}
				}
				count++;
			}
		}

		public void generatePlayer(ZoneObsolete zone, int currentDepth) {
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
						entityStrings.add(
								"Player;" + NameGenerator.generateRandomName() + ";" + pos + ";1;2;;playerControl;");
						playerAdded = true;
					}
				}
			}
		}

		// prints all the generated entities to randommap.txt
		public void printToMap() {
			try {
				
				PrintWriter pw = new PrintWriter(new FileOutputStream("output/maps/randommap.txt", true));
				
				worldsToPrintAfterThis.add(0, this);
				for (World world : worldsToPrintAfterThis) {
					pw.println(world.id);

					for (int i = 0; i < world.entityStrings.size(); i++) {
						pw.println(world.entityStrings.get(i));
						pw.flush();
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
}
