package org.barrelorgandiscovery.recognition.math;

import org.apache.log4j.Logger;

import Jama.Matrix;

/**
 * Gradient method
 * 
 * @author pfreydiere
 * 
 */
public class GradientDescent {

	private static Logger logger = Logger.getLogger(GradientDescent.class);

	public interface FunctionEvaluation {
		double compute(Matrix v);
	}

	private FunctionEvaluation f;

	public GradientDescent(FunctionEvaluation f) {
		assert f != null;
		this.f = f;
	}

	/**
	 * one step gradient descent
	 * 
	 * @param point
	 * @param alpha
	 * @return
	 */
	public Matrix gradientDescent(Matrix point, double alpha) {

		double epsilon = 1e-5;

		Matrix origin = point.copy();
		double result = f.compute(origin);

		for (int i = 0; i < point.getRowDimension(); i++) {
			Matrix pointCompute = point.copy();
			pointCompute.set(i, 0, pointCompute.get(i, 0) + epsilon);
			double resultEpsilon = f.compute(pointCompute);
			// partial derivative
			double derive = (resultEpsilon - result) / epsilon;
			origin.set(i, 0, origin.get(i, 0) - alpha * derive);
		}

		logger.debug("distance :" + f.compute(origin));

		return origin;

	}

}
