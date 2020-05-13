package voronoinew;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.random.HaltonSequenceGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultUndirectedGraph;

import com.flowpowered.noise.module.source.Perlin;

import de.alsclo.voronoi.Voronoi;
import de.alsclo.voronoi.graph.Edge;
import de.alsclo.voronoi.graph.Graph;
import de.alsclo.voronoi.graph.Point;
import de.alsclo.voronoi.graph.Vertex;

public class TerrainBuilder2 {
	private static final double CONSTANT_FLUX = 1;
	private int numberOfPoints;
	private CellType cellType;
	private ArrayList<Location> depressions;
	private int numberOfLakes;
	private Lake[] lakes;

	public TerrainBuilder2(int numberOfPoints, CellType cellType) {
		this.numberOfPoints = numberOfPoints;
		this.cellType = cellType;
	}

	public enum CellType {
		VORONOI
	}

	public Graphs run(Random r, int relaxations) {
		ArrayList<Point> initialSites = new ArrayList<>();

		switch (cellType) {
		case VORONOI:
			//			generateHaltonSequencePoints(initialSites, r, numberOfPoints);
			generateRandomPoints(initialSites, r, numberOfPoints);
			break;
		}

		System.out.println("creating voronoi diagram...");
		Voronoi voronoi = new Voronoi(initialSites);

		for (int i = 0; i < relaxations; i++) {
			voronoi = voronoi.relax();
		}

		Graph graph = voronoi.getGraph();

		Graphs graphs = generateGraphs(graph);

		return graphs;

	}

	private void generateHaltonSequencePoints(List<Point> points, Random r, int numberOfPoints) {
		HaltonSequenceGenerator hsg = new HaltonSequenceGenerator(2);
		hsg.skipTo(20 + r.nextInt(100000));
		for (int i = 0; i < numberOfPoints; i++) {
			double[] vector = hsg.nextVector();
			points.add(new Point(vector[0], vector[1]));
		}
	}

	private void generateRandomPoints(List<Point> points, Random r, int numberOfPoints) {
		for (int i = 0; i < numberOfPoints; i++) {
			points.add(new Point(r.nextDouble(), r.nextDouble()));
		}
	}

