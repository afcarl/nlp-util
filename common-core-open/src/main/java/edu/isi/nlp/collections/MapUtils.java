package edu.isi.nlp.collections;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import edu.isi.nlp.StringUtils;
import edu.isi.nlp.collections.IterableUtils.ZipPair;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utilities for working with {@link Map}s
 *
 * @author Ryan Gabbard, Jay DeYoung
 */
public final class MapUtils {

  private MapUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Pairs up the values of a map by their common keys.
   */
  public static <K, V> PairedMapValues<V> zipValues(final Map<K, V> left, final Map<K, V> right) {
    checkNotNull(left);
    checkNotNull(right);
    final ImmutableList.Builder<ZipPair<V, V>> pairedValues =
        ImmutableList.builder();
    final ImmutableList.Builder<V> leftOnly = ImmutableList.builder();
    final ImmutableList.Builder<V> rightOnly = ImmutableList.builder();

    for (final Map.Entry<K, V> leftEntry : left.entrySet()) {
      final K key = leftEntry.getKey();
      if (right.containsKey(key)) {
        pairedValues.add(ZipPair.from(leftEntry.getValue(), right.get(key)));
      } else {
        leftOnly.add(leftEntry.getValue());
      }
    }

    for (final Map.Entry<K, V> rightEntry : right.entrySet()) {
      if (!left.containsKey(rightEntry.getKey())) {
        rightOnly.add(rightEntry.getValue());
      }
    }

    return new PairedMapValues<V>(pairedValues.build(), leftOnly.build(),
        rightOnly.build());
  }

  /**
   * Return a copy of the input map with keys transformed by {@code keyInjection} and values
   * transformed by {@code valueFunction}. Beware: {@code keyInjection} must be an injection over
   * all the keys of the input map. If two original keys are mapped to the same value, an {@link
   * java.lang.IllegalArgumentException} will be thrown.
   *
   * Neither {@code keyInjection} nor {@code valueFunction} may return null. If one does, an
   * exception will be thrown.
   */
  public static <K1, V1, K2, V2> ImmutableMap<K2, V2> copyWithTransformedEntries(Map<K1, V1> input,
      Function<? super K1, K2> keyInjection, Function<? super V1, V2> valueFunction) {
    final ImmutableMap.Builder<K2, V2> ret = ImmutableMap.builder();

    for (final Map.Entry<K1, V1> entry : input.entrySet()) {
      ret.put(keyInjection.apply(entry.getKey()), valueFunction.apply(entry.getValue()));
    }
    return ret.build();
  }


  public static class PairedMapValues<V> {

    public PairedMapValues(final List<ZipPair<V, V>> pairedValues, final List<V> leftOnly,
        final List<V> rightOnly) {
      this.pairedValues = ImmutableList.copyOf(pairedValues);
      this.leftOnly = ImmutableList.copyOf(leftOnly);
      this.rightOnly = ImmutableList.copyOf(rightOnly);
    }

    public List<ZipPair<V, V>> pairedValues() {
      return pairedValues;
    }

    public List<V> leftOnly() {
      return leftOnly;
    }

    public List<V> rightOnly() {
      return rightOnly;
    }

    public boolean perfectlyAligned() {
      return leftOnly.isEmpty() && rightOnly.isEmpty();
    }

    private final List<ZipPair<V, V>> pairedValues;
    private final List<V> leftOnly;
    private final List<V> rightOnly;
  }

  /**
   * Builds a map from sequence items to their zero-indexed positions in the sequence.
   */
  public static <T> ImmutableMap<T, Integer> indexMap(Iterable<? extends T> items) {
    final ImmutableMap.Builder<T, Integer> ret = ImmutableMap.builder();
    int idx = 0;
    for (final T item : items) {
      ret.put(item, idx++);
    }
    return ret.build();
  }

  /**
   * Gets the union of the {@code keySet()}s of all provided {@link Maps}s.
   */
  public static <K, V> ImmutableSet<K> allKeys(final Iterable<? extends Map<K, V>> maps) {
    final ImmutableSet.Builder<K> builder = ImmutableSet.builder();

    for (final Map<K, V> map : maps) {
      builder.addAll(map.keySet());
    }

    return builder.build();
  }

