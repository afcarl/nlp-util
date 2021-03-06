package edu.isi.nlp.converters;

import static com.google.common.base.Preconditions.checkNotNull;

public class StringToLong implements StringConverter<Long> {

  public StringToLong() {
  }

  public Class<Long> getValueClass() {
    return Long.class;
  }

  @Override
  public Long decode(final String s) {
    try {
      return Long.parseLong(checkNotNull(s));
    } catch (NumberFormatException nfe) {
      throw new ConversionException("Not a long: " + s, nfe);
    }
  }
}