	private Graphs generateGraphs(Graph graph) {
		DefaultUndirectedGraph<Location, MapEdge> voronoiGraph = new DefaultUndirectedGraph<>(MapEdge.class);
		DefaultUndirectedGraph<Location, MapEdge> dualGraph = new DefaultUndirectedGraph<>(MapEdge.class);

		Set<Location> dualVertices = new HashSet<>();
		Map<Point, Location> pointsToLocations = new HashMap<>();
		Set<MapEdge> dualEdges = new HashSet<>();
		Set<Location> voronoiVertices = new HashSet<>();
		Set<MapEdge> voronoiEdges = new HashSet<>();
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
			if (site1 != null && site2 != null) {// && a != null & b != null) {
				Location loc1 = pointsToLocations.get(site1);
				Location loc2 = pointsToLocations.get(site2);
				MapEdge edge = new MapEdge(loc1, loc2);
				dualGraph.addEdge(loc1, loc2, edge);
				dualEdges.add(edge);

				if (a != null && b != null) {
					Point pointa = e.getA().getLocation();
					Point pointb = e.getB().getLocation();
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
				} else {
					edge.boundaryEdge = true;
					edge.loc1.boundaryLocation = true;
					edge.loc2.boundaryLocation = true;
				}
			}
		});

		voronoiVertices.forEach((loc) -> {
			if (voronoiGraph.degreeOf(loc) < 3) {
				loc.boundaryLocation = true;
			}
		});

		Graphs graphs = new Graphs(voronoiGraph, dualGraph, dualVertices, voronoiVertices, dualEdges, voronoiEdges,
				dualToVoronoi, voronoiToDual);

		return graphs;
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
		graphs.voronoiVertices.forEach((loc) -> {
			double latitude = 0.5 - loc.y;
			double distanceFromEquator = Math.abs(latitude);
			double angleFromEquator = Math.PI * distanceFromEquator;
			double baseTemperature = Math.cos(angleFromEquator);
			double adjustedTemperature = baseTemperature
					- (loc.elevation - MapperMain.SEALEVEL) * (loc.elevation - MapperMain.SEALEVEL)
					+ 0.1 * loc.temperatureVariance;
			loc.temperature = adjustedTemperature;
		});
	}

	public void setBiomes(Graphs graphs) {
		graphs.voronoiVertices.forEach((loc) -> {
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

	public void normalizeElevations(Graphs buildResult) {
		double average = buildResult.voronoiVertices.stream().mapToDouble((v) -> {
			return v.elevation;
		}).average().getAsDouble();
		double min = buildResult.voronoiVertices.stream().mapToDouble((v) -> {
			return v.elevation;
		}).min().getAsDouble();
		double max = buildResult.voronoiVertices.stream().mapToDouble((v) -> {
			return v.elevation;
		}).max().getAsDouble();

		double shift = (max + min) / 2;
		double scale = 2 / (max - min);

		buildResult.voronoiVertices.forEach((v) -> {
			if (v.elevation < min) {
				throw new IllegalStateException();
			}
			v.elevation = -1 + 2 * (v.elevation - min) / (max - min);
		});
		buildResult.dualVertices.forEach((v) -> {
			v.elevation = -1 + 2 * (v.elevation - min) / (max - min);
		});
		buildResult.voronoiEdges.forEach((e) -> {
			e.elevation = -1 + 2 * (e.elevation - min) / (max - min);
		});
		buildResult.dualEdges.forEach((e) -> {
			e.elevation = -1 + 2 * (e.elevation - min) / (max - min);
		});

	}

	public void fillDepressions(Graphs graphs) {
		DefaultUndirectedGraph<Location, MapEdge> graphWithLakes = new DefaultUndirectedGraph<>(MapEdge.class);
		lakes = new Lake[numberOfLakes];
		for (int i = 0; i < numberOfLakes; i++) {
			lakes[i] = new Lake(0, 0);
			lakes[i].elevation = 0.5;
			graphWithLakes.addVertex(lakes[i]);
		}

		graphs.dualVertices.forEach((v) -> {
			if (v.isLake) {
				lakes[v.lakeNumber].addVertex(v);
				v.lake = lakes[v.lakeNumber];
			} else {
				graphWithLakes.addVertex(v);
			}
		});

		graphs.dualEdges.forEach((edge) -> {
			Location loc1 = edge.loc1;
			Location loc2 = edge.loc2;
			if (loc1.isLake) {
				loc1 = loc1.lake;
			}
			if (loc2.isLake) {
				loc2 = loc2.lake;
			}

			if (loc1 != loc2) {
				if (!graphWithLakes.containsEdge(loc1, loc2)) {
					graphWithLakes.addEdge(loc1, loc2, new MapEdge(loc1, loc2));
				}
			}
		});

		this.depressions = new ArrayList<>();
		graphWithLakes.vertexSet().forEach((v) -> {
			List<Location> neighbors = graphWithLakes.edgesOf(v).stream().map((edge1) -> {
				return edge1.oppositeLocation(v);
			}).collect(Collectors.toList());
			double min = neighbors.stream().mapToDouble((n) -> {
				return n.elevation;
			}).min().orElse(Double.POSITIVE_INFINITY);
			if (min >= v.elevation) {
				depressions.add(v);
			}
		});

		graphWithLakes.vertexSet().forEach((v) -> {
			v.pdElevation = (v.water || v.boundaryLocation) ? v.elevation : Double.POSITIVE_INFINITY;
		});

		double epsilon = 0.000001;
		boolean somethingDone = true;
		while (somethingDone) {
			somethingDone = false;
			for (Location c : graphWithLakes.vertexSet()) {
				if (c.pdElevation > c.elevation) {
					List<Location> neighbors = graphWithLakes.edgesOf(c).stream().map((edge1) -> {
						return edge1.oppositeLocation(c);
					}).collect(Collectors.toList());

					for (Location n : neighbors) {
						if (c.elevation >= n.pdElevation + epsilon) {
							c.pdElevation = c.elevation;
							somethingDone = true;
							//	break;
						}
						if (c.pdElevation > n.pdElevation + epsilon) {
							c.pdElevation = n.pdElevation + epsilon;
							somethingDone = true;
						}
					}
				}
			}
		}

		graphWithLakes.vertexSet().forEach((v) -> {
			v.elevation = v.pdElevation;
		});

		for (Lake l : lakes) {
			for (Location loc : l.getVertices()) {
				loc.elevation = l.elevation;
				loc.water = true;
			}
			double min = graphWithLakes.edgesOf(l).stream().map((e) -> {
				return e.oppositeLocation(l);
			}).mapToDouble((v) -> {
				return v.elevation;
			}).min().orElse(Double.POSITIVE_INFINITY);
			double max = graphWithLakes.edgesOf(l).stream().map((e) -> {
				return e.oppositeLocation(l);
			}).mapToDouble((v) -> {
				return v.elevation;
			}).max().orElse(Double.POSITIVE_INFINITY);
			if (min >= l.elevation) {
				throw new IllegalStateException();
			}
			if (max <= l.elevation) {
				throw new IllegalStateException();
			}
		}

		for (Location loc : graphs.dualVertices) {
			if (loc.isLake) {
				assert (lakes[loc.lakeNumber].getVertices().contains(loc));
			}
		}
	}

	public void runRivers(Graphs graphs) {
		DefaultDirectedGraph<Location, MapEdge> auxGraph = new DefaultDirectedGraph<>(MapEdge.class);

		graphs.dualVertices.forEach((loc) -> {
			auxGraph.addVertex(loc);
		});

		graphs.dualVertices.forEach((loc) -> {
			Set<MapEdge> neighborEdges = graphs.dualGraph.edgesOf(loc);

			MapEdge newEdge = null;
			double minHeight = Double.POSITIVE_INFINITY;
			for (MapEdge edge : neighborEdges) {
				if (minHeight > edge.oppositeLocation(loc).elevation) {
					newEdge = edge;
					minHeight = edge.oppositeLocation(loc).elevation;
				}
			}

			if (minHeight >= loc.elevation && !loc.boundaryLocation && !loc.water) {
				throw new IllegalStateException();
			} else if (minHeight > loc.elevation && !loc.boundaryLocation && loc.isLake) {
				throw new IllegalStateException();
			} else {
				if (newEdge != null) {
					newEdge.river = true;
					if (loc.isLake && newEdge.oppositeLocation(loc).elevation > loc.elevation) {
						int a = 0;
					}
					auxGraph.addEdge(loc, newEdge.oppositeLocation(loc), newEdge);
				}
			}
		});

		graphs.riverGraph = auxGraph;

		// now label vertices in river graph with height
		boolean changed = true;
		while (changed) {
			changed = graphs.dualVertices.stream().reduce(Boolean.FALSE, (c, v) -> {
				if (v.graphHeight == -1) {
					Set<MapEdge> outgoingEdges = auxGraph.outgoingEdgesOf(v);
					if (outgoingEdges.size() > 1) {
						throw new IllegalStateException();
					} else if (outgoingEdges.size() == 1) {
						MapEdge edge = outgoingEdges.iterator().next();
						Location nextVertex = edge.oppositeLocation(v);
						if (nextVertex.graphHeight >= 0) {
							v.graphHeight = nextVertex.graphHeight + 1;
							return true;
						}
					} else {
						v.graphHeight = 0;
						return true;
					}
				}
				return c;
			}, (c, c2) -> {
				return c || c2;
			});
		}

		// now calculate flux
		ArrayList<Location> vertices = new ArrayList<Location>(graphs.dualVertices);
		vertices.sort((v1, v2) -> {
			return v2.graphHeight - v1.graphHeight;
		});

		for (Location v : vertices) {
			Set<MapEdge> incomingEdges = auxGraph.incomingEdgesOf(v);
			double flux = Math.max(0, Math.min(2, 1 + v.baseMoisture));
			int i = 0;
			for (MapEdge edge : incomingEdges) {
//				if (edge.elevation == v.elevation) {
//					continue;
//				}
				i++;
				flux += edge.flux;

				if (edge.oppositeLocation(v).isLake) {
					if (!v.isLake) {
						System.out.println("fooooo");
						System.out.println(edge.oppositeLocation(v));
					}
					v.foo = true;
					
					flux += 100;//3 * edge.oppositeLocation(v).lake.getVertices().size();
					edge.oppositeLocation(v).riverHead = true;
					edge.oppositeLocation(v).flux = 100;
					edge.oppositeLocation(v).foo = true;
				}
			}

			Set<MapEdge> outgoingEdges = auxGraph.outgoingEdgesOf(v);
			if (!outgoingEdges.isEmpty()) {
				MapEdge outgoingEdge = outgoingEdges.iterator().next();
				outgoingEdge.flux = flux;
				v.flux = flux;
			} else {
				int a = 0;
			}

			v.riverJuncture = i > 1; //auxGraph.incomingEdgesOf(v).size() > 1;
			v.riverHead = v.riverHead && i == 0; //v.riverHead || auxGraph.incomingEdgesOf(v).size() == 0;
		}

		// finally compile into separate paths
		List<Path> paths = new ArrayList<>();
		for (Location v : vertices) {
			if (v.riverHead || v.riverJuncture) {
				if (v.isLake) {
					System.out.println("LAKE!");
				}
				Path p = new Path();
				MapEdge outgoingEdge = singleOutgoingEdge(auxGraph, v);
				Location currentVertex = v;
				while (outgoingEdge != null) {
					p.addPoint(currentVertex);
					currentVertex = outgoingEdge.oppositeLocation(currentVertex);
					outgoingEdge = singleOutgoingEdge(auxGraph, currentVertex);
					if (currentVertex.riverJuncture || outgoingEdge == null) {
						p.addPoint(currentVertex);
						break;
					}
				}
				if (p.size() > 0) {
					p.createRelaxedPath();
					paths.add(p);
				}
			}

		}
		graphs.riverPaths = paths;
	}

	private MapEdge singleOutgoingEdge(DefaultDirectedGraph<Location, MapEdge> graph, Location v) {
		Set<MapEdge> outgoingEdges = graph.outgoingEdgesOf(v);
		MapEdge outgoingEdge = outgoingEdges.size() == 1 ? outgoingEdges.iterator().next() : null;
		return outgoingEdge;
	}

	public void setVoronoiCornerElevations(Graphs graphs) {
		graphs.dualEdges.forEach((edge) -> {
			edge.elevation = (edge.loc1.elevation + edge.loc2.elevation) / 2.0;
			MapEdge voronoiEdge = graphs.dualToVoronoi.get(edge);
			if (voronoiEdge != null) {
				voronoiEdge.elevation = edge.elevation;
			}
		});
		graphs.voronoiVertices.forEach((loc) -> {
			Set<MapEdge> neighborEdges = graphs.voronoiGraph.edgesOf(loc);
			double elevationSum = 0;
			for (MapEdge edge : neighborEdges) {
				elevationSum += edge.elevation;
			}
			loc.elevation = elevationSum / neighborEdges.size();
		});
	}

	public void setDualCornerElevations(Graphs graphs) {
		graphs.voronoiEdges.forEach((edge) -> {
			edge.elevation = (edge.loc1.elevation + edge.loc2.elevation) / 2.0;
			MapEdge dualEdge = graphs.voronoiToDual.get(edge);
			if (dualEdge != null) {
				dualEdge.elevation = edge.elevation;
			}
		});
		graphs.dualVertices.forEach((loc) -> {
			Set<MapEdge> neighborEdges = graphs.dualGraph.edgesOf(loc);
			double elevationSum = 0;
			for (MapEdge edge : neighborEdges) {
				elevationSum += edge.elevation;
			}
			loc.elevation = elevationSum / neighborEdges.size();
		});
	}

	public void markWater(Graphs graphs, double sealevel) {
		double average = graphs.dualVertices.stream().mapToDouble((v) -> {
			return v.elevation;
		}).average().getAsDouble();
		graphs.voronoiVertices.forEach((loc) -> {
			loc.water = (loc.elevation < average + sealevel);
		});
		graphs.dualVertices.forEach((loc) -> {
			loc.water = (loc.elevation < average + sealevel);
			if (loc.water) {
			}
		});
	}

	public void smoothElevations(Graphs graphs) {
		graphs.dualVertices.forEach((loc) -> {
			List<Location> neighbors = neighboringDualVertices(graphs, loc).collect(Collectors.toList());
			double avg = neighbors.stream().mapToDouble((n) -> {
				return n.elevation;
			}).average().getAsDouble();
			loc.pdElevation = avg;
		});

		graphs.dualVertices.forEach((loc) -> {
			loc.elevation = loc.pdElevation;
		});
	}

	private Stream<Location> neighboringDualVertices(Graphs graphs, Location loc) {
		return graphs.dualGraph.edgesOf(loc).stream().map((edge) -> {
			return edge.oppositeLocation(loc);
		});
	}

	public void growForests(Graphs graphs) {
		graphs.dualVertices.forEach((loc) -> {
			List<Location> neighbors = neighboringDualVertices(graphs, loc).collect(Collectors.toList());
			double max = neighbors.stream().mapToDouble((n) -> {
				return n.baseMoisture;
			}).max().getAsDouble();

			if (loc.baseMoisture > 0.15 && max > 0.15) {
				loc.forest = true;
			}
		});
	}

	public void raiseMountains(Graphs graphs, int numPoints) {
		graphs.dualVertices.forEach((loc) -> {
			List<Location> neighbors = neighboringDualVertices(graphs, loc).collect(Collectors.toList());
			double min = neighbors.stream().mapToDouble((n) -> {
				return n.elevation;
			}).min().getAsDouble();

			double diff = loc.elevation - min;
			double dNormal = diff * Math.sqrt(numPoints) / 100;

			if (dNormal > 0.1) {
				loc.mountain = true;
			} else if (dNormal > 0.07) {
				loc.hill = true;
			}
		});
	}

	public void setBaseMoisture(Graphs graphs, Random r, Perlin perlin) {
		generateValues(graphs, r, perlin, (target, value) -> {
			target.baseMoisture = value;
		});
	}

	public void normalizeBaseMoisture(Graphs buildResult) {
		double average = buildResult.dualVertices.stream().mapToDouble((v) -> {
			return v.baseMoisture;
		}).average().getAsDouble();
		double min = buildResult.dualVertices.stream().mapToDouble((v) -> {
			return v.baseMoisture;
		}).min().getAsDouble();
		double max = buildResult.dualVertices.stream().mapToDouble((v) -> {
			return v.baseMoisture;
		}).max().getAsDouble();

		buildResult.dualVertices.forEach((v) -> {
			v.baseMoisture = -1 + 2 * (v.baseMoisture - min) / (max - min);
		});

	}

	public void calculateFinalMoisture(Graphs graphs) {
		graphs.dualVertices.forEach((loc) -> {
			double sum = neighboringDualVertices(graphs, loc).mapToDouble((v) -> {
				return v.flux / 100;
			}).average().getAsDouble();
			loc.moisture = loc.baseMoisture + loc.flux / 100 + sum;
		});
	}

	public void fillInMountainGaps(Graphs graphs) {
		graphs.dualVertices.stream().filter((loc) -> {
			return !loc.mountain;
		}).forEach((loc) -> {
			boolean surroundedByMountains = neighboringDualVertices(graphs, loc).allMatch((v) -> {
				return v.mountain;
			});
			if (surroundedByMountains) {
				loc.tmpMountain = true;
				loc.hill = false;
			}
		});

		graphs.dualVertices.stream().filter((loc) -> {
			return loc.tmpMountain;
		}).forEach((loc) -> {
			loc.mountain = true;
		});
	}

	public void eliminateStrandedWaterAndFindLakes(Graphs graphs, double sealevel) {
		Set<Location> waterVertices = graphs.dualVertices.stream().filter((loc) -> {
			return loc.water;
		}).collect(Collectors.toSet());

		Set<Location> frontierVertices = graphs.dualVertices.stream().filter((loc) -> {
			return loc.water && (loc.x < 0 || loc.y < 0 || loc.x > 1 || loc.y > 1);
		}).collect(Collectors.toSet());

		while (frontierVertices.size() > 0) {
			waterVertices.removeAll(frontierVertices);

			Set<Location> newVertices = new HashSet<Location>();

			frontierVertices.forEach((loc) -> {
				loc.neighboringVertices(graphs.dualGraph).forEach((v) -> {
					if (waterVertices.contains(v)) {
						newVertices.add(v);
					}
				});
			});

			frontierVertices = newVertices;
		}

		numberOfLakes = 0;

		while (waterVertices.size() > 0) {
			Location seedVertex = waterVertices.iterator().next();
			seedVertex.isLake = true;
			seedVertex.lakeNumber = numberOfLakes;
			seedVertex.elevation = sealevel + 0.001;
			Set<Location> lakeVertices = new HashSet<Location>();
			frontierVertices = new HashSet<Location>();
			frontierVertices.add(seedVertex);

			while (frontierVertices.size() > 0) {
				waterVertices.removeAll(frontierVertices);
				Set<Location> newVertices = new HashSet<Location>();
				frontierVertices.forEach((loc) -> {
					loc.neighboringVertices(graphs.dualGraph).forEach((v) -> {
						if (waterVertices.contains(v)) {
							newVertices.add(v);
						}
					});
				});

				for (Location loc : newVertices) {
					loc.isLake = true;
					loc.lakeNumber = numberOfLakes;
					loc.elevation = sealevel + 0.001;
				}
				frontierVertices = newVertices;
			}
			numberOfLakes++;
		}

		System.out.println(numberOfLakes + " lakes");
	}

}
