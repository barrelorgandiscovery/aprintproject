package org.barrelorgandiscovery.optimizers.cad;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.barrelorgandiscovery.extensionsng.perfo.cad.TrouType;
import org.barrelorgandiscovery.extensionsng.perfo.cad.TypePliure;

public class XOptimParameters implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7295413239681833891L;

	/**
	 * Formes de trous pour les trous généraux
	 */
	private TrouType typeTrous = TrouType.TROUS_RECTANGULAIRES;

	/**
	 * forme des trous pour les ponts
	 */
	private TrouType typePonts = TrouType.TROUS_RECTANGULAIRES;

	/**
	 * taille max des trous
	 */
	private double tailleTrous = 3.0;

	private double pont = 1.0;

	private double pasDePontSiIlReste = 0.0;

	private double taillePagePourPliure = 160.0;

	private boolean exportTrous = true;


	private double startBookAdjustementFromBeginning = 80; // 80mm by default

	private int nombreDePlisAAjouterAuDebut = 0;

	private TypePliure typePliure = TypePliure.CONTINUE;

	private double powerFractionPass1 = 1.0; // by default, 100%

	private double halfCutPower = 0.5; // by default 50%

	private double speedFractionPass1 = 1.0;

	private boolean hasMultiplePass = false;

	private double powerFractionMultiplePass = 1.0; // be default, 100%

	private double speedFractionMultiplePass = 1.0;

	private double optimPageSize = 5.0; // 5 cm par défaut

	private double largeurTrous = 3.0;

	private boolean surchargeLargeurTrous = false;
	
	
	private boolean exportPliures = false;
	
	// puissance et vitesse pour les pliures

	public boolean isExportTrous() {
		return exportTrous;
	}

	public void setExportTrous(boolean exportTrous) {
		this.exportTrous = exportTrous;
	}

	public void setHalfCutPower(double halfCutPower) {
		this.halfCutPower = halfCutPower;
	}

	public double getHalfCutPower() {
		return halfCutPower;
	}

	public double getPowerFractionPass1() {
		return powerFractionPass1;
	}

	public void setPowerFractionPass1(double powerFractionPass1) {
		this.powerFractionPass1 = powerFractionPass1;
	}

	public double getSpeedFractionPass1() {
		return speedFractionPass1;
	}

	public void setSpeedFractionPass1(double speedFractionPass1) {
		this.speedFractionPass1 = speedFractionPass1;
	}

	///////////////////////////////////////////////////////////////////
	// multiple pass

	int multiplePass = 1;

	public int getMultiplePass() {
		return multiplePass;
	}

	public void setMultiplePass(int multiplePass) {
		this.multiplePass = multiplePass;
	}

	public boolean isHasMultiplePass() {
		return hasMultiplePass;
	}

	public void setHasMultiplePass(boolean hasmultiplepass) {
		this.hasMultiplePass = hasmultiplepass;
	}

	public double getPowerFractionMultiplePass() {
		return powerFractionMultiplePass;
	}

	public void setPowerFractionMultiplePass(double powerFractionmultiplePass) {
		this.powerFractionMultiplePass = powerFractionmultiplePass;
	}

	public double getSpeedFractionMultiplePass() {
		return speedFractionMultiplePass;
	}

	public void setSpeedFractionMultiplePass(double speedFractionMultiplePass) {
		this.speedFractionMultiplePass = speedFractionMultiplePass;
	}

	public void setOptimPageSize(double optimPageSize) {
		this.optimPageSize = optimPageSize;
	}

	public double getOptimPageSize() {
		return optimPageSize;
	}

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
	
	
	
	
	// reglage des passes pour les pliures
	private double powerFractionPliures = 1.0;
	private double speedFractionPliures = 1.0;
	private boolean pliureMultipass = false;
	private int pliuresMultipassPassNumber = 1;
	private double pliuresMultipassPowerFraction = 1.0;
	private double pliuresMultipassSpeedFraction = 1.0;
	
	
	public int getPliuresMultipassPassNumber() {
		return pliuresMultipassPassNumber;
	}
	
	public void setPliuresMultipassPassNumber(int pliuresMultipassPassNumber) {
		this.pliuresMultipassPassNumber = pliuresMultipassPassNumber;
	}
	
	public double getPowerFractionPliures() {
		return powerFractionPliures;
	}

	public void setPowerFractionPliures(double powerFractionPliures) {
		this.powerFractionPliures = powerFractionPliures;
	}

	public double getSpeedFractionPliures() {
		return speedFractionPliures;
	}

	public void setSpeedFractionPliures(double speedFractionPliures) {
		this.speedFractionPliures = speedFractionPliures;
	}

	public boolean isPliureMultipass() {
		return pliureMultipass;
	}

	public void setPliureMultipass(boolean pliureMultipass) {
		this.pliureMultipass = pliureMultipass;
	}

	public double getPliuresMultipassPowerFraction() {
		return pliuresMultipassPowerFraction;
	}

	public void setPliuresMultipassPowerFraction(double pliuresMultipassPowerFraction) {
		this.pliuresMultipassPowerFraction = pliuresMultipassPowerFraction;
	}

	public double getPliuresMultipassSpeedFraction() {
		return pliuresMultipassSpeedFraction;
	}

	public void setPliuresMultipassSpeedFraction(double pliuresMultipassSpeedFraction) {
		this.pliuresMultipassSpeedFraction = pliuresMultipassSpeedFraction;
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

		powerFractionPass1 = in.readDouble();
		speedFractionPass1 = in.readDouble();

		hasMultiplePass = in.readBoolean();
		multiplePass = in.readInt();
		if (multiplePass < 0) {
			multiplePass = 0;
		}
		powerFractionMultiplePass = in.readDouble();
		speedFractionMultiplePass = in.readDouble();

		optimPageSize = in.readDouble();

		exportTrous = in.readBoolean();

		try {
			halfCutPower = in.readDouble();
		} catch (Exception ex) {
			// cannot read the half cut power
		}

		try {
			surchargeLargeurTrous = in.readBoolean();
		} catch (Exception ex) {
		}

		try {
			largeurTrous = in.readDouble();
		} catch (Exception ex) {
		}
	
		// additional properties for book page separation
		try {
			powerFractionPliures = in.readDouble();
		} catch (Exception ex) {
		}
		
		try {
			speedFractionPliures = in.readDouble();
		} catch (Exception ex) {
		}
		
		try {
			pliureMultipass = in.readBoolean();
		} catch (Exception ex) {
		}
		
		try {
			pliuresMultipassPowerFraction = in.readDouble();
		} catch (Exception ex) {
		}
		try {
			pliuresMultipassSpeedFraction = in.readDouble();
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
		out.writeDouble(powerFractionPass1);
		out.writeDouble(speedFractionPass1);
		out.writeBoolean(hasMultiplePass);
		out.writeInt(multiplePass);
		out.writeDouble(powerFractionMultiplePass);
		out.writeDouble(speedFractionMultiplePass);
		out.writeDouble(optimPageSize);
		out.writeBoolean(exportTrous);
		out.writeDouble(halfCutPower);
		out.writeBoolean(surchargeLargeurTrous);
		out.writeDouble(largeurTrous);
		
		
		out.writeDouble(powerFractionPliures);
		out.writeDouble(speedFractionPliures);
		out.writeBoolean(pliureMultipass);
		out.writeDouble(pliuresMultipassPowerFraction);
		out.writeDouble(pliuresMultipassSpeedFraction);
		
	
		
		
	}

}
