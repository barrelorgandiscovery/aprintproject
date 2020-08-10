package org.barrelorgandiscovery.model.type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.model.ModelType;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.io.ScaleIO;

/**
 * Scale of a specific organ Permit to parametrize a scale for a given organ or
 * couple of organ
 * 
 * @author pfreydiere
 * 
 */
public class ScaleType implements ModelType, Serializable {

	private Scale scale;

	private JavaType inner = new JavaType(Scale.class);

	public ScaleType(String serializedForm) throws Exception {

		// read the Scale
		Decoder base64Decoder = Base64.getDecoder();

		scale = ScaleIO.readGamme(new ByteArrayInputStream(base64Decoder.decode(serializedForm.getBytes())));

	}

	public String serializedForm() throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ScaleIO.writeGamme(scale, baos);

		Encoder base64Encoder = Base64.getEncoder();
		// BASE64Encoder base64Encoder = new BASE64Encoder();

		return new String(base64Encoder.encode(baos.toByteArray()));
	}

	public ScaleType(Scale scale) {
		this.scale = scale;
	}

	public boolean doesValueBelongToThisType(Object value) {
		boolean b = inner.doesValueBelongToThisType(value);
		if (!b)
			return false;

		if (value == null) {
			if (scale == null)
				return true;

			return false;
		}

		// check the scale definition
		Scale scaleValue = (Scale) value;

		return scaleValue.equals(scale);
	}

	public boolean isAssignableFrom(ModelType type) {
		assert type != null;

		if (type.getClass() != getClass())
			return false;

		return ((ScaleType) type).scale.equals(scale);

	}

	public String getDescription() {
		return "Scale of type " + scale.getName();
	}

	public String getName() {
		return scale.getName();
	}

	@Override
	public String getLabel() {
		String key = getName();
		return Messages.getString(key);
	}

}
