package edu.isi.nlp.gnuplot;

// package-private
enum AxisType {
  X {
    @Override
    String letter() {
      return "x";
    }
  }, Y {
    @Override
    String letter() {
      return "y";
    }
  };

  abstract String letter();
}
