package brainfreeze.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgrapht.graph.DefaultUndirectedGraph;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;
import org.locationtech.jts.triangulate.quadedge.QuadEdge;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;
import org.locationtech.jts.triangulate.quadedge.Vertex;

import brainfreeze.framework.WorldGeometry.WorldGeometryType;
import brainfreeze.world.Graphs;
import brainfreeze.world.Location;
import brainfreeze.world.MapEdge;

public class GraphBuilder {
	//	public static Graphs buildGraph(ArrayList<Location> initialSites, double xMin, double xMax, double yMin, double yMax) {
	//		List<Location> clip = new ArrayList<Location>();
	//		clip.add(new Location(xMin, yMin));
	//		clip.add(new Location(xMax, yMin));
	//		clip.add(new Location(xMax, yMax));
	//		clip.add(new Location(xMin, yMax));
	//		return buildGraph(initialSites, clip, 0);
	//	}

	public static Graphs buildGraph(List<Location> initialSites, RegionParameters rParams, int relaxations,
			WorldGeometry geometry) {
		Graphs graphs = buildVoronoi(initialSites, rParams, geometry);
		for (int i = 0; i < relaxations; i++) {
			List<Location> relaxedSites = relax(graphs);
			if (geometry.type == WorldGeometryType.CYLINDRICAL) {
				snipCylindricalSites(relaxedSites, null, geometry, rParams.xMin, rParams.xMax);
			}
			graphs = buildVoronoi(relaxedSites, rParams, geometry);
		}

		if (geometry.type == WorldGeometryType.CYLINDRICAL) {
			identifyCylindricalSites(graphs, geometry, rParams.xMin, rParams.xMax);
			snipCylindricalSites(graphs.dualVertices, graphs.voronoiVertices, geometry, rParams.xMin, rParams.xMax);
		}
		
		Graphs g = graphs;
		graphs.dualVertices.forEach((loc) -> {
			for (Location l : loc.adjacentCells) {
				if (!g.dualVertices.contains(l)) {
					throw new IllegalStateException();
				}
			}
		});
		graphs.voronoiVertices.forEach((loc) -> {
			for (Location l : loc.adjacentCells) {
				if (!g.dualVertices.contains(l)) {
					throw new IllegalStateException();
				}
			}
			if (loc.adjacentCells.size() == 0) {
				throw new IllegalStateException();
			}
		});

		return graphs;
	}

	private static List<Location> relax(Graphs graphs) {
		List<Location> relaxedLocations = new LinkedList<Location>();
		graphs.dualVertices.forEach((site) -> {
			if (site.sides.size() > 0) {
				Set<Location> vertices = site.sides.stream().flatMap((side) -> {
					return Arrays.stream(new Location[] { side.loc1, side.loc2 });
				}).collect(Collectors.toSet());
				double avgX = vertices.stream().mapToDouble((v) -> {
					return v.getX();
				}).average().getAsDouble();
				double avgY = vertices.stream().mapToDouble((v) -> {
					return v.getY();
				}).average().getAsDouble();
				Location e = new Location(avgX, avgY);
				e.originalSite = site.originalSite;
				relaxedLocations.add(e);
			}
		});

		return relaxedLocations;
	}

	private static Graphs buildVoronoi(List<Location> initialSites, RegionParameters rParams, WorldGeometry geometry) {
		List<Location> clippingPolygon = rParams.clippingPolygon;
		if (clippingPolygon == null) {
			clippingPolygon = new ArrayList<Location>();
			clippingPolygon.add(new Location(rParams.xMin, rParams.yMin));
			clippingPolygon.add(new Location(rParams.xMax, rParams.yMin));
			clippingPolygon.add(new Location(rParams.xMax, rParams.yMax));
			clippingPolygon.add(new Location(rParams.xMin, rParams.yMax));
		}

		List<Location> sites = new ArrayList<>(initialSites);
		if (geometry.type == WorldGeometryType.CYLINDRICAL) {
			augmentCylindricalSites(sites, geometry, rParams.xMin, rParams.xMax);
		}

		GeometryFactory geomFact = new GeometryFactory();
		Geometry clipGeometry = buildClipGeometry(clippingPolygon, geomFact, geometry);
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
		for (Location p : sites) {
			Coordinate coordinate = new Coordinate(p.getX(), p.getY());
			coords.add(coordinate);
			pointsToLocations.put(coordinate, p);
		}
		for (Location p : sites) {
			if (p.originalSite != null) {
				Location newOriginalSite = pointsToLocations.get(new Coordinate(p.originalSite.getX(), p.originalSite.getY()));
				p.originalSite = newOriginalSite;
			}
		}
		
		System.out.println("Building voronoi diagram...");
		VoronoiDiagramBuilder vBuilder = new VoronoiDiagramBuilder();
		vBuilder.setSites(coords);
		QuadEdgeSubdivision subdivision = vBuilder.getSubdivision();

		System.out.println("Generating graph structure...");
		Geometry voronoiDiagram = subdivision.getVoronoiDiagram(geomFact);

		Collection<Vertex> vertices = subdivision.getVertices(false);

		System.out.println("cataloging vertices...");
		for (Vertex v : vertices) {
			Location loc = pointsToLocations.get(v.getCoordinate());
			dualVertices.add(loc);
			dualGraph.addVertex(loc);
		}

		Collection<QuadEdge> edges = subdivision.getEdges();

		System.out.println("building cells...");
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

						Point p = geomFact.createPoint(c0);
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

						Point p = geomFact.createPoint(c1);
						if (clipGeometry.intersects(p)) {
							loc1.boundaryLocation = true;
						}
					}
					loc1.adjacentCells.add(centerLocation);
					if (!dualVertices.contains(centerLocation)) {
						throw new IllegalStateException();
					}
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

