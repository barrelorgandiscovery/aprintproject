package aprintextensions.fr.freydierepatrice.perfo.gerard;

public class PerfoMidiParameters {

	private int decalage = 0;

	private double minholesize;

	public int getDecalage() {
		return decalage;
	}

	public void setDecalage(int decalage) {
		this.decalage = decalage;
	}

	public double getMinholesize() {
		return minholesize;
	}

	public void setMinholesize(double minholesize) {
		this.minholesize = minholesize;
	}

	private double mininterholesize = 2.0;

	public double getMininterholesize() {
		return mininterholesize;
	}

	public void setMininterholesize(double mininterholesize) {
		this.mininterholesize = mininterholesize;
	}

	private boolean preserveInterHoles = false;

	public void setPreserveInterHoles(boolean preserveInterHoles) {
		this.preserveInterHoles = preserveInterHoles;
	}

	public boolean isPreserveInterHoles() {
		return preserveInterHoles;
	}

}