  /**
   * Gets the union of the {@code keySet()}s of all provided {@link Multimap}s.
   */
  public static <K> ImmutableSet<K> allMultimapKeys(final Iterable<? extends Multimap<K, ?>> multimaps) {
    final ImmutableSet.Builder<K> builder = ImmutableSet.builder();

    for (final Multimap<K, ?> multimap : multimaps) {
      builder.addAll(multimap.keySet());
    }

    return builder.build();
  }


  public static <K, V> Function<Map.Entry<K, V>, V> entryValueFunction() {
    return new Function<Map.Entry<K, V>, V>() {
      @Override
      public V apply(final Map.Entry<K, V> entry) {
        return entry.getValue();
      }
    };
  }


  public static <K, V> Function<Map.Entry<K, V>, K> entryKeyFunction() {
    return new Function<Map.Entry<K, V>, K>() {
      @Override
      public K apply(final Map.Entry<K, V> entry) {
        return entry.getKey();
      }
    };
  }


  public static <K, V> Function<Map<K, V>, Iterable<V>> mapValuesFunction() {
    return new Function<Map<K, V>, Iterable<V>>() {
      @Override
      public Iterable<V> apply(final Map<K, V> input) {
        return input.values();
      }
    };
  }

  /**
   * A partial ordering over {@link Map.Entry} according to their values, in ascending natural
   * order.
   */
  public static <K, V extends Comparable<V>> Ordering<Map.Entry<K, V>> byValueOrderingAscending() {
    return Ordering.<V>natural().onResultOf(Entry::getValue);
  }

  /**
   * A partial ordering over {@link Map.Entry} according to their values, in descending natural order.
   */
  public static <K, V extends Comparable<V>> Ordering<Map.Entry<K, V>> byValueOrderingDescending() {
    return MapUtils.<K, V>byValueOrderingAscending().reverse();
  }

  /**
   * A partial ordering over {@link Map.Entry} according to {@code valueOrdering} applied to their values.
   */
  public static <K, V> Ordering<Map.Entry<K, V>> byValueOrdering(final Ordering<V> valueOrdering) {
    return valueOrdering.onResultOf(Entry::getValue);
  }


  /**
   * A partial ordering of {@link Map.Entry} by the reverse of the natural ordering of the keys.
   */
  public static <K extends Comparable<K>, V> Ordering<Entry<K, V>> byKeyDescendingOrdering() {
    return Ordering.<K>natural().onResultOf(MapUtils.<K, V>entryKeyFunction());
  }


  /**
   * A partial ordering of {@link Map.Entry} by the natural ordering of the keys.
   */
  public static <K, V> Ordering<Entry<K, V>> byKeyOrdering(Ordering<K> keyOrdering) {
    return keyOrdering.onResultOf(Entry::getKey);
  }


  /**
   * Creates a copy of the supplied map with its keys transformed by the supplied function, which
   * must be one-to-one on the keys of the original map. If two keys are mapped to the same value by
   * {@code injection}, an {@code IllegalArgumentException} is thrown.
   */
  public static <K1, K2, V> ImmutableMap<K2, V> copyWithKeysTransformedByInjection(
      final Map<K1, V> map, final Function<? super K1, K2> injection) {
    final ImmutableMap.Builder<K2, V> ret = ImmutableMap.builder();
    for (final Map.Entry<K1, V> entry : map.entrySet()) {
      ret.put(injection.apply(entry.getKey()), entry.getValue());
    }
    return ret.build();
  }

  /**
   * Returns the length of the longest key in a map, or 0 if the map is empty. Useful for printing
   * tables, etc. The map may not have any null keys.
   */
  public static <V> int longestKeyLength(Map<String, V> map) {
    if (map.isEmpty()) {
      return 0;
    }

    return Ordering.natural().max(
        FluentIterable.from(map.keySet())
            .transform(StringUtils.lengthFunction()));
  }

  /**
   * Just like {@link com.google.common.base.Functions#forMap(Map, Object)} except it allows a
   * potentially non-constant {@link Function} as the default.
   */
  public static <K, V> Function<K, V> asFunction(Map<K, ? extends V> map,
      Function<K, ? extends V> defaultFunction) {
    return new ForMapWithDefaultFunction<K, V>(map, defaultFunction);
  }

  // indebted to Guava's Functions.forMap()
  private static class ForMapWithDefaultFunction<K, V> implements Function<K, V> {

    private final Map<K, ? extends V> map;
    private final Function<K, ? extends V> defaultFunction;

    private ForMapWithDefaultFunction(
        final Map<K, ? extends V> map, final Function<K, ? extends V> defaultFunction) {
      this.map = checkNotNull(map);
      this.defaultFunction = checkNotNull(defaultFunction);
    }

