package entities;

public abstract class Entity {
	private float height = 64;
	private float width = 64;
		
	public void setHeight(float height) {
		this.height = height;
	}
	
	public void setWidth(float width) {
		this.width = width;
	}
	
	public float getHeight() {
		return this.height;
	}
	
	public float getWidth() {
		return this.width;
	}
}
