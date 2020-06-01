package brainfreeze.world;

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

	public List<List<Location>> pick(Random r, double fluxThreshold) {
		int widthInBuckets = (int) (1 / maxRadius) + 1;
		double bucketWidth = 1.0 / widthInBuckets;
		int heightInBuckets = (int) (1 / maxRadius) + 1;
		double bucketHeight = 1.0 / heightInBuckets;

		List<Location> obstacles = new ArrayList<Location>();

		graphs.dualVertices.forEach((loc) -> {
			double maxRadius = loc.sides.stream().filter((e) -> {
				return e != null;
			}).flatMap((e) -> {
				return Arrays.stream(new Location[] { e.loc1, e.loc2 });
			}).mapToDouble((v) -> {
				return Math.sqrt((v.getX() - loc.getX()) * (v.getX() - loc.getX())
						+ (v.getY() - loc.getY()) * (v.getY() - loc.getY()));
			}).min().orElse(Double.NEGATIVE_INFINITY);
			loc.radius = maxRadius; //Math.min(.01, maxRadius / 2);
			
			if ((loc.getX() >= 0 && loc.getX() <= 1 && loc.getY() >= 0 && loc.getY() <= 1) && (loc.water || loc.city
					|| loc.road || loc.secondaryRoad || (loc.river && loc.flux > fluxThreshold))) {
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
			if (loc.getX() >= 0 && loc.getX() <= 1 && loc.getY() >= 0 && loc.getY() <= 1) {
				int x = (int) (loc.getX() / bucketWidth);
				int y = (int) (loc.getY() / bucketHeight);
				int index = x * widthInBuckets + y;
				bucketList.get(index).add(loc);
			}
		});

		for (int u = 0; u < widthInBuckets; u++) {
			for (int v = 0; v < heightInBuckets; v++) {
				int index = u * widthInBuckets + v;
				List<Location> candidates = bucketList.get(index);
				while (candidates.size() > 0) {
					for (Location candidate : new ArrayList<Location>(candidates)) {
						double radius = 0;
						if (!candidate.water) {
							if (candidate.hill) {
								radius = candidate.radius;
							} else if (candidate.mountain) {
								radius = candidate.radius;
							} else if (candidate.forest) {
								radius = candidate.radius / 4;
							}
						}

						if (radius == 0) {
							candidates.remove(candidate);
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

						double newX = candidate.getX() + (r.nextDouble() - 0.5) * radius;
						double newY = candidate.getY() + (r.nextDouble() - 0.5) * radius;
						Location newLocation = new Location(newX, newY);
						newLocation.mountain = candidate.mountain;
						newLocation.hill = candidate.hill;
						newLocation.forest = candidate.forest;
						newLocation.water = candidate.water;
						newLocation.radius = candidate.radius;
						candidates.add(newLocation);
						candidates.remove(candidate);

						// we survived, add to pick list.
						candidate.radius = radius;
						pickList.get(index).add(candidate);
					}
				}
			}
		}

		return pickList;
	}

	private boolean collides(Location candidate, List<Location> existingPicks, double collisionDistance) {
		for (Location loc : existingPicks) {
			double dist = Math.sqrt((loc.getX() - candidate.getX()) * (loc.getX() - candidate.getX())
					+ (loc.getY() - candidate.getY()) * (loc.getY() - candidate.getY()));
			if (dist <= collisionDistance + loc.radius) {
				return true;
			}
		}
		return false;
	}
}
