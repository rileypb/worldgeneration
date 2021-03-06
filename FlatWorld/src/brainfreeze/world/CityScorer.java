package brainfreeze.world;

import java.util.Set;

public class CityScorer {
	private int points;

	public CityScorer(int points) {
		this.points = points;
	}

	public void scoreCitySites(Graphs graphs) {
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
			bestSite.city = true;
			graphs.cities.add(bestSite);
		}
	}

	private double scoreOneSite(Graphs graphs, Location loc) {
		double siteScore = 0;
		double sizeFactor = Math.sqrt(points) / 70;

		// are other cities close by?
		for (Location city : graphs.cities) {
			double distance = Math.sqrt((city.getX() - loc.getX()) * (city.getX() - loc.getX()) + (city.getY() - loc.getY()) * (city.getY() - loc.getY()));
			if (distance < 0.05) {
				siteScore = Double.NEGATIVE_INFINITY;
			} else if (distance <  0.10) {
				siteScore -= 800;
			}
		}
		
		if (loc.getX() < 0.1 || loc.getX() > .9 || loc.getY() < 0.1 || loc.getY() > 0.9) {
			siteScore = Double.NEGATIVE_INFINITY;
		}

		if (loc.water) {
			siteScore = Double.NEGATIVE_INFINITY;
		} else if (loc.mountain) {
			siteScore -= 20;
		} else if (loc.hill) {
			siteScore -= 3;
		} else if (loc.forest) {
			siteScore -= 2;
		}

		if (loc.riverJuncture) {
			siteScore += loc.flux / sizeFactor;
		} else {
			siteScore += loc.flux / (2 * sizeFactor);
		}

		Set<Location> neighboringVertices = loc.neighboringVertices(graphs.dualGraph);
		for (Location neighbor : neighboringVertices) {
			if (neighbor.water) {
				siteScore += 5;
			}
		}

		loc.cityScore = siteScore;
		return siteScore;
	}
}
