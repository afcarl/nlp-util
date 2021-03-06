package edu.isi.nlp.collections;

import com.google.common.collect.ImmutableList;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A collection whose iteration order is randomized on every attempt to iterate over it.
 *
 * @author Ryan Gabbard
 */
public final class ShufflingCollection<T> extends AbstractCollection<T> implements Collection<T> {

  private final ShufflingIterable<T> data;
  private final int size;

  private ShufflingCollection(final ShufflingIterable<T> data, final int size) {
    this.data = checkNotNull(data);
    this.size = size;
    checkArgument(size >=0);
  }

  public static <T> ShufflingCollection<T> shufflingCopy(final Iterable<? extends T> data,
      final Random rng) {
    final ImmutableList<T> dataAsList = ImmutableList.copyOf(data);
    return new ShufflingCollection<T>(ShufflingIterable.from(dataAsList, rng),
        dataAsList.size());
  }

  @Override
  public Iterator<T> iterator() {
    return data.iterator();
  }

  @Override
  public int size() {
    return size;
  }
}
