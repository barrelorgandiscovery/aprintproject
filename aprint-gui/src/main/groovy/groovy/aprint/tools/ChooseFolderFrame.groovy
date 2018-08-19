package groovy.aprint.tools

import groovy.swing.SwingBuilder
import java.awt.BorderLayout as BL
import com.l2fprod.common.swing.JDirectoryChooser
import javax.swing.*


/**
 * Class for ease the use of a window for selecting a folder <br/>
 * 
 * utilisation de la classe :
 * 	import groovy.aprint.tools.*
 *  new ChooseFolderFrame(action : { f-> print "Hello le répertoire sélectionné est : ${f}" }).show()
 * 
 * 
 * @author use
 */
class ChooseFolderFrame {

	/*
	 Exemple de fenetre lançant un traitement sur un répertoire
	 */
	def action = { println "No Action Defined, please define the action member on the choose folder frame" }
	def title="Default Title"
	def label="Execute"

	/*
	 * Display the frame
	 */
	def show() {
		def f
		f = new SwingBuilder().
			frame(title:this.title, size:[400, 200], show: true, defaultCloseOperation : JFrame.DISPOSE_ON_CLOSE) {
				borderLayout()
				d = widget(new JDirectoryChooser())
				hbox(constraints : BL.CENTER) {
					button(text:'Selection du repertoire', actionPerformed : {
						try {

							d.showOpenDialog()

							foldername.setText("" + d.selectedFile)
						} catch (Throwable t) {
							println t
						}
					})
					foldername = label(text : ' <Repertoire > ')
				}

				button(text:this.label,
						actionPerformed: {
							
							try {
							
								if (d.selectedFile == null) {
									JOptionPane.showMessageDialog(null, "Vous Devez sélectionner un répertoire");
								} else {
									action(d.selectedFile)
									f.dispose()
								}
								
							} catch(Throwable t)
							{
								println t
							}
							
						},
						constraints:BL.SOUTH)
			}
		}
	
}
