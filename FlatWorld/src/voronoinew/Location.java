package voronoinew;
import static de.alsclo.voronoi.Math.EPSILON;
import static de.alsclo.voronoi.Math.PRECISION;
import static java.lang.Math.abs;

import java.awt.Color;

public class Location {

	public final double x;
	public final double y;
	public double elevation;
	public boolean water;
	public boolean ocean;
	public int subdivision;
	
	public double distanceToSubdivisionEdge = -1;
	public float distanceToCoast = -1;
	public float heightForSubdivision;
	public Lake lake;
	public boolean sea;
	public boolean river;
	public boolean visited;
	public double red;
	public double green;
	public double blue;
	public double moisture;
	public double temperatureVariance;
	public double temperature;
	public Biome biome;
	public double wildness;
	public double calmValue;
	public double wildValue;
	public boolean boundaryLocation;
	public double pdElevation;
	public int graphHeight = -1;
	public boolean riverJuncture;
	public double flux;
	public boolean riverHead;
	public boolean foo;
	public boolean mountain;
	public boolean hill;
	public double baseMoisture;
	public boolean tmpMountain;
	public Color baseColor;
	public Color color;
	public boolean forest;
	public double radius = 0;

	public Location(double x, double y) {
		this.x = x;
		this.y = y;
		subdivision = 0;
	}

	@Override
	public String toString() {
		return "[" + x + ", " + y + "]"; 
	}
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        Location point = (Location) o;
//        return abs(x - point.x) <= EPSILON && abs(y - point.y) <= EPSILON;
//    }

//    @Override
//    public int hashCode() {
//        return (int) (x * PRECISION * 31) + (int) (y * PRECISION);
//    }
}
