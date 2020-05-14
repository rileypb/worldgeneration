package voronoiold;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GraphDrawLayer implements DrawLayer {

	@Override
	public void draw(Graphics2D g, Graphs graphs) {
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setBackground(Color.white);
		g.setColor(Color.white);
		Rectangle clipBounds = g.getDeviceConfiguration().getBounds();
		g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);

		double x0 = clipBounds.x - 0;
		double y0 = clipBounds.y - 0;
		double xWidth = clipBounds.width + 0;
		double yHeight = clipBounds.height + 0;


		Color parchment = new Color(144, 126, 96);
		Color seaBlue = new Color(19,18,52);
		
		//		g.setColor(Color.black);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(new BasicStroke(1));

		//				graphs.dualVertices.forEach((loc) -> {
		//					g.drawOval((int)(xWidth*loc.x), (int)(yHeight*loc.y), 1, 1);
		//				});

		g.setColor(Color.blue);
		graphs.dualVertices.forEach((loc) -> {
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

					//				if (loc.elevation >= -10) {
					//					g.setColor(new Color((float) loc.red, (float) loc.green, (float) loc.blue));
					//				} else {
					//					g.setColor(Color.blue);
					//				}
					if (loc.elevation <= MapperMain.SEALEVEL) {
						g.setColor(seaBlue);
					} else {
						//g.setColor(loc.biome.color);
						g.setColor(parchment);
					}
					g.fill(p);
					if (loc.elevation > MapperMain.MOUNTAIN_THRESHOLD) {
						g.setColor(Color.black);
					}
//					if (loc.elevation > MapperMain.SEALEVEL) {
//						g.setColor(Color.LIGHT_GRAY);
						g.draw(p);
//					}
				}
			});
		});

		//		g.setColor(Color.lightGray);
		//		graphs.voronoiVertices.forEach((loc) -> {
		//			List<MapEdge> edgeList = graphs.voronoiGraph.edgesOf(loc).stream().map((dualEdge) -> {
		//				return graphs.voronoiToDual.get(dualEdge);
		//			}).collect(Collectors.toList());
		//
		//			edgeList.forEach((e) -> {
		//				Path2D.Double p = new Path2D.Double();
		//				p.moveTo(x0 + xWidth/2 * loc.x, y0 + yHeight * minmax(0, 1, loc.y));
		//				p.lineTo(x0 + xWidth/2 * e.loc1.x, y0 + yHeight * minmax(0, 1, e.loc1.y));
		//				p.lineTo(x0 + xWidth/2 * e.loc2.x, y0 + yHeight * minmax(0, 1, e.loc2.y));
		//				p.closePath();
		//
		//				g.draw(p);
		//			});
		//		});

		//		graphs.voronoiVertices.forEach((loc) -> {
		//			List<MapEdge> edgeList = graphs.voronoiGraph.edgesOf(loc).stream().map((dualEdge) -> {
		//				return graphs.voronoiToDual.get(dualEdge);
		//			}).collect(Collectors.toList());
		//
		//			edgeList.forEach((e) -> {
		//				Path2D.Double p = new Path2D.Double();
		//				p.moveTo(x0 + xWidth * minmax(0, 1, loc.x) + xWidth, y0 + yHeight * minmax(0, 1, loc.y));
		//				p.lineTo(x0 + xWidth * minmax(0, 1, e.loc1.x) + xWidth, y0 + yHeight * minmax(0, 1, e.loc1.y));
		//				p.lineTo(x0 + xWidth * minmax(0, 1, e.loc2.x) + xWidth, y0 + yHeight * minmax(0, 1, e.loc2.y));
		//				p.closePath();
		//
		//				if (loc.elevation >= 0) {
		//					g.setColor(new Color((float) loc.red, (float) loc.green, (float) loc.blue));
		//				} else {
		//					g.setColor(Color.blue);
		//				}
		//				g.fill(p);
		//			});
		//		});

		//		g.setColor(Color.black);
		//		graphs.dualVertices.forEach((loc) -> {
		//			if (loc.elevation > .5) {
		//
		//				List<MapEdge> edgeList = graphs.dualGraph.edgesOf(loc).stream().map((dualEdge) -> {
		//					return graphs.dualToVoronoi.get(dualEdge);
		//				}).collect(Collectors.toList());
		//
		//				edgeList.forEach((e) -> {
		//					if (e != null) {
		//						g.drawLine((int) (loc.x * xWidth), (int) (loc.y * yHeight), (int) (e.loc1.x * xWidth),
		//								(int) (e.loc1.y * yHeight));
		//						g.drawLine((int) (loc.x * xWidth), (int) (loc.y * yHeight), (int) (e.loc2.x * xWidth),
		//								(int) (e.loc2.y * yHeight));
		//					}
		//				});
		//			}
		//		});

		//		g.setColor(Color.black);
		//		graphs.voronoiEdges.forEach((edge) -> {
		//			//			if (Math.abs(edge.loc1.x - edge.loc2.x) > 0.001 && Math.abs(edge.loc1.y - edge.loc2.y) > 0.005
		//			//					) {//&& Math.abs(edge.loc1.x - edge.loc2.x) < 0.05 && Math.abs(edge.loc1.y - edge.loc2.y) < 0.05) {
		//			g.drawLine((int) (x0 + xWidth /2* edge.loc1.x), (int) (y0 + yHeight * minmax(0,1,edge.loc1.y)),
		//					(int) (x0 + xWidth /2* edge.loc2.x), (int) (y0 + yHeight * minmax(0,1,edge.loc2.y)));
		//			//			}
		//		});

		//		g.setStroke(new BasicStroke(3, 0, 0));
		//		g.setColor(Color.red);
		//		graphs.voronoiEdges.forEach((edge) -> {
		//			if (edge.river) {
		//				g.setColor(edge.riverColor);
		//				g.drawLine((int) (x0 + xWidth * edge.loc1.x), (int) (y0 + yHeight * edge.loc1.y),
		//						(int) (x0 + xWidth * edge.loc2.x), (int) (y0 + yHeight * edge.loc2.y));
		//			}
		//		});

	}

	private double minmax(double min, double max, double value) {
		return Math.min(max, Math.max(min, value));
	}

}
