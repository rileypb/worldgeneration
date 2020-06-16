package brainfreeze.framework;

import com.flowpowered.noise.module.source.Perlin;

import brainfreeze.framework.WorldGeometry.WorldGeometryType;
import brainfreeze.world.PerlinHelper;

public class PerlinHeightMap implements HeightMap {

	private Perlin source;
	private WorldGeometry geometry;
	private double xWidth;
	private double yHeight;

	public PerlinHeightMap(double xWidth, double yHeight, Perlin source, WorldGeometry geometry) {
		this.xWidth = xWidth;
		this.yHeight = yHeight;
		this.source = source;
		this.geometry = geometry;
	}
	
	@Override
	public double getValue(double x, double y) {
		switch (geometry.type) {
		case PLANAR:
			return PerlinHelper.getPlanarNoise(source, x, xWidth, y, yHeight);
			
		case CYLINDRICAL:
			return PerlinHelper.getCylindricalNoise(source, x, xWidth, y, yHeight);
		
		default:
			return 0;
		}
	}

}
