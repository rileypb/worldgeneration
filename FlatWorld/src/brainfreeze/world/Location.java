package brainfreeze.world;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.triangulate.quadedge.Vertex;

public class Location {

	private double x;
	private double y;
	public Coordinate delegate;
	
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
	public double drawRadius = 0;
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
	public double angle;
	public Set<MapEdge> sides;
	public Set<Location> adjacentCells = new HashSet<>();
	public boolean extra;
	public double packingRadius;
	public int plateIndex = -1;
	public Plate plate;
	public boolean plateEdge;
	public double tectonicStress;
	public Location originalSite;
	public double baseElevation;
	public double tmpElevation;

	public Location(double x, double y) {
		this.setX(x);
		this.setY(y);
		tmpX = x;
		tmpY = y;
		subdivision = 0;
		sides = new HashSet<>();
	}

	public Location(Coordinate c) {
		delegate = c;
		tmpX = x;
		tmpY = y;
		subdivision = 0;
		sides = new HashSet<>();
	}

	@Override
	public String toString() {
		return "[" + getX() + ", " + getY() + "]";
	}

	public Set<Location> neighboringVertices(Graph<Location, MapEdge> graph) {
		return graph.edgesOf(this).stream().map((edge) -> {
			return edge.oppositeLocation(this);
		}).collect(Collectors.toSet());
	}


	public static Location average(Collection<Location> locations) {
		if (locations.size() == 0) {
			throw new IllegalArgumentException("Tried to take average of empty collection.");
		}
		double sumX = 0;
		double sumY = 0;
		for (Location loc : locations) {
			sumX += loc.getX();
			sumY += loc.getY();
		}
		return new Location(sumX / locations.size(), sumY / locations.size());
	}

	public void setAngleWithRespectTo(Location center) {
		double diffX = getX() - center.getX();
		double diffY = getY() - center.getY();
		angle = Math.atan2(diffY, diffX);
	}

	public double getX() {
		return delegate == null ? x : delegate.getX();
	}

	public void setX(double x) {
		if (delegate == null) {
			this.x = x;
		} else {
			delegate.setX(x);
		}
	}

	public double getY() {
		return delegate == null ? y : delegate.getY();
	}

	public void setY(double y) {
		if (delegate == null) {
			this.y = y;
		} else {
			delegate.setY(y);
		}
	}

}
