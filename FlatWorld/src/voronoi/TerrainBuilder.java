package voronoi;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.random.HaltonSequenceGenerator;
import org.jgrapht.graph.DefaultUndirectedGraph;

import com.flowpowered.noise.module.source.Perlin;

import de.alsclo.voronoi.Voronoi;
import de.alsclo.voronoi.graph.Edge;
import de.alsclo.voronoi.graph.Graph;
import de.alsclo.voronoi.graph.Point;
import de.alsclo.voronoi.graph.Vertex;

public class TerrainBuilder {
	private int numberOfPoints;
	private CellType cellType;

	public TerrainBuilder(int numberOfPoints, CellType cellType) {
		this.numberOfPoints = numberOfPoints;
		this.cellType = cellType;
	}

	public enum CellType {
		VORONOI, BASE_MODIFIERS
	}

	public Graphs run(Random r) {
		ArrayList<Point> initialSites = new ArrayList<>();

		switch (cellType) {
		case VORONOI:
			generatePoints(initialSites, r, numberOfPoints);
			break;
		case BASE_MODIFIERS:
			generateBaseModifiersPoints(initialSites, r);
			break;
		}

		System.out.println("creating voronoi diagram...");
		Voronoi voronoi = new Voronoi(initialSites);
		Graph graph = voronoi.getGraph();
		Graphs graphs = generateGraphs(graph);

		return graphs;

	}

	private void generateBaseModifiersPoints(ArrayList<Point> initialSites, Random r) {
		Perlin perlin = new Perlin();
		perlin.setSeed(r.nextInt(10000));
		perlin.setFrequency(2);
		perlin.setOctaveCount(1);
		Perlin perlin2 = new Perlin();
		perlin2.setSeed(r.nextInt(10000));
		perlin2.setFrequency(1);
		perlin2.setOctaveCount(1);

		int pointsPerSide = (int) Math.sqrt(numberOfPoints);
		float spacing = 1 / (float) (pointsPerSide + 1);

		for (int i = 0; i < pointsPerSide + 1; i++) {
			for (int j = 0; j < pointsPerSide + 1; j++) {
				float x = spacing * (i);
				float y = spacing * (j);
				double chaosx = Math.max(0.001, PerlinHelper.getCylindricalNoise(perlin, x, 1, y, 1) - .25);
				//				double chaosy = Math.max(0, PerlinHelper.getPlanarNoise(perlin, x + 93.12, 1, y + 2390.2, 1) - 0.25);

				double chaosdx = chaosx * r.nextDouble() * spacing / 2;
				double chaosdy = chaosx * r.nextDouble() + spacing / 2;

				//				dx *= chaosdx; //4 * spacing * Math.max(0.01, PerlinHelper.getPlanarNoise(perlin2, x, 20, y, 20));
				//				dy *= chaosdy;//4 * spacing * Math.max(0.01, PerlinHelper.getPlanarNoise(perlin2, x, 20, y + 909.2, 20));
				//System.out.println(dx + ", " + dy);
				x += chaosdx;
				y += chaosdy;
				Point newPoint = new Point(x, y);
				initialSites.add(newPoint);
				initialSites.add(new Point(x + 1, y));
			}
		}
	}

	private void generatePoints(List<Point> points, Random r, int numberOfPoints) {
		HaltonSequenceGenerator hsg = new HaltonSequenceGenerator(2);
		hsg.skipTo(20 + r.nextInt(100000));
		for (int i = 0; i < numberOfPoints; i++) {
			double[] vector = hsg.nextVector();
			points.add(new Point(vector[0], vector[1]));
			points.add(new Point(vector[0] + 1, vector[1]));
		}
	}

