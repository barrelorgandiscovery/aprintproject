package org.barrelorgandiscovery.optimizers.cad;

import org.barrelorgandiscovery.extensionsng.perfo.cad.TrouTypeComboBoxPropertyEditor;
import org.barrelorgandiscovery.extensionsng.perfo.cad.TypePliuresComboBoxPropertyEditor;

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.beans.editor.BooleanPropertyEditor;
import com.l2fprod.common.beans.editor.DoublePropertyEditor;
import com.l2fprod.common.beans.editor.IntegerPropertyEditor;

public class XOptimParametersBeanInfo extends BaseBeanInfo {

	public XOptimParametersBeanInfo() {
		super(XOptimParameters.class);

		ExtendedPropertyDescriptor startBookAdjustementFromBeginning = addProperty("startBookAdjustementFromBeginning");
		startBookAdjustementFromBeginning
				.setShortDescription("Définition de la marge entre le premier trou du carton et le debut, en mm");
		startBookAdjustementFromBeginning.setDisplayName("Marge de debut du carton (mm)");
		startBookAdjustementFromBeginning.setPropertyEditorClass(DoublePropertyEditor.class);
		startBookAdjustementFromBeginning.setCategory("Carton");

		ExtendedPropertyDescriptor nombreDePlisAAjouterAuDebut = addProperty("nombreDePlisAAjouterAuDebut");
		nombreDePlisAAjouterAuDebut
				.setShortDescription("Plis supplémentaires à ajouter avant le debut du carton (nombre de plis)");
		nombreDePlisAAjouterAuDebut.setDisplayName("Nombre de plis supplémentaires de debut de carton");
		nombreDePlisAAjouterAuDebut.setPropertyEditorClass(IntegerPropertyEditor.class);
		nombreDePlisAAjouterAuDebut.setCategory("Carton");

		ExtendedPropertyDescriptor optimPageSize = addProperty("optimPageSize");
		optimPageSize.setShortDescription("Taille de la page pour optimisation du déplacement de la tête");
		optimPageSize.setPropertyEditorClass(DoublePropertyEditor.class);
		optimPageSize.setDisplayName("Taille de la page d'optimisation (cm)");
		optimPageSize.setCategory("Optimisation");
		
		
		ExtendedPropertyDescriptor exportTrous = addProperty("exportTrous");
		exportTrous.setShortDescription("Export des trous, cette case à cocher définit l'export des trous");
		exportTrous.setDisplayName("Export des trous ?");
		exportTrous.setCategory("Trous");

		ExtendedPropertyDescriptor typeTrous = addProperty("typeTrous");
		typeTrous.setShortDescription("Type de trous");
		typeTrous.setDisplayName("Types de trous");
		typeTrous.setPropertyEditorClass(TrouTypeComboBoxPropertyEditor.class);
		typeTrous.setCategory("Trous");

		ExtendedPropertyDescriptor taillTrous = addProperty("tailleTrous");
		taillTrous.setShortDescription(
				"Longueur Maximum des découpes pour les trous, si le trou est plus grand que cette longeur, un pont est créé.\nSi vous ne souhaitez pas de ponts (mécanique, ... ), \n mettez une dimension de pont à 0");
		taillTrous.setDisplayName("Longueur Maximum des découpes (mm)");
		taillTrous.setPropertyEditorClass(DoublePropertyEditor.class);
		taillTrous.setCategory("Trous");

		ExtendedPropertyDescriptor surchargeLargeurTrous = addProperty("surchargeLargeurTrous");
		surchargeLargeurTrous.setShortDescription(
				"Utiliser le paramètre Largeur Trous, ici au lieu de l'utilisation des métriques dans la gamme");
		surchargeLargeurTrous.setDisplayName("Surcharge de largeur des trous (activation)");
		surchargeLargeurTrous.setCategory("Trous");

		ExtendedPropertyDescriptor hauteurDesTrous = addProperty("largeurTrous");
		hauteurDesTrous
				.setShortDescription("Largeur des trous spécifiée si la surcharge par rapport à la gamme est activée");
		hauteurDesTrous.setDisplayName("Surcharge de largeur des trous (mm)");
		hauteurDesTrous.setPropertyEditorClass(DoublePropertyEditor.class);
		hauteurDesTrous.setCategory("Trous");

		ExtendedPropertyDescriptor dimensionPonts = addProperty("pont");
		dimensionPonts.setShortDescription("Dimension des ponts (mm)");
		dimensionPonts.setPropertyEditorClass(DoublePropertyEditor.class);
		dimensionPonts.setDisplayName("Dimension des ponts (mm)");
		dimensionPonts.setCategory("Trous");

		ExtendedPropertyDescriptor silreste = addProperty("pasDePontSiIlReste");
		silreste.setShortDescription(
				"On abandonne le dernier pont s'il reste un petite distance intérieure à la valeure saisie (en mm)");
		silreste.setPropertyEditorClass(DoublePropertyEditor.class);
		silreste.setDisplayName("Pas de pont s'il reste (mm)");
		silreste.setCategory("Trous");
		// pass 1 parameters
		ExtendedPropertyDescriptor percentMaxPower = addProperty("powerFractionPass1");
		percentMaxPower.setShortDescription("Pourcentage de la puissance pour la passe 1");
		percentMaxPower.setPropertyEditorClass(DoublePropertyEditor.class);
		percentMaxPower.setDisplayName("Fraction Puissance Passe 1");
		percentMaxPower.setCategory("Passe 1 Trous");

		ExtendedPropertyDescriptor percentSpeed = addProperty("speedFractionPass1");
		percentSpeed.setShortDescription("Pourcentage de la vitesse pour la passe 1");
		percentSpeed.setPropertyEditorClass(DoublePropertyEditor.class);
		percentSpeed.setDisplayName("Fraction Vitesse Passe 1");
		percentSpeed.setCategory("Passe 1 Trous");

		ExtendedPropertyDescriptor hasMultiplePass = addProperty("hasMultiplePass");
		hasMultiplePass.setShortDescription("Doit on faire le tracé avec des passes additionnelles");
		hasMultiplePass.setDisplayName("Passes additionnelles ?");
		hasMultiplePass.setCategory("Ajout de passes Additionnelles Trous");

		ExtendedPropertyDescriptor multiplePass = addProperty("multiplePass");
		multiplePass.setShortDescription("Combien de passes additionnelles");
		multiplePass.setDisplayName("Nombre de passes additionnelles ?");
		multiplePass.setCategory("Ajout de passes Additionnelles Trous");

		// pass 2 parameters
		ExtendedPropertyDescriptor percentMaxPower2 = addProperty("powerFractionMultiplePass");
		percentMaxPower2.setShortDescription("Pourcentage de la puissance pour les passes additionnelles");
		percentMaxPower2.setPropertyEditorClass(DoublePropertyEditor.class);
		percentMaxPower2.setDisplayName("Fraction Puissance passes Additionnelles");
		percentMaxPower2.setCategory("Ajout de passes Additionnelles Trous");

		ExtendedPropertyDescriptor percentSpeed2 = addProperty("speedFractionMultiplePass");
		percentSpeed2.setShortDescription("Pourcentage de la vitesse pour les passes additionnelles");
		percentSpeed2.setPropertyEditorClass(DoublePropertyEditor.class);
		percentSpeed2.setDisplayName("Fraction Vitesse passes Additionnelles");
		percentSpeed2.setCategory("Ajout de passes Additionnelles Trous");


		ExtendedPropertyDescriptor exportPliures = addProperty("exportPliures");
		exportPliures.setShortDescription("Export des traits de pliures");
		exportPliures.setDisplayName("Export Pliures");
		exportPliures.setCategory("Pliures");

		ExtendedPropertyDescriptor halfCutPowerProperty = addProperty("halfCutPower");
		halfCutPowerProperty.setShortDescription("Pourcentage de puissance pour couper à mi chaire");
		halfCutPowerProperty.setPropertyEditorClass(DoublePropertyEditor.class);
		halfCutPowerProperty.setDisplayName("Fraction Puissance Mi Decoupe");
		halfCutPowerProperty.setCategory("Pliures");

		ExtendedPropertyDescriptor taillePage = addProperty("taillePagePourPliure");
		taillePage.setShortDescription(
				"Si les pliures sont sélectionnées, Taille de la page du carton pour réaliser les pliures en mm");
		taillePage.setPropertyEditorClass(DoublePropertyEditor.class);
		taillePage.setDisplayName("Taille page carton (mm)");
		taillePage.setCategory("Pliures");

		ExtendedPropertyDescriptor typePliure = addProperty("typePliure");
		typePliure.setShortDescription("Dessin des pliures (continue, ou pointillé)");
		typePliure.setPropertyEditorClass(TypePliuresComboBoxPropertyEditor.class);
		typePliure.setDisplayName("Type de pliures");
		typePliure.setCategory("Pliures");

		ExtendedPropertyDescriptor powerFractionPliures = addProperty("powerFractionPliures");
		powerFractionPliures.setShortDescription("Fraction de puissance pour les pliures");
		powerFractionPliures.setPropertyEditorClass(DoublePropertyEditor.class);
		powerFractionPliures.setDisplayName("Fraction Puissance Passe Pliures");
		powerFractionPliures.setCategory("Pliures");

		ExtendedPropertyDescriptor speedFractionPliures = addProperty("speedFractionPliures");
		speedFractionPliures.setShortDescription("Pourcentage de la vitesse pour la passe de pliures");
		speedFractionPliures.setPropertyEditorClass(DoublePropertyEditor.class);
		speedFractionPliures.setDisplayName("Fraction Vitesse Passe Pliures");
		speedFractionPliures.setCategory("Pliures");

		ExtendedPropertyDescriptor pliureMultipass = addProperty("pliureMultipass");
		pliureMultipass.setShortDescription("Plusieures passes Laser pour les pliures");
		pliureMultipass.setDisplayName("Passes Multiples pour les pliures ?");
		pliureMultipass.setCategory("Pliures");

		ExtendedPropertyDescriptor pliuresMultipassPassNumber = addProperty("pliuresMultipassPassNumber");
		pliuresMultipassPassNumber.setShortDescription(
				"Nombre de passe multiple pour les pliures (dans le cas où le multipass est activé)");
		pliuresMultipassPassNumber.setDisplayName("Nombre de Passe Multiple Pliures");
		pliuresMultipassPassNumber.setCategory("Pliures");

		ExtendedPropertyDescriptor pliuresMultipassPowerFraction = addProperty("pliuresMultipassPowerFraction");
		pliuresMultipassPowerFraction.setShortDescription("Fraction de puissance pour la passe multiple de pliures");
		pliuresMultipassPowerFraction.setPropertyEditorClass(DoublePropertyEditor.class);
		pliuresMultipassPowerFraction.setDisplayName("Fraction Puissance Passe Multiple Pliures");
		pliuresMultipassPowerFraction.setCategory("Pliures");

		ExtendedPropertyDescriptor pliuresMultipassSpeedFraction = addProperty("pliuresMultipassSpeedFraction");
		pliuresMultipassSpeedFraction
				.setShortDescription("Pourcentage de la vitesse pour la passe multiple de pliures");
		pliuresMultipassSpeedFraction.setPropertyEditorClass(DoublePropertyEditor.class);
		pliuresMultipassSpeedFraction.setDisplayName("Fraction Vitesse Passe Multiple Pliures");
		pliuresMultipassSpeedFraction.setCategory("Pliures");


	

	}

}
