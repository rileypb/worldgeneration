package voronoiold;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.alsclo.voronoi.graph.Edge;

public class MapEdge {

	public final Location loc1;
	public final Location loc2;

	private List<Face> adjacentFaces = new ArrayList<>();
	public boolean river;
	public float lighting;
	public Color riverColor;
	
	public MapEdge(Location loc1, Location loc2) {
		this.loc1 = loc1;
		this.loc2 = loc2;
		if (loc1 == loc2) {
			int a = 0;
		}
	}
	
	@Override
	public String toString() {
		return loc1 + " <-> " + loc2;
	}


	public void addAdjacentFace(Face face) {
		if (!adjacentFaces.contains(face)) {
			adjacentFaces.add(face);
		}
	}
	
	public List<Face> getAdjacentFaces() {
		return Collections.unmodifiableList(adjacentFaces);
	}
	


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MapEdge edge = (MapEdge) o;

        if (!loc1.equals(edge.loc1)) return false;
        if (!loc2.equals(edge.loc2)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = loc1.hashCode();
        result = 31 * result + loc2.hashCode();
        return result;
    }

	public Location oppositeLocation(Location point) {
		if (point == loc1) {
			return loc2;
		} else if (point == loc2) {
			return loc1;
		}
		return null;
		//throw new IllegalArgumentException();
	}
}
