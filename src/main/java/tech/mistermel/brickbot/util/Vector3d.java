package tech.mistermel.brickbot.util;

public class Vector3d {

	private double x;
	private double y;
	private double z;
	
	public Vector3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3d floor() {
		this.x = Math.floor(x);
		this.y = Math.floor(y);
		this.z = Math.floor(z);
		return this;
	}
	
	public Vector3d ceil() {
		this.x = Math.ceil(x);
		this.y = Math.ceil(y);
		this.z = Math.ceil(z);
		return this;
	}
	
	public Vector3d round() {
		this.x = (int) x;
		this.y = (int) y;
		this.z = (int) z;
		return this;
	}
	
	public Vector3d add(Vector3d v) {
		this.x += v.getX();
		this.y += v.getY();
		this.z += v.getZ();
		return this;
	}
	
	public Vector3d subtract(Vector3d v) {
		this.x -= v.getX();
		this.y -= v.getY();
		this.z -= v.getZ();
		return this;
	}
	
	@Override
	public Vector3d clone() {
		return new Vector3d(this.getX(), this.getY(), this.getZ());
	}
	
	@Override
	public String toString() {
		return "Vector3d(" + this.getX() + " " + this.getY() + " " + this.getZ() + ")";
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public void setZ(double z) {
		this.z = z;
	}
	
}
