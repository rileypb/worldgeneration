package square;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public class Terrain {

	public final double width;
	public final double height;

	private double[][] wildness;
	private double[][] calmValue;
	private double[][] wildValue;
	private double[][] elevation;
	public final int numHorizontalSamples;
	public final int numVerticalSamples;
	private int[][] relativeIntegerHeight;
	private int[][] contourNumber;
	private boolean[][] drawContour;
	private double[][] moisture;
	private double[][] temperatureVariation;
	private double[][] temperature;
	private Biome[][] biome;
	private List<Point2D.Double> roads;
	private List<Point2D.Double> populationCenters;
	private double[][] popFactor;
	private int[][] distanceFromCoast;

	public Terrain(double width, double height, int numHorizontalSamples, int numVerticalSamples) {
		this.width = width;
		this.height = height;
		this.numHorizontalSamples = numHorizontalSamples;
		this.numVerticalSamples = numVerticalSamples;

		wildness = new double[numHorizontalSamples][numVerticalSamples];
		calmValue = new double[numHorizontalSamples][numVerticalSamples];
		wildValue = new double[numHorizontalSamples][numVerticalSamples];
		elevation = new double[numHorizontalSamples][numVerticalSamples];
		relativeIntegerHeight = new int[numHorizontalSamples][numVerticalSamples];
		contourNumber = new int[numHorizontalSamples][numVerticalSamples];
		drawContour = new boolean[numHorizontalSamples][numVerticalSamples];
		moisture = new double[numHorizontalSamples][numVerticalSamples];
		temperatureVariation = new double[numHorizontalSamples][numVerticalSamples];
		temperature = new double[numHorizontalSamples][numVerticalSamples];
		biome = new Biome[numHorizontalSamples][numVerticalSamples];
		popFactor = new double[numHorizontalSamples][numVerticalSamples];
		distanceFromCoast = new int[numHorizontalSamples][numVerticalSamples];
		
		for (int x = 0; x < numHorizontalSamples; x++) {
			for (int y = 0; y < numVerticalSamples; y++) {
				distanceFromCoast[x][y] = Integer.MAX_VALUE;
			}
		}
		
		roads = new ArrayList<Point2D.Double>();
		populationCenters = new ArrayList<Point2D.Double>();
	}

	public void setWildness(int x, int y, double wildness) {
		this.wildness[x][y] = wildness;
	}

	public void setCalmValue(int x, int y, double calmValue) {
		this.calmValue[x][y] = calmValue;
	}

	public void setWildValue(int x, int y, double wildValue) {
		this.wildValue[x][y] = wildValue;
	}

	public double getWildness(int x, int y) {
		return this.wildness[x][y];
	}

	public double getCalmValue(int x, int y) {
		return this.calmValue[x][y];
	}

	public double getWildValue(int x, int y) {
		return this.wildValue[x][y];
	}

	public void setElevation(int x, int y, double elevation) {
		this.elevation[x][y] = elevation;
	}

	public double getElevation(int x, int y) {
		if (x < 0) {
			x = numHorizontalSamples + x;
		}
		if (x > numHorizontalSamples - 1) {
			x = x - numHorizontalSamples;
		}
		if (y < 0) {
			y = numVerticalSamples + y;
		}
		if (y > numVerticalSamples - 1) {
			y = y - numVerticalSamples;
		}
		return this.elevation[x][y];
	}


	public void setRelativeIntegerHeight(int x, int y, int relativeIntegerHeight) {
		this.relativeIntegerHeight[x][y] = relativeIntegerHeight;
	}

	public int getRelativeIntegerHeight(int x, int y) {
		return this.relativeIntegerHeight[x][y];
	}

	public void setContourNumber(int x, int y, int contourNumber) {
		this.contourNumber[x][y] = contourNumber;
	}

	public void setDrawContour(int x, int y, boolean drawContour) {
		this.drawContour[x][y] = drawContour;
	}

	public int getContourNumber(int x, int y) {
		return this.contourNumber[x][y];
	}

	public boolean getDrawContour(int x, int y) {
		return this.drawContour[x][y];
	}

	public void setMoisture(int x, int y, double moisture) {
		this.moisture[x][y] = moisture;
	}

	public void setTemperatureVariation(int x, int y, double tempvar) {
		this.temperatureVariation[x][y] = tempvar;
	}

	public void setTemperature(int x, int y, double temperature) {
		this.temperature[x][y] = temperature;
	}

	public void setBiome(int x, int y, Biome biome) {
		this.biome[x][y] = biome;
	}

	public Biome getBiome(int x, int y) {
		return this.biome[x][y];
	}

	public void markRoad(int roadX, int roadY) {
		roads.add(new Point2D.Double(roadX, roadY));
	}
	
	public Stream<Point2D.Double> roadPoints() {
		return roads.stream();
	}

	public void setPopFactor(int x, int y, double popFactor) {
		this.popFactor[x][y] = popFactor;
	}
	
	public double getPopFactor(int x, int y) {
		return this.popFactor[x][y];
	}

	public void addPopulationCenter(int x, int y) {
		this.populationCenters.add(new Point2D.Double(x, y));
	}

	public Stream<Point2D.Double> popCenterPoints() {
		return populationCenters.stream();
	}

	public double getMoisture(int x, int y) {
		return moisture[x][y];
	}

	public void setDistanceFromCoast(int x, int y, int distance) {
		this.distanceFromCoast[fixX(x)][fixY(y)] = distance;
	}

	private int fixX(int x) {
		if (x < 0) return numHorizontalSamples - 1;
		if (x >= numHorizontalSamples) return 0;
		return x;
	}

	private int fixY(int y) {
		if (y < 0) return numVerticalSamples - 1;
		if (y >= numVerticalSamples) return 0;
		return y;
	}

	public int getDistanceFromCoast(int x, int y) {
		return this.distanceFromCoast[fixX(x)][fixY(y)];
	}

}
