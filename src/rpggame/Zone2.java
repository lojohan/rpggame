package rpggame;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import rpggame.MapGenerator2.World;

public class Zone2 {
	public static interface Layer {
		int FOREGROUND = 0, MIDDLE = 1, BACKGROUND = 2;
	}
	
	public static interface Direction {
		int NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3;
	}
	
	// List containing previously generated zones in the same world as this zone
	ArrayList<Zone2> zonesInWorld = new ArrayList<>();
	
	// List containing the rectangles which make up this zone.
	ArrayList<Rectangle> rects = new ArrayList<>();
	
	// The name of this zone.
	String name = "";
	
	// Maps coordinates to a list of entities on that coordinate.
	HashMap<IntegerPair, ArrayList<String> > entities = new HashMap<>();
	
	// List containing the edges of the current zone.
	ArrayList<Edge> edges = new ArrayList<>();
	
	// List containing the exits from this zone.
	ArrayList<IntegerPair> exits = new ArrayList<>();
	
	// List of the strings containing the coordinates of this zone which can be parsed the game.
	ArrayList<String> zone = new ArrayList<>();
	
	// List of tiles which solid entities cannot be spawned on.
	ArrayList<IntegerPair> nonBuildable = new ArrayList<>();
	
	// Whether or not this zone is considered 'friendly'.
	boolean friendly;

	//private int worldID;
	private World world;

	/**
	 * Generates an empty zone.
	 * @param name - name of the zone.
	 * @param zonesInWorld - list of previously constructed zones.
	 */
	public Zone2(String name, ArrayList<Zone2> zonesInWorld, World world) {
		this.name = name;
		this.zonesInWorld = zonesInWorld;
		this.world = world;
	}
	
	/**
	 * Checks whether a new rectangle can be added to this zone and
	 * does so if possible.
	 * @param x - x of the new rectangle.
	 * @param y - y of the new rectangle.
	 * @param w - width of the new rectangle.
	 * @param h - height of the new rectangle.
	 * @return
	 */
	public boolean addRectangle(int x, int y, int w, int h) {
		Rectangle tmpRect = new Rectangle(x,y,w,h);
		if(canAddRectangle(tmpRect)) {
			this.rects.add(tmpRect);
			updateEdgeList(tmpRect);
			return true;
		}
		return false;
	}
	
	/**
	 * Checks whether a new rectangle can be added to this zone,
	 * attached to appropriate edge, and does so if possible.
	 * @param x - x of the new rectangle.
	 * @param y - y of the new rectangle.
	 * @param w - width of the new rectangle.
	 * @param h - height of the new rectangle.
	 * @param edge - edge to attach the rectangle to.
	 * @return
	 */
	public boolean addRectangleToEdge(int x, int y, int w, int h, Edge edge) {
		Rectangle tmpRect = new Rectangle(x,y,w,h);
		if(canAddRectangleToEdge(tmpRect,edge)) {
			this.rects.add(tmpRect);
			updateEdgeList(tmpRect);
			return true;
		}
		return false;
	}
	
	/**
	 * Sets whether this zone is considered friendly.
	 * @param friendly
	 */
	public void setFriendly(boolean friendly) {
		this.friendly = friendly;
	}
	
	/**
	 * Adds a solid tile representing a wall to this zone.
	 * @param ip - IntegerPair containing the coordinates of the tile to be added.
	 */
	public void addWallTile(IntegerPair ip, int direction) {
		addStringToEntities(ip,
				generateEntityString(
						"Tile","wall",ip.x,ip.y,true,1,
						Layer.MIDDLE, direction,new String[]{},new String[][]{{}},new String[]{},new String[][]{{}}, new String[]{}, new String[][]{{}}));
	}
	
	public void addWaterTile(IntegerPair ip) {
		addStringToEntities(ip,
				generateEntityString(
						"Tile","water",ip.x,ip.y,true,9,
						Layer.MIDDLE, Direction.NORTH, new String[]{},new String[][]{{}},new String[]{},new String[][]{{}}, new String[]{}, new String[][]{{}}));
	}
	
	public void addTreeTile(IntegerPair ip) {
		addStringToEntities(ip,
				generateEntityString(
						"Tile","tree",ip.x,ip.y,true,5,
						Layer.MIDDLE, Direction.NORTH, new String[]{},new String[][]{{}},new String[]{},new String[][]{{}}, new String[]{}, new String[][]{{}}));
	}
	
