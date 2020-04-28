package square;

import java.awt.Color;

import voronoi.Biome;

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

}
