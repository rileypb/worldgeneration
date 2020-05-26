package brainfreeze.framework;

import java.util.Random;

import com.flowpowered.noise.module.source.Perlin;

import brainfreeze.world.Graphs;
import brainfreeze.world.Location;
import brainfreeze.world.TerrainBuilder;

public class World {
	private WorldGeometry geometry;
	private int points;
	private double seaLevel;
	private Random r;
	private double width;
	private double height;

	public World(WorldGeometry geometry, double width, double height, int points, double seaLevel, Random r) {
		this.geometry = geometry;
		this.width = width;
		this.height = height;
		this.points = points;
		this.seaLevel = seaLevel;
		this.r = r;
	}

	private Graphs graphs;

	public void buildWorld() {
		if (graphs != null) {
			throw new IllegalStateException("Can't build world twice");
		}

		Perlin perlin = new Perlin();
		perlin.setFrequency(1);
		perlin.setOctaveCount(16);
		perlin.setSeed(r.nextInt());
		Perlin calmPerlin = new Perlin();
		calmPerlin.setFrequency(.25);
		calmPerlin.setOctaveCount(16);
		calmPerlin.setSeed(r.nextInt());
		Perlin wildPerlin = new Perlin();
		wildPerlin.setFrequency(4);
		wildPerlin.setOctaveCount(30);
		wildPerlin.setSeed(r.nextInt());
		
		HeightMap mixtureMap = new PerlinHeightMap(width, height, perlin, geometry);
		HeightMap calmMap = new PerlinHeightMap(width, height, calmPerlin, geometry);
		HeightMap wildMap = new PerlinHeightMap(width, height, wildPerlin, geometry);
		
		HeightMap elevationMap = new CalmAndWildPerlinHeightMap(mixtureMap, calmMap, wildMap);

		TerrainBuilder builder = new TerrainBuilder(points, seaLevel, elevationMap);
		
		graphs = builder.run(r, 1);
	}

	public Graphs getGraphs() {
		return graphs;
	}

	
	
}
