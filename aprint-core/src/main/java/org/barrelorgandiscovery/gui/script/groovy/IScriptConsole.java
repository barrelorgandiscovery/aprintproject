package org.barrelorgandiscovery.gui.script.groovy;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.text.AttributeSet;

public interface IScriptConsole {

	/**
	 * Append to script console ...
	 * 
	 * @param component
	 * @param style
	 * @throws Exception
	 */
	public abstract void appendOutput(Component component, AttributeSet style)
			throws Exception;

	/**
	 * Append to script console ...
	 * 
	 * @param icon
	 * @param style
	 * @throws Exception
	 */
	public abstract void appendOutput(Icon icon, AttributeSet style)
			throws Exception;

	/**
	 * Append to script console ...
	 * 
	 * @param text
	 * @param style
	 * @throws Exception
	 */
	public abstract void appendOutput(String text, AttributeSet style)
			throws Exception;

	/**
	 * Append to script console ...
	 * 
	 * @param text
	 * @param style
	 * @throws Exception
	 */
	public abstract void appendOutputNl(String text, AttributeSet style)
			throws Exception;

	/**
	 * append exception to console output
	 * @param t
	 * @throws Exception
	 */
	public abstract void appendOutput(Throwable t) throws Exception;

	/**
	 * Clear the console ...
	 */
	public abstract void clearConsole();

}