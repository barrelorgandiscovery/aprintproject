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

	private double tailleTrous = 3.0;

	private double pont = 1.0;

	private double pasDePontSiIlReste = 0.0;

	private double taillePagePourPliure = 160.0;

	private boolean exportTrous = true;

	private boolean exportPliures = false;

	private double startBookAdjustementFromBeginning = 80; // 80mm by default

	private int nombreDePlisAAjouterAuDebut = 0;

	private TypePliure typePliure = TypePliure.CONTINUE;

	private double powerFractionPass1 = 1.0; // by default, 100%

	private double halfCutPower = 0.5; // by default 50%

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

	public boolean isHas2pass() {
		return has2pass;
	}

	public void setHas2pass(boolean has2pass) {
		this.has2pass = has2pass;
	}

	public double getPowerFractionPass2() {
		return powerFractionPass2;
	}

	public void setPowerFractionPass2(double powerFractionPass2) {
		this.powerFractionPass2 = powerFractionPass2;
	}

	public double getSpeedFractionPass2() {
		return speedFractionPass2;
	}

	public void setSpeedFractionPass2(double speedFractionPass2) {
		this.speedFractionPass2 = speedFractionPass2;
	}

	private double speedFractionPass1 = 1.0;

	private boolean has2pass = false;

	private double powerFractionPass2 = 1.0; // be default, 100%

	private double speedFractionPass2 = 1.0;

	private double optimPageSize = 5.0; // 5 cm par d�faut

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

		has2pass = in.readBoolean();
		powerFractionPass2 = in.readDouble();
		speedFractionPass2 = in.readDouble();

		optimPageSize = in.readDouble();

		exportTrous = in.readBoolean();

		try {
			halfCutPower = in.readDouble();
		} catch (Exception ex) {
			// cannot read the half cut power
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
		out.writeBoolean(has2pass);
		out.writeDouble(powerFractionPass2);
		out.writeDouble(speedFractionPass2);
		out.writeDouble(optimPageSize);
		out.writeBoolean(exportTrous);
		out.writeDouble(halfCutPower);
	}

}
