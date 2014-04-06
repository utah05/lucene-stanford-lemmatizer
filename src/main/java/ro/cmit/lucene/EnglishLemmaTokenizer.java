/*
 * Lemmatizing library for Lucene
 * Copyright (c) 2010-2011 Lars Buitinck
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ro.cmit.lucene;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import com.google.common.collect.Iterables;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * A tokenizer that retrieves the lemmas (base forms) of English words.
 * Relies internally on the sentence splitter and tokenizer supplied with
 * the Stanford POS tagger.
 *
 */
public class EnglishLemmaTokenizer extends Tokenizer {
    private Iterator<TaggedWord> tagged;
    private PositionIncrementAttribute posIncr;
    private TaggedWord currentWord;
    private CharTermAttribute termAtt;
    private boolean lemmaNext;

    /**
     * Construct a tokenizer processing the given input and a tagger
     * using the given model file.
     */
    public EnglishLemmaTokenizer(Reader input, String posModelFile)
            throws Exception {
        this(input, EnglishLemmaAnalyzer.makeTagger(posModelFile));
    }

    /**
     * Construct a tokenizer processing the given input using the given tagger.
     */
    public EnglishLemmaTokenizer(Reader input, MaxentTagger tagger) {
        super(input);

        lemmaNext = false;
        posIncr = addAttribute(PositionIncrementAttribute.class);
        termAtt = addAttribute(CharTermAttribute.class);

        List<List<HasWord>> tokenized =
            MaxentTagger.tokenizeText(input);
        tagged = Iterables.concat(tagger.process(tokenized)).iterator();
    }

    /**
     * Consumers use this method to advance the stream to the next token.
     * The token stream emits inflected forms and lemmas interleaved (form1,
     * lemma1, form2, lemma2, etc.), giving lemmas and their inflected forms
     * the same PositionAttribute.
     */
    @Override
    public final boolean incrementToken() throws IOException {
        if (lemmaNext) {
            // Emit a lemma
            posIncr.setPositionIncrement(1);
            String tag  = currentWord.tag();
            String form = currentWord.word();
            termAtt.setEmpty().append(Morphology.stemStatic(form, tag).word());
        } else {
            // Emit inflected form, if not filtered out.

            // 0 because the lemma will come in the same position
            int increment = 0;
            for (;;) {
                if (!tagged.hasNext())
                    return false;
                currentWord = tagged.next();
                if (!unwantedPOS(currentWord.tag()))
                    break;
                increment++;
            }

            posIncr.setPositionIncrement(increment);
            termAtt.setEmpty().append(currentWord.word());
        }

        lemmaNext = !lemmaNext;
        return true;
    }

    private static final Pattern unwantedPosRE = Pattern.compile(
      "^(CC|DT|[LR]RB|MD|POS|PRP|UH|WDT|WP|WP\\$|WRB|\\$|\\#|\\.|\\,|:)$"
    );

    /**
     * Determines if words with a given POS tag should be omitted from the
     * index. Defaults to filtering out punctuation and function words
     * (pronouns, prepositions, "the", "a", etc.).
     *
     * @see <a href="http://www.ims.uni-stuttgart.de/projekte/CorpusWorkbench/CQP-HTMLDemo/PennTreebankTS.html">The Penn Treebank tag set</a> used by Stanford NLP
     */
    protected boolean unwantedPOS(String tag) {
        return unwantedPosRE.matcher(tag).matches();
    }
}
