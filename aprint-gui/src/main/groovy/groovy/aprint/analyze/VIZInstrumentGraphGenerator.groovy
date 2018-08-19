package groovy.aprint.analyze

import org.barrelorgandiscovery.virtualbook.*
import org.barrelorgandiscovery.scale.*
import org.barrelorgandiscovery.tools.*


/**
 * Class for generating differential analysis on instruments
 * 
 * @author use
 *
 */
class VIZInstrumentGraphGenerator {

	/**
	 * instrument list (instrument object), not just the name
	 */
	def instrumentList = []
	
	/**
	 * Label for missing element
	 */
	def differenceLabel = "D"
	
	/**
	 * Missing label text
	 */
	def missingLabel = "-"
	
	/**
	 * add Label text
	 */
	def addLabel = "+"
	/**
	 * Specify the instrument label to display on the graph
	 */
	def makeInstrumentLabel = { instrument -> instrument.name }
	/**
	 * Specify a which differencies # we avoid plotting the link
	 */
	def linkThreshold = 5
	
	/**
	 * method to compare instrument
	 */
	def collectElementsToCompare = { instrument ->
		def scale = instrument.scale
		def listNotes = scale.tracksDefinition.findAll { it instanceof NoteDef }

		def textList = listNotes.collect { "" + MidiHelper.getMidiNote(it.midiNote) + "" + MidiHelper.getOctave(it.midiNote) }
		// def textList = listNotes.collect { "" + it.registerSetName + "-" + MidiHelper.getMidiNote(it.midiNote) + "" + MidiHelper.getOctave(it.midiNote) }


		return textList.sort()

	}

	/**
	 * Create the graph in graph viz format
	 * @return
	 */
	public String generateGraph() {

		def sb = new StringBuffer();

		def listInstrument = instrumentList

		listInstrument = listInstrument 

		def compare = { instrument1, instrument2 ->

			def listTrack = collectElementsToCompare

			def list  = []

			def el1 = listTrack(instrument1)
			
			def el2 = listTrack(instrument2)
			
			def retvalue = []

			el1.each {
				if ( ! (it in el2)  )
					retvalue.push(addLabel + it)

			}

			el2.each {
				if ( ! (it in el1)  )
					retvalue.push(missingLabel + it)

			}

			retvalue

		}

		def compared = []

		def emit = []


		def toID = { i ->
			"I" + StringTools.toHex( makeInstrumentLabel(i) )
		}

		def toNode = { i ->
			emit.push(i)
			toID(i) + " [label = \"" + makeInstrumentLabel(i) + "\"]"
		}


		sb << "digraph g{ "

		listInstrument.each {

			def i1 = it

			compared.push(i1)

			if (!(i1 in emit))
				sb << "" + toNode(i1) +";"

			(listInstrument.findAll { ! (it in compared)  }) .each {

				def i2 = it

				if (!(i2 in emit))
					sb << "" + toNode(i2)+";"

				def diff = compare(i1, i2)

				if (diff.size() < linkThreshold)
				{

					def weight = 1.0/( (diff.size() * diff.size() ) +1);

					String label = "";

					if ( diff.size() > 0)
					{
						label = differenceLabel + (diff.size()) + "\\n" +  (diff.inject(""){ str, i -> str + i + "\\n" } )
					}

					sb << toID(i1) + " -> " + toID(i2) + " [ label = \"" + label + "\", weight=" + weight +  ",penwidth=" + weight + "];"

				}
			}


		}

		sb << "}"

		return sb
	}

}
