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
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
			generatePoints(initialSites, r, numberOfPoints);
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
//			System.out.println(p);
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

		//		dualVertices.forEach((loc) -> {
		//			List<Location> neighboringLocations = dualGraph.edgesOf(loc).stream().map((dualEdge) -> {
		//				return dualEdge.oppositeLocation(loc);
		//			}).collect(Collectors.toList());
		//
		//			boolean boundary = false;
		//			for (Location neighbor : neighboringLocations) {
		//				Set<MapEdge> edgesOf = dualGraph.edgesOf(neighbor);
		//				for (MapEdge edge : edgesOf) {
		//					boundary = boundary || edge.boundaryEdge;
		//					if (boundary)
		//						break;
		//				}
		//				if (boundary)
		//					break;
		//			}
		//			loc.boundaryLocation = boundary;
		//		});

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
//			System.out.println(loc.y + ", " + adjustedTemperature);
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
//			System.out.println(v.elevation);
			return v.elevation;
		}).min().getAsDouble();
		double max = buildResult.voronoiVertices.stream().mapToDouble((v) -> {
			return v.elevation;
		}).max().getAsDouble();

		System.out.println(average + ", " + min + ", " + max);

		double shift = (max + min) / 2;
		double scale = 2 / (max - min);

		buildResult.voronoiVertices.forEach((v) -> {
			if (v.elevation < min) {
				System.out.println(v.elevation + " < " + min);
				throw new IllegalStateException();
			}
//			v.elevation = -1;
//			System.out.print("-1 + 2 * (" + v.elevation + " - " + min + ")/(" + max + " - " + min +") = ");
			v.elevation = -1 + 2*(v.elevation - min)/(max - min);
//			System.out.println(v.elevation);
//			v.elevation = 2*Math.random() - 4;
//			v.elevation -= shift;
//			v.elevation *= scale;
		});
		buildResult.dualVertices.forEach((v) -> {
			v.elevation = -1 + 2*(v.elevation - min)/(max - min);
		});
		buildResult.voronoiEdges.forEach((e) -> {
			e.elevation = -1 + 2*(e.elevation - min)/(max - min);
		});
		buildResult.dualEdges.forEach((e) -> {
			e.elevation = -1 + 2*(e.elevation - min)/(max - min);
		});

		double average2 = buildResult.voronoiVertices.stream().mapToDouble((v) -> {
			return v.elevation;
		}).average().getAsDouble();
		double min2 = buildResult.voronoiVertices.stream().mapToDouble((v) -> {
			return v.elevation;
		}).min().getAsDouble();
		double max2 = buildResult.voronoiVertices.stream().mapToDouble((v) -> {
			return v.elevation;
		}).max().getAsDouble();
		System.out.println(average2 + ", " + min2 + ", " + max2);
	}

	//	public void fillDepressions(Graphs graphs) {
	//		graphs.voronoiVertices.forEach((v) -> {
	//			if (v.boundaryLocation) {
	//				System.out.println("boundary");
	//				v.pdElevation = v.elevation;
	//			} else {
	//				System.out.println("inside");
	//				v.pdElevation = Double.POSITIVE_INFINITY;
	//			}
	//		});
	//
	//		boolean isChanged = true;
	//		while (isChanged) {
	//			System.out.println("loop!!!!!!!1");
	//			isChanged = graphs.voronoiVertices.stream().reduce(Boolean.FALSE, (c, v) -> {
	//				//				System.out.println(c);
	//				Set<MapEdge> edges = graphs.voronoiGraph.edgesOf(v);
	//				double min = edges.stream().mapToDouble((edge) -> {
	//					return edge.oppositeLocation(v).pdElevation;
	//				}).min().orElse(Double.POSITIVE_INFINITY);
	//
	//				//				System.out.println(v.pdElevation + ", min = " + min);
	//
	//				boolean somethingDone = false;
	//				if (v.pdElevation > v.elevation) {
	//					List<Location> neighbors = graphs.voronoiGraph.edgesOf(v).stream().map((e) -> {
	//						return e.oppositeLocation(v);
	//					}).collect(Collectors.toList());
	//					for (Location neighbor : neighbors) {
	//						if (v.elevation >= neighbor.pdElevation + 0.00001) {
	//							v.pdElevation = v.elevation;
	//							somethingDone = true;
	//						} else if (v.pdElevation > neighbor.pdElevation + 0.00001) {
	//							v.pdElevation = neighbor.pdElevation + 0.00001;
	//							somethingDone = true;
	//						}
	//					}
	//				}
	//
	//				return c || somethingDone;
	//			}, (c, c2) -> {
	//				System.out.println(c + " - " + c2);
	//				return c || c2;
	//			});
	//			//			System.out.println("end");
	//		}
	//
	//		graphs.voronoiVertices.forEach((v) -> {
	//			v.elevation = v.pdElevation;
	//		});
	//	}

	public void fillDepressions(Graphs graphs) {
		this.depressions = new ArrayList<>();
		graphs.dualVertices.forEach((v) -> {
			List<Location> neighbors = graphs.dualGraph.edgesOf(v).stream().map((e) -> {
				return e.oppositeLocation(v);
			}).collect(Collectors.toList());
			double min = neighbors.stream().mapToDouble((n) -> {
				return n.elevation;
			}).min().orElse(Double.POSITIVE_INFINITY);
			if (min > v.elevation) {
				depressions.add(v);
//				System.out.println("depression: " + v.hashCode());
			}
		});

		Iterator<Location> iterator = graphs.dualVertices.iterator();
		iterator.next().foo = true;
		iterator.next().foo = true;
		iterator.next().foo = true;
		
		graphs.dualVertices.forEach((v) -> {
			if (v.foo) {
				v.pdElevation = v.elevation;
			} else {
				v.pdElevation = Double.POSITIVE_INFINITY;
			}
		});

		double epsilon = 0.000001;
		boolean somethingDone = true;
		while (somethingDone) {
			somethingDone = false;
			for (Location c : graphs.dualVertices) {
				//				if (c.boundaryLocation) {
				//					continue;
				//				}
				if (c.pdElevation > c.elevation) {
					List<Location> neighbors = graphs.dualGraph.edgesOf(c).stream().map((e) -> {
						return e.oppositeLocation(c);
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

		graphs.dualVertices.forEach((v) -> {
//			System.out.println(v.elevation + " -> " + v.pdElevation);
			v.elevation = v.pdElevation;
		});
	}

	public void runRivers(Graphs graphs) {
		DefaultDirectedGraph<Location, MapEdge> auxGraph = new DefaultDirectedGraph<>(MapEdge.class);

		Map<Location, Location> originalLocation = new HashMap<>();
		Map<MapEdge, MapEdge> originalEdge = new HashMap<>();

		graphs.dualVertices.forEach((loc) -> {
			auxGraph.addVertex(loc);
		});

		graphs.dualVertices.forEach((loc) -> {
			Set<MapEdge> neighborEdges = graphs.dualGraph.edgesOf(loc);

			MapEdge newEdge = null;
			double minHeight = Double.POSITIVE_INFINITY;
			for (MapEdge edge : neighborEdges) {
				//				System.out.println(edge.oppositeLocation(loc).elevation);
				if (minHeight > edge.oppositeLocation(loc).elevation) {
					newEdge = edge;
					minHeight = edge.oppositeLocation(loc).elevation;
				}
			}

			if (minHeight >= loc.elevation && !loc.boundaryLocation) {
				System.out.println("error: " + loc.hashCode());
				//				if (depressions.contains(loc)) {
				//					System.out.println("contained");
				//				}
				//				throw new IllegalStateException();

				//				newEdge.river = true;
				//				auxGraph.addEdge(loc, newEdge.oppositeLocation(loc), newEdge);
			} else {
				if (newEdge != null) {
					newEdge.river = true;
					auxGraph.addEdge(loc, newEdge.oppositeLocation(loc), newEdge);
				}
			}
		});

		graphs.riverGraph = auxGraph;

		// now label vertices in river graph with height
		boolean changed = true;
		while (changed) {
			//			System.out.println("labeling...");
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
			double flux = CONSTANT_FLUX;
			for (MapEdge edge : incomingEdges) {
				flux += edge.flux;
			}
			Set<MapEdge> outgoingEdges = auxGraph.outgoingEdgesOf(v);
			if (!outgoingEdges.isEmpty()) {
				MapEdge outgoingEdge = outgoingEdges.iterator().next();
				outgoingEdge.flux = flux;
				v.flux = flux;
				//				System.out.println(flux);
			}

			v.riverJuncture = auxGraph.incomingEdgesOf(v).size() > 1;
			v.riverHead = auxGraph.incomingEdgesOf(v).size() == 0;
		}

		// finally compile into separate paths
		List<Path> paths = new ArrayList<>();
		for (Location v : vertices) {
			if (v.riverHead || v.riverJuncture) {
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
		MapEdge outgoingEdge = outgoingEdges.size() > 0 ? outgoingEdges.iterator().next() : null;
		return outgoingEdge;
	}

	public void setVoronoiCornerElevations(Graphs graphs) {
		graphs.dualEdges.forEach((edge) -> {
			edge.elevation = (edge.loc1.elevation + edge.loc2.elevation) / 2.0;
//			System.out.println("!!! " + edge.elevation);
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
//			System.out.println("+++ " + loc.elevation);
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
		graphs.voronoiVertices.forEach((loc) -> {
			loc.water = (loc.elevation < sealevel);
			//			if (loc.water) {
			//				System.out.println("water!");
			//			} else {
			//				System.out.println("not water!");
			//			}
		});
		graphs.dualVertices.forEach((loc) -> {
//			System.out.println("marking water: " + loc.elevation);
			loc.water = (loc.elevation < sealevel);
			if (loc.water) {
				//				System.out.println("water!");
			}
			//			loc.water = graphs.voronoiGraph.edgesOf(loc).stream().map((edge) -> {
			//				return graphs.voronoiToDual.get(edge);
			//			}).flatMap((edge) -> {
			//				return Arrays.stream(new Location[] { edge.loc1, edge.loc2 });
			//			}).allMatch((dualVertex) -> {
			//				return dualVertex.water;
			//			});
		});
	}

}
