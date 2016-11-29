package rpggame;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

public class Zone2 {
	ArrayList<Zone2> zonesInWorld = new ArrayList<>();
	
	ArrayList<Rectangle> rects = new ArrayList<>();
	String name = "";
	
	HashMap<IntegerPair, ArrayList<String> > entities = new HashMap<>();
	
	ArrayList<ArrayList<IntegerPair>> edgeList = new ArrayList<>();
	
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
		for(ArrayList<IntegerPair> edge : edgeList) {
			for(IntegerPair tile : edge) {
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
					return false;
				}
			}
		}
		return true;
	}
	
	private ArrayList<ArrayList<IntegerPair>> getEdges(Rectangle rect) {
		int x, y;
		ArrayList<ArrayList<IntegerPair>> edges = new ArrayList<>();
		ArrayList<IntegerPair> edge1 = new ArrayList<>();
		ArrayList<IntegerPair> edge2 = new ArrayList<>();
		ArrayList<IntegerPair> edge3 = new ArrayList<>();
		ArrayList<IntegerPair> edge4 = new ArrayList<>();
		
			
		for(x = rect.x; x <= rect.width+rect.x; x++) {
			edge1.add(new IntegerPair(x,rect.y));
			edge2.add(new IntegerPair(x,rect.y+rect.height));
		}
	
		for(y = rect.y; y <= rect.height+rect.y; y++) {
			edge3.add(new IntegerPair(rect.x,y));
			edge4.add(new IntegerPair(rect.x+rect.width,y));
		}
		
		edges.add(edge1);
		edges.add(edge2);
		edges.add(edge3);
		edges.add(edge4);
		
		return edges;
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
	
	public boolean generateFirstRectangle(int x, int y, int w, int h) {
		if(this.zonesInWorld.isEmpty()) return this.addRectangle(x, y, w, h);
		return this.tryToAddRect(x, y, w, h);
	}
	
	private boolean tryToAddRect(int x, int y, int w, int h) {
		Random rand = new Random();
		// should not be in a set order.
		int[][] tests = new int[][]{new int[]{x,y,w,h},
			{x-w,y,w,h},
			{x+w,y,w,h},
			{x,y-h,w,h},
			{x,y+h,w,h}};
			
		int randind = rand.nextInt(tests.length);
		
		if(addRectangle(tests[randind][0],tests[randind][1],tests[randind][2],tests[randind][3])) {
			return true;
		}
		return false;
	}
	
	
	private ArrayList<IntegerPair> getRandomEdgeAtLeastSize(int minsize) {
		ArrayList<ArrayList<IntegerPair>> edges = new ArrayList<>();
		
		for(ArrayList<IntegerPair> edge : this.edgeList) {
			if(edge.size() >= minsize) {
				edges.add(edge);
			}
		}
		
		if(edges.isEmpty()) return new ArrayList<IntegerPair>();
		
		Random rand = new Random();
		int rn = rand.nextInt(edges.size());
		return edges.get(rn);
		
	}
	
	private ArrayList<IntegerPair> getRandomEdge() {
		Random rand = new Random();
		int rn = rand.nextInt(this.edgeList.size());
		return this.edgeList.get(rn);
	}
	
	private IntegerPair getRandomPointOnEdge(ArrayList<IntegerPair> edge) {
		Random rand = new Random();
		int rn = rand.nextInt(edge.size());
		return edge.get(rn);
	}
	
	private IntegerPair getRandomPointOnEdgeExceptCorners(ArrayList<IntegerPair> edge) {
		ArrayList<IntegerPair> edgeCopy = new ArrayList<>();
		
		for(int i = 1; i < edge.size()-1;i++) {
			edgeCopy.add(edge.get(i));
		}
		
		return getRandomPointOnEdge(edgeCopy);
	}
	
	private ArrayList<ArrayList<IntegerPair>> findCommonEdgesExceptCorners(ArrayList<ArrayList<IntegerPair>> edgeList1, 
			ArrayList<ArrayList<IntegerPair>> edgeList2) {
		
		if (edgeList1.isEmpty() || edgeList2.isEmpty()) return new ArrayList<ArrayList<IntegerPair>>();
		
		final ArrayList<ArrayList<IntegerPair>> edgeList1_2 = new ArrayList<>();
		for (ArrayList<IntegerPair> edge : edgeList1) {
			final ArrayList<IntegerPair> newEdge = new ArrayList<>();
			for (int i = 1; i < edge.size()-1; i++) {
				newEdge.add(edge.get(i));
			}
			edgeList1_2.add(newEdge);
		}
		
		ArrayList<ArrayList<IntegerPair>> edgeList2_2 = new ArrayList<>();
		for (ArrayList<IntegerPair> edge : edgeList2) {
			final ArrayList<IntegerPair> newEdge = new ArrayList<>();
			for (int i = 1; i < edge.size()-1; i++) {
				newEdge.add(edge.get(i));
			}
			edgeList2_2.add(newEdge);
		}
		
		return findCommonEdges(edgeList1_2, edgeList2_2);
		
	}
	
	private ArrayList<ArrayList<IntegerPair>> findCommonEdges(ArrayList<ArrayList<IntegerPair>> edgeList1, 
			ArrayList<ArrayList<IntegerPair>> edgeList2) {
		
		ArrayList<ArrayList<IntegerPair>> commonEdges = new ArrayList<>();
			
		for(ArrayList<IntegerPair> edge1 : edgeList1) {
			for(ArrayList<IntegerPair> edge2 : edgeList2) {
				ArrayList<IntegerPair> tmp = new ArrayList<>();
				getCommonEdgePoints(edge1, edge2, tmp);
				if(!tmp.isEmpty())
					commonEdges.add(tmp);
			}
		}
		return commonEdges;
		
	}

	private void getCommonEdgePoints(ArrayList<IntegerPair> edge1, ArrayList<IntegerPair> edge2,ArrayList<IntegerPair> points) {
		for(IntegerPair edgepoint1 : edge1) {
			for(IntegerPair edgepoint2 : edge2) {
				if(edgepoint1.equals(edgepoint2)) {
					points.add(edgepoint1);
				}
			}
		}
	}
	
	/*
	public ArrayList<IntegerPair> getAllCoordsInZone() {
		ArrayList<IntegerPair> tmp = new ArrayList<>();
		
		for(Rectangle rect : rects) {
			for()
		}
		
		return tmp;
	}
	*/
	
	private boolean canAddRectangle(Rectangle rectangle) {
		
		// checks if adding this rectangle causes the zone to overlap with other zones
		this.rects.add(rectangle);
		
		for(Zone2 zone : zonesInWorld) {
			if(this.overlaps(zone) && zone != this) {
				return false;
			}
		}
		
		this.rects.remove(rectangle);
		
		if(this.rects.isEmpty()) return true;

		for(Rectangle rect : rects) {
			if(rectangle.intersects(rect) || rectangle.contains(rect) || rect.contains(rectangle)) {
				return false;
			}
		}
		ArrayList<ArrayList<IntegerPair>> commonEdges = new ArrayList<>();
		
		commonEdges.addAll(
				findCommonEdgesExceptCorners(getEdges(rectangle),this.edgeList) 
				);
		if(commonEdges.isEmpty()) {
			return false;
		}
		
		
		return true;
	}
	
	private void updateEdgeList(Rectangle rect) {
		ArrayList<ArrayList<IntegerPair>> commonEdges = findCommonEdgesExceptCorners(getEdges(rect),this.edgeList);
		edgeList.addAll(getEdges(rect));
		removeCommonEdgePoints(commonEdges);
	}
	
	private void removeCommonEdgePoints(ArrayList<ArrayList<IntegerPair>> edgeList1) {
		for(ArrayList<IntegerPair> edge1 : edgeList1) {
			for(ArrayList<IntegerPair> edge2 : this.edgeList) {
				edge2.removeAll(edge1);
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
