package rpggame;

public class IntegerPair {
	public int x;
	public int y;
	
	public IntegerPair(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public IntegerPair(IntegerPair ip) {
		this(ip.x,ip.y);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntegerPair other = (IntegerPair) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "(" + x + ", " + y + ")";
	}
	
	
}
