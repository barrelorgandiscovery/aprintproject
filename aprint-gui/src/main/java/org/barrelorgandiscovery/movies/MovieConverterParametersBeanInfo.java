package org.barrelorgandiscovery.movies;

import org.barrelorgandiscovery.messages.Messages;

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;

/**
 * Class for UI displaying the movie parameters in an editor panel
 * 
 * @author Freydiere Patrice
 * 
 */
public class MovieConverterParametersBeanInfo extends BaseBeanInfo {

	public MovieConverterParametersBeanInfo() {
		super(MovieConverterParameters.class);

		ExtendedPropertyDescriptor height = addProperty("height"); //$NON-NLS-1$
		height.setShortDescription(Messages
				.getString("MovieConverterParametersBeanInfo.1")); //$NON-NLS-1$
		height.setDisplayName(Messages
				.getString("MovieConverterParametersBeanInfo.2")); //$NON-NLS-1$
		height.setCategory(Messages
				.getString("MovieConverterParametersBeanInfo.0")); //$NON-NLS-1$

		ExtendedPropertyDescriptor width = addProperty("width"); //$NON-NLS-1$
		width.setShortDescription(Messages
				.getString("MovieConverterParametersBeanInfo.5")); //$NON-NLS-1$
		width.setDisplayName(Messages
				.getString("MovieConverterParametersBeanInfo.6")); //$NON-NLS-1$
		width.setCategory(Messages
				.getString("MovieConverterParametersBeanInfo.7")); //$NON-NLS-1$

		ExtendedPropertyDescriptor fastRendering = addProperty("fastRendering"); //$NON-NLS-1$
		fastRendering.setShortDescription(Messages
				.getString("MovieConverterParametersBeanInfo.9")); //$NON-NLS-1$
		fastRendering.setDisplayName(Messages
				.getString("MovieConverterParametersBeanInfo.10")); //$NON-NLS-1$
		fastRendering.setCategory(Messages
				.getString("MovieConverterParametersBeanInfo.11")); //$NON-NLS-1$

		ExtendedPropertyDescriptor time = addProperty("showTime"); //$NON-NLS-1$
		time.setShortDescription(Messages
				.getString("MovieConverterParametersBeanInfo.13")); //$NON-NLS-1$
		time.setDisplayName(Messages
				.getString("MovieConverterParametersBeanInfo.14")); //$NON-NLS-1$
		time.setCategory(Messages
				.getString("MovieConverterParametersBeanInfo.15")); //$NON-NLS-1$

		ExtendedPropertyDescriptor showComposition = addProperty("showComposition"); //$NON-NLS-1$
		showComposition
				.setShortDescription(Messages.getString("MovieConverterParametersBeanInfo.16")); //$NON-NLS-1$
		showComposition.setDisplayName(Messages.getString("MovieConverterParametersBeanInfo.17")); //$NON-NLS-1$
		showComposition.setCategory(Messages
				.getString("MovieConverterParametersBeanInfo.15")); //$NON-NLS-1$
		
		ExtendedPropertyDescriptor showRegistration = addProperty("showRegistration"); //$NON-NLS-1$
		showRegistration
				.setShortDescription(Messages.getString("MovieConverterParametersBeanInfo.18")); //$NON-NLS-1$
		showRegistration.setDisplayName(Messages.getString("MovieConverterParametersBeanInfo.19")); //$NON-NLS-1$
		showRegistration.setCategory(Messages
				.getString("MovieConverterParametersBeanInfo.15")); //$NON-NLS-1$

	}

}
