package brainfreeze.framework;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.random.HaltonSequenceGenerator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

import brainfreeze.world.Graphs;
import brainfreeze.world.Location;
import brainfreeze.world.MapEdge;
import de.alsclo.voronoi.Voronoi;
import de.alsclo.voronoi.graph.Edge;
import de.alsclo.voronoi.graph.Graph;
import de.alsclo.voronoi.graph.Point;
import de.alsclo.voronoi.graph.Vertex;

public class GraphHelper {
	public static void generateHaltonSequencePoints(List<Point> points, Random r, int numberOfPoints) {
		HaltonSequenceGenerator hsg = new HaltonSequenceGenerator(2);
		hsg.skipTo(20 + r.nextInt(100000));
		for (int i = 0; i < numberOfPoints; i++) {
			double[] vector = hsg.nextVector();
			points.add(new Point(vector[0], vector[1]));
		}
	}

	public static void generateRandomPoints(List<Point> points, Random r, int numberOfPoints,
			Rectangle2D.Double bounds) {
		double minX = bounds.getMinX();
		double minY = bounds.getMinY();
		double dx = bounds.getMaxX() - minX;
		double dy = bounds.getMaxY() - minY;
		for (int i = 0; i < numberOfPoints; i++) {
			points.add(new Point(minX + dx * r.nextDouble(), minY + dy * r.nextDouble()));
		}
	}

	public static void generateRandomPointsClipped(List<Point> points, Random r, int numberOfPoints,
			List<Location> clippingPolygon) {

		List<LineSegment> polygonSegments = new ArrayList<LineSegment>();
		for (int i = 0; i < clippingPolygon.size() - 1; i++) {
			Location l1 = clippingPolygon.get(i);
			Location l2 = clippingPolygon.get(i + 1);
			LineSegment e = new LineSegment(l1.getX(), l1.getY(), l2.getX(), l2.getY());
			polygonSegments.add(e);
		}
		Location l1 = clippingPolygon.get(clippingPolygon.size() - 1);
		Location l2 = clippingPolygon.get(0);
		polygonSegments.add(new LineSegment(l1.getX(), l1.getY(), l2.getX(), l2.getY()));

		for (int i = 0; i < numberOfPoints; i++) {
			Point newPoint = new Point(r.nextDouble(), r.nextDouble());
			//			if (!RegionBuilder.insidePolygon(new Location(newPoint.x, newPoint.y), polygonSegments)) {
			//				i--;
			//				continue;
			//			}

			if (RegionBuilder.insidePolygon(new Location(newPoint.x, newPoint.y), polygonSegments)) {
				points.add(newPoint);
			}
		}
	}

	public static Voronoi relax(Voronoi voronoi) {
		Graph graph = voronoi.getGraph();
		Map<Point, Set<Edge>> edges = new HashMap<>();
		graph.getSitePoints().forEach(p -> edges.put(p, new HashSet<>()));
		graph.edgeStream().forEach(e -> {
			edges.get(e.getSite1()).add(e);
			edges.get(e.getSite2()).add(e);
		});
		List<Point> newPoints = graph.getSitePoints().stream().map(site -> {
			Set<Vertex> vertices = Stream
					.concat(edges.get(site).stream().map(Edge::getA), edges.get(site).stream().map(Edge::getB))
					.collect(Collectors.toSet());
			if (vertices.isEmpty() || vertices.contains(null)) {
				return site;
			} else {
				double avgX = vertices.stream().mapToDouble(v -> v.getLocation().x).average().getAsDouble();
				double avgY = vertices.stream().mapToDouble(v -> v.getLocation().y).average().getAsDouble();
				if (avgX < 0) {
					avgX = Math.random() * 0.001;
				}
				if (avgX > 1) {
					avgX = 1 - Math.random() * 0.001;
				}
				if (avgY < 0) {
					avgY = Math.random() * 0.001;
				}
				if (avgY > 1) {
					avgY = 1 - Math.random() * 0.001;
				}
				return new Point(avgX, avgY);
			}
		}).filter((x) -> {
			return x != null;
		}).collect(Collectors.toList());
		return new Voronoi(newPoints);
	}

	/**
	 * 
	 * @param polygon points must be in order and counter-clockwise.
	 * @param graphs
	 */
	public static void clipGraph(List<Location> polygon, Graphs graphs) {
		for (int i = 0; i < polygon.size() - 1; i++) {
			trim(polygon.get(i), polygon.get(i + 1), graphs);
		}
		trim(polygon.get(polygon.size() - 1), polygon.get(0), graphs);
	}

	public static void trim(Location l1, Location l2, Graphs graphs) {
		LineSegment ls0 = new LineSegment(l1.getX(), l1.getY(), l2.getX(), l2.getY());
		Set<MapEdge> toRemove = new HashSet<>();
		Set<MapEdge> toAdd = new HashSet<MapEdge>();
		graphs.voronoiEdges.forEach((edge) -> {
			LineSegment ls = new LineSegment(edge.loc1.getX(), edge.loc1.getY(), edge.loc2.getX(), edge.loc2.getY());
			Coordinate intersection = ls0.intersection(ls);
			if (intersection != null) {
				int oi1 = ls0.orientationIndex(new Coordinate(edge.loc1.getX(), edge.loc1.getY()));
				int oi2 = ls0.orientationIndex(new Coordinate(edge.loc2.getX(), edge.loc2.getY()));
				Location newLoc = new Location(intersection.x, intersection.y);
				if (oi1 < oi2) {
					// snip edge.loc2
					MapEdge newEdge = new MapEdge(edge.loc1, newLoc);
					graphs.voronoiGraph.addVertex(newLoc);
					graphs.voronoiGraph.addEdge(edge.loc1, newLoc, newEdge);
					graphs.voronoiGraph.removeEdge(edge);
					toRemove.add(edge);
					toAdd.add(newEdge);
				} else {
					// snip edge.loc1
					MapEdge newEdge = new MapEdge(edge.loc2, newLoc);
					graphs.voronoiGraph.addVertex(newLoc);
					graphs.voronoiGraph.addEdge(edge.loc2, newLoc, newEdge);
					graphs.voronoiGraph.removeEdge(edge);
					toRemove.add(edge);
					toAdd.add(newEdge);
				}
			} else if (ls0.orientationIndex(ls) > 0) {
				toRemove.add(edge);
			}
		});
		toRemove.forEach((edge) -> {
			graphs.voronoiEdges.remove(edge);
		});
		toAdd.forEach((edge) -> {
			graphs.voronoiEdges.add(edge);
		});
	}
}
