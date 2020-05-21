package brainfreeze.world;

import java.awt.Color;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;

public class Location {

	public double x;
	public double y;
	public double elevation;
	public boolean water;
	public boolean ocean;
	public int subdivision;

	public double distanceToSubdivisionEdge = -1;
	public float distanceToCoast = -1;
	public float heightForSubdivision;
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
	public int lakeNumber;
	public boolean isLake;
	public Lake lake;
	public double cityScore;
	public boolean city;
	public boolean road;
	public boolean usedForRoad;
	public boolean town;
	public boolean coast;
	public double tmpX;
	public double tmpY;
	public boolean secondaryRoad;
	public String name;
	public int index;

	public Location(double x, double y) {
		this.x = x;
		this.y = y;
		tmpX = x;
		tmpY = y;
		subdivision = 0;
	}

	@Override
	public String toString() {
		return "[" + x + ", " + y + "]";
	}

	public Set<Location> neighboringVertices(Graph<Location, MapEdge> graph) {
		return graph.edgesOf(this).stream().map((edge) -> {
			return edge.oppositeLocation(this);
		}).collect(Collectors.toSet());
	}

}