	/**
	 * Adds a solid entity representing the player to this zone.
	 * @param ip - IntegerPair containing the coordinates of the entity to be added.
	 */
	public void addPlayer(IntegerPair ip, String name) {
		addStringToEntities(ip,
				generateEntityString(
						"Player",name,ip.x,ip.y,true,2,
						Layer.MIDDLE, Direction.NORTH, new String[]{},new String[][]{{}},new String[]{"playerControl"},new String[][]{{}}, new String[]{}, new String[][]{{}}));
	}
	
	public void addFriendlyNPC(IntegerPair ip, String name) {
		addStringToEntities(ip,
				generateEntityString(
						"NPC",name,ip.x,ip.y,true,8,
						Layer.MIDDLE, Direction.NORTH, new String[]{}, new String[][]{{}}, new String[]{"randomAI"}, 
						new String[][]{{"2000000000"}}, new String[]{"displayDialogue"}, new String[][]{{"0"}}));
	}
	
	public void addEnemyNPC(IntegerPair ip, String name) {
		addStringToEntities(ip,
				generateEntityString(
						"NPC",name,ip.x,ip.y,true,7,
						Layer.MIDDLE, Direction.NORTH, new String[]{"battle"}, new String[][]{{}}, new String[]{"randomAI"}, 
						new String[][]{{"2000000000"}}, new String[]{"displayDialogue"}, new String[][]{{"0"}}));
	}
	
	/**
	 * Adds non-solid tile representing grass to this zone.
	 * @param ip - IntegerPair containing the coordinates of the tile to be added.
	 */
	public void addNonSolidGrass(IntegerPair ip, String name) {
		addStringToEntities(ip,
				generateEntityString(
						"Tile",name,ip.x,ip.y,false,4,
						Layer.BACKGROUND, Direction.NORTH, new String[]{},new String[][]{{}},new String[]{},new String[][]{{}}, new String[]{}, new String[][]{{}}));
	}
	
	// TODO: should add more than just the door.
	// TODO: in desperate need of cleanup.
	public void addHouseTile(IntegerPair ip, String name) {
		
		world.incrementWorldID();
		
		addDoorTile(ip, new IntegerPair(1,1), name, World.getWorldID());
		
		World houseWorld = new World(World.getWorldID(), 0, 0);
		
		Zone2 houseZone = new Zone2("House", houseWorld.zones, houseWorld);
		
		houseZone.setFriendly(this.friendly);
		
		houseZone.generateFirstRectangle(0, 0, 10, 10, null);
		houseZone.exits.add(new IntegerPair(0,1));
		
		houseWorld.generateNPCs(houseZone, 0);
		
		houseWorld.generateScenery(houseZone, 0);
		
		houseZone.addDoorTile(new IntegerPair(0,1), ip, "door", this.world.id);
		
		houseWorld.putEntityMap(houseZone);
		houseZone.addZones();
		
		houseWorld.addZoneToWorld(houseZone);
		
		MapGenerator2.worlds.add(houseWorld);
		
	}
	
	public void addDoorTile(IntegerPair ip1, IntegerPair ip2, String name, int doorToWorldID) {
		addStringToEntities(ip1,
				generateEntityString(
						"Tile",name,ip1.x,ip1.y,true,3,
						Layer.MIDDLE, Direction.NORTH, new String[]{"enterLevel"},new String[][]{{doorToWorldID+" "+ip2.x+" "+ip2.y}},new String[]{},new String[][]{{}}, new String[]{}, new String[][]{{}}));
	}
	
	/**
	 * Generates the strings representing this zone which can be parsed by the game.
	 */
	public void addZones() {
		for(Rectangle rect : this.rects) {
			zone.add("Zone;" + this.name + ";" + rect.x + "," + rect.y + ";" + (rect.x + rect.width) + ","
				+ (rect.y + rect.height) + ";" + ((friendly) ? 1 : 0) + ";");
		}
	}
	
	/**
	 * Generates grass on every coordinate of this zone.
	 */
	public void generateGrass() {
		Set<IntegerPair> allTiles = this.getAllCoordsInZone();
		
		for(IntegerPair tile : allTiles) {
			addNonSolidGrass(tile,"grass");
		}
	}
	
	public void generateWater(double entityDensity) {
		addSolidEntityRandom(entityDensity, "Water");
	}
	
