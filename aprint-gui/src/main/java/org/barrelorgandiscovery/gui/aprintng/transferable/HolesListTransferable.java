package org.barrelorgandiscovery.gui.aprintng.transferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.Hole;

public class HolesListTransferable implements Transferable {

	private static Logger logger = Logger.getLogger(HolesListTransferable.class);

	private static final String HOLES = "aprint-binary-holes";
	// private static final String DATAFLAVOR_BINARY =
	// "application/aprint-binary-holes";

	public static class Data implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8886511925544089203L;

		public Scale associatedScale;
		public List<Hole> holes;
	}

	private Data data;

	public HolesListTransferable(Scale scale, Collection<Hole> holes) {
		Data d = new Data();
		d.associatedScale = scale;
		d.holes = new ArrayList<>();
		if (holes != null) {
			d.holes.addAll(holes);
		}
		this.data = d;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] {};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		try {
			return flavor.isMimeTypeEqual(createDataFlavorBinary());
		} catch (Exception ex) {
			logger.error("error : " + ex.getMessage(), ex);
			return false;
		}
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (isDataFlavorSupported(flavor)) {
			return data;
		}
		throw new UnsupportedFlavorException(flavor);
	}

	public static DataFlavor createDataFlavorBinary() throws Exception {
		return new DataFlavor(HolesListTransferable.Data.class, HOLES);
	}

}
