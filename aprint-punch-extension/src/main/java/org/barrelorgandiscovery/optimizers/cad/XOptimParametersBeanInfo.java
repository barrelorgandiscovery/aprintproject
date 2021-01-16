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

		ExtendedPropertyDescriptor exportTrous = addProperty("exportTrous");
		exportTrous.setShortDescription("Export des trous");
		exportTrous.setDisplayName("Export des trous");
		exportTrous.setCategory("Trous");

		ExtendedPropertyDescriptor typeTrous = addProperty("typeTrous");
		typeTrous.setShortDescription("Type de trous");
		typeTrous.setDisplayName("Types de trous");
		typeTrous.setPropertyEditorClass(TrouTypeComboBoxPropertyEditor.class);
		typeTrous.setCategory("Trous");

		ExtendedPropertyDescriptor taillTrous = addProperty("tailleTrous");
		taillTrous.setShortDescription(
				"Taille Maximum des trous créés, si le trou est plus grand, un pont est créé.\nSi vous ne souhaitez pas de ponts, mettez un grand nombre ici");
		taillTrous.setDisplayName("Taille Maximum des trous (mm)");
		taillTrous.setPropertyEditorClass(DoublePropertyEditor.class);
		taillTrous.setCategory("Trous");

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

		ExtendedPropertyDescriptor exportPliures = addProperty("exportPliures");
		exportPliures.setShortDescription("Export des traits de pliures");
		exportPliures.setDisplayName("Export Pliures");
		exportPliures.setCategory("Pliures");

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

		// pass 1 parameters
		ExtendedPropertyDescriptor percentMaxPower = addProperty("powerFractionPass1");
		percentMaxPower.setShortDescription("Pourcentage de la puissance pour la passe 1");
		percentMaxPower.setPropertyEditorClass(DoublePropertyEditor.class);
		percentMaxPower.setDisplayName("Fraction Puissance Passe 1");
		percentMaxPower.setCategory("Passe 1");

		ExtendedPropertyDescriptor percentSpeed = addProperty("speedFractionPass1");
		percentSpeed.setShortDescription("Pourcentage de la vitesse pour la passe 1");
		percentSpeed.setPropertyEditorClass(DoublePropertyEditor.class);
		percentSpeed.setDisplayName("Fraction Vitesse Passe 1");
		percentSpeed.setCategory("Passe 1");

		ExtendedPropertyDescriptor hasMultiplePass = addProperty("hasMultiplePass");
		hasMultiplePass.setShortDescription("Doit on faire le tracé avec des passes additionnelles");
		hasMultiplePass.setDisplayName("Passes additionnelles ?");
		hasMultiplePass.setCategory("Passe Additionnelles");

		ExtendedPropertyDescriptor multiplePass = addProperty("multiplePass");
		multiplePass.setShortDescription("Combien de passes additionnelles");
		multiplePass.setDisplayName("Nombre de passes additionnelles ?");
		multiplePass.setCategory("Passes Additionnelles");

		
		// pass 2 parameters
		ExtendedPropertyDescriptor percentMaxPower2 = addProperty("powerFractionMultiplePass");
		percentMaxPower2.setShortDescription("Pourcentage de la puissance pour les passes additionnelles");
		percentMaxPower2.setPropertyEditorClass(DoublePropertyEditor.class);
		percentMaxPower2.setDisplayName("Fraction Puissance passes Additionnelles");
		percentMaxPower2.setCategory("Passes Additionnelles");

		ExtendedPropertyDescriptor percentSpeed2 = addProperty("speedFractionMultiplePass");
		percentSpeed2.setShortDescription("Pourcentage de la vitesse pour les passes additionnelles");
		percentSpeed2.setPropertyEditorClass(DoublePropertyEditor.class);
		percentSpeed2.setDisplayName("Fraction Vitesse passes Additionnelles");
		percentSpeed2.setCategory("Passes Additionnelles");

		
		// pass 1 parameters
		ExtendedPropertyDescriptor halfCutPowerProperty = addProperty("halfCutPower");
		halfCutPowerProperty.setShortDescription("Pourcentage de puissance pour couper à mi chaire");
		halfCutPowerProperty.setPropertyEditorClass(DoublePropertyEditor.class);
		halfCutPowerProperty.setDisplayName("Fraction Puissance Mi Decoupe");
		halfCutPowerProperty.setCategory("Puissance Lazer");

		ExtendedPropertyDescriptor optimPageSize = addProperty("optimPageSize");
		optimPageSize.setShortDescription("Taille de la page pour optimisation du déplacement de la tête");
		optimPageSize.setPropertyEditorClass(DoublePropertyEditor.class);
		optimPageSize.setDisplayName("Taille de la page d'optimisation (cm)");
		optimPageSize.setCategory("Optimisation");

	}

}
