package edu.isi.nlp.corenlp;

import edu.isi.nlp.strings.offsets.CharOffset;
import edu.isi.nlp.strings.offsets.OffsetRange;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

@Beta
public final class CoreNLPDocument {

  private final ImmutableList<CoreNLPSentence> sentences;

  private CoreNLPDocument(final ImmutableList<CoreNLPSentence> sentences) {
    this.sentences = checkNotNull(sentences);
  }

  public ImmutableList<CoreNLPSentence> sentences() {
    return sentences;
  }

  /**
   * Returns the first {@code CoreNLPSentence} that contains these offsets, if any.
   * @param offsets
   * @return
   */
  public Optional<CoreNLPSentence> firstSentenceContaining(final OffsetRange<CharOffset> offsets) {
    for(final CoreNLPSentence sentence: sentences) {
      if(sentence.offsets().contains(offsets)) {
        return Optional.of(sentence);
      }
    }
    return Optional.absent();
  }

  public static CoreNLPDocumentBuilder builder() {
    return new CoreNLPDocumentBuilder();
  }

  public static class CoreNLPDocumentBuilder {

    private ImmutableList<CoreNLPSentence> sentences;

    private CoreNLPDocumentBuilder() {
    }

    public CoreNLPDocumentBuilder withSentences(Iterable<CoreNLPSentence> sentences) {
      this.sentences = ImmutableList.copyOf(sentences);
      return this;
    }

    public CoreNLPDocument build() {
      CoreNLPDocument document = new CoreNLPDocument(sentences);
      return document;
    }
  }
}