	public void generateForest(double entityDensity) {
		addSolidEntityRandom(entityDensity, "Tree");
	}
	
	public Set<IntegerPair> generateHouses(double entityDensity) {
		return addSolidEntityRandom(entityDensity, "House");
	}
	
	/**
	 * Generates walls along the edges of this zone.
	 */
	public void generateWallTiles() {
		for(Edge edge : edges) {
			for(IntegerPair tile : edge.edge) {
				if(!exits.contains(tile))
					if(edge.isEdgeVertical())
						addWallTile(tile, Direction.WEST);
					else
						addWallTile(tile, Direction.NORTH);
			}
		}
	}
	
	public void generateCaveTiles() {
		Set<IntegerPair> allTiles = this.getAllCoordsInZone();
		// TODO: placeholder. will look like shit
		for(IntegerPair tile : allTiles) {
			addWallTile(tile, Direction.NORTH);
		}
	}
	
	public void generatePlayer() {
		IntegerPair[] coords = getAllCoordsInZone().toArray(new IntegerPair[getAllCoordsInZone().size()]);
		
		Random rand = new Random();
		
		int randi = rand.nextInt(coords.length);
		
		int count = 0;
		
		while(checkTileForSolid(coords[randi]) && count < coords.length) {
			count++;
		}
		
		this.addPlayer(new IntegerPair(coords[randi]), NameGenerator.generateRandomName());
	}
	
	public Set<IntegerPair> generateNPCs(double entityDensity) {
		if(this.friendly) return generateFriendlies(entityDensity);
		else return generateEnemies(entityDensity);
	}
	
	private Set<IntegerPair> generateFriendlies(double entityDensity) {
		return addSolidEntityRandom(entityDensity,"FriendlyNPC");
	}
	
	private Set<IntegerPair> generateEnemies(double entityDensity) {
		return addSolidEntityRandom(entityDensity,"EnemyNPC");
	}
	
	private Set<IntegerPair> addSolidEntityRandom(double entityDensity, String type) {
		
		Set<IntegerPair> addedEntities = new HashSet<>();
		
		final Random rn = new Random();
		
		Set<IntegerPair> allCoords = this.getAllCoordsInZone();
		
		for(IntegerPair ip : allCoords) {
			double makeEntity = rn.nextDouble();
			
			if(makeEntity < entityDensity) {
				if (!checkTileForSolid(ip)) {
					switch(type) {
					case "FriendlyNPC": 
						this.addFriendlyNPC(ip, NameGenerator.generateRandomName());
						addedEntities.add(ip);
						break;
					case "EnemyNPC": 
						this.addEnemyNPC(ip, NameGenerator.generateRandomName());
						addedEntities.add(ip);
						break;
					case "Water": 
						if(!this.nonBuildable.contains(ip)) {
							this.addWaterTile(ip);
							addedEntities.add(ip);
						}
						break;
					case "Tree":
						if(!this.nonBuildable.contains(ip)) {
							this.addTreeTile(ip);
							addedEntities.add(ip);
						}
						break;
					case "House":
					if(!this.nonBuildable.contains(ip)) {
						this.addHouseTile(ip, "House");
						addedEntities.add(ip);
					}
					break;
					}
				}
			}
		}
		return addedEntities;
	}
	
	public void randomFriendly(double pFriendly) {
		Random rand = new Random();
		int max = 10000;
		int pFriend = (int)(pFriendly * max);
		
		int randi = rand.nextInt(max);
		
		if(randi < pFriend) {
			this.setFriendly(true);
		} else {
			this.setFriendly(false);
		}
	}
	
