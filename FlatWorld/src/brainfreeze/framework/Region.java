package brainfreeze.framework;

import java.util.List;

import brainfreeze.world.Graphs;
import brainfreeze.world.Lake;
import brainfreeze.world.Road;
import brainfreeze.world.SecondaryRoad;

public class Region {
	public Region(Graphs graphs) {
		this.graphs = graphs;
	}

	public Graphs graphs;
	public Lake[] lakes;
	public int numberOfLakes;
	public List<Road> roads;
	public List<SecondaryRoad> secondaryRoads;
	
}
