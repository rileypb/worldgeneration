package voronoinew;

import java.awt.Color;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseDrawLayer implements DrawLayer {
	protected abstract void setBaseColors(Graphs graphs);

	protected void setVertexColors(Graphs graphs, Color waterColor) {
		graphs.voronoiVertices.forEach((loc) -> {
			Set<Location> nearbySites = graphs.voronoiGraph.edgesOf(loc).stream().map((e) -> {
				return graphs.voronoiToDual.get(e);
			}).flatMap((dualEdge) -> {
				return Arrays.stream(new Location[] { dualEdge.loc1, dualEdge.loc2 });
			}).filter((s) -> {
				return !s.water;
			}).collect(Collectors.toSet());
	
			int numNeighbors = nearbySites.size();
	
			if (numNeighbors < 3) {
				int a = 0;
			}
	
			if (numNeighbors == 0) {
				loc.color = waterColor;
			} else {
				int aAll = 0;
				int rAll = 0;
				int gAll = 0;
				int bAll = 0;
				for (Location site : nearbySites) {
					Color c = site.color;
					aAll += c.getAlpha();
					rAll += c.getRed();
					gAll += c.getGreen();
					bAll += c.getBlue();
				}
				if (rAll == 0 || gAll == 0 || bAll == 0) {
					int a = 0;
				}
				Color newColor = new Color(rAll / numNeighbors, gAll / numNeighbors, bAll / numNeighbors,
						aAll / numNeighbors);
				loc.color = newColor;
			}
		});
	}
}
