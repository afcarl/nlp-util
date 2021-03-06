package edu.isi.nlp.gnuplot;

import com.google.common.annotations.Beta;

@Beta
public final class Point2D {

  private Point2D(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  public static Point2D fromXY(final double x, final double y) {
    return new Point2D(x,y);
  }

  public double x() {
    return x;
  }

  public double y() {
    return y;
  }

  private final double x;
  private final double y;
}
