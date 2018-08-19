
package groovy.aprint.transform

import org.barrelorgandiscovery.virtualbook.transformation.*
import org.barrelorgandiscovery.virtualbook.*
import org.barrelorgandiscovery.scale.*
import org.barrelorgandiscovery.tools.*


/**
 * Transformation helper, help construct a mapping between 2 instruments and create a transformation on it
 */
class TransformHelper {

    private LinearTransposition lt
    ScaleHelper origineScale;
    ScaleHelper destinationScale;
    
    /**
     * Construct the object using ScaleHelper
     */
    public TransformHelper(ScaleHelper origine, ScaleHelper destination)
    {
         this(origine, destination, false)
    }
    
    public TransformHelper(ScaleHelper origine, ScaleHelper destination, 
		                       boolean applyDischarge)
    {
     	   lt = new LinearTransposition(origine.scale, destination.scale, " to ", applyDischarge, false)
	       assert origine != null
	       origineScale = origine
	       assert destination != null
	       destinationScale = destination
    }
    
 
 
       /**
     * Construct the object given the original scale, and destination scale
     */
     public TransformHelper(Scale origine, Scale destination)
     {
     	this(origine, destination, false)
     }
    
     public TransformHelper(Scale origine, Scale destination, boolean applyDischarge)
     {
        lt = new LinearTransposition(origine, destination, " to ", applyDischarge, false)
        assert origine != null
        origineScale = new ScaleHelper(scale:origine)
        assert destination != null
        destinationScale = new ScaleHelper(scale:destination)
     }
    
 
    
    /**
    * create an association from source track o to source track d
    */
    public def map(Track o, Track d) { 
    	if (o != null && d != null)
    	{
    	     lt.setCorrespondance(o.no, d.no)
    	} 
    }
    
    /**
     * create an association from the o tracks to the d track
     * @param o origin tracks
     * @param d destination tracks, an array of track
     * @return the unmapped elements in a Map
     */
    public def map(Track[] o, Track[] d) {
        def unmapped = [:]
        
        o.eachWithIndex { t, index ->
            if ( index < d.length && d[index] )
            {
                map(t, d[index])    
            } else 
            {
                unmapped[t]="Not found"
            }
        }
        
		// return unmapped tracks
        unmapped
    
    } 
    
    /**
     * Transform the virtual book in an other virtual book, using this object configuration
     */
    public TranspositionResult transform(VirtualBook vb)
    {
    		lt.transpose(vb) // return the transposition result
    }
    
    /**
     * To String, display the current transform
     */
    public String toString() { 
		
        def r = ""
		
        for(i in 0..lt.scaleSource.tracksDefinition.length - 1)
        {
            r += " Track " + i + "(" + lt.scaleSource.tracksDefinition[i] + ")" +  " mapped to " + lt.getAllCorrespondances(i) + "   "  + "\n"
        }
        
        r
    
    }
    
    /**
     * Dump the current transformation, in a hash containing track matching
     */
    public def dumpTransformation()
    {
        def m = [:]
        lt.scaleSource.tracksDefinition.eachWithIndex { item,index ->
        	int[] t = lt.getAllCorrespondances(index);
        	m[index] = t
        }
        
        m
    }
    
    /**
     * Clear Mapping for this transformation
     */
    public def clear()
    {
    	  lt.scaleSource.tracksDefinition.eachWithIndex { item,index ->
        	lt.clearCorrespondance(index)
        }
    }
    
    /**
     * Load a transformation
     * 
     */
    public def loadTransformation(m)
    {
    	if (m == null) { 
    		throw new Exception("null transform passed")
    	}
    	
    	def unmap = [:]
    	m.each { key,value -> 
    	    Track to = origineScale.track(key)
    	    if (to == null)
    	    {
    	    	throw new Exception("track ${key} not found")
    	    }
    		value.each {
    		    Track de =  destinationScale.track(it)
    		    if (de == null)
    		       throw new Exception("destination track ${it} not found")
    		    def runmap = map(to,de)
    			if (runmap != null)
    				unmap.addAll(runmap)
    		}
    	}
    	
    	unmap
    	
    }
   
    
}