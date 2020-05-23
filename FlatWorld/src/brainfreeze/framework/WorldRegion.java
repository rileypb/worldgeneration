package brainfreeze.framework;

import brainfreeze.world.Location;

public class WorldRegion implements Region {

	private Region enclosingRegion;
	private Location regionId;
	private int detailLevel;

	public WorldRegion(Region enclosingRegion, Location regionId, int detailLevel) {
		this.enclosingRegion = enclosingRegion;
		this.regionId = regionId;
		this.detailLevel = detailLevel;
	}

	@Override
	public Region getSubRegion(Location id) {
		return null;
	}

}
