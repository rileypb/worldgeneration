package voronoi;

import java.util.HashSet;
import java.util.Set;

public class Lake extends Location {

	private Set<Location> vertices = new HashSet<Location>();
	
	public Lake(double x, double y) {
		super(x, y);
	}

	public void addVertex(Location v) {
		vertices.add(v);
	}
	
	public Set<Location> getVertices() {
		return new HashSet<>(vertices);
	}

}
