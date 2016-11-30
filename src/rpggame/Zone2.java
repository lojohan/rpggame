package rpggame;
// TODO: organize methods and make appropriate methods static
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Zone2 {
	ArrayList<Zone2> zonesInWorld = new ArrayList<>();
	
	ArrayList<Rectangle> rects = new ArrayList<>();
	String name = "";
	
	HashMap<IntegerPair, ArrayList<String> > entities = new HashMap<>();
	
	ArrayList<Edge> edges = new ArrayList<>();
	
	ArrayList<IntegerPair> exits = new ArrayList<>();
	
	ArrayList<String> zone = new ArrayList<>();
	
	boolean friendly;
	
	public Zone2(String name, ArrayList<Zone2> zonesInWorld) {
		this.name = name;
		this.zonesInWorld = zonesInWorld;
	}
	
	public boolean addRectangle(int x, int y, int w, int h) {
		Rectangle tmpRect = new Rectangle(x,y,w,h);
		if(canAddRectangle(tmpRect)) {
			rects.add(tmpRect);
			updateEdgeList(tmpRect);
			return true;
		}
		return false;
	}
	
	public void setFriendly(boolean friendly) {
		this.friendly = friendly;
	}
	
	public void addWallTile(IntegerPair ip) {
		addStringToEntities(ip,
				generateEntityString(
						"Tile","wall",ip.x,ip.y,true,1,
						new String[]{}, new String[][]{{}},new String[]{},new String[][]{{}},new String[]{}, new String[][]{{}}));
	}
	
	public void addPlayer(IntegerPair ip, String name) {
		addStringToEntities(ip,
				generateEntityString(
						"Player",name,ip.x,ip.y,true,2,
						new String[]{}, new String[][]{{}},new String[]{"playerControl"},new String[][]{{}},new String[]{}, new String[][]{{}}));
	}
	
	public void addZones() {
		for(Rectangle rect : rects) {
			zone.add("Zone;" + this.name + ";" + rect.x + "," + rect.y + ";" + (rect.x + rect.width) + ","
				+ (rect.y + rect.height) + ";" + ((friendly) ? 1 : 0) + ";");
		}
	}
	
	public void generateWallTiles() {
		for(Edge edge : edges) {
			for(IntegerPair tile : edge.edge) {
				if(!exits.contains(tile))
					addWallTile(tile);
			}
		}
	}
	
	public void addStringToEntities(IntegerPair coords, String entity) {
		if(!entities.containsKey(coords)) {
			entities.put(coords, new ArrayList<>(Arrays.asList(entity)));
		} else if(!entities.get(coords).contains(entity)){
			entities.get(coords).add(entity);
		}
	}
	
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
	
	private ArrayList<Edge> getEdges(Rectangle rect) {
		//int x, y;
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
	
	public void generateRandomRectangles(int n,int minwidth, int minheight, int maxwidth, int maxheight) {
		for(int i = 0; i < n; i++) {
			randomRect(minwidth, minheight, maxwidth, maxheight);
		}
	}
	
	public IntegerPair getPotentialExit() {
		IntegerPair exitPoint = getRandomPointOnEdgeExceptCorners(getRandomEdgeAtLeastSize(3));
		return exitPoint;
	}
	
	public void generateExits(int numexits) {
		for(int i = 0; i < numexits; i++) {
			IntegerPair exitPoint = getRandomPointOnEdgeExceptCorners(getRandomEdgeAtLeastSize(3));
			this.exits.add(exitPoint);
		}
	}
	
	private boolean randomRect(int minwidth, int minheight, int maxwidth, int maxheight) {
		Random rand = new Random();
		int randwidth = minwidth + rand.nextInt(maxwidth);

		int randheight = minheight + rand.nextInt(maxheight);
		
		IntegerPair startPoint = getRandomPointOnEdgeExceptCorners(getRandomEdgeAtLeastSize(3));
		
		
		return tryToAddRect(startPoint.x,startPoint.y,randwidth,randheight);
			
	}
	
	// should be able to guarantee that it is properly attached to edge of last zone
	public boolean generateFirstRectangle(int x, int y, int w, int h, Edge edge) {
		if(this.zonesInWorld.isEmpty()) return this.addRectangle(x, y, w, h);
		// not appropriate method because it does not guarantee zone is attached to edge
		return this.tryToAddRectToEdge(x, y, w, h, edge);
	}
	
	// Attempts to attach rectangle to edge 'edge'
	private boolean tryToAddRectToEdge(int x, int y, int w, int h, Edge edge) {
		if( edge != null && !edge.isEmpty()) {		
			if(addRectangle(x,y,w,h)) {
				return true;
			}
			
			if(edge.isEdgeVertical()) {	
				if(addRectangle(x-w,y,w,h)) {
					return true;
				} else if(addRectangle(x+w,y,w,h)){
					return true;
				}
			} else {
				if(addRectangle(x,y-h,w,h)) {
					return true;
				} else if(addRectangle(x,y+h,w,h)){
					return true;
				}
			}
		}
		
		return false;
	}
	
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
	
	public Edge getRandomEdgeForExit() {
		return getRandomEdgeAtLeastSize(3);
	}
	
	public IntegerPair getRandomPointForExitOnEdge(Edge edge) {
		return getRandomPointOnEdgeExceptCorners(edge);
	}
	
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
	
	private Edge getRandomEdge() {
		Random rand = new Random();
		int rn = rand.nextInt(this.edges.size());
		return this.edges.get(rn);
	}
	
	private IntegerPair getRandomPointOnEdge(Edge edge) {
		Random rand = new Random();
		int rn = rand.nextInt(edge.size());
		return edge.get(rn);
	}
	
	private IntegerPair getRandomPointOnEdgeExceptCorners(Edge edge) {
		Edge edgeCopy = new Edge();
		
		for(int i = 1; i < edge.size()-1;i++) {
			edgeCopy.edge.add(edge.get(i));
		}
		
		return getRandomPointOnEdge(edgeCopy);
	}
	
	private ArrayList<Edge> findCommonEdgesExceptCorners(ArrayList<Edge> edgeList1, 
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
	
	private ArrayList<Edge> findCommonEdges(ArrayList<Edge> edgeList1, 
			ArrayList<Edge> edgeList2) {
		
		ArrayList<Edge> commonEdges = new ArrayList<>();
			
		for(Edge edge1 : edgeList1) {
			for(Edge edge2 : edgeList2) {
				Edge tmp = new Edge();
				getCommonEdgePoints(edge1, edge2, tmp);
				if(!tmp.isEmpty())
					commonEdges.add(tmp);
			}
		}
		return commonEdges;
		
	}

	// TODO: rework such that it produces an edge containing all the points which the two edges have in common.
	private void getCommonEdgePoints(Edge edge1, Edge edge2,Edge points) {
		for(IntegerPair edgepoint1 : edge1.edge) {
			for(IntegerPair edgepoint2 : edge2.edge) {
				if(edgepoint1.equals(edgepoint2)) {
					points.edge.add(edgepoint1);
				}
			}
		}
	}
	
	public Set<IntegerPair> getAllCoordsInZone() {
		Set<IntegerPair> tmp = new HashSet<>();
		
		for(Rectangle rect : rects) {
			for(int x = 0; x <= rect.width; x++) {
				for(int y = 0; y <= rect.height; y++) {
					tmp.add(new IntegerPair(x,y));
				}
			}
		}
		
		for(Edge edge : this.edges) {
			for(IntegerPair pointOnEdge : edge.edge) {
				if(tmp.contains(pointOnEdge)) {
					tmp.remove(pointOnEdge);
				}
			}
		}
		
		return tmp;
	}
	
	private boolean overlapsInclusive(Rectangle rectangle, Zone2 zone) {
		if(overlapsExclusive(rectangle, zone)) return true;
		
		if(!findCommonEdges(zone.edges, getEdges(rectangle)).isEmpty()) return true;
		
		return false;
	}
	
	private boolean overlapsExclusive(Rectangle rectangle, Zone2 zone) {
		for(Rectangle rect : zone.rects) {
			if(overlaps(rect,rectangle)) return true;
		}
		return false;
	}
	
	private boolean overlaps(Rectangle rect1, Rectangle rect2) {
		if(rect1.intersects(rect2) || rect1.contains(rect2) || rect2.contains(rect1)) {
			return true;
		}
		return false;
	}
	
	private boolean canAddRectangle(Rectangle rectangle) {
		
		// checks if adding this rectangle causes the zone to overlap with other zones
		for(Zone2 zone : zonesInWorld) {
			// TODO: should use inclusive once generation of first rectangle is fixed
			//if(overlapsInclusive(rectangle,zone) && zone != this)
			if(overlapsExclusive(rectangle,zone) && zone != this)
				return false;
		}
		
		if(this.rects.isEmpty()) return true;

		for(Rectangle rect : rects) {
			if(overlaps(rect,rectangle)) return false;
		}
		
		ArrayList<Edge> commonEdges = new ArrayList<>();
		
		commonEdges.addAll(
				findCommonEdgesExceptCorners(getEdges(rectangle),this.edges) 
				);
		if(commonEdges.isEmpty()) {
			return false;
		}
		
		
		return true;
	}
	
	private boolean canAddRectangle(int x,int y,int w,int h) {
		return canAddRectangle(new Rectangle(x,y,w,h));
	}
	
	private void updateEdgeList(Rectangle rect) {
		ArrayList<Edge> commonEdges = findCommonEdgesExceptCorners(getEdges(rect),this.edges);
		edges.addAll(getEdges(rect));
		removeCommonEdgePoints(commonEdges);
	}
	
	private void removeCommonEdgePoints(ArrayList<Edge> edgeList1) {
		for(Edge edge1 : edgeList1) {
			for(Edge edge2 : this.edges) {
				edge2.edge.removeAll(edge1.edge);
			}
		}
	}
	
	private String generateEntityString(String type, String name, int x, int y, boolean solid, int img,
			String[] collision, String[][] colparams, String[] move, String[][] moveparams, String[] use, String[][] useparams) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(type+";");
		sb.append(name+";");
		sb.append(x+","+y+";");
		
		if(solid) sb.append(1+";");
		else sb.append(0+";");
		
		sb.append(img+";");
		
		createFunctionPointers(collision,colparams,sb);
		createFunctionPointers(move,moveparams,sb);
		createFunctionPointers(use,useparams,sb);
		
		return sb.toString();
	}
	
	private void createFunctionPointers(String[] fptrs, String[][] params, StringBuilder sb) {
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
