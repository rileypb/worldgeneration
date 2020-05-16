package voronoi;

import java.util.Set;

public class TownPlanner {
	private int points;

	public TownPlanner(int points) {
		this.points = points;
	}

	public void placeTowns(Graphs graphs) {
		double maxScore = Double.NEGATIVE_INFINITY;
		Location bestSite = null;
		for (Location loc : graphs.dualVertices) {
			double siteScore = scoreOneSite(graphs, loc);
			if (siteScore > maxScore) {
				maxScore = siteScore;
				bestSite = loc;
			}
		}
		if (bestSite != null) {
			bestSite.town = true;
			graphs.towns.add(bestSite);
		}
	}

	private double scoreOneSite(Graphs graphs, Location loc) {
		double siteScore = 0;
		double sizeFactor = Math.sqrt(points) / 70;

		// are other towns close by?
		for (Location town : graphs.towns) {
			double distance = Math.sqrt((town.x - loc.x) * (town.x - loc.x) + (town.y - loc.y) * (town.y - loc.y));
			if (distance < 0.01) {
				siteScore = Double.NEGATIVE_INFINITY;
			} else if (distance < 0.03) {
				siteScore -= 10;
			}
		}

		// is there a city close by?
		for (Location city : graphs.cities) {
			double distance = Math.sqrt((city.x - loc.x) * (city.x - loc.x) + (city.y - loc.y) * (city.y - loc.y));
			if (distance < 0.01) {
				siteScore = Double.NEGATIVE_INFINITY;
			} else {
				siteScore += 0.05 / distance;
			}
		}

		if (loc.x < 0.1 || loc.x > .9 || loc.y < 0.1 || loc.y > 0.9) {
			siteScore = Double.NEGATIVE_INFINITY;
		}

		if (loc.water) {
			siteScore = Double.NEGATIVE_INFINITY;
		} else if (loc.mountain) {
			siteScore -= 5;
		} else if (loc.hill) {
			siteScore -= 3;
		} else if (loc.forest) {
			siteScore -= 2;
		}

		if (loc.riverJuncture) {
			siteScore += loc.flux / (2 * sizeFactor);
		} else {
			siteScore += loc.flux / (3 * sizeFactor);
		}

		if (loc.road) {
			siteScore += 3;
		}

		Set<Location> neighboringVertices = loc.neighboringVertices(graphs.dualGraph);
		for (Location neighbor : neighboringVertices) {
			if (neighbor.water) {
				siteScore += 10;
			}
		}

		loc.cityScore = siteScore;
		return siteScore;
	}
}
