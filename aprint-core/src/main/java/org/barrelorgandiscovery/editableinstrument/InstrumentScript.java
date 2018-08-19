package org.barrelorgandiscovery.editableinstrument;

import java.io.Serializable;

/**
 * Class for defining a script associated to the instrument
 * 
 * @author Freydiere Patrice
 * 
 */
public class InstrumentScript implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3965316105257575166L;

	/**
	 * script name
	 */
	private String name;

	public static enum InstrumentScriptLanguage {
		GROOVY, OTHER
	};
	
	public static enum InstrumentScriptType {
		IMPORTER_SCRIPT, MIDI_OUTPUT_SCRIPT
	}

	private String content;

	private InstrumentScriptLanguage language;
	
	private InstrumentScriptType typescript;

	public InstrumentScript(String name, InstrumentScriptLanguage language, InstrumentScriptType type,
			String content) throws Exception {
		if (name == null || "".equals(name))
			throw new Exception("bad name for script");
		this.name = name;
		this.language = language;
		this.content = content;
		this.typescript = type;

	}

	/**
	 * Get the name of the script
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * get the content of the type
	 * 
	 * @return
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Change the script content ...
	 * 
	 * @param newContent
	 *            the new script content
	 */
	public void changeContent(String newContent) {
		this.content = newContent;
	}

	/**
	 * get the type of script
	 * 
	 * @return
	 */
	public InstrumentScriptLanguage getScriptLanguage() {
		return language;
	}
	
	public InstrumentScriptType getType()
	{
		return typescript;
	}

	@Override
	public String toString() {
		return this.getName() + "-" +  getType().toString();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;

		if (obj.getClass() != getClass())
			return false;

		InstrumentScript sobj = (InstrumentScript) obj;

		return this.content.equals(sobj.content) && this.name.equals(sobj.name)
				&& this.language == sobj.language && this.typescript == sobj.typescript;
	}

}
