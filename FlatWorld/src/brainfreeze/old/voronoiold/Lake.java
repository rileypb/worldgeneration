package brainfreeze.old.voronoiold;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lake {

	private double elevation = -1000;
	private List<Location> faces = new ArrayList<>();
	private double outletElevation = 1000;

	public double getElevation() {
		return elevation;
	}

	public void addFace(Location newFace) {
		faces.add(newFace);
		newFace.lake = this;
		newFace.water = true;
		elevation = newFace.elevation > elevation ? newFace.elevation : elevation;
	}

	public void addLakeFaces(Lake lake2) {
		lake2.faces.forEach((face) -> {
			addFace(face);
		});
	}

	public void setOutletElevation(double elevation) {
		if (elevation < outletElevation) {
			outletElevation = elevation;
		}
	}

	public double getOutletElevation() {
		return outletElevation;
	}

	public List<Location> getFaces() {
		return Collections.unmodifiableList(faces);
	}

}