    @Override
    public V apply(final K input) {
      final V result = map.get(input);
      return (result != null || map.containsKey(input)) ? result : defaultFunction.apply(input);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(map, defaultFunction);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final ForMapWithDefaultFunction other = (ForMapWithDefaultFunction) obj;
      return Objects.equal(this.map, other.map)
          && Objects.equal(this.defaultFunction, other.defaultFunction);
    }

    @Override
    public String toString() {
      return "asFunction(" + map + ", default=" + defaultFunction + ")";
    }
  }

  /**
   * Creates a {@link ImmutableMap} by pairing up a sequence of keys and values. The {@code n}-th
   * key is paired to the {@code n}-th value.  Neither null keys nor null values are permitted.  The
   * keys must be unique or an {@link IllegalArgumentException} will be thrown.  If the numberof
   * elements returned by the two {@link Iterable}s differ, an {@link IllegalArgumentException} will
   * be thrown.
   */
  public static <K, V> ImmutableMap<K, V> copyParallelListsToMap(Iterable<K> keys,
      Iterable<V> values) {
    final ImmutableMap.Builder<K, V> ret = ImmutableMap.builder();

    final Iterator<K> keyIt = keys.iterator();
    final Iterator<V> valueIt = values.iterator();

    while (keyIt.hasNext() && valueIt.hasNext()) {
      ret.put(keyIt.next(), valueIt.next());
    }

    if (!keyIt.hasNext() && !valueIt.hasNext()) {
      return ret.build();
    } else {
      if (keyIt.hasNext()) {
        throw new IllegalArgumentException(
            "When pairing keys and values, there were more keys than values");
      } else {
        throw new IllegalArgumentException(
            "When pairing keys and values, there were more values than keys");
      }
    }
  }

  /**
   * Creates a {@code LaxImmutableMapBuilder} which will behave exactly like a
   * {@link com.google.common.collect.ImmutableMap.Builder} except it will allow adding the
   * same key-value pair more than once. "Same" is determined by the value's equality method.
   * The behavior of this builder and the resulting {@link ImmutableMap} is deterministic.
   */
  public static <K,V> LaxImmutableMapBuilder<K,V> immutableMapBuilderAllowingSameEntryTwice() {
    return new MonotonicLaxImmutableMapBuilder<>(false);
  }

  /**
   * Creates a {@code LaxImmutableMapBuilder} which will behave exactly like a
   * {@link com.google.common.collect.ImmutableMap.Builder} except it will silently ignore
   * any attempt to add a new entry with a previously seen key.
   * The behavior of this builder and the resulting {@link ImmutableMap} is deterministic.
   */
  public static <K,V> LaxImmutableMapBuilder<K,V> immutableMapBuilderIgnoringDuplicates() {
    return new MonotonicLaxImmutableMapBuilder<>(true);
  }

  /**
   * Creates a {@code LaxImmutableMapBuilder} which will behave exactly like a
   * {@link com.google.common.collect.ImmutableMap.Builder} except that when attempting to add
   * a value for a previously seen key, the greater of the older value and the new value according
   * to the provided comparator will be used.  In case of a tie, the incumbent stays.
   *
   * The behavior of this builder and the resulting {@link ImmutableMap} will be deterministic if
   * the supplied comparator is.
   */
  public static <K,V> LaxImmutableMapBuilder<K,V> immutableMapBuilderResolvingDuplicatesBy(Comparator<? super V> conflictComparator) {
    return new NonMonotonicLaxImmutableMapBuilder<>(Ordering.from(conflictComparator));
  }

  /**
   * Returns a Guava {@link Function} to transform map entries to strings by taking the {@link
   * Object#toString()} of the key and the value and joining them with the supplied {@code
   * separator}. None of the key, value, or separator may be null.
   */
  public static <K, V> Function<Map.Entry<K, V>, String> toStringWithKeyValueSeparator(
      final String separator) {
    return new EntryJoinerFunction<>(separator);
  }

  private static class EntryJoinerFunction<K, V> implements Function<Entry<K, V>, String> {

    private final String separator;

    public EntryJoinerFunction(final String separator) {
      this.separator = checkNotNull(separator);
    }

    @Override
    public String apply(final Entry<K, V> input) {
      return input.getKey() + separator + input.getValue();
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(separator);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final EntryJoinerFunction other = (EntryJoinerFunction) obj;
      return Objects.equal(this.separator, other.separator);
    }
  }

