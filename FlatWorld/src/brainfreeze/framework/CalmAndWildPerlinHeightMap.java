package brainfreeze.framework;

import com.flowpowered.noise.module.source.Perlin;

import brainfreeze.world.PerlinHelper;

public class CalmAndWildPerlinHeightMap implements HeightMap {

	private HeightMap mixtureMap;
	private HeightMap calmMap;
	private HeightMap wildMap;

	public CalmAndWildPerlinHeightMap(HeightMap mixtureMap, HeightMap calmMap, HeightMap wildMap) {
		this.mixtureMap = mixtureMap;
		this.calmMap = calmMap;
		this.wildMap = wildMap;
	}

	@Override
	public double getValue(double x, double y) {
		double wildness = mixtureMap.getValue(x, y);
		double calmValue = calmMap.getValue(x, y);
		double wildValue = wildMap.getValue(x, y);

		return (1 - wildness * wildness * wildness) * calmValue + wildness * wildness * wildness * wildValue;
	}

}
