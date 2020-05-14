package voronoinew;

import java.util.ArrayList;
import java.util.List;

public class Road {

	private List<Location> sites = new ArrayList<Location>();
	private double score = 0;
	private List<MapEdge> edges = new ArrayList<MapEdge>();
	
	public void add(Location loc) {
		sites.add(loc);
	}

	public double getScore() {
		return score;
	}

	public Location getHead() {
		return sites.get(sites.size() - 1);
	}

	public boolean contains(Location loc) {
		return sites.contains(loc);
	}

	public Road extend(Location loc, MapEdge edge) {
		Road dup;
		try {
			dup = (Road) this.clone();
			dup.add(loc);
			dup.add(edge);
			
			double edgeScore = 100*edge.length();
			if (loc.mountain)
			{
				edgeScore *= 5;
			} else if (loc.hill) {
				edgeScore *= 3;
			} else if (loc.forest) {
				edgeScore *= 2;
			}
			if (edge.river) {
				edgeScore /= 2;
			}
			if (edge.road) {
				edgeScore /= 2;
			}
			dup.score += edgeScore;
			
			return dup;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void add(MapEdge edge) {
		this.edges.add(edge);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Road dup = new Road();
		dup.sites = new ArrayList<>(this.sites);
		dup.edges = new ArrayList<MapEdge>(this.edges);
		dup.score = this.score;
		return dup;
	}

	public void markRoad() {
		for (Location s : sites) {
			s.road = true;
		}
		for (MapEdge edge : edges) {
			edge.road = true;
		}
	}
	
	@Override
	public String toString() {
		return String.valueOf(this.score);
	}

}