  /**
   * Just like {@link Maps#transformEntries(Map, Maps.EntryTransformer)} but on an {@link Iterable}
   * of {@link Map.Entry}.
   */
  public static <K, V1, V2> Iterable<Map.Entry<K, V2>> transformValues(
      Iterable<Map.Entry<K, V1>> input,
      final Function<? super V1, V2> valueTransformer) {
    return Iterables.transform(input, new Function<Entry<K, V1>, Entry<K, V2>>() {
      @Override
      public Entry<K, V2> apply(final Entry<K, V1> input) {
        return Maps.immutableEntry(input.getKey(), valueTransformer.apply(input.getValue()));
      }
    });
  }

  /**
   * Just like {@link Maps#transformEntries(Map, Maps.EntryTransformer)} but on an {@link Iterable}
   * of {@link Map.Entry}.
   */
  public static <K, V1, V2> Iterable<Map.Entry<K, V2>> transformEntries(
      Iterable<Map.Entry<K, V1>> input,
      final Maps.EntryTransformer<? super K, ? super V1, V2> entryTransformer) {
    return Iterables.transform(input, new Function<Entry<K, V1>, Entry<K, V2>>() {
      @Override
      public Entry<K, V2> apply(final Entry<K, V1> input) {
        return Maps.immutableEntry(input.getKey(),
            entryTransformer.transformEntry(input.getKey(), input.getValue()));
      }
    });
  }

  /**
   * Creates a {@link com.google.common.collect.Maps.EntryTransformer} which applies the supplied
   * {@link Function} to map entry values.
   */
  public static <K, V1, V2> Maps.EntryTransformer<K, V1, V2> valueTransformer(
      final Function<? super V1, V2> valueFunction) {
    checkNotNull(valueFunction);
    return new Maps.EntryTransformer<K, V1, V2>() {
      @Override
      public V2 transformEntry(final K key, final V1 value) {
        return valueFunction.apply(value);
      }
    };
  }

  /**
   * Returns a wrapper around an {@link com.google.common.collect.ImmutableMultimap.Builder} which
   * hides the differences between it and {@link com.google.common.collect.ImmutableMap.Builder} for
   * the purpose of population.
   */
  public static <K, V> KeyValueSink<K, V> asMapSink(ImmutableMultimap.Builder<K, V> multimapB) {
    return new ImmutableMultimapBuilderSink<>(multimapB);
  }

  /**
   * Returns a wrapper around an {@link com.google.common.collect.ImmutableMap.Builder} which
   * hides the differences between it and {@link com.google.common.collect.ImmutableMultimap.Builder} for
   * the purpose of population. Beware {@link com.google.common.collect.ImmutableMap.Builder} will
   * still throw an exception for duplicate entries!
   */
  public static <K, V> KeyValueSink<K, V> asMapSink(ImmutableMap.Builder<K, V> mapB) {
    return new ImmutableMapBuilderSink<>(mapB);
  }
}

abstract class AbstractKeyValueSink<K, V> implements KeyValueSink<K, V> {

  @Override
  public final KeyValueSink<K, V> put(final Entry<K, V> entry) {
    put(entry.getKey(), entry.getValue());
    return this;
  }

  @Override
  public final KeyValueSink<K, V> putAll(
      final Iterable<? extends Entry<? extends K, ? extends V>> entries) {
    for (final Entry<? extends K, ? extends V> entry : entries) {
      put(entry.getKey(), entry.getValue());
    }
    return this;
  }
}

final class ImmutableMapBuilderSink<K, V> extends AbstractKeyValueSink<K, V> {
  final ImmutableMap.Builder<K,V> builder;

  ImmutableMapBuilderSink(final ImmutableMap.Builder<K, V> builder) {
    this.builder = checkNotNull(builder);
  }

  @Override
  public KeyValueSink<K, V> put(final K key, final V value) {
    builder.put(key, value);
    return this;
  }


}

final class ImmutableMultimapBuilderSink<K, V> extends AbstractKeyValueSink<K, V> {
  final ImmutableMultimap.Builder<K,V> builder;

  ImmutableMultimapBuilderSink(final ImmutableMultimap.Builder<K, V> builder) {
    this.builder = checkNotNull(builder);
  }

  @Override
  public KeyValueSink<K, V> put(final K key, final V value) {
    builder.put(key, value);
    return this;
  }
}
