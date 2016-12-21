package rpggame;

public abstract class  Generator {
	
	protected static boolean abort = false;
	
	public static void init() {
		abort = false;
	}
	
	public static void abort() {
		abort = true;
	}
	
}
