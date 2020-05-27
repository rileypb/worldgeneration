package brainfreeze.world;

import java.util.ArrayList;
import java.util.List;

public class Path {

	private List<Location> points = new ArrayList<Location>();
	
	private double[] x;
	private double[] y;
	private double[] score;

	private double[] elevation;
	
	public void addPoint(Location newPoint) {
		points.add(newPoint);
	}

	public void createRelaxedPath() {
		if (points.size() == 0) {
			int a = 0;
		}
		x = new double[points.size()];
		y = new double[points.size()];
		score = new double[points.size()];
		elevation = new double[points.size()];
		double[] xInit = new double[points.size()];
		double[] yInit = new double[points.size()];
		for (int i = 0; i < points.size(); i++) {
			xInit[i] = points.get(i).getX();
			yInit[i] = points.get(i).getY();
			score[i] = points.get(i).flux;
			elevation[i] = points.get(i).elevation;
		}
		
		x[0] = xInit[0];
		x[points.size() - 1] = xInit[points.size() - 1];
		y[0] = yInit[0];
		y[points.size() - 1] = yInit[points.size() - 1];
		for (int i = 1; i < points.size() - 1; i++) {
//			x[i] = xInit[i];
//			y[i] = yInit[i];
			x[i] = 0.25 * xInit[i-1] + 0.5 * xInit[i] + 0.25 * xInit[i+1];
			y[i] = 0.25 * yInit[i-1] + 0.5 * yInit[i] + 0.25 * yInit[i+1];
		}
	}
	
	public double getX(int index) {
		return x[index];
	}
	
	public double getY(int index) {
		return y[index];
	}
	
	public double getScore(int index) {
		return score[index];
	}
	
	public double getElevation(int index) {
		return elevation[index];
	}

	public int size() {
		return points.size();
	}

	public Location getPoint(int i) {
		return this.points.get(i);
	}

}
