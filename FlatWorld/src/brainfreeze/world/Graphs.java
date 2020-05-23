package brainfreeze.world;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultUndirectedGraph;

public class Graphs {

	public final DefaultUndirectedGraph<Location, MapEdge> voronoiGraph;
	public final DefaultUndirectedGraph<Location, MapEdge> dualGraph;
	public final Set<Location> dualVertices;
	public final Set<Location> voronoiVertices;
	public final Set<MapEdge> dualEdges;
	public final Set<MapEdge> voronoiEdges;
	public final Map<MapEdge, MapEdge> dualToVoronoi;
	public final Map<MapEdge, MapEdge> voronoiToDual;
	public List<Location> subdivisionSeeds;
	public DefaultDirectedGraph<Location, MapEdge> riverGraph;
	public List<Path> riverPaths;
	public Set<Location> cities;
	public Set<Location> towns;

	public Graphs(DefaultUndirectedGraph<Location, MapEdge> voronoiGraph,
			DefaultUndirectedGraph<Location, MapEdge> dualGraph, Set<Location> dualVertices,
			Set<Location> voronoiVertices, Set<MapEdge> dualEdges, Set<MapEdge> voronoiEdges,
			Map<MapEdge, MapEdge> dualToVoronoi, Map<MapEdge, MapEdge> voronoiToDual) {
		this.voronoiGraph = voronoiGraph;
		this.dualGraph = dualGraph;
		this.dualVertices = dualVertices;
		this.voronoiVertices = voronoiVertices;
		this.dualEdges = dualEdges;
		this.voronoiEdges = voronoiEdges;
		this.dualToVoronoi = dualToVoronoi;
		this.voronoiToDual = voronoiToDual;

		cities = new HashSet<Location>();
		towns = new HashSet<Location>();
	}

	public Stream<Location> getVoronoiVerticesForDualVertex(Location v) {
		return dualGraph.edgesOf(v).stream().flatMap((dualEdge) -> {
			MapEdge voronoiEdge = dualToVoronoi.get(dualEdge);
			return voronoiEdge == null ? Arrays.stream(new Location[] {})
					: Arrays.asList(voronoiEdge.loc1, voronoiEdge.loc2).stream();
		}).distinct();
	}

	public List<Location> getNeighboringDualVertices(Location face) {
		Set<MapEdge> neighboringEdges = dualGraph.edgesOf(face);
		return neighboringEdges.stream().map((edge) -> {
			Location edgeTarget = dualGraph.getEdgeTarget(edge);
			Location edgeSource = dualGraph.getEdgeSource(edge);
			if (edgeTarget.equals(face)) {
				return edgeSource;
			} else {
				return edgeTarget;
			}
		}).collect(Collectors.toList());
	}

	public List<Location> getNeighboringVoronoiVertices(Location corner) {
		Set<MapEdge> neighboringEdges = voronoiGraph.edgesOf(corner);
		return neighboringEdges.stream().map((edge) -> {
			Location edgeTarget = dualGraph.getEdgeTarget(edge);
			Location edgeSource = dualGraph.getEdgeSource(edge);
			if (edgeTarget.equals(corner)) {
				return edgeSource;
			} else {
				return edgeTarget;
			}
		}).collect(Collectors.toList());
	}

}