		System.out.println("building adjacency info...");
		for (QuadEdge edge : edges) {
			Polygon cell1 = subdivision.getVoronoiCellPolygon(edge, geomFact);
			Polygon cell2 = subdivision.getVoronoiCellPolygon(edge.sym(), geomFact);
			Object center1 = cell1.getUserData();
			Object center2 = cell2.getUserData();
			Location centerLoc1 = pointsToLocations.get(center1);
			Location centerLoc2 = pointsToLocations.get(center2);
			if (!dualVertices.contains(centerLoc1) && centerLoc1 != null) {
				throw new IllegalStateException();
			}
			if (!dualVertices.contains(centerLoc2) && centerLoc2 != null) {
				throw new IllegalStateException();
			}

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
			}
		}

		System.out.println("labeling boundary vertices...");
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

		dualVertices.forEach((loc) -> {
			for (Location l : loc.adjacentCells) {
				if (!dualVertices.contains(l)) {
					throw new IllegalStateException();
				}
			}
		});
		return graphs;
	}

	private static void augmentCylindricalSites(List<Location> sites, WorldGeometry geometry, double xMin,
			double xMax) {
		double width = xMax - xMin;
		List<Location> newSites = new ArrayList<Location>();
		for (Location site : sites) {
			if (site.getX() < xMax && site.getX() > xMin) {
				Location newSiteLeft = new Location(site.getX() - width, site.getY());
				if (newSiteLeft.getX() > geometry.xMin) {
					newSiteLeft.originalSite = site;
					newSites.add(newSiteLeft);
				}
				Location newSiteRight = new Location(site.getX() + width, site.getY());
				if (newSiteRight.getX() < geometry.xMax) {
					newSiteRight.originalSite = site;
					newSites.add(newSiteRight);
				}
			}
		}
		sites.addAll(newSites);
	}

	private static void snipCylindricalSites(Collection<Location> dualVertices, Collection<Location> voronoiVertices, WorldGeometry geometry, double xMin,
			double xMax) {
		if (voronoiVertices != null) {
			for (Location site : voronoiVertices) {
				List<Location> toRemove = new ArrayList<Location>();
				List<Location> toAdd = new ArrayList<Location>();
				for (Location adj : site.adjacentCells) {
					if (adj.getX() > xMax || adj.getX() < xMin) {
						toRemove.add(adj);
						toAdd.add(adj.originalSite);
					}
				}
				site.adjacentCells.removeAll(toRemove);
				site.adjacentCells.addAll(toAdd);
			}
		}
		
		List<Location> toRemove = new ArrayList<Location>();
		for (Location site : dualVertices) {
			if (site.getX() > xMax || site.getX() < xMin) {
				toRemove.add(site);
			}
		}
		dualVertices.removeAll(toRemove);
	}

	private static void identifyCylindricalSites(Graphs graphs, WorldGeometry geometry, double xMin, double xMax) {
		graphs.dualVertices.forEach((loc) -> {
			Location originalSite = loc.originalSite;
			if (originalSite != null) {
				loc.adjacentCells.forEach((neighbor) -> {
					if (neighbor.originalSite == null) {
						neighbor.adjacentCells.remove(loc);
						neighbor.adjacentCells.add(originalSite);
						originalSite.adjacentCells.add(neighbor);
					}
				});
			}
		});
		List<Location> toRemove = new ArrayList<Location>();
		for (Location site : graphs.dualVertices) {
			if (site.originalSite != null) {
				toRemove.add(site);
			}
		}
		//		graphs.dualVertices.removeAll(toRemove);

		// also remove all old voronoi edges
		graphs.voronoiEdges.removeAll(graphs.voronoiEdges.stream().filter((edge) -> {
			boolean remove = edge.adjacentCells.stream().allMatch((cell) -> {
				return cell.originalSite != null;
			});
			return remove;
		}).collect(Collectors.toSet()));

	}

	public static Geometry buildClipGeometry(List<Location> clip, GeometryFactory geomFact, WorldGeometry geometry) {
		List<Location> clippingPolygon = clip;
		switch (geometry.type) {
		case CYLINDRICAL:
			clippingPolygon = new ArrayList<Location>();
			clippingPolygon.add(new Location(geometry.xMin, geometry.yMin));
			clippingPolygon.add(new Location(geometry.xMax, geometry.yMin));
			clippingPolygon.add(new Location(geometry.xMax, geometry.yMax));
			clippingPolygon.add(new Location(geometry.xMin, geometry.yMax));

		default:
			Coordinate[] coordinates = new Coordinate[clippingPolygon.size() + 1];
			for (int i = 0; i < clippingPolygon.size(); i++) {
				coordinates[i] = new Coordinate(clippingPolygon.get(i).getX(), clippingPolygon.get(i).getY());
			}
			coordinates[clippingPolygon.size()] = new Coordinate(clippingPolygon.get(0).getX(),
					clippingPolygon.get(0).getY());
			LineString ls = geomFact.createLineString(coordinates);
			return ls;
		}
	}
}
