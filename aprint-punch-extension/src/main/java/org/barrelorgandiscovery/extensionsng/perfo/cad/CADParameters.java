package org.barrelorgandiscovery.extensionsng.perfo.cad;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

public class CADParameters implements Serializable, Externalizable {

	/**
	 * Formes de trous pour les trous généraux
	 */
	private TrouType typeTrous = TrouType.TROUS_RECTANGULAIRES;

	/**
	 * forme des trous pour les ponts
	 */
	private TrouType typePonts = TrouType.TROUS_RECTANGULAIRES;

	private double tailleTrous = 3.0;

	private double pont = 1.0;

	private double pasDePontSiIlReste = 0.0;

	private double taillePagePourPliure = 160.0;

	private boolean exportPliures = false;

	private double startBookAdjustementFromBeginning = 80; // 80mm by default

	private int nombreDePlisAAjouterAuDebut = 0;

	private TypePliure typePliure = TypePliure.CONTINUE;

	private boolean exportDecoupeDesBords = true;
	
	private boolean exportTrous = true;
	
	

	public void setNombreDePlisAAjouterAuDebut(int nombreDePlisAAjouterAuDebut) {
		this.nombreDePlisAAjouterAuDebut = nombreDePlisAAjouterAuDebut;
	}

	public int getNombreDePlisAAjouterAuDebut() {
		return nombreDePlisAAjouterAuDebut;
	}

	public void setStartBookAdjustementFromBeginning(double startBookAdjustementFromBeginning) {
		this.startBookAdjustementFromBeginning = startBookAdjustementFromBeginning;
	}

	public double getStartBookAdjustementFromBeginning() {
		return startBookAdjustementFromBeginning;
	}

	public void setExportPliures(boolean exportPliures) {
		this.exportPliures = exportPliures;
	}

	public boolean isExportPliures() {
		return exportPliures;
	}

	public boolean isExportTrous() {
		return exportTrous;
	}
	
	public void setExportTrous(boolean exportTrous) {
		this.exportTrous = exportTrous;
	}
	
	public TrouType getTypeTrous() {
		return typeTrous;
	}

	public void setTypeTrous(TrouType typeTrous) {
		this.typeTrous = typeTrous;
	}

	public double getTailleTrous() {
		return tailleTrous;
	}

	public void setTailleTrous(double tailleTrous) {
		this.tailleTrous = tailleTrous;
	}

	public double getPont() {
		return pont;
	}

	public void setPont(double pont) {
		this.pont = pont;
	}

	public double getPasDePontSiIlReste() {
		return pasDePontSiIlReste;
	}

	public void setPasDePontSiIlReste(double pasDePontSiIlReste) {
		this.pasDePontSiIlReste = pasDePontSiIlReste;
	}

	public double getTaillePagePourPliure() {
		return taillePagePourPliure;
	}

	public void setTaillePagePourPliure(double taillePagePourPliure) {
		this.taillePagePourPliure = taillePagePourPliure;
	}

	public TrouType getTypePonts() {
		return typePonts;
	}

	public void setTypePonts(TrouType typePonts) {
		this.typePonts = typePonts;
	}

	public TypePliure getTypePliure() {
		return typePliure;
	}

	public void setTypePliure(TypePliure typePliure) {
		this.typePliure = typePliure;
	}

	public void setExportDecoupeDesBords(boolean exportDecoupeDesBords) {
		this.exportDecoupeDesBords = exportDecoupeDesBords;
	}

	public boolean isExportDecoupeDesBords() {
		return exportDecoupeDesBords;
	}
	
	
	private double largeurTrous = 3.0;

	private boolean surchargeLargeurTrous = false;
	
	public double getLargeurTrous() {
		return largeurTrous;
	}

	public void setLargeurTrous(double largeurTrous) {
		this.largeurTrous = largeurTrous;
	}

	public void setSurchargeLargeurTrous(boolean surchargeLargeurTrous) {
		this.surchargeLargeurTrous = surchargeLargeurTrous;
	}

	public boolean isSurchargeLargeurTrous() {
		return surchargeLargeurTrous;
	}
	
	

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

		typeTrous = TrouType.valueOf(in.readUTF());
		typePonts = TrouType.valueOf(in.readUTF());
		pasDePontSiIlReste = in.readDouble();
		exportPliures = in.readBoolean();
		pont = in.readDouble();
		taillePagePourPliure = in.readDouble();
		tailleTrous = in.readDouble();
		typePliure = TypePliure.valueOf(in.readUTF());
		startBookAdjustementFromBeginning = in.readDouble();
		nombreDePlisAAjouterAuDebut = in.readInt();
		try {
			exportDecoupeDesBords = in.readBoolean();
		} catch (Exception ex) {
			// for maintaining compatibility
		}
		try {
			exportTrous = in.readBoolean();
		} catch(Exception ex) {
			
		}
		

		try {
			surchargeLargeurTrous = in.readBoolean();
		} catch (Exception ex) {
		}

		try {
			largeurTrous = in.readDouble();
		} catch (Exception ex) {
		}

	}

	public void writeExternal(ObjectOutput out) throws IOException {

		out.writeUTF(typeTrous.name());
		out.writeUTF(typePonts.name());
		out.writeDouble(pasDePontSiIlReste);
		out.writeBoolean(exportPliures);
		out.writeDouble(pont);
		out.writeDouble(taillePagePourPliure);
		out.writeDouble(tailleTrous);
		out.writeUTF(typePliure.name());
		out.writeDouble(startBookAdjustementFromBeginning);
		out.writeInt(nombreDePlisAAjouterAuDebut);
		out.writeBoolean(exportDecoupeDesBords);
		out.writeBoolean(exportTrous);
		out.writeBoolean(surchargeLargeurTrous);
		out.writeDouble(largeurTrous);
	}

}
