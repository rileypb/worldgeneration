package brainfreeze.world;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.flowpowered.noise.module.source.Perlin;

import brainfreeze.framework.GraphBuilder;
import brainfreeze.framework.GraphHelper;
import brainfreeze.framework.Region;
import brainfreeze.framework.RegionParameters;
import brainfreeze.framework.TechnicalParameters;
import brainfreeze.framework.WorldGeometry;
import brainfreeze.framework.WorldParameters;
import de.alsclo.voronoi.graph.Point;

public class WorldBuilder {

	public World buildWorld(WorldParameters wParams, RegionParameters rParams, TechnicalParameters tParams) {
		ArrayList<Point> initialSites = new ArrayList<>();

		GraphHelper.generateRandomPoints(initialSites, wParams.rnd, rParams.numberOfPoints,
				new Rectangle2D.Double(0, 0, wParams.width, wParams.height));

		Graphs graphs;
		
		Perlin moisturePerlin = new Perlin();
		moisturePerlin.setSeed(wParams.rnd.nextInt());

		List<Location> locations = new ArrayList<Location>();
		initialSites.forEach((site) -> {
			locations.add(new Location(site.x, site.y));
		});

		graphs = GraphBuilder.buildGraph(locations, rParams, tParams.relaxations, wParams.geometry);

		World world = new World(graphs);

		System.out.println("building plates...");
		buildPlates(world, wParams.numberOfPlates, wParams.rnd);

		classifyPlates(world, wParams.rnd);

		markPlateEdges(world);

		movePlates(world, wParams.rnd);

		markTectonicStress(world, wParams.rnd, wParams.width, wParams.height);

		setPlateHeights(world, wParams.rnd);

		buildLand(world, wParams.rnd, wParams);

		applyTectonics(world, Math.sqrt(wParams.height));

		smoothElevations(world);
//		smoothElevations(world);
		
		finalizeElevations(world);
		setVoronoiCornerElevations(graphs);

		markWater(graphs, wParams.seaLevel);
		
		System.out.println("Gauging base moisture...");
		setBaseMoisture(graphs, wParams.rnd, moisturePerlin);

//		System.out.println("Filling lakes...");
//		eliminateStrandedWaterAndFindLakes(graphs, wParams.seaLevel, world);
		
		
		System.out.println("done.");

		return world;
	}

	public void setVoronoiCornerElevations(Graphs graphs) {
		graphs.voronoiVertices.forEach((loc) -> {
			Set<Location> neighbors = loc.adjacentCells;
			double avg = neighbors.stream().mapToDouble((v) -> {
				return v.elevation;
			}).average().orElse(0);
			loc.elevation = avg;
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
		});
	}
	
	public void eliminateStrandedWaterAndFindLakes(Graphs graphs, double sealevel, Region region) {
		Set<Location> waterVertices = graphs.dualVertices.stream().filter((loc) -> {
			return loc.water;
		}).collect(Collectors.toSet());

		Set<Location> frontierVertices = graphs.dualVertices.stream().filter((loc) -> {
			return loc.water && loc.boundaryLocation;
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

		region.numberOfLakes = 0;

		while (waterVertices.size() > 0) {
			Location seedVertex = waterVertices.iterator().next();
			seedVertex.isLake = true;
			seedVertex.lakeNumber = region.numberOfLakes;
			seedVertex.elevation = sealevel + 0.001;
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
					loc.lakeNumber = region.numberOfLakes;
					loc.elevation = sealevel + 0.001;
				}
				frontierVertices = newVertices;
			}
			region.numberOfLakes++;
		}

	}
	
	public void setBaseMoisture(Graphs graphs, Random r, Perlin perlin) {
		generateValues(graphs, r, perlin, (target, value) -> {
			target.baseMoisture = value;
		});
	}
	
	public void generateValues(Graphs graphs, Random r, Perlin perlin, Setter setter) {
		double x0 = r.nextDouble();
		double y0 = r.nextDouble();
		graphs.dualVertices.forEach((loc) -> {
			double noise = PerlinHelper.getCylindricalNoise(perlin, x0 + loc.getX(), 1, y0 + loc.getY(), 1);
			setter.set(loc, noise);
		});
	}

	private void finalizeElevations(World world) {
		world.graphs.dualVertices.forEach((loc) -> {
			loc.elevation = loc.baseElevation;
		});
	}

	private void smoothElevations(World world) {
		world.graphs.dualVertices.forEach((loc) -> {
			double avgHeight = loc.adjacentCells.stream().mapToDouble((neighbor) -> { return neighbor.baseElevation; }).average().getAsDouble();
			loc.tmpElevation = (avgHeight + 2 * loc.baseElevation)/3;
		});
		world.graphs.dualVertices.forEach((loc) -> {
			loc.baseElevation = loc.tmpElevation;
		});
	}

