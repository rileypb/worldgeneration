package brainfreeze.framework;

public class WorldGeometry {
	public static enum WorldGeometryType {
		PLANAR, CYLINDRICAL, SPHERICAL, CROSSCAP, MOBIUS, TOROIDAL, KLEINBOTTLE;

	}

	public WorldGeometryType type;

	public double xMin;
	public double xMax;
	public double yMin;
	public double yMax;

	public static WorldGeometry cylinder(double xMin, double xMax, double yMin, double yMax) {
		WorldGeometry geometry = new WorldGeometry();
		geometry.type = WorldGeometryType.CYLINDRICAL;
		geometry.xMin = xMin;
		geometry.xMax = xMax;
		geometry.yMin = yMin;
		geometry.yMax = yMax;
		return geometry;
	}

	public static WorldGeometry plane() {
		WorldGeometry geometry = new WorldGeometry();
		geometry.type = WorldGeometryType.PLANAR;
		return geometry;
	}
}
