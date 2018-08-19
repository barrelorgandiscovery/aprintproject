package org.barrelorgandiscovery.gui.ascale.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.barrelorgandiscovery.gui.ascale.ScaleComponent;

/**
 * Transfert Handler for trackdefs for the scale component, this class is only
 * usable with scalecomponent
 * 
 * @author use
 * 
 */
public class ScaleComponentTrackDefTransfertHandler extends TransferHandler {

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {

		final ScaleComponent sc = (ScaleComponent) c;

		Transferable t = new Transferable() {

			public boolean isDataFlavorSupported(DataFlavor flavor) {

				return flavor.isFlavorTextType();
			}

			public DataFlavor[] getTransferDataFlavors() {

				return new DataFlavor[] {
						ScaleComponentTrackDnd.trackDefDataFlavor,
						DataFlavor.getTextPlainUnicodeFlavor() };
				
			}

			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException, IOException {

				ScaleComponentTrackDnd scdnd = null;

				int track = sc.getHighLightedTrackDef();
				if (track != -1) {
					scdnd = new ScaleComponentTrackDnd(sc.getTrackDef(track),
							track);
				}
				if (flavor.isFlavorTextType()) {
					return ""
							+ (scdnd == null ? "null" : ""
									+ scdnd.getTrackNumber() + " "
									+ scdnd.getTrackDef());
				}
				if (flavor.isFlavorSerializedObjectType())
					return scdnd;

				throw new UnsupportedFlavorException(flavor);
			}
		};

		return t;
	}

}
