
package groovy.aprint.transform

import org.barrelorgandiscovery.scale.*
import org.barrelorgandiscovery.tools.MidiHelper

/**
 * This category add a .helper method on the scale object to create a scale helper
 * 
 */
@Category(Scale)
class ScaleCategory {

	/**
	 * Create a scale helper
	 * @return
	 */
	ScaleHelper getHelper() {
		new ScaleHelper(this)
	}
}