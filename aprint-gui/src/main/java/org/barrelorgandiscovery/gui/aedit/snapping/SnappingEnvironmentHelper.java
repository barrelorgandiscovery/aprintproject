package org.barrelorgandiscovery.gui.aedit.snapping;

import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.virtualbook.MarkerEvent;
import org.barrelorgandiscovery.virtualbook.SignatureEvent;

public class SnappingEnvironmentHelper {

	public static HolesSnappingEnvironnement createHoleSnappingEnv(
			JVirtualBookScrollableComponent c) {
		return new HolesSnappingEnvironnement(c);
	}

	public static ISnappingEnvironment createMesureAndMarkerSnappingEnv(
			JVirtualBookScrollableComponent c) {

		EventSnappingEnvironment sig = new EventSnappingEnvironment(c,
				SignatureEvent.class);
		EventSnappingEnvironment markers = new EventSnappingEnvironment(c,
				MarkerEvent.class);

		return new CompositeSnappingEnvironement(new ISnappingEnvironment[] {
				sig, markers });

	}

	public static ISnappingEnvironment createMarkerSnappingEnv(
			JVirtualBookScrollableComponent c) {

		EventSnappingEnvironment markers = new EventSnappingEnvironment(c,
				MarkerEvent.class);

		return new CompositeSnappingEnvironement(
				new ISnappingEnvironment[] { markers });

	}

	public static ISnappingEnvironment createMesureAndHolesSnappingEnv(
			JVirtualBookScrollableComponent c) {

		EventSnappingEnvironment sig = new EventSnappingEnvironment(c,
				SignatureEvent.class);
		HolesSnappingEnvironnement holesSnappingEnvironnement = new HolesSnappingEnvironnement(
				c);

		return new CompositeSnappingEnvironement(new ISnappingEnvironment[] {
				sig, holesSnappingEnvironnement });

	}
}
