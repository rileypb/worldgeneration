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

	public List<Location> pick(Random r, double fluxThreshold) {
		List<Location> obstacles = new ArrayList<Location>();

		graphs.dualVertices.forEach((loc) -> {
			double minRadius = loc.sides.stream().filter((e) -> {
				return e != null;
			}).flatMap((e) -> {
				return Arrays.stream(new Location[] { e.loc1, e.loc2 });
			}).mapToDouble((v) -> {
				return Math.sqrt((v.getX() - loc.getX()) * (v.getX() - loc.getX())
						+ (v.getY() - loc.getY()) * (v.getY() - loc.getY()));
			}).min().orElse(Double.NEGATIVE_INFINITY);
			loc.radius = minRadius; //Math.min(.01, maxRadius / 2);

			if ((loc.getX() >= 0 && loc.getX() <= 1 && loc.getY() >= 0 && loc.getY() <= 1) && (loc.water || loc.city
					|| loc.road || loc.secondaryRoad || (loc.river && loc.flux > fluxThreshold))) {
				obstacles.add(loc);
			}

		});

		List<Location> bucketList = new ArrayList<Location>();

		List<Location> pickList = new ArrayList<Location>();

		graphs.dualVertices.forEach((loc) -> {
			if (loc.getX() >= 0 && loc.getX() <= 1 && loc.getY() >= 0 && loc.getY() <= 1) {
				bucketList.add(loc);
			}
		});

		List<Location> candidates = bucketList;
		while (candidates.size() > 0) {
			for (Location candidate : new ArrayList<Location>(candidates)) {
				if (candidate.extra) {
					int a = 0;
				}

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
				boolean collision = collides(candidate, pickList, radius);
				if (collision) {
					candidates.remove(candidate);
					continue;
				}

				collision = collides(candidate, obstacles, radius);
				if (collision) {
					candidates.remove(candidate);
					continue;
				}

				for (int i = -1; i <= 1; i++) {
					for (int j = -1; j <= 1; j++) {
						if (!candidate.extra) {
							double newX = candidate.getX() + Math.random() * i * candidate.radius;
							double newY = candidate.getY() + Math.random() * j * candidate.radius;
							Location newLocation = new Location(newX, newY);
							newLocation.mountain = candidate.mountain;
							newLocation.hill = candidate.hill;
							newLocation.forest = candidate.forest;
							newLocation.water = candidate.water;
							newLocation.radius = candidate.radius;
							newLocation.extra = true;
							if (newX >= 0 && newX <= 1 && newY >= 0 && newY <= 1) {
								candidates.add(newLocation);
							}
						}
					}
				}
				if (candidate.extra) {
					System.out.println("survived: " + candidate);
				}
				candidates.remove(candidate);
				// we survived, add to pick list.
				candidate.radius = radius;
				pickList.add(candidate);
			}
		}

		return pickList;
	}

	private boolean collides(Location candidate, List<Location> existingPicks, double collisionDistance) {
		for (Location loc : existingPicks) {
			double dist = Math.sqrt((loc.getX() - candidate.getX()) * (loc.getX() - candidate.getX())
					+ (loc.getY() - candidate.getY()) * (loc.getY() - candidate.getY()));
			if (dist < collisionDistance + loc.radius) {
				return true;
			}
		}
		return false;
	}
}
