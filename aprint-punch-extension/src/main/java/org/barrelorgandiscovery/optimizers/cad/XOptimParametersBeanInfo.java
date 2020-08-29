package org.barrelorgandiscovery.optimizers.cad;

import org.barrelorgandiscovery.extensionsng.perfo.cad.TrouTypeComboBoxPropertyEditor;
import org.barrelorgandiscovery.extensionsng.perfo.cad.TypePliuresComboBoxPropertyEditor;

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
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

		ExtendedPropertyDescriptor has2Pass = addProperty("has2pass");
		has2Pass.setShortDescription("Doit on faire le tracé en 2 passes");
		has2Pass.setDisplayName("Deux passes ?");
		has2Pass.setCategory("Passe 2");

		// pass 2 parameters
		ExtendedPropertyDescriptor percentMaxPower2 = addProperty("powerFractionPass2");
		percentMaxPower2.setShortDescription("Pourcentage de la puissance pour la passe 2");
		percentMaxPower2.setPropertyEditorClass(DoublePropertyEditor.class);
		percentMaxPower2.setDisplayName("Fraction Puissance Passe 2");
		percentMaxPower2.setCategory("Passe 2");
		
		ExtendedPropertyDescriptor percentSpeed2 = addProperty("speedFractionPass2");
		percentSpeed2.setShortDescription("Pourcentage de la vitesse pour la passe 2");
		percentSpeed2.setPropertyEditorClass(DoublePropertyEditor.class);
		percentSpeed2.setDisplayName("Fraction Vitesse Passe 2");
		percentSpeed2.setCategory("Passe 2");

		
	}

}
