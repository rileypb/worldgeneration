package brainfreeze.world;

public class Vector {

	private double x;
	private double y;

	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector normalize() {
		double mag = getMagnitude();
		return new Vector(x / mag, y / mag);
	}

	public double getMagnitude() {
		return Math.sqrt(x * x + y * y);
	}

	public Vector plus(Vector v2) {
		return new Vector(x + v2.x, y + v2.y);
	}

	public double dot(Vector v2) {
		return x * v2.x + y * v2.y;
	}

	public Vector projectOnto(Vector v2) {
		Vector v2n = v2.normalize();
		double dot = dot(v2n);
		return new Vector(v2n.x * dot, v2n.y * dot);
	}
	
	@Override
	public String toString() {
		return String.format("<%f, %f>", x, y);
	}

}
