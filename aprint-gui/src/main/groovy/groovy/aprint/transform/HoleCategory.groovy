
package groovy.aprint.transform

import org.barrelorgandiscovery.scale.*
import org.barrelorgandiscovery.virtualbook.*

import org.barrelorgandiscovery.tools.MidiHelper

/**
 * Tools for hole manipulation, creating a new hole from a new start, a new length, or changing the track number
 */
@Category(Hole)
class HoleCategory {

	/**
	 * Create a new Hole with a new Track given in parameter
	 */
	Hole newTrack(int t) {
		new Hole(t,this.timestamp, this.timeLength )
	}

	/**
	 * New Hole in which we change track
	 * @param t
	 * @return
	 */
	Hole changeTrack(int t) {
		newTrack(t)
	}


	/**
	 * Create a new Hole with the track given in parameter
	 */
	Hole newTrack(Track t) {
		newTrack(t.no)
	}

	/**
	 * Create a new Hole with the track given in parameter
	 */
	Hole changeTrack(Track t) {
		newTrack(t)
	}

	/**
	 * Create a new Hole copy with a new timestamp
	 */
	Hole newTs(long ts) {
		new Hole(this.track,ts, this.timeLength )
	}

	/**
	 * change the hole start
	 */
	Hole beginAt(long ts) {
		newTs(ts)
	}



	/**
	 * Create a new Hole copy with a new time length
	 */
	Hole newLength(long tl) {
		new Hole(this.track,this.timestamp, tl )
	}

	/**
	 * Change the length of the hole
	 * @param tl
	 * @return
	 */
	Hole changeLength(long tl) {
		newLength(tl)
	}
	
	
}


