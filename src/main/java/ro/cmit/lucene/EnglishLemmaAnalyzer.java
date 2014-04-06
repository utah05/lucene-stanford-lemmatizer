/*
 * Lemmatizing library for Lucene
 * Copyright (C) 2010 Lars Buitinck
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

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.tagger.maxent.TaggerConfig;

/**
 * An analyzer that uses an {@link EnglishLemmaTokenizer}.
 * 
 */
public class EnglishLemmaAnalyzer extends Analyzer {
	private MaxentTagger posTagger;

	/**
	 * Construct an analyzer with a tagger using a default model file.
	 * Need to add the following dependency to your project.
	 * <dependency>
	 *   <groupId>edu.stanford.nlp</groupId>
	 *   <artifactId>stanford-corenlp</artifactId>
	 *   <version>${stanford-corenlp.version}</version>
	 *   <classifier>models</classifier> 
	 *  </dependency>
	 * 
	 * @throws Exception
	 */
	public EnglishLemmaAnalyzer() throws Exception {
		this(
				makeTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger"));
	}

	/**
	 * Construct an analyzer with a tagger using the given model file.
	 */
	public EnglishLemmaAnalyzer(String posModelFile) throws Exception {
		this(makeTagger(posModelFile));
	}

	/**
	 * Construct an analyzer using the given tagger.
	 */
	public EnglishLemmaAnalyzer(MaxentTagger tagger) {
		posTagger = tagger;
	}

	/**
	 * Factory method for loading a POS tagger.
	 */
	public static MaxentTagger makeTagger(String modelFile) throws Exception {
		TaggerConfig config = new TaggerConfig("-model", modelFile);
		// The final argument suppresses a "loading" message on stderr.
		return new MaxentTagger(modelFile, config, false);
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			Reader reader) {
		return new TokenStreamComponents(new EnglishLemmaTokenizer(reader,
				posTagger));
	}
}
