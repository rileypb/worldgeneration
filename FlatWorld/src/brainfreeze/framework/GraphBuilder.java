package brainfreeze.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.graph.DefaultUndirectedGraph;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;
import org.locationtech.jts.triangulate.quadedge.QuadEdge;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;
import org.locationtech.jts.triangulate.quadedge.Vertex;

import brainfreeze.world.Graphs;
import brainfreeze.world.Location;
import brainfreeze.world.MapEdge;
import de.alsclo.voronoi.graph.Point;

public class GraphBuilder {
	public static Graphs buildGraph(ArrayList<Point> initialSites, double xMin, double xMax, double yMin, double yMax) {
		List<Location> clip = new ArrayList<Location>();
		clip.add(new Location(xMin, yMin));
		clip.add(new Location(xMax, yMin));
		clip.add(new Location(xMax, yMax));
		clip.add(new Location(xMin, yMax));
		return buildGraph(initialSites, clip, 0);
	}

	public static Graphs buildGraph(ArrayList<Point> initialSites, List<Location> clippingPolygon, int relaxations) {
		Graphs graphs = buildVoronoi(initialSites, clippingPolygon);
		for (int i = 0; i < relaxations; i++) {
			relax(graphs);
			graphs = buildVoronoi(initialSites, clippingPolygon);
		}

		return graphs;
	}

	private static void relax(Graphs graphs) {
		graphs.dualVertices.forEach((site) -> {
			Set<Location> vertices = site.sides.stream().flatMap((side) -> {
				return Arrays.stream(new Location[] { side.loc1, side.loc2 });
			}).collect(Collectors.toSet());
			double avgX = vertices.stream().mapToDouble((v) -> {
				return v.getX();
			}).average().getAsDouble();
			double avgY = vertices.stream().mapToDouble((v) -> {
				return v.getY();
			}).average().getAsDouble();
			site.setX(avgX);
			site.setY(avgY);
		});
	}

