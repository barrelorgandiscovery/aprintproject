package groovy.aprint.tools

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;

import groovy.swing.SwingBuilder;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.barrelorgandiscovery.tools.BeanAsk;

import com.jeta.forms.components.image.ImageComponent;
import com.jeta.forms.components.line.HorizontalLineComponent;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.beans.BaseBeanInfo;


/**
 * Helper class for creating GUI frames or interfaces
 * 
 * @author pfreydiere
 */
public class GUIHelper {

	/**
	 * function to show a message to the user
	 * 
	 * @param textToDisplay the displayed text to show
	 */
	public static void showMessage(textToDisplay){
		JOptionPane.showMessageDialog(null, textToDisplay);
	}


	/**
	 * internal method for describing the parameters to take
	 * @param c
	 * @param properties
	 * @return
	 */
	private static def addPropertyBeanProperties(Class c , Map properties) {

		def bi = new BaseBeanInfo(c)

		properties.each { k, v ->
			def p = bi.addProperty(k)
			v.each{ key, value ->
				p[key] = value
			}
		}

		return bi
	}



	/**
	 * Ask parameters from a class
	 * 
	 * <b>exemple d'utilisation</b>
	 * 
	 * <pre>
	 import groovy.aprint.tools.*
	 class Test {
	 String test
	 }
	 GUIHelper.beanAsk(services.owner, "saisie de paramètres", new Test(), ["test":["category":"ma description"]])
	 </pre>
	 * 
	 * @param owner main window, for dialog showing
	 * @param titre the window title
	 * @param bean the bean to fill from input parameters
	 * @param properties, hash of hash defining for each properties, the input method and so on
	 * 
	 * @return the filled bean
	 */
	public static Object beanAsk(owner,titre, Object bean, Map properties) throws Exception {
		if (bean == null)
			throw new Exception("null bean")
		if (properties == null)
			throw new Exception("null properties")
		BeanAsk.askForParameters(owner, titre, bean, addPropertyBeanProperties(bean.getClass(), properties))
	}


	
	
	/**
	 * Adds fill components to empty cells in the first row and first column of the grid.
	 * This ensures that the grid spacing will be the same as shown in the designer.
	 * @param cols an array of column indices in the first row where fill components should be added.
	 * @param rows an array of row indices in the first column where fill components should be added.
	 */
	private static void addFillComponents( Container panel, int[] cols, int[] rows )
	{
	   Dimension filler = new Dimension(10,10);
 
	   boolean filled_cell_11 = false;
	   CellConstraints cc = new CellConstraints();
	   if ( cols.length > 0 && rows.length > 0 )
	   {
		  if ( cols[0] == 1 && rows[0] == 1 )
		  {
			 /** add a rigid area  */
			 panel.add( Box.createRigidArea( filler ), cc.xy(1,1) );
			 filled_cell_11 = true;
		  }
	   }
 
	   for( int index = 0; index < cols.length; index++ )
	   {
		  if ( cols[index] == 1 && filled_cell_11 )
		  {
			 continue;
		  }
		  panel.add( Box.createRigidArea( filler ), cc.xy(cols[index],1) );
	   }
 
	   for( int index = 0; index < rows.length; index++ )
	   {
		  if ( rows[index] == 1 && filled_cell_11 )
		  {
			 continue;
		  }
		  panel.add( Box.createRigidArea( filler ), cc.xy(1,rows[index]) );
	   }
 
	}
	
	
	private static JPanel createPanelTitle(String title)
	{
	   JPanel jpanel1 = new JPanel();
	   FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE");
	   CellConstraints cc = new CellConstraints();
	   jpanel1.setLayout(formlayout1);
 
	   ImageComponent ic = new ImageComponent();
	   ic.setIcon(new ImageIcon(GUIHelper.class.getResource("misc.png")));
	   jpanel1.add(ic,cc.xy(1,1));
 
	   JLabel jlabel1 = new JLabel();
	   jlabel1.setFont(new Font("Tahoma",Font.BOLD,14));
	   jlabel1.setText(title);
	   jpanel1.add(jlabel1,cc.xy(3,1));
 
	   addFillComponents(jpanel1,[ 2 ] as int[],new int[0]);
	   return jpanel1;
	}
 
