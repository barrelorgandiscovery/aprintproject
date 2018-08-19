package groovy.aprint.tools


import groovy.swing.SwingBuilder
import java.awt.BorderLayout as BL
import javax.swing.*

/**
 * Class for selecting a file, the action closure is launch after the 
 * file has been selected
 * 
 * 
 */
class ChooseFileFrame {

	/**
	 * Callback with one parameter(the selected file) invoked when the file has been selected
	 */
	def action = { println "No Action Defined, please define the action member on the choose folder frame" }

	/**
	 * Window title
	 */
	def title="Default Title"


	/**
	 * Display the frame
	 */
	def show() {

		JFileChooser fc = new JFileChooser();

		int result = fc.showOpenDialog(null)
		if (result == JFileChooser.APPROVE_SELECTION) {
			action(fc.selectedFile);
		}
	}
}
