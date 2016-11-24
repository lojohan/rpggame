package rpggame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class Zone {
	public int x;
	public int y;
	public int sizeX;
	public int sizeY;
	public boolean friendly;
	
	public Map<Dir,ArrayList<IntegerPair>> edges = new HashMap<>();
	
	public static enum Dir {
	    NORTH, EAST, SOUTH, WEST
	}
	
	public Zone(int x, int y, int sizeX, int sizeY, boolean friendly) {
		this.x = x;
		this.y = y;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.friendly = friendly;
		
		generateEdges();
	}
	
	private void generateEdges() {
		for (Dir dir : Dir.values()) {
			ArrayList<IntegerPair> tmp = new ArrayList<>();
			switch(dir) {
			case NORTH:
				for(int i = 0; i <= this.sizeY; i++) {
					tmp.add( new IntegerPair(this.x, this.y + i) );
				}
				break;
			case EAST:
				for(int i = 0; i <= this.sizeX; i++) {
					tmp.add( new IntegerPair(this.x+i, this.y + this.sizeY) );
				}
				break;
			case SOUTH:
				for(int i = 0; i <= this.sizeY; i++) {
					tmp.add( new IntegerPair(this.x+this.sizeX, this.y + i) );
				}
				break;
			case WEST:
				for(int i = 0; i <= this.sizeX; i++) {
					tmp.add( new IntegerPair(this.x+i, this.y) );
				}
				break;
			}
			this.edges.put(dir, tmp);
		}
	}
	
	// return edge corresponding to direction
	public ArrayList<IntegerPair> getEdge(Dir dir) {
		return this.edges.get(dir);
	}
	
	public ArrayList<IntegerPair> getRandomEdgeExclude(Dir exclude) {
		Dir dir = exclude;
		while(dir == exclude) {
			dir = getRandomDirection();
		}
		return getEdge(dir);
	}
	
	static public Dir getRandomDirectionExcl(ArrayList<Dir> exclude) {
		final Random rn = new Random();
		ArrayList<Dir> directions = new ArrayList<>();
		
		for(Dir dir : Dir.values()) {
			directions.add(dir);
		}
		
		Iterator<Dir> it = directions.iterator();
		
		while(it.hasNext()) {
			Dir dir = it.next();
			if(exclude.size() != 0) {
				for(Dir direxcl : exclude) {
				  if(dir == direxcl) {
					  it.remove();
				  }
				}
			}
		}
		
		if(directions.size() != 0) {
			int rand = rn.nextInt(directions.size());
        	return directions.get(rand);
		} else {
			return null;
		}
	}
	
	static public Dir getRandomDirection() {
		final Random rn = new Random();
		int rand = rn.nextInt(4);
		
		switch (rand) {
        case 0:
            return Dir.NORTH;
                
        case 1:
            return Dir.EAST;
                     
        case 2:
            return Dir.SOUTH;
                    
        case 3:
            return Dir.WEST;
        default:
        	return null;
		}
	}
}