	private static JPanel createPanel2(Map m)
	{
	   JPanel jpanel1 = new JPanel();
	   
	   def l = ["CENTER:DEFAULT:NONE"] * (m.size()*2 - 1)
	   
	   FormLayout formlayout1 = 
	   				new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0)",
		   							l.join(','));
	
								   
	   CellConstraints cc = new CellConstraints();
	   jpanel1.setLayout(formlayout1);
 
	   def i = 1;
	   m.each { k,v ->
		   
		   def label
		   if (k instanceof String)
		   {
			   label = new JLabel(k);
		   }
		   
		   jpanel1.add label,cc.xy(1, i)
		   jpanel1.add v,cc.xy(3,i)
		   if (i < m.size()*2-1){
			   addFillComponents(jpanel1, [2] as int[] ,[ i+1 ] as int[]);
		   }
		   i+=2
		   
		   
	   }
	  
	   
	   
	   return jpanel1;
	}
 
	private static JPanel createPanelComponents(Component[] components)
	{
	   
	   JPanel jpanel1 = new JPanel();
	   
	   if (components == null || components.length == 0) {
	   		return jpanel1
	   }
	   
	   def l = ["FILL:DEFAULT:NONE"] * (components.length * 2 - 1)
	   FormLayout formlayout1 = new FormLayout(l.join(','),
		   										"CENTER:DEFAULT:NONE");
											   
	   CellConstraints cc = new CellConstraints();
	   jpanel1.setLayout(formlayout1);
 
	   def i=1
	   components.each {
		   jpanel1.add(it,cc.xy(i,1));
		   
		   if (i < 2*components.length - 1){
			   addFillComponents(jpanel1,[ i+1 ] as int[],new int[0]);
		   }
		   
		   i+=2
	   }
	   
	 
	   
	   return jpanel1;
	}
 
 
	
	private static JPanel createPanel(String title, Map parameters, Component[] bottomComponents)
	{
	   JPanel jpanel1 = new JPanel();
	   FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:GROW(1.0),CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE");
	   CellConstraints cc = new CellConstraints();
	   jpanel1.setLayout(formlayout1);
 
	   HorizontalLineComponent lc = new HorizontalLineComponent();
	   
	   jpanel1.add(lc,cc.xywh(2,3,3,1));
 
	   jpanel1.add(createPanelTitle(title),cc.xywh(2,2,3,1));
	   
	   def panelParameters =createPanel2(parameters)
	   
	   JScrollPane jscrollpane1 = new JScrollPane();
	   jscrollpane1.setBorder(null);
	   jscrollpane1.setViewportView(panelParameters);
	   jscrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	   jscrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	  
	   jpanel1.add(jscrollpane1,new CellConstraints(2,4,2,1,CellConstraints.FILL,CellConstraints.TOP));
	   
	   
	   jpanel1.add(createPanelComponents(bottomComponents),new CellConstraints(3,6,1,1,CellConstraints.CENTER,CellConstraints.DEFAULT));
	   addFillComponents(jpanel1,[ 1,2,3,4,5,6 ] as int[],[1,2,3,4,5,6,7 ] as int[]);
	   return jpanel1;
	}
 
	
	/**
	 * ShowFrame with a list of parameter, the parameters are listed vertically
	 *
	 * @param components a map, containing the label (component or text), and the associated component in value
	 * 
	 * @param bottomComponent component displayed at the bottom of the window
	 * 
	 * @return the frame (JFrame)
	 */
	public static JFrame showFrame(Map components, 
								   Object bottomComponent = null, 
								   String title = "Parametres") {
								   
		Component[] bc;
		if (bottomComponent != null)
		{
			if (bottomComponent instanceof Component)
			{
				bc = [ bottomComponent ]
			}
		} 
		
		bc = bc as Component[]
								   
		SwingBuilder swing = new SwingBuilder()
		swing.frame(defaultCloseOperation: JFrame.DISPOSE_ON_CLOSE) {
			
			borderLayout()
			widget(createPanel(title,components, bc ),
						constraints: BorderLayout.CENTER);

		}
	}
}
