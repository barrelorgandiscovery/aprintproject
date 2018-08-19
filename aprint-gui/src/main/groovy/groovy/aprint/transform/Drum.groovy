package groovy.aprint.transform

class Drum implements Comparable<Drum> {

	int midiCode;
	
	public int compareTo(Drum o) {
		return midiCode <=> o.midiCode;
	}
	
}