	private static Graphs buildVoronoi(ArrayList<Point> initialSites, List<Location> clippingPolygon) {
		if (clippingPolygon == null) {
			clippingPolygon = new ArrayList<Location>();
			clippingPolygon.add(new Location(0, 0));
			clippingPolygon.add(new Location(1, 0));
			clippingPolygon.add(new Location(1, 1));
			clippingPolygon.add(new Location(0, 1));
		}
		Location center = Location.average(clippingPolygon);

		GeometryFactory geomFact = new GeometryFactory();
		Geometry clipGeometry = buildClipGeometry(clippingPolygon, geomFact);
		Geometry convexHull = clipGeometry.convexHull();

		DefaultUndirectedGraph<Location, MapEdge> voronoiGraph = new DefaultUndirectedGraph<>(MapEdge.class);
		DefaultUndirectedGraph<Location, MapEdge> dualGraph = new DefaultUndirectedGraph<>(MapEdge.class);

		Set<Location> dualVertices = new HashSet<>();
		Map<Coordinate, Location> pointsToLocations = new HashMap<>();
		Set<MapEdge> dualEdges = new HashSet<>();
		Set<Location> voronoiVertices = new HashSet<>();
		Set<MapEdge> voronoiEdges = new HashSet<>();
		Map<MapEdge, MapEdge> voronoiToDual = new HashMap<>();
		Map<MapEdge, MapEdge> dualToVoronoi = new HashMap<>();

		System.out.println("Generating initial sites...");
		Collection<Coordinate> coords = new ArrayList<Coordinate>();
		for (Point p : initialSites) {
			Coordinate coordinate = new Coordinate(p.x, p.y);
			coords.add(coordinate);
		}

		System.out.println("Building voronoi diagram...");
		VoronoiDiagramBuilder vBuilder = new VoronoiDiagramBuilder();
		vBuilder.setSites(coords);
		QuadEdgeSubdivision subdivision = vBuilder.getSubdivision();

		System.out.println("Generating graph structure...");
		Geometry voronoiDiagram = subdivision.getVoronoiDiagram(geomFact);

		Collection<Vertex> vertices = subdivision.getVertices(false);

		for (Vertex v : vertices) {
			Location loc = new Location(v.getCoordinate());
			dualVertices.add(loc);
			dualGraph.addVertex(loc);
			pointsToLocations.put(v.getCoordinate(), loc);
		}

		Collection<QuadEdge> edges = subdivision.getEdges();

		for (int i = 0; i < voronoiDiagram.getNumGeometries(); i++) {
			Geometry cell = voronoiDiagram.getGeometryN(i);
			Coordinate centerCoords = (Coordinate) cell.getUserData();
			Location centerLocation = pointsToLocations.get(centerCoords);
			Geometry clippedCell = cell.intersection(convexHull);
			if (centerLocation != null) {
				if (cell.intersects(clipGeometry)) {
					centerLocation.boundaryLocation = true;
				}
				// extract voronoi edges
				Coordinate[] coordinates = clippedCell.getCoordinates();
				for (int k = 0; k < coordinates.length; k++) {
					Coordinate c0 = coordinates[k];

					Coordinate c1;
					if (k == coordinates.length - 1) {
						c1 = coordinates[0];
					} else {
						c1 = coordinates[k + 1];
					}
					Location loc0 = pointsToLocations.get(c0);
					Location loc1 = pointsToLocations.get(c1);
					if (loc0 == null) {
						loc0 = new Location(c0);
						pointsToLocations.put(c0, loc0);
						voronoiVertices.add(loc0);
						voronoiGraph.addVertex(loc0);

						org.locationtech.jts.geom.Point p = geomFact.createPoint(c0);
						if (clipGeometry.intersects(p)) {
							loc0.boundaryLocation = true;
						}
					}
					loc0.adjacentCells.add(centerLocation);
					if (loc1 == null) {
						loc1 = new Location(c1);
						pointsToLocations.put(c1, loc1);
						voronoiVertices.add(loc1);
						voronoiGraph.addVertex(loc1);

						org.locationtech.jts.geom.Point p = geomFact.createPoint(c1);
						if (clipGeometry.intersects(p)) {
							loc1.boundaryLocation = true;
						}
					}
					loc1.adjacentCells.add(centerLocation);
					if (loc0 != null && loc1 != null) {
						Set<MapEdge> existingEdges = voronoiGraph.getAllEdges(loc0, loc1);

						MapEdge newEdge;
						if (existingEdges.size() > 0) {
							newEdge = existingEdges.iterator().next();
						} else {
							newEdge = new MapEdge(loc0, loc1);
							voronoiEdges.add(newEdge);
							voronoiGraph.addEdge(loc0, loc1, newEdge);
						}

						centerLocation.sides.add(newEdge);
						newEdge.adjacentCells.add(centerLocation);
					}
				}
			}
		}

		// at this point, the voronoi graph has been built and the center locations cataloged. 
		// now we need to record adjacency info for voronoi edges and center locations.

		for (QuadEdge edge : edges) {
			Polygon cell1 = subdivision.getVoronoiCellPolygon(edge, geomFact);
			Polygon cell2 = subdivision.getVoronoiCellPolygon(edge.sym(), geomFact);
			Object center1 = cell1.getUserData();
			Object center2 = cell2.getUserData();
			Polygon clippedCell1 = (Polygon) cell1.intersection(convexHull);
			Polygon clippedCell2 = (Polygon) cell2.intersection(convexHull);
			Location centerLoc1 = pointsToLocations.get(center1);
			Location centerLoc2 = pointsToLocations.get(center2);

			// if both "centers" are actual voronoi sites
			// there may be polygons that are formed incidentally 
			// outside the boundaries of the diagram, and aren't voronoi polygons.
			if (centerLoc1 != null && centerLoc2 != null) {
				if (!dualGraph.containsEdge(centerLoc1, centerLoc2)) {
					MapEdge newEdge = new MapEdge(centerLoc1, centerLoc2);
					dualGraph.addEdge(centerLoc1, centerLoc2, newEdge);
					dualEdges.add(newEdge);
				}

				centerLoc1.adjacentCells.add(centerLoc2);
				centerLoc2.adjacentCells.add(centerLoc1);
				Geometry commonEdge = clippedCell1.intersection(clippedCell2);
				if (!commonEdge.isEmpty()) {
					Coordinate[] coordinates = commonEdge.getCoordinates();
					Location c0 = pointsToLocations.get(coordinates[0]);
					Location c1 = pointsToLocations.get(coordinates[1]);
					Set<MapEdge> allEdges = voronoiGraph.getAllEdges(c0, c1);
					for (MapEdge e : allEdges) {
						//						e.adjacentCells.add(c0);
						//						e.adjacentCells.add(c1);
					}
				}
			}
		}

		dualVertices.stream().filter((loc) -> {
			return loc.boundaryLocation;
		}).forEach((loc) -> {
			loc.sides.stream().flatMap((edge) -> {
				return Arrays.stream(new Location[] { edge.loc1, edge.loc2 });
			}).forEach((v) -> {
				v.boundaryLocation = true;
			});
		});

		Graphs graphs = new Graphs(voronoiGraph, dualGraph, dualVertices, voronoiVertices, dualEdges, voronoiEdges,
				dualToVoronoi, voronoiToDual);
		return graphs;
	}

	private static Geometry buildClipGeometry(List<Location> clip, GeometryFactory geomFact) {
		Coordinate[] coordinates = new Coordinate[clip.size() + 1];
		for (int i = 0; i < clip.size(); i++) {
			coordinates[i] = new Coordinate(clip.get(i).getX(), clip.get(i).getY());
		}
		coordinates[clip.size()] = new Coordinate(clip.get(0).getX(), clip.get(0).getY());
		LineString ls = geomFact.createLineString(coordinates);
		return ls;
	}
}
