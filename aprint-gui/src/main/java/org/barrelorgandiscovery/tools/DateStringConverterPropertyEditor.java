package org.barrelorgandiscovery.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.l2fprod.common.beans.editor.StringConverterPropertyEditor;

public class DateStringConverterPropertyEditor extends
		StringConverterPropertyEditor {

	private Logger logger = Logger
			.getLogger(DateStringConverterPropertyEditor.class);

	@Override
	protected Object convertFromString(String text) {

		if (text != null) {
			try {
				return DateFormat.getDateInstance(DateFormat.SHORT,
						Locale.getDefault()).parse(text);

			} catch (Exception ex) {
				logger.error("error in parsing date :" + text, ex);
			}
		}

		return null;
	}

	@Override
	protected String convertToString(Object value) {
		if (value == null)
			return null;

		return DateFormat
				.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(
						(Date) value);

	}

}
