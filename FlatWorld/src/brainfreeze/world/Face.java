package brainfreeze.world;
import java.util.Collections;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import de.alsclo.voronoi.graph.Point;

public class Face {

	private List<MapEdge> edges;
	Integer elevation;
	Vector3f normal;
	public Point location;
	public float moisture;
	public float temperature;
	public Biome biome;
	
	public static Face getNew(List<MapEdge> edges, Integer elevation) {
		Face face = new Face(edges, elevation);
//		face.init();
		
		return face;
	}

	private Face(List<MapEdge> edges, Integer elevation) {
		this.edges = edges;
		this.elevation = elevation;
	}

//	private void init() {
//		for (MapEdge edge : edges) {
//			edge.addAdjacentFace(this);
//		}
//	}

	public List<MapEdge> getEdges() {
		return Collections.unmodifiableList(edges);
	}
	
	

}