	private Graphs generateGraphs(Graph graph) {
		DefaultUndirectedGraph<Location, MapEdge> voronoiGraph = new DefaultUndirectedGraph<>(MapEdge.class);
		DefaultUndirectedGraph<Location, MapEdge> dualGraph = new DefaultUndirectedGraph<>(MapEdge.class);

		List<Location> dualVertices = new ArrayList<>();
		Map<Point, Location> pointsToLocations = new HashMap<>();
		List<MapEdge> dualEdges = new ArrayList<>();
		List<Location> voronoiVertices = new ArrayList<>();
		List<MapEdge> voronoiEdges = new ArrayList<>();
		Map<MapEdge, MapEdge> voronoiToDual = new HashMap<>();
		Map<MapEdge, MapEdge> dualToVoronoi = new HashMap<>();

		for (Point p : graph.getSitePoints()) {
			Location loc = new Location(p.x, p.y);
			dualGraph.addVertex(loc);
			dualVertices.add(loc);
			pointsToLocations.put(p, loc);
		}

		graph.edgeStream().forEach((Edge e) -> {
			Point site1 = e.getSite1();
			Point site2 = e.getSite2();
			if (site1.equals(site2)) {
				int xvxcvxc = 0;
			}
			Vertex a = e.getA();
			Vertex b = e.getB();
			if (site1 != null && site2 != null) {
				Location loc1 = pointsToLocations.get(site1);
				Location loc2 = pointsToLocations.get(site2);
				MapEdge edge = new MapEdge(loc1, loc2);
				dualGraph.addEdge(loc1, loc2, edge);
				dualEdges.add(edge);

				if (a != null && b != null) {
					Point pointa = e.getA().getLocation();
					Point pointb = e.getB().getLocation();
					if (pointa == pointb) {
						int h = 0;
					}
					Location aLoc = pointsToLocations.get(pointa);
					if (aLoc == null) {
						aLoc = new Location(pointa.x, pointa.y);
						pointsToLocations.put(pointa, aLoc);
					}
					Location bLoc = pointsToLocations.get(pointb);
					if (bLoc == null) {
						bLoc = new Location(pointb.x, pointb.y);
						pointsToLocations.put(pointb, bLoc);
					}
					voronoiGraph.addVertex(aLoc);
					voronoiGraph.addVertex(bLoc);
					voronoiVertices.add(aLoc);
					voronoiVertices.add(bLoc);

					if (!voronoiGraph.containsEdge(aLoc, bLoc)) {
						MapEdge newEdge = new MapEdge(aLoc, bLoc);
						voronoiGraph.addEdge(aLoc, bLoc, newEdge);
						voronoiEdges.add(newEdge);
						dualToVoronoi.put(edge, newEdge);
						voronoiToDual.put(newEdge, edge);
					}
				}
			}
		});

		Graphs graphs = new Graphs(voronoiGraph, dualGraph, dualVertices, voronoiVertices, dualEdges, voronoiEdges,
				dualToVoronoi, voronoiToDual);

		return graphs;
	}

	public void addGentleLine(Graphs graphs, Location startingPoint, MapEdge startingEdge, Color color) {
		Set<Location> visitedPoints = new HashSet<Location>();
		visitedPoints.add(startingPoint);

		Location currentPoint = startingPoint;
		MapEdge currentEdge = startingEdge;

		currentEdge.river = true;
		currentEdge.riverColor = color;
		currentPoint.visited = true;
		currentEdge.oppositeLocation(currentPoint).visited = true;

		//		System.out.println("start: " + currentPoint + ", " + currentEdge);

		boolean done = false;
		while (!done) {
			Vector2D cv = createVector(currentEdge, currentPoint);
			if (cv.getNorm() == 0) {
				return;
			}
			Vector2D enteringVector = cv.normalize();
			//			System.out.println("enteringVector = " + enteringVector);
			//currentPoint = currentEdge.oppositeLocation(currentPoint);

			MapEdge bestEdge = null;
			double maxDot = -100;
			for (MapEdge edge : graphs.voronoiGraph.edgesOf(currentPoint)) {
				//				System.out.println("edge: " + edge);
				Vector2D createVector = createVector(edge, currentPoint);
				if (createVector.getNorm() == 0) {
					continue;
				}
				Vector2D leavingVector = createVector.normalize();
				//				System.out.println("leavingVector = " + leavingVector);
				double dotProduct = -enteringVector.dotProduct(leavingVector);
				//				System.out.println("dotProduct: " + dotProduct);
				Location oppositeLocation = edge.oppositeLocation(currentPoint);
				if (dotProduct > maxDot && !oppositeLocation.visited && !edge.river) {
					maxDot = dotProduct;
					bestEdge = edge;
				}
			}
			if (bestEdge == null) {
				done = true;
			} else {
				currentEdge = bestEdge;
				if (currentEdge.river) {
					return;
				}
				currentPoint = currentEdge.oppositeLocation(currentPoint);
				currentPoint.visited = true;
				if (visitedPoints.contains(currentPoint)) {
					done = true;
				}
				visitedPoints.add(currentPoint);
				currentEdge.river = true;
				currentEdge.riverColor = color;

				//				System.out.println(currentPoint + ", " + currentEdge);
			}
		}
	}

