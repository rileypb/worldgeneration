package voronoi;

import com.flowpowered.noise.module.source.Perlin;

public class PerlinHelper {
	//		static double maxX0;

	public static double getNoise(Perlin perlin, Face cell, double xWidth, float xFactor, float yFactor, float zFactor,
			double scaleFactor) {
		float leftLimit = (float) xWidth; //2f/3f*(float) map.xWidth; //(float) (2 * (map.maxX - 1) * Math.sqrt(3));
		float lowerLimit = 3f / 2f * leftLimit;

		double x0 = 2 * Math.PI * (float) cell.location.x / (float) leftLimit;
		//				maxX0 = Math.max(maxX0, x0);
		//				System.out.println(maxX0);

		double rawValue = (float) scaleFactor * perlin.getValue(xFactor + 0.1 * Math.sin(x0),
				yFactor + 0.1 * Math.cos(x0), zFactor + cell.location.y / (float) lowerLimit);
		return rawValue;
	}

	//	public static double getNoise(Perlin perlin, SquareCorner corner, SquareMap map, float xFactor, float yFactor,
	//			float zFactor, double scaleFactor) {
	//		float leftLimit = (float) map.xWidth; //2f/3f*(float) map.xWidth; //(float) (2 * (map.maxX - 1) * Math.sqrt(3));
	//		float lowerLimit = 3f / 2f * leftLimit;
	//
	//		double x0 = 2 * Math.PI * (float) corner.x / (float) leftLimit;
	//		//				maxX0 = Math.max(maxX0, x0);
	//		//				System.out.println(maxX0);
	//
	//		double rawValue = (float) scaleFactor * perlin.getValue(xFactor + 0.1 * Math.sin(x0),
	//				yFactor + 0.1 * Math.cos(x0), zFactor + corner.y / (float) lowerLimit);
	//		return rawValue;
	//	}

	public static double getCylindricalNoise(Perlin perlin, double theta, double y) {
		return perlin.getValue(Math.cos(theta), Math.sin(theta), 2*Math.PI*y);
	}

	public static double getCylindricalNoise(Perlin perlin, double x, double xWidth, double y, double yWidth) {
		return getCylindricalNoise(perlin, 2 * Math.PI * (x / xWidth), y / yWidth);
	}
	
	public static double getPlanarNoise(Perlin perlin, double x, double xWidth, double y, double yWidth) {
		return perlin.getValue(x/xWidth, y/yWidth, 0);
	}

	public static double getPlanarNoise(Perlin perlin, double x, int xWidth, double y, int yWidth, double boundaryFade) {
		if (boundaryFade > 0 && x < 0 || x > xWidth || y < 0 || y > yWidth) { 
			return -boundaryFade;
		}
		
		double noise = getPlanarNoise(perlin, x, xWidth, y, yWidth);
		double distanceToBoundary = Math.min(Math.min(y, Math.abs(yWidth - y)), Math.min(x, Math.abs(xWidth - x)));
		if (distanceToBoundary < boundaryFade && noise > -boundaryFade && boundaryFade > 0) {
			double u = distanceToBoundary * noise - (1-distanceToBoundary) * boundaryFade;
			noise = u;
		}
		if (noise > 0) {
			int a = 0;
		}
		return noise;
	}
	
}
