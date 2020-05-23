package brainfreeze.framework;

import brainfreeze.world.Location;

public interface Region {
	Region getSubRegion(Location id);
}