	private void applyTectonics(World world, double distScale) {
		world.graphs.dualVertices.stream().filter((loc) -> {
			return loc.tectonicStress != 0;
		}).forEach((loc) -> {
			world.graphs.dualVertices.forEach((loc2) -> {
				if (loc2 != loc) {
					double distsq = (loc.getX() - loc2.getX()) * (loc.getX() - loc2.getX())
							+ (loc.getY() - loc2.getY()) * (loc.getY() - loc2.getY());
					loc2.baseElevation = loc2.baseElevation + 4 * distScale * loc.tectonicStress / distsq;
				}
			});
		});
	}

	private void buildLand(World world, Random rnd, WorldParameters wParams) {
		Perlin perlin = new Perlin();
		perlin.setSeed(rnd.nextInt());

		for (Location loc : world.graphs.dualVertices) {
			loc.baseElevation = PerlinHelper.getCylindricalNoise(perlin, loc.getX(), wParams.width, loc.getY(),
					2 * wParams.height) + loc.plate.height;
		}
	}

	private void setPlateHeights(World world, Random rnd) {
		for (Plate plate : world.plates) {
			plate.height = 1.25 * rnd.nextDouble() - 1;
		}
	}

	private void movePlates(World world, Random rnd) {
		for (Plate plate : world.plates) {
			Vector newVector = new Vector(1 - 2 * rnd.nextDouble(), 1 - 2 * rnd.nextDouble());
			newVector = newVector.normalize();
			plate.movement = newVector;
		}
	}

	private void markPlateEdges(World world) {
		world.graphs.dualVertices.forEach((loc) -> {
			loc.plateEdge = loc.adjacentCells.stream().anyMatch((cell) -> {
				return cell.plateIndex != loc.plateIndex;
			});
		});
	}

	private void markTectonicStress(World world, Random rnd, double worldWidth, double worldHeight) {
		//		Perlin perlin = new Perlin();
		//		perlin.setSeed(rnd.nextInt());
		//
		//		world.graphs.dualVertices.stream().filter((loc) -> {
		//			return loc.plateEdge;
		//		}).forEach((loc) -> {
		//			loc.tectonicStress = PerlinHelper.getCylindricalNoise(perlin, loc.getX(), worldWidth, loc.getY(), worldHeight);
		//		});

		world.graphs.dualVertices.stream().filter((loc) -> {
			return loc.plateEdge;
		}).forEach((loc) -> {
			Double totalStress = loc.adjacentCells.stream().filter((neighbor) -> {
				return loc.plateIndex != neighbor.plateIndex;
			}).reduce(0d, (d, cell) -> {
				Vector towardsNeighbor = new Vector(cell.getX() - loc.getX(), cell.getY() - loc.getY()).normalize();
				Vector v1 = loc.plate.movement.projectOnto(towardsNeighbor);
				Vector v2 = cell.plate.movement.projectOnto(towardsNeighbor);

				double stress = v1.dot(towardsNeighbor) - v2.dot(towardsNeighbor);

				return stress / 4;
			}, (d1, d2) -> {
				return d1 + d2;
			});

			loc.tectonicStress = totalStress;
		});

	}

	private void buildPlates(World world, int numberOfPlates, Random r) {
		List<Location> frontierVertices = new LinkedList<>();

		List<Plate> plates = new ArrayList<>();

		for (int i = 0; i < numberOfPlates; i++) {
			Plate e = new Plate();
			plates.add(e);
			e.growthFactor = r.nextDouble();
		}

		world.plates = plates;

		List<Location> dualVertices = new ArrayList<>(world.graphs.dualVertices);
		Collections.shuffle(dualVertices, r);
		for (int i = 0; i < numberOfPlates; i++) {
			Location loc = dualVertices.get(i);
			loc.plateIndex = i;
			loc.plate = plates.get(i);
			loc.plate.cells.add(loc);
			frontierVertices.add(loc);
		}

		while (frontierVertices.size() > 0) {
			Location loc = frontierVertices.remove(0);
			if (r.nextDouble() < loc.plate.growthFactor) {
				Set<Location> adjacentCells = loc.adjacentCells;
				Location first = adjacentCells.stream().filter((cell) -> {
					return cell.plateIndex == -1;
				}).findFirst().orElse(null);
				if (first != null) {
					first.plateIndex = loc.plateIndex;
					first.plate = loc.plate;
					first.plate.cells.add(first);
					frontierVertices.add(first);
					frontierVertices.add(loc);
				}
			} else {
				frontierVertices.add(loc);
			}
		}
	}

	private void classifyPlates(World world, Random rnd) {
		for (Plate plate : world.plates) {
			if (rnd.nextDouble() < 0.3) {
				plate.type = Plate.PlateType.LAND;
			} else {
				plate.type = Plate.PlateType.OCEAN;
			}
		}
	}

}
