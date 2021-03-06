package edu.isi.nlp.strings.offsets;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.primitives.Ints;

public abstract class AbstractOffset<SelfType extends Offset<SelfType>>
    implements Offset<SelfType> {

  private final int value;

  protected AbstractOffset(final int value) {
    checkArgument(value >= 0);
    this.value = value;
  }

  @JsonProperty("value")
  @Override
  public final int asInt() {
    return value;
  }

  @Override
  public boolean precedes(SelfType other) {
    return asInt() < other.asInt();
  }

  @Override
  public boolean precedesOrEquals(SelfType other) {
    return asInt() <= other.asInt();
  }

  @Override
  public boolean follows(SelfType other) {
    return asInt() > other.asInt();
  }

  @Override
  public boolean followsOrEquals(SelfType other) {
    return asInt() >= other.asInt();
  }

  @Override
  public final boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (getClass() != o.getClass()) {
      return false;
    }

    return value == ((Offset) o).asInt();
  }

  @Override
  public final int hashCode() {
    return Objects.hashCode(value);
  }


  @Override
  public final int compareTo(final SelfType o) {
    checkNotNull(o);
    return Ints.compare(asInt(), o.asInt());
  }
}

