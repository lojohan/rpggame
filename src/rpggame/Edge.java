package rpggame;

import java.util.ArrayList;

public class Edge {
	ArrayList<IntegerPair> edge = new ArrayList<>();
	
	boolean vertical = false;
	
	public Edge() {
		
	}
	
	public Edge(IntegerPair ip1, IntegerPair ip2) {
		this(ip1.x,ip1.y,ip2.x,ip2.y);
	}
	
	public Edge(int startx, int starty, int endx, int endy) {
		if(startx == endx) {
			vertical = true;
			for(int y = starty; y <= endy; y++) {
				edge.add(new IntegerPair(startx,y));
			}
		} else {
			for(int x = startx; x <= endx; x++) {
				edge.add(new IntegerPair(x,starty));
			}
			vertical = false;
		}
	}
	
	public boolean isEdgeVertical() {
		return vertical;
	}
	
	public ArrayList<IntegerPair> getCorners() {
		ArrayList<IntegerPair> corners = new ArrayList<>();
		
		corners.add(this.edge.get(0));
		corners.add(this.edge.get(this.edge.size()-1));
		
		return corners;
	}
	
	public int size() {
		return this.edge.size();
	}
	
	public IntegerPair get(int i) {
		return this.edge.get(i);
	}
	
	public void removeCorners() {
		this.edge.removeAll(getCorners());
	}
	
	public boolean isEmpty() {
		return this.edge.isEmpty();
	}
	
}
