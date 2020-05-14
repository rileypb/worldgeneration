package squareold;

public class TerrainWindow {
	public final double x0;
	public final double y0;
	public final double windowWidth;
	public final double windowHeight;
	public final int numHorizontalSamples;
	public final int numVerticalSamples;

	public TerrainWindow(double x0, double y0, double windowWidth, double windowHeight, int numHorizontalSamples) {
		this.x0 = x0;
		this.y0 = y0;
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
		this.numHorizontalSamples = numHorizontalSamples;
		this.numVerticalSamples = (int) (windowHeight/windowWidth*numHorizontalSamples);
	}

	public TerrainWindow deriveNewWindow(int x0new, int y0new, double newWindowWidth, double newWindowHeight) {
		double x0scaled = x0 + x0new/(double)numHorizontalSamples * windowWidth;
		double y0scaled = y0 + y0new/(double)numVerticalSamples * windowHeight;
		double widthScaled = newWindowWidth / (double)numHorizontalSamples * windowWidth;
		double heightScaled = newWindowHeight / (double)numVerticalSamples * windowHeight;
		
		return new TerrainWindow(x0scaled, y0scaled, widthScaled, heightScaled, numHorizontalSamples);
	}
}