	private Vector2D createVector(MapEdge edge, Location startPoint) {
		Location oppositeLocation = edge.oppositeLocation(startPoint);
		double diffX = oppositeLocation.x - startPoint.x;
		double diffY = oppositeLocation.y - startPoint.y;
		if (diffX == 0 || diffY == 0) {
			int a = 0;
		}
		return new Vector2D(diffX, diffY);
	}

	public void addRandomLine(Graphs graphs, Random rnd) {
		Set<Location> visitedPoints = new HashSet<Location>();
		Location point = graphs.voronoiVertices.get(rnd.nextInt(graphs.voronoiVertices.size()));
		point.visited = true;
		MapEdge lastEdge = null;
		for (int i = 0; i < 100; i++) {
			int count = 100;
			visitedPoints.add(point);
			Location newPoint = point;
			MapEdge newEdge = lastEdge;
			while ((newPoint == point || visitedPoints.contains(newPoint) || newPoint.visited) & count > 0) {
				count--;
				Set<MapEdge> edges = graphs.voronoiGraph.edgesOf(point);
				while (newEdge == lastEdge) {
					Iterator<MapEdge> iterator = edges.iterator();
					int clicks = rnd.nextInt(edges.size());
					for (int j = 0; j < clicks; j++) {
						iterator.next();
					}
					newEdge = iterator.next();
				}
				newPoint = newEdge.oppositeLocation(point);
			}
			if (count == 0) {
				return;
			}
			newEdge.river = true;
			if (newPoint == null) {
				return;
			}
			point = newPoint;
			point.visited = true;
			lastEdge = newEdge;
		}
	}

	public void generateValues(Graphs graphs, Random r, Perlin perlin, Setter setter) {
		double x0 = r.nextDouble();
		double y0 = r.nextDouble();
		graphs.dualVertices.forEach((loc) -> {
			double noise = PerlinHelper.getCylindricalNoise(perlin, x0 + loc.x, 1, y0 + loc.y, 1);
			setter.set(loc, noise);
		});
	}

	public void calculateTemperatures(Graphs graphs) {
		graphs.dualVertices.forEach((loc) -> {
			double latitude = 0.5 - loc.y;
			double distanceFromEquator = Math.abs(latitude);
			double angleFromEquator = Math.PI * distanceFromEquator;
			double baseTemperature = Math.cos(angleFromEquator);
			double adjustedTemperature = baseTemperature
					- (loc.elevation - MapperMain.SEALEVEL) * (loc.elevation - MapperMain.SEALEVEL)
					+ 0.1 * loc.temperatureVariance;
			System.out.println(loc.y + ", " + adjustedTemperature);
			loc.temperature = adjustedTemperature;
		});
	}

	public void setBiomes(Graphs graphs) {
		graphs.dualVertices.forEach((loc) -> {
			double temperature = loc.temperature;
			double moisture = loc.moisture;
			if (moisture < 0.2) {
				if (temperature < 0.2) {
					loc.biome = Biome.TUNDRA;
				} else if (temperature < 0.8) {
					loc.biome = Biome.TEMPERATE_DESERT;
				} else {
					loc.biome = Biome.SUBTROPICAL_DESERT;
				}
			} else if (moisture < 0.66) {
				if (temperature < 0.2) {
					loc.biome = Biome.TAIGA;
				} else if (temperature < 0.8) {
					loc.biome = Biome.GRASSLAND;
				} else {
					loc.biome = Biome.SAVANNA;
				}
			} else {
				if (temperature < 0.2) {
					loc.biome = Biome.SNOW;
				} else if (temperature < 0.8) {
					loc.biome = Biome.TEMPERATE_DECIDUOUS_FOREST;
				} else {
					loc.biome = Biome.TROPICAL_RAIN_FOREST;
				}
			}
		});
	}

	public void forEachLocation(Graphs buildResult, Consumer<Location> consumer) {
		buildResult.dualVertices.forEach(consumer);
	}

}
