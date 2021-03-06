package edu.isi.nlp.files;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.io.CharSource;
import com.google.common.io.LineProcessor;
import edu.isi.nlp.IsiNlpImmutable;
import edu.isi.nlp.collections.ImmutableListMultitable;
import edu.isi.nlp.collections.ImmutableMultitable;
import edu.isi.nlp.collections.ImmutableSetMultitable;
import edu.isi.nlp.collections.ListMultitable;
import edu.isi.nlp.collections.Multitable;
import edu.isi.nlp.collections.SetMultitable;
import edu.isi.nlp.symbols.Symbol;
import edu.isi.nlp.symbols.SymbolUtils;
import java.io.IOException;
import java.util.List;

/**
 * Loads a {@link Multitable} from a {@link CharSource}.
 *
 * The input is required to fall into three fields when split by {@link #fieldSplitter()}
 * (defaults to tab-separated).  The first of these is expected to be the row key, the second
 * the column key, and the third the value.  Multiple values may appear in one value field
 * if the optional {@link #valueListSplitter()} is specified to split them with.
 *
 * Comment lines are currently not skipped, but this could be added as an option in the future.
 *
 * @author Ryan Gabbard
 */
@org.immutables.value.Value.Immutable
@IsiNlpImmutable
public abstract class MultitableLoader<R, C, V> {
  public abstract Optional<Splitter> valueListSplitter();
  @org.immutables.value.Value.Default
  public Splitter fieldSplitter() {
    return Splitter.on("\t");
  }
  public abstract Function<String, ? extends R> rowInterpreter();
  public abstract Function<String, ? extends C> columnInterpreter();
  public abstract Function<String, ? extends V> valueInterpreter();

  /**
   * Loads a {@link ListMultitable} from the specified source
   * according to the configuration of this loader.
   */
  public final ImmutableListMultitable<R, C, V> loadToListMultitable(CharSource source)
      throws IOException {
    final ImmutableListMultitable.Builder<R, C, V> ret =
        ImmutableListMultitable.builder();

    loadToMultitable(source, ret);

    return ret.build();
  }

  /**
   * Loads a {@link SetMultitable} from the specified source
   * according to the configuration of this loader.
   */
  public final ImmutableSetMultitable<R, C, V> loadToSetMultitable(CharSource source)
      throws IOException {
    final ImmutableSetMultitable.Builder<R, C, V> ret =
        ImmutableSetMultitable.builder();

    loadToMultitable(source, ret);

    return ret.build();
  }

  public static <R,C,V> MultitableLoader.Builder<R,C,V> builder() {
    return new MultitableLoader.Builder<>();
  }

  public static MultitableLoader.Builder<String, String, String> builderForStrings() {
    return new MultitableLoader.Builder<String,String,String>()
        .rowInterpreter(Functions.<String>identity())
        .columnInterpreter(Functions.<String>identity())
        .valueInterpreter(Functions.<String>identity());
  }

  public static MultitableLoader.Builder<Symbol, Symbol, Symbol> builderForSymbols() {
    return new MultitableLoader.Builder<Symbol, Symbol, Symbol>()
        .rowInterpreter(SymbolUtils.symbolizeFunction())
        .columnInterpreter(SymbolUtils.symbolizeFunction())
        .valueInterpreter(SymbolUtils.symbolizeFunction());
  }

  private void loadToMultitable(final CharSource source,
      final ImmutableMultitable.Builder<R, C, V> ret) throws IOException {
    source.readLines(new LineProcessor<Void>() {
      @Override
      public boolean processLine(final String line) throws IOException {
        final List<String> fields = fieldSplitter().splitToList(line);
        if (fields.size() == 3) {
          final R rowKey = interpret(fields.get(0), rowInterpreter(), "row key", line);
          final C columnKey = interpret(fields.get(1), columnInterpreter(), "column key", line);

          if (valueListSplitter().isPresent()) {
            for (final String value : valueListSplitter().get().split(fields.get(2))) {
              ret.put(rowKey, columnKey, interpret(value, valueInterpreter(), "value", line));
            }
          } else {
            ret.put(rowKey, columnKey, interpret(fields.get(2), valueInterpreter(), "value", line));
          }
        } else {
          throw new IOException("Cannot parse lines as multitable entries:\n" + line);
        }
        // we never stop procesisng lines early
        return true;
      }

      @Override
      public Void getResult() {
        return null;
      }
    });
  }

  private <T> T interpret(String field, Function<String, T> interpreter, String fieldName, String line)
      throws IOException {
    try {
      return interpreter.apply(field);
    } catch (Exception e) {
      throw new IOException("While parsing multitable line\n" + line + "\n failed to interpret " +
          fieldName + " " + field + " using " + interpreter);
    }
  }

  public static class Builder<R, C, V> extends ImmutableMultitableLoader.Builder<R, C, V> {

    public Builder<R, C, V> splitValuesOnCommas() {
      valueListSplitter(Splitter.on(","));
      return this;
    }
  }
}