	private boolean checkTileForSolid(IntegerPair ip) {
		if(!this.entities.containsKey(ip)) return false;
		ArrayList<String> entities = this.entities.get(ip);
		for(String entity : entities) {
			final String[] tokens = entity.split(Pattern.quote(";"));
			if(tokens[3] == "1") {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Generates all solid tiles and entities in this zone.
	 */
	public void generateBlockingScenery() {
		this.generateWallTiles();
		
		if(this.name.contains("River") || this.name.contains("Ocean") || this.name.contains("Sea")) {
			this.generateWater(0.1);
		} else if(this.name.contains("Forest")) {
			this.generateForest(0.15);
		} else if(this.name.contains("Village") || this.name.contains("City") || this.name.contains("Town")) {
			this.generateHouses(0.1);
		}
	}
	
	/**
	 * Generates all non-solid tiles and entities in this zone.
	 */
	public void generateNonBlockingScenery() {
		// TODO: should really check this based on the zones name
		//if(this.name.contains("Forest") || this.name.contains("Field")) {
			generateGrass();
		//}
	}
	
	/**
	 * Takes a string representing an entity and adds it to the map of all entities in this zone.
	 * @param coords - coordinates of the entity to be added.
	 * @param entity - string containing information about the entity to be added.
	 */
	public void addStringToEntities(IntegerPair coords, String entity) {
		if(!entities.containsKey(coords)) {
			entities.put(coords, new ArrayList<>(Arrays.asList(entity)));
		} else if(!entities.get(coords).contains(entity)){
			entities.get(coords).add(entity);
		}
	}
	
	/**
	 * Checks whether this zone overlaps with another zone.
	 * Zones are not considered to overlap if they share an edge.
	 * @param zone - zone to check if it overlaps with this zone.
	 * @return true if there is overlap, false otherwise.
	 */
	public boolean overlaps(Zone2 zone) {
		for(Rectangle rect1 : zone.rects) {
			for(Rectangle rect2 : this.rects) {
				if(rect1.intersects(rect2) || rect1.contains(rect2) || rect1.contains(rect1)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Generates a number of random rectangles and attaches them to this zone.
	 * The generated rectangles are considered to be part of this zone.
	 * @param n - number of rectangles to be generated.
	 * @param minwidth - minimum width of the generated rectangles.
	 * @param minheight - minimum height of the generated rectangles.
	 * @param maxwidth - maximum width of the generated rectangles.
	 * @param maxheight - maximum height of the generated rectangles.
	 */
	public void generateRandomRectangles(int n,int minwidth, int minheight, int maxwidth, int maxheight) {
		for(int i = 0; i < n; i++) {
			randomRect(minwidth, minheight, maxwidth, maxheight);
		}
	}
	
	/**
	 * Returns the coordinates of a potential exit-point from this zone.
	 * The exit-point cannot be on a corner and can only be on an edge 
	 * of sufficient length.
	 * @return the coordinates of a potential exit-point from this zone.
	 */
	public IntegerPair getPotentialExit() {
		IntegerPair exitPoint = getRandomPointOnEdgeExceptCorners(getRandomEdgeAtLeastSize(3));
		return exitPoint;
	}
	
	/**
	 * Generates a number of potential exits.
	 * @param numexits - number of exits to be generated.
	 */
	public void generateExits(int numexits) {
		for(int i = 0; i < numexits; i++) {
			IntegerPair exitPoint = getRandomPointOnEdgeExceptCorners(getRandomEdgeAtLeastSize(3));
			this.exits.add(exitPoint);
		}
	}
	
	/**
	 * Used when generating the first rectangle of this zone.
	 * Attempts to add it to the appropriate edge.
	 * @param x - x of the new rectangle.
	 * @param y - y of the new rectangle.
	 * @param w - width of the new rectangle.
	 * @param h - height of the new rectangle.
	 * @param edge - edge to attach the rectangle to.
	 * @return
	 */
	// TODO: should be able to guarantee that it is properly attached to edge of last zone
	public boolean generateFirstRectangle(int x, int y, int w, int h, Edge edge) {
		if(this.zonesInWorld.isEmpty()) return this.addRectangle(x, y, w, h);
		return this.tryToAddRectToEdge(x, y, w, h, edge);
	}
	
	/**
	 * Gets a random edge of appropriate size to put an exit-point on.
	 * @return a random edge of appropriate size to put an exit-point on.
	 */
	public Edge getRandomEdgeForExit() {
		return getRandomEdgeAtLeastSize(3);
	}
	
	/**
	 * Gets a random point on an edge to put an exit on.
	 * @param edge - edge to put an exit on.
	 * @return coordinates to put the exit on.
	 */
	public IntegerPair getRandomPointForExitOnEdge(Edge edge) {
		return getRandomPointOnEdgeExceptCorners(edge);
	}
	
	/**
	 * Gets the coordinates of all points in this zone excluding edges.
	 * @return the coordinates of all points in this zone excluding edges.
	 */
	public Set<IntegerPair> getAllCoordsInZone() {
		Set<IntegerPair> tmp = new HashSet<>();
		
		for(Rectangle rect : this.rects) {
			for(int x = rect.x; x <= rect.x +rect.width; x++) {
				for(int y = rect.y; y <= rect.y + rect.height; y++) {
					tmp.add(new IntegerPair(x,y));
				}
			}
		}
		
		for(Edge edge : this.edges) {
			tmp.removeAll(edge.getAllTilesOnEdge());
		}
		
		tmp.addAll(this.exits);
		
		return tmp;
	}
	
	public Set<IntegerPair> getNeighbours(IntegerPair ip) {
		Set<IntegerPair> neighbours = new HashSet<>();
		
		neighbours.add(new IntegerPair(ip.x,ip.y+1));
		neighbours.add(new IntegerPair(ip.x,ip.y-1));
		neighbours.add(new IntegerPair(ip.x+1,ip.y));
		neighbours.add(new IntegerPair(ip.x-1,ip.y));
		
		return neighbours;
	}
	
	/**
	 * Generates a random rectangle and attempts to attach it to this zone.
	 * @param minwidth - minimum width of the generated rectangle.
	 * @param minheight - minimum height of the generated rectangle.
	 * @param maxwidth - maximum width of the generated rectangle.
	 * @param maxheight - maximum height of the generated rectangle.
	 * @return true if generation was successful, false otherwise.
	 */
	private boolean randomRect(int minwidth, int minheight, int maxwidth, int maxheight) {
		Random rand = new Random();
		int randwidth = minwidth + rand.nextInt(maxwidth);

		int randheight = minheight + rand.nextInt(maxheight);
		
		IntegerPair startPoint = getRandomPointOnEdgeExceptCorners(getRandomEdgeAtLeastSize(3));
		
		
		return tryToAddRect(startPoint.x,startPoint.y,randwidth,randheight);
			
	}
	
	/**
	 * Attempts to add a rectangle to the appropriate edge.
	 * @param x - x of the rectangle.
	 * @param y - y of the rectangle.
	 * @param w - width of the rectangle.
	 * @param h - height of the rectangle.
	 * @param edge - edge to attach the rectangle to.
	 * @return
	 */
	// Attempts to attach rectangle to edge 'edge'
	private boolean tryToAddRectToEdge(int x, int y, int w, int h, Edge edge) {
		if( edge != null && !edge.isEmpty()) {		
			if(addRectangleToEdge(x,y,w,h,edge)) {
				return true;
			}
			
			if(edge.isEdgeVertical()) {	
				if(addRectangleToEdge(x-w,y,w,h,edge)) {
					return true;
				} else if(addRectangleToEdge(x+w,y,w,h,edge)){
					return true;
				}
			} else {
				if(addRectangleToEdge(x,y-h,w,h,edge)) {
					return true;
				} else if(addRectangleToEdge(x,y+h,w,h,edge)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Attempts to add a rectangle to this zone.
	 * @param x - x of the rectangle.
	 * @param y - y of the rectangle.
	 * @param w - width of the rectangle.
	 * @param h - height of the rectangle.
	 * @return
	 */
	private boolean tryToAddRect(int x, int y, int w, int h) {
		Random rand = new Random();

		ArrayList<int[]> tests = new ArrayList<>();
		int[][] testtmp = new int[][]{new int[]{x,y,w,h},
			{x-w,y,w,h},
			{x+w,y,w,h},
			{x,y-h,w,h},
			{x,y+h,w,h}};
			
		for(int i = 0; i < testtmp.length; i++) {
			tests.add(testtmp[i]);
		}
		
		while(tests.size() != 0) {
			int randind = rand.nextInt(tests.size());
			
			if(addRectangle(tests.get(randind)[0],tests.get(randind)[1],tests.get(randind)[2],tests.get(randind)[3])) {
				return true;
			} else {
				tests.remove(randind);
			}
		}
		return false;
	}
	
	/**
	 * Gets a random edge from this zone of at least size minsize.
	 * @param minsize - minimum size of the edge to be returned.
	 * @return an edge of appropriate size if it exists, empty edge otherwise.
	 */
	private Edge getRandomEdgeAtLeastSize(int minsize) {
		ArrayList<Edge> edgesOfSize = new ArrayList<>();
		
		for(Edge edge : this.edges) {
			if(edge.size() >= minsize) {
				edgesOfSize.add(edge);
			}
		}
		
		if(edgesOfSize.isEmpty()) return new Edge();
		
		Random rand = new Random();
		int rn = rand.nextInt(edgesOfSize.size());
		return edgesOfSize.get(rn);
		
	}
	
	/**
	 * Gets a random edge from this zone.
	 * @return a random edge from this zone.
	 */
	private Edge getRandomEdge() {
		Random rand = new Random();
		int rn = rand.nextInt(this.edges.size());
		return this.edges.get(rn);
	}
	
	
	
	/**
	 * Checks whether a rectangle can be added.
	 * @param rectangle
	 * @return true if it can be added, false otherwise.
	 */
	private boolean canAddRectangle(Rectangle rectangle) {
		
		if(this.zonesInWorld.isEmpty() && this.rects.isEmpty()) return true;
		
		boolean connectedToPreviousZone = false;
		
		// checks if adding this rectangle causes the zone to overlap with other zones
		for(Zone2 zone : zonesInWorld) {
			// TODO: should use inclusive once generation of first rectangle is fixed
			//if(overlapsInclusive(rectangle,zone) && zone != this)
			if(overlapsExclusive(rectangle,zone) && zone != this)
				return false;
			// TODO: rework to allow for separation by wall between zones
			if(!findCommonEdges(getEdges(rectangle), zone.edges).isEmpty()) {
				connectedToPreviousZone = true;
			}
		}

		for(Rectangle rect : rects) {
			if(overlaps(rect,rectangle)) return false;
		}
		
		ArrayList<Edge> commonEdges = new ArrayList<>();
		
		if(!this.edges.isEmpty()) {
			commonEdges.addAll(
					findCommonEdgesExceptCorners(getEdges(rectangle),this.edges) 
					);
			if(commonEdges.isEmpty()) {
				return false;
			}
		}
		
		
		return connectedToPreviousZone;
	}
	
	/**
	 * Checks whether a rectangle can be added and attached to particular edge.
	 * @param rectangle
	 * @param edge
	 * @return true if it can be added and attached, false otherwise.
	 */
	// TODO: should check whether it is next to the appropriate edge instead of attached to it.
	private boolean canAddRectangleToEdge(Rectangle rectangle, Edge edge) {
		if(findCommonEdges(getEdges(rectangle), edge).isEmpty()) {
			return false;
		}
		
		return canAddRectangle(rectangle);
	}
	
	/**
	 * Checks whether a rectangle can be added.
	 * @param x - x of the rectangle.
	 * @param y - y of the rectangle.
	 * @param w - width of the rectangle.
	 * @param h - height of the rectangle.
	 * @return
	 */
	private boolean canAddRectangle(int x,int y,int w,int h) {
		return canAddRectangle(new Rectangle(x,y,w,h));
	}
	
	/**
	 * Updates the list of edges of this zone.
	 * Should be called when a new rectangle is added.
	 * @param rect - rectangle which has been added.
	 */
	private void updateEdgeList(Rectangle rect) {
		ArrayList<Edge> commonEdges = findCommonEdgesExceptCorners(getEdges(rect),this.edges);
		edges.addAll(getEdges(rect));
		removeCommonEdgePoints(commonEdges);
		fillNonbuildable(commonEdges, 1);
	}
	
	/**
	 * Removes points which exist on this zones edges and also in edgeList1
	 * from this zones list of edges. edgeList1 is left unchanged.
	 * @param edgeList1
	 */
	private void removeCommonEdgePoints(ArrayList<Edge> edgeList1) {
		for(Edge edge1 : edgeList1) {
			for(Edge edge2 : this.edges) {
				edge2.edge.removeAll(edge1.edge);
			}
		}
	}
	
	// should also add points next to non buildable as well as exits and spaces next to exits
	public void fillNonbuildable(ArrayList<Edge> edgeList, int minFreeSpaces) {
		for(Edge edge : edgeList) {
			Edge tmpEdge = new Edge(edge);
			Random rand = new Random();
			while(tmpEdge.size() > minFreeSpaces) {
				int i = rand.nextInt(tmpEdge.size());
				tmpEdge.edge.remove(i);
			}
			//this.nonBuildable.addAll(tmpEdge.edge);
			for(IntegerPair ip : tmpEdge.edge) {
				fillNonBuildable(ip);
			}
		}
	}
	
	public void fillNonBuildable(IntegerPair ip) {
		this.nonBuildable.add(ip);
		Set<IntegerPair> coords = getAllCoordsInZone();
		Set<IntegerPair> neighbours = getNeighbours(ip);
		
		for(IntegerPair neighbour : neighbours) {
			if(coords.contains(neighbour)) {
				this.nonBuildable.add(neighbour);
			}
		}
	}
	
	/**
	 * Gets a random point on an edge.
	 * @param edge - edge to get a point from.
	 * @return IntegerPair with the coordinates of random on Edge edge.
	 */
	private static IntegerPair getRandomPointOnEdge(Edge edge) {
		Random rand = new Random();
		int rn = rand.nextInt(edge.size());
		return edge.get(rn);
	}
	
	/**
	 * Gets a random point on an edge excluding corners.
	 * @param edge - edge to get a point from.
	 * @return IntegerPair with the coordinates of random on Edge edge.
	 */
	private static IntegerPair getRandomPointOnEdgeExceptCorners(Edge edge) {
		Edge edgeCopy = new Edge();
		
		for(int i = 1; i < edge.size()-1;i++) {
			edgeCopy.edge.add(edge.get(i));
		}
		
		return getRandomPointOnEdge(edgeCopy);
	}
	
	/**
	 * Finds common edges from two lists of edges excluding edges which only have corners in common.
	 * @param edgeList1 - list of edges to search in.
	 * @param edgeList2 - list of edges to search in.
	 * @return list of edges in common to both edgeList1 and edgeList2.
	 */
	private static ArrayList<Edge> findCommonEdgesExceptCorners(ArrayList<Edge> edgeList1, 
			ArrayList<Edge> edgeList2) {
		
		if (edgeList1.isEmpty() || edgeList2.isEmpty()) return new ArrayList<Edge>();
		
		final ArrayList<Edge> edgeList1_2 = new ArrayList<>();
		for (Edge edge : edgeList1) {
			Edge newEdge = new Edge(edge.getCorners().get(0), edge.getCorners().get(1));
			newEdge.removeCorners();
			edgeList1_2.add(newEdge);
		}
		
		ArrayList<Edge> edgeList2_2 = new ArrayList<>();
		for (Edge edge : edgeList2) {
			Edge newEdge;
			newEdge = new Edge(edge.getCorners().get(0), edge.getCorners().get(1));
			newEdge.removeCorners();
			edgeList2_2.add(newEdge);
		}
		
		return findCommonEdges(edgeList1_2, edgeList2_2);
		
	}
	
	/**
	 * Finds common edges from two lists of edges.
	 * @param edgeList1 - list of edges to search in.
	 * @param edgeList2 - list of edges to search in.
	 * @return list of edges in common to both edgeList1 and edgeList2.
	 */
	private static ArrayList<Edge> findCommonEdges(ArrayList<Edge> edgeList1, 
			ArrayList<Edge> edgeList2) {
		
		ArrayList<Edge> commonEdges = new ArrayList<>();
			
		for(Edge edge1 : edgeList1) {
			commonEdges.addAll(findCommonEdges(edgeList2, edge1));
		}
		return commonEdges;
	}
	
	/**
	 * Finds common edges between a list of edges and a specific edge.
	 * @param edgeList - list of edges to search in.
	 * @param edge - an edge.
	 * @return list of edges in common to both edgeList1 and edgeList2.
	 */
	private static ArrayList<Edge> findCommonEdges(ArrayList<Edge> edgeList, Edge edge) {
		ArrayList<Edge> commonEdges = new ArrayList<>();
		for(Edge edge2 : edgeList) {
			Edge tmp = new Edge();
			getCommonEdgePoints(edge, edge2, tmp);
			if(!tmp.isEmpty())
				commonEdges.add(tmp);
		}
		return commonEdges;
	}

	/**
	 * Finds points which two edges have in common.
	 * @param edge1 - an edge.
	 * @param edge2 - an edge.
	 * @param points - a list of points to which the common points are added.
	 */
	// TODO: rework such that it produces an edge containing all the points which the two edges have in common.
	private static void getCommonEdgePoints(Edge edge1, Edge edge2,Edge points) {
		for(IntegerPair edgepoint1 : edge1.edge) {
			for(IntegerPair edgepoint2 : edge2.edge) {
				if(edgepoint1.equals(edgepoint2)) {
					points.edge.add(edgepoint1);
				}
			}
		}
	}
	
	/**
	 * Checks whether a rectangle overlaps with a zone.
	 * Common edges are counted as overlap.
	 * @param rectangle
	 * @param zone
	 * @return true if overlap, false otherwise.
	 */
	private static boolean overlapsInclusive(Rectangle rectangle, Zone2 zone) {
		if(overlapsExclusive(rectangle, zone)) return true;
		
		if(!findCommonEdges(zone.edges, getEdges(rectangle)).isEmpty()) return true;
		
		return false;
	}
	
	/**
	 * Checks whether a rectangle overlaps with a zone.
	 * Common edges are not counted as overlap.
	 * @param rectangle
	 * @param zone
	 * @return true if overlap, false otherwise.
	 */
	private static boolean overlapsExclusive(Rectangle rectangle, Zone2 zone) {
		for(Rectangle rect : zone.rects) {
			if(overlaps(rect,rectangle)) return true;
		}
		return false;
	}
	
	/**
	 * Checks whether two rectangles overlap.
	 * Common edges are not counted as overlap.
	 * @param rect1
	 * @param rect2
	 * @return true if overlap, false otherwise.
	 */
	private static boolean overlaps(Rectangle rect1, Rectangle rect2) {
		if(rect1.intersects(rect2) || rect1.contains(rect2) || rect2.contains(rect1)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Generates the list of all the edges of a rectangle.
	 * @param rect - get edges of this rectangle.
	 * @return a list of all the edges of the rectangle.
	 */
	private static ArrayList<Edge> getEdges(Rectangle rect) {
		ArrayList<Edge> edgesOfRectangle = new ArrayList<>();
		Edge edge1 = new Edge(rect.x,rect.y,rect.x+rect.width,rect.y);
		Edge edge2 = new Edge(rect.x,rect.y,rect.x,rect.y+rect.height);
		Edge edge3 = new Edge(rect.x+rect.width,rect.y,rect.x+rect.width,rect.y+rect.height);
		Edge edge4 = new Edge(rect.x,rect.y+rect.height,rect.x+rect.width,rect.y+rect.height);
		
		edgesOfRectangle.add(edge1);
		edgesOfRectangle.add(edge2);
		edgesOfRectangle.add(edge3);
		edgesOfRectangle.add(edge4);
		
		return edgesOfRectangle;
	}
	
	/**
	 * Generates a string representing an entity.
	 * @param type - Class of the entity to be generated.
	 * @param name - name of the entity to be generated.
	 * @param x - x-coordinate of the entity to be generated.
	 * @param y - y-coordinate of the entity to be generated.
	 * @param solid - whether this entity is solid or not.
	 * @param img - an int referencing the image of this entity.
	 * @param layer TODO
	 * @param collision - an array containing the functions to be executed when this entity collides with another entity.
	 * @param colparams - the parameters to be sent to the collision functions.
	 * @param move - an array containing the functions to be executed when this entity attempts to move.
	 * @param moveparams - the parameters to be sent to the move functions.
	 * @param use - an array containing the functions to be executed when the player attempts to 'use' this entity.
	 * @param useparams - the parameters to be sent to the use functions.
	 * @return
	 */
	private static String generateEntityString(String type, String name, int x, int y, boolean solid, int img, 
			int layer, int direction, String[] collision, String[][] colparams, String[] move, String[][] moveparams, String[] use, String[][] useparams) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(type+";");
		sb.append(name+";");
		sb.append(x+","+y+";");
		
		if(solid) sb.append(1+";");
		else sb.append(0+";");
		
		sb.append(img+";");
		
		sb.append(layer+";");
		
		sb.append(direction+";");
		
		createFunctionPointers(collision,colparams,sb);
		createFunctionPointers(move,moveparams,sb);
		createFunctionPointers(use,useparams,sb);
		
		return sb.toString();
	}
	
	/**
	 * Creates string representing function pointers and their parameters
	 * in such a way that the game can parse them.
	 * @param fptrs
	 * @param params
	 * @param sb
	 */
	private static void createFunctionPointers(String[] fptrs, String[][] params, StringBuilder sb) {
		for(int i = 0; i < fptrs.length; i++) {
			sb.append(fptrs[i]+"(");
			
			for(int j = 0; j < params[i].length; j++) {
				sb.append(params[i][j]);
				if(j != params.length -1) sb.append(" ");
			}
			
			sb.append(")");
			if(i != fptrs.length -1) sb.append(",");
		}
		sb.append(";");
	}
}
