package org.barrelorgandiscovery.extensionsng.perfo.ng.panel.wizard;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

/**
 * specialize thr HTML editorKit for rendering embedded images
 * 
 * @author pfreydiere
 * 
 */
public class CustomEditorKit extends HTMLEditorKit {
	private static HTMLEditorKit.HTMLFactory factory = null;

	@Override
	public ViewFactory getViewFactory() {
		if (factory == null) {
			factory = new HTMLEditorKit.HTMLFactory() {

				@Override
				public View create(Element elem) {
					AttributeSet attrs = elem.getAttributes();
					Object elementName = attrs
							.getAttribute(AbstractDocument.ElementNameAttribute);
					Object o = (elementName != null) ? null : attrs
							.getAttribute(StyleConstants.NameAttribute);
					if (o instanceof HTML.Tag) {
						HTML.Tag kind = (HTML.Tag) o;
						if (kind == HTML.Tag.IMG) {
							// HERE is the call to the special class...
							return new CustomImageView(elem);
						} // End if(kind == IMG)...
					} // End if(instance of Tag)...
					return super.create(elem);
				} // End create()...

			}; // End new HTMLFactory()...
		} // End if(factory == null)...
		return factory;
	} // End getViewFactory()...

} // End RITB64_HTMLE