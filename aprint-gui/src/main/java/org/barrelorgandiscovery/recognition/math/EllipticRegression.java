package org.barrelorgandiscovery.recognition.math;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.recognition.math.GradientDescent.FunctionEvaluation;

import Jama.Matrix;

public class EllipticRegression {

	private static Logger logger = Logger.getLogger(EllipticRegression.class);

	/**
	 * Renvoie les paramètre de l'ellipse qui s'ajuste mieux au nuage de points
	 * 2D reçu (au sens des moindres carrés)
	 * 
	 * @param lstPts2D
	 *            liste de points 2D (Point2D.Double) du contour de l'ellipse
	 * @return un tableau qui contient les paramètres de l'ellipse
	 */
	public static EllipseParameters mathElipseRegression(
			List<Rectangle2D.Double> lstPts2D) {
		// construction du système linéaire
		int nNb_pts = lstPts2D.size();
		Matrix mA = new Matrix(nNb_pts, 5);
		Matrix vB = new Matrix(nNb_pts, 1);
		for (int i = 0; i < nNb_pts; ++i) {

			Double r = lstPts2D.get(i);
			double x = r.x + r.width / 2;
			double y = r.y + r.height / 2;

			mA.set(i, 0, x * x);
			mA.set(i, 1, x * y);
			mA.set(i, 2, y * y);
			mA.set(i, 3, x);
			mA.set(i, 4, y);
			vB.set(i, 0, -1.0);
		}
		// resolution du systeme lineaire A.x = b
		Matrix vX = mA.solve(vB);

		// evaluation des 5 inconnues
		double a = vX.get(0, 0);
		double b = vX.get(1, 0) / 2.0;
		double c = vX.get(2, 0);
		double d = vX.get(3, 0) / 2.0;
		double f = vX.get(4, 0) / 2.0;
		double g = 1.0;

		// calcule les caracteristiques de l'ellipse:
		//
		// d'après http://mathworld.wolfram.com/Ellipse.html
		//
		// formules (15), (16), ..., (23)

		// verifie que l'ellipse est valide
		double delta = a * (c * g - f * f) - b * (b * g - d * f) + d
				* (b * f - d * c);
		double nJ = a * c - b * b;
		double nI = a + c;
		if (delta == 0)
			return null;
		if (nJ <= 0)
			return null;
		if ((delta / nI) >= 0)
			return null;

		// calcul du centre de l'ellipse
		double nX_centre = (c * d - b * f) / (b * b - a * c);
		double nY_centre = (a * f - b * d) / (b * b - a * c);

		// angle de rotation (en radian)
		double nAngle_rad = 0.5 * Math.atan(2.0 * b / (c - a));

		// longueurs des 2 demi-axes
		double num = 2.0 * (a * f * f + c * d * d + g * b * b - 2 * b * d * f - a
				* c * g);
		double sq = Math.sqrt((a - c) * (a - c) + 4.0 * b * b);

		double denom1 = (b * b - a * c) * (sq - (a + c));
		double nDemi_axe_x = Math.sqrt(num / denom1);

		double denom2 = (b * b - a * c) * (-1.0 * sq - (a + c));
		double nDemi_axe_y = Math.sqrt(num / denom2);

		// retourne un angle entre [0 et 2\Pi[
		if (nAngle_rad < 0.0) {
			nAngle_rad += 2.0 * Math.PI;
		}

		EllipseParameters result = new EllipseParameters();

		result.centre = new Point2D.Double(nX_centre, nY_centre);
		result.angle = -nAngle_rad;
		result.a = nDemi_axe_x;
		result.b = nDemi_axe_y;

		return result;
	}// getParametres_ellipse_mc

	public static Matrix computeEllipsePoint(EllipseParameters ellipse,
			double angle) {
		Matrix rotationMatrix = new Matrix(new double[][] {
				{ Math.cos(ellipse.angle), -Math.sin(ellipse.angle) },
				{ Math.sin(ellipse.angle), Math.cos(ellipse.angle) } });

		Matrix center = new Matrix(new double[][] { { ellipse.centre.x,
				ellipse.centre.y, } });

		Matrix vpos = new Matrix(new double[][] { {
				ellipse.a * Math.cos(angle), ellipse.b * Math.sin(angle) } });

		Matrix r = rotationMatrix.times(vpos.transpose());

		Matrix res = r.plus(center.transpose());
		return res;
	}

	/**
	 * calc the distance of a point to ellipse
	 * 
	 * @param parameters
	 *            the ellipse parameters
	 * @param x
	 *            the x abscisse
	 * @param y
	 *            the y ordonnee
	 * @return
	 */
	public static double computeDistances(EllipseParameters parameters,
			double x, double y) {

		// compute angle

		MathVect v = new MathVect(x, y);
		MathVect center = new MathVect(parameters.centre.x, parameters.centre.y);

		double angleOrigine = v.moins(center).angleOrigine();

		double angleEllipse = angleOrigine - parameters.angle;

		MathVect pt = toVect(computeEllipsePoint(parameters, angleEllipse));

		return pt.moins(v).norme();

	}

	private static MathVect toVect(Matrix m) {
		return new MathVect(m.get(0, 0), m.get(1, 0));
	}

	private static Matrix toM(EllipseParameters p) {
		return new Matrix(new double[][] { { p.centre.x, p.centre.y, p.angle,
				p.a, p.b } }).transpose();
	}

	private static EllipseParameters fromM(Matrix m) {

		assert m.getRowDimension() == 5;
		EllipseParameters p = new EllipseParameters();

		p.centre = new Point2D.Double(m.get(0, 0), m.get(1, 0));
		p.angle = m.get(2, 0);
		p.a = m.get(3, 0);
		p.b = m.get(4, 0);

		return p;

	}

	public static EllipseParameters iterativeRegression(
			final List<Rectangle2D.Double> list, EllipseParameters init) {

		EllipseParameters p = (init != null ? init : mathElipseRegression(list));

		Matrix c = toM(p);

		FunctionEvaluation f = new FunctionEvaluation() {

			public double compute(Matrix v) {

				double distance = 0;
				for (Iterator<Rectangle2D.Double> it = list.iterator(); it
						.hasNext();) {
					Double n = it.next();

					EllipseParameters p = fromM(v);
					distance += computeDistances(p, n.x + n.width / 2, n.y
							+ n.height / 2);

				}

				return distance;
			}
		};
		GradientDescent g = new GradientDescent(f);

		double d = f.compute(c);
		double d1 = d;

		int cpt = 0;
		int attempt = 3;
		while (attempt > 0) {
			double factor = 10 * attempt;
			logger.debug("attempt :" + attempt);
			while (true) { // (d1 <= d) { // while descending
				c = g.gradientDescent(c, 1e-4 * factor);
				d = d1;
				d1 = f.compute(c);

				double relativeDescent = Math.abs(d - d1);
				logger.debug(attempt + " - relative descent :" + relativeDescent);
				if (relativeDescent < 1e-4 / factor){
					break;
				}
				
				cpt++;
				if (cpt > 10000)
				{
					cpt = 0;
					break;
				}
			}
			attempt --;
		}
		logger.debug("nb iteration :" + cpt);

		return fromM(c);

	}

}
