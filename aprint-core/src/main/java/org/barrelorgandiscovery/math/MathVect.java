package org.barrelorgandiscovery.math;

import java.awt.geom.Point2D;

/**
 * simple class for handling vectors
 *
 * @author pfreydiere
 */
public class MathVect {

  private double x;
  private double y;

  public MathVect(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public MathVect(Point2D.Double p1, Point2D.Double p2) {
    this((p2.x - p1.x), (p2.y - p1.y));
  }

  public MathVect orthogonal() {
    return new MathVect(-y, x);
  }

  public double norme() {
    return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
  }

  public MathVect orthoNorme() {
    double n = norme();
    return new MathVect(x / n, y / n);
  }

  public MathVect moins() {
    return new MathVect(-x, -y);
  }

  public MathVect plus(MathVect v) {
    assert v != null;
    return new MathVect(x + v.x, y + v.y);
  }

  public MathVect moins(MathVect v) {
    assert v != null;
    return plus(v.moins());
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public Point2D.Double plus(Point2D.Double p) {
    return new Point2D.Double(p.x + x, p.y + y);
  }

  public MathLine lineFrompoint(Point2D.Double p) {
    return new MathLine(p, this);
  }

  public MathVect scale(double s) {
    return new MathVect(x * s, y * s);
  }

  public double vectorielZ(MathVect v) {
    return x * v.y - v.x * y;
  }

  /**
   * calc angle from origin, in -Math.PI , Math.PI
   *
   * @return
   */
  public double angleOrigine() {
    return Math.atan2(y, x);
  }

  /**
   * calc angle from the given vector
   *
   * @param v the given vector
   * @return
   */
  public double angle(MathVect v) {
    return v.angleOrigine() - angleOrigine();
  }

  /**
   * rotate from origin
   *
   * @param angle in rad
   * @return
   */
  public MathVect rotate(double angle) {
    double n = norme();
    double angleorigine = angleOrigine();

    double newX = n * Math.cos(angleorigine + angle);
    double newY = n * Math.sin(angleorigine + angle);
    return new MathVect(newX, newY);
  }

  @Override
  public String toString() {
    return "(" + x + "," + y + ")";
  }
}
