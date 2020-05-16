package voronoi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CellPicker {
	private Graphs graphs;
	private double maxRadius;

	public CellPicker(Graphs graphs, double maxRadius) {
		this.graphs = graphs;
		this.maxRadius = maxRadius;
	}

	public List<List<Location>> pick(Random r) {
		int widthInBuckets = (int) (1 / maxRadius) + 1;
		double bucketWidth = 1.0 / widthInBuckets;
		int heightInBuckets = (int) (1 / maxRadius) + 1;
		double bucketHeight = 1.0 / heightInBuckets;

		List<Location> obstacles = new ArrayList<Location>();

		graphs.dualVertices.forEach((loc) -> {
			if ((loc.x >= 0 && loc.x <= 1 && loc.y >= 0 && loc.y <= 1) && (loc.water || loc.city || loc.road)) {
				double maxRadius = graphs.dualGraph.edgesOf(loc).stream().map((e) -> {
					return graphs.dualToVoronoi.get(e);
				}).filter((e) -> {
					return e != null;
				}).flatMap((e) -> {
					return Arrays.stream(new Location[] { e.loc1, e.loc2 });
				}).mapToDouble((v) -> {
					return Math.sqrt((v.x - loc.x) * (v.x - loc.x) + (v.y - loc.y) * (v.y - loc.y));
				}).max().getAsDouble();
				loc.radius = Math.min(.01, maxRadius / 2);
				obstacles.add(loc);
			}
		});

		List<List<Location>> bucketList = new ArrayList<List<Location>>();

		for (int i = 0; i < widthInBuckets * heightInBuckets; i++) {
			bucketList.add(new ArrayList<>());
		}

		List<List<Location>> pickList = new ArrayList<List<Location>>();

		for (int i = 0; i < widthInBuckets * heightInBuckets; i++) {
			pickList.add(new ArrayList<>());
		}

		graphs.dualVertices.forEach((loc) -> {
			if (loc.x >= 0 && loc.x <= 1 && loc.y >= 0 && loc.y <= 1) {
				int x = (int) (loc.x / bucketWidth);
				int y = (int) (loc.y / bucketHeight);
				int index = x * widthInBuckets + y;
				bucketList.get(index).add(loc);
			}
		});

		for (int u = 0; u < widthInBuckets; u++) {
			for (int v = 0; v < heightInBuckets; v++) {
				int index = u * widthInBuckets + v;
				List<Location> candidates = bucketList.get(index);
				for (Location candidate : new ArrayList<Location>(candidates)) {
					double radius = 0;
					if (!candidate.water) {
						if (candidate.hill) {
							radius = 2 * maxRadius / 3;
						} else if (candidate.mountain) {
							radius = 2 * maxRadius;
						} else if (candidate.forest) {
							radius = maxRadius / 2;
						}
					}
					
					if (radius == 0) {
						continue;
					}
					int i0 = index;
					boolean collision = collides(candidate, pickList.get(i0), radius);
					if (collision) {
						candidates.remove(candidate);
						continue;
					}
					if (u > 0) {
						int i1 = index - widthInBuckets;
						collision = collides(candidate, pickList.get(i1), radius);
						if (collision) {
							candidates.remove(candidate);
							continue;
						}
					}
					if (v > 0) {
						int i2 = index - 1;
						collision = collides(candidate, pickList.get(i2), radius);
						if (collision) {
							candidates.remove(candidate);
							continue;
						}
					}
					if (u > 0 && v > 0) {
						int i3 = index - 1 - widthInBuckets;
						collision = collides(candidate, pickList.get(i3), radius);
						if (collision) {
							candidates.remove(candidate);
							continue;
						}
					}
					if (u < widthInBuckets - 1) {
						int i4 = index + widthInBuckets;
						collision = collides(candidate, pickList.get(i4), radius);
						if (collision) {
							candidates.remove(candidate);
							continue;
						}
					}
					if (v < heightInBuckets - 1) {
						int i5 = index + 1;
						collision = collides(candidate, pickList.get(i5), radius);
						if (collision) {
							candidates.remove(candidate);
							continue;
						}
					}
					if (u < widthInBuckets - 1 && v < heightInBuckets - 1) {
						int i6 = index + 1 + widthInBuckets;
						collision = collides(candidate, pickList.get(i6), radius);
						if (collision) {
							candidates.remove(candidate);
							continue;
						}
					}

					collision = collides(candidate, obstacles, radius);
					if (collision) {
						candidates.remove(candidate);
						continue;
					}

					// we survived, add to pick list.
					candidate.radius = radius;
					pickList.get(index).add(candidate);
				}
			}
		}

		return pickList;
	}

	private boolean collides(Location candidate, List<Location> existingPicks, double collisionDistance) {
		for (Location loc : existingPicks) {
			double dist = Math.sqrt(
					(loc.x - candidate.x) * (loc.x - candidate.x) + (loc.y - candidate.y) * (loc.y - candidate.y));
			if (dist <= collisionDistance + loc.radius) {
				return true;
			}
		}
		return false;
	}
}
