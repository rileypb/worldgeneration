package brainfreeze.world;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BoundaryCellDrawLayer implements DrawLayer {

	private Graphs graphs;

	public BoundaryCellDrawLayer(Graphs graphs) {
		this.graphs = graphs;
	}
	
	@Override
	public void draw(BufferedImage im) {
		Graphics2D g = (Graphics2D) im.getGraphics();
		Rectangle clipBounds = g.getDeviceConfiguration().getBounds();

		double x0 = clipBounds.x + 20;
		double y0 = clipBounds.y + 20;
		double xWidth = clipBounds.width * 0.9;
		double yHeight = clipBounds.height * 0.9;

		g.setColor(Color.pink);

		graphs.dualVertices.forEach((loc) -> {

			if (loc.boundaryLocation) {
				List<MapEdge> edgeList = graphs.dualGraph.edgesOf(loc).stream().map((dualEdge) -> {
					return graphs.dualToVoronoi.get(dualEdge);
				}).collect(Collectors.toList());

				edgeList.forEach((e) -> {
					if (e != null) {
						Path2D.Double p = new Path2D.Double();
						p.moveTo(x0 + xWidth * loc.x, y0 + yHeight * minmax(0, 1, loc.y));
						p.lineTo(x0 + xWidth * e.loc1.x, y0 + yHeight * minmax(0, 1, e.loc1.y));
						p.lineTo(x0 + xWidth * e.loc2.x, y0 + yHeight * minmax(0, 1, e.loc2.y));
						p.closePath();
						g.setColor(Color.pink);
						g.fill(p);
					}
				});
			}
		});
	}

	private double minmax(double min, double max, double value) {
		return Math.min(max, Math.max(min, value));
	}

}
