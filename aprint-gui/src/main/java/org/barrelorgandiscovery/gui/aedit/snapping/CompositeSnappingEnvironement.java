package org.barrelorgandiscovery.gui.aedit.snapping;

import java.awt.Graphics2D;
import java.awt.geom.Point2D.Double;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookComponent;

public class CompositeSnappingEnvironement implements ISnappingEnvironment {

	private static Logger logger = Logger
			.getLogger(CompositeSnappingEnvironement.class);

	private ISnappingEnvironment[] envs;

	public CompositeSnappingEnvironement(ISnappingEnvironment[] envs) {
		this.envs = envs;
	}

	public String getName() {
		return "Composite";
	}

	public boolean snapPosition(Double position) {

		for (int i = 0; i < envs.length; i++) {
			ISnappingEnvironment env = envs[i];
			if (env.snapPosition(position)) {
				// it has snapped, by priority
				// we don't evaluate the others
				return true;
			}
		}

		return false;
	}

	public void drawFeedBack(Graphics2D g) {
		// call each Snapping Environment
		for (int i = 0; i < envs.length; i++) {
			ISnappingEnvironment env = envs[i];
			try {
				env.drawFeedBack(g);
			} catch (Exception ex) {
				logger.error("error in drawing feedback for env :" + env, ex);
			}
		}

	}

}
