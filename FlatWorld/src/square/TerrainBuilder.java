package square;

import java.util.Random;

import com.flowpowered.noise.module.source.Perlin;

import voronoi.Biome;
import voronoi.MapperMain;
import voronoi.PerlinHelper;

public class TerrainBuilder {

	public static int NUM_CONTOURS = 6;

	private int width;
	private int height;
	private Random r;
	private int seed1;
	private int seed2;
	private int seed3;
	private double sealevel;

	public TerrainBuilder(int width, int height, Random r, double sealevel) {
		this.width = width;
		this.height = height;
		this.r = r;
		this.sealevel = sealevel;
		this.seed1 = r.nextInt();
		this.seed2 = r.nextInt();
		this.seed3 = r.nextInt();
	}

	public Terrain run(TerrainWindow window) {
		double x0 = window.x0;
		double y0 = window.y0;
		double windowWidth = window.windowWidth;
		double windowHeight = window.windowHeight;
		int numHorizontalSamples = window.numHorizontalSamples;

		int numVerticalSamples = (int) (windowHeight / windowWidth * numHorizontalSamples);
		Terrain terrain = new Terrain(windowWidth, windowHeight, numHorizontalSamples, numVerticalSamples);

		final int POW = 8;

		Perlin perlin = new Perlin();
		perlin.setFrequency(2);
		perlin.setOctaveCount(16);
		perlin.setSeed(seed1);
		Perlin calmPerlin = new Perlin();
		calmPerlin.setFrequency(1);
		calmPerlin.setOctaveCount(4);
		//		calmPerlin.setLacunarity(1.9);
		//		calmPerlin.setPersistence(0.4);
		calmPerlin.setSeed(seed2);
		Perlin wildPerlin = new Perlin();
		wildPerlin.setFrequency(4);
		wildPerlin.setOctaveCount(4);
		wildPerlin.setSeed(seed3);

		double xRatio = windowWidth / numHorizontalSamples;
		double yRatio = windowHeight / numVerticalSamples;

		for (int x = 0; x < numHorizontalSamples; x++) {
			for (int y = 0; y < numVerticalSamples; y++) {
				double cylindricalNoise = PerlinHelper.getCylindricalNoise(perlin, x0 + x * xRatio, width,
						y0 + y * yRatio, 2 * height);
				cylindricalNoise = Math.min(1, Math.max(0, cylindricalNoise));
				terrain.setWildness(x, y, cylindricalNoise);
			}
		}
		for (int x = 0; x < numHorizontalSamples; x++) {
			for (int y = 0; y < numVerticalSamples; y++) {
				terrain.setCalmValue(x, y, PerlinHelper.getCylindricalNoise(calmPerlin, x0 + x * xRatio, width,
						y0 + y * yRatio, 2 * height));
			}
		}
		for (int x = 0; x < numHorizontalSamples; x++) {
			for (int y = 0; y < numVerticalSamples; y++) {
				terrain.setWildValue(x, y, PerlinHelper.getCylindricalNoise(wildPerlin, x0 + x * xRatio, width,
						y0 + y * yRatio, 2 * height));
			}
		}

		double lowElevation = 2;
		double highElevation = sealevel;
		for (int x = 0; x < numHorizontalSamples; x++) {
			for (int y = 0; y < numVerticalSamples; y++) {
				double wildness = terrain.getWildness(x, y);
				double wildness4 = Math.pow(wildness, POW);
				double calmValue = terrain.getCalmValue(x, y);
				double wildValue = terrain.getWildValue(x, y);
				double elevation = (1 - wildness4) * calmValue + wildness4 * wildValue;
				terrain.setElevation(x, y, elevation);
				lowElevation = Math.min(lowElevation, elevation);
				highElevation = Math.max(highElevation, elevation);
			}
		}

		lowElevation = Math.max(lowElevation, sealevel);
		double elevationRange = highElevation - lowElevation;
		if (elevationRange > 0) {
			double rangePerContour = elevationRange / NUM_CONTOURS;

			for (int x = 0; x < numHorizontalSamples; x++) {
				for (int y = 0; y < numVerticalSamples; y++) {
					int relativeIntegerHeight = (int) (10 * (terrain.getElevation(x, y) - lowElevation)
							/ rangePerContour);
					terrain.setRelativeIntegerHeight(x, y, relativeIntegerHeight);
				}
			}

			for (int x = 0; x < numHorizontalSamples; x++) {
				for (int y = 0; y < numVerticalSamples; y++) {
					int rih = terrain.getRelativeIntegerHeight(x, y);
					if (rih > 0 && rih % 10 == 0) {
						boolean drawContour = false;
						for (int i = -1; i <= 1; i++) {
							for (int j = -1; j <= 1; j++) {
								if (x + i > 0 && x + i < 1200 && y + j > 0 && y + j < 600) {
									int rih2 = terrain.getRelativeIntegerHeight(x + i, y + j);
									if (rih2 < rih) {
										drawContour = true;
									}
								}
							}
						}
						terrain.setDrawContour(x, y, drawContour);
					}
					terrain.setContourNumber(x, y, rih / 10);
				}
			}
			
			Perlin calmMoisturePerlin = new Perlin();
			calmMoisturePerlin.setFrequency(1);
			calmMoisturePerlin.setOctaveCount(4);
			//		calmPerlin.setLacunarity(1.9);
			//		calmPerlin.setPersistence(0.4);
			calmMoisturePerlin.setSeed(r.nextInt());
			Perlin wildMoisturePerlin = new Perlin();
			wildMoisturePerlin.setFrequency(4);
			wildMoisturePerlin.setOctaveCount(4);
			wildMoisturePerlin.setSeed(r.nextInt());

			for (int x = 0; x < numHorizontalSamples; x++) {
				for (int y = 0; y < numVerticalSamples; y++) {

//					double cylindricalNoise = PerlinHelper.getCylindricalNoise(perlin, x0 + x * xRatio, width,
//							y0 + y * yRatio, 2 * height);
//					double wildness = Math.min(1, Math.max(0, cylindricalNoise));
//					
//					double calmValue = PerlinHelper.getCylindricalNoise(calmMoisturePerlin, x0 + x * xRatio, width,
//							y0 + y * yRatio, 2 * height);
//					double wildValue = PerlinHelper.getCylindricalNoise(wildMoisturePerlin, x0 + x * xRatio, width,
//							y0 + y * yRatio, 2 * height);
//					
//
//					double wildness4 = Math.pow(wildness, POW);
//					double moisture = (1 - wildness4) * calmValue + wildness4 * wildValue;
					
					perlin.setFrequency(0.25);
					perlin.setOctaveCount(4);
					double cylindricalNoise = PerlinHelper.getCylindricalNoise(perlin, x0 + x * xRatio, width,
							y0 + y * yRatio, 2 * height);
					double moisture = Math.min(1, Math.max(0, cylindricalNoise));
					terrain.setMoisture(x, y, moisture);
					cylindricalNoise = PerlinHelper.getCylindricalNoise(perlin, x0 + x * xRatio, width, y0 + y * yRatio,
							2 * height);
					double temperatureVariation = Math.min(1, Math.max(0, cylindricalNoise));
					terrain.setTemperatureVariation(x, y, temperatureVariation);

					double globalY = y0 + (double) y / numVerticalSamples * windowHeight;
					double latitude = 300 - globalY;
					double distanceFromEquator = Math.abs(latitude);
					double angleFromEquator = Math.PI * distanceFromEquator/600;
					double baseTemperature = Math.cos(angleFromEquator);
					double adjustedTemperature = baseTemperature
							- (terrain.getElevation(x, y) - MapperMain.SEALEVEL)
									* (terrain.getElevation(x, y) - MapperMain.SEALEVEL)
							+ 0.1 * temperatureVariation;
//					System.out.println(loc.y + ", " + adjustedTemperature);
					double temperature = adjustedTemperature;
					terrain.setTemperature(x, y, temperature);
					
					Biome biome;
					if (moisture < 0.2) {
						if (temperature < 0.2) {
							biome = Biome.TUNDRA;
						} else if (temperature < 0.8) {
							biome = Biome.TEMPERATE_DESERT;
						} else {
							biome = Biome.SUBTROPICAL_DESERT;
						}
					} else if (moisture < 0.66) {
						if (temperature < 0.2) {
							biome = Biome.TAIGA;
						} else if (temperature < 0.8) {
							biome = Biome.GRASSLAND;
						} else {
							biome = Biome.SAVANNA;
						}
					} else {
						if (temperature < 0.2) {
							biome = Biome.SNOW;
						} else if (temperature < 0.8) {
							biome = Biome.TEMPERATE_DECIDUOUS_FOREST;
						} else {
							biome = Biome.TROPICAL_RAIN_FOREST;
						}
					}
					
					terrain.setBiome(x, y, biome);
				}
			}

		}

		return terrain;

	}

}
