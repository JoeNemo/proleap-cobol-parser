/*
 * Copyright (C) 2017, Ulrich Wolffgang <ulrich.wolffgang@proleap.io>
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package io.proleap.cobol.preprocessor.sub.document.impl;

import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.BufferedTokenStream;

import io.proleap.cobol.CobolPreprocessorParser.ReplaceClauseContext;

/**
 * A replacement context that defines, which replaceables should be replaced by
 * which replacements.
 */
public class CobolDocumentContext {

	private CobolReplacementMapping[] currentReplaceableReplacements;

	private StringBuffer outputBuffer = new StringBuffer();

	public String read() {
          String out = outputBuffer.toString();
          // System.err.printf("JOE CobDocCtx read, len=%d\n",out.length());
          return out;
	}

	/**
	 * Replaces replaceables with replacements.
	 */
	public void replaceReplaceablesByReplacements(final BufferedTokenStream tokens) {
		if (currentReplaceableReplacements != null) {
			Arrays.sort(currentReplaceableReplacements);

			for (final CobolReplacementMapping replaceableReplacement : currentReplaceableReplacements) {
				final String currentOutput = outputBuffer.toString();
				final String replacedOutput = replaceableReplacement.replace(currentOutput, tokens);

				outputBuffer = new StringBuffer();
                                int interestingStringPos = replacedOutput.indexOf("CALC1-FYH2-CALC1");
                                if (interestingStringPos != -1){
                                  int rangeStart = Math.max(0,interestingStringPos-80);
                                  int rangeEnd = Math.min(replacedOutput.length(),interestingStringPos+80);
                                  System.err.printf("interesting replacement at %d\n%s\n",
                                                    interestingStringPos,
                                                    replacedOutput.substring(rangeStart,rangeEnd));
                                  replaceableReplacement.describe(tokens);
                                  Thread.dumpStack();
                                }
				outputBuffer.append(replacedOutput);
			}
		}
	}

	public void storeReplaceablesAndReplacements(final List<ReplaceClauseContext> replaceClauses) {
		if (replaceClauses == null) {
			currentReplaceableReplacements = null;
		} else {
			final int length = replaceClauses.size();
			currentReplaceableReplacements = new CobolReplacementMapping[length];

			int i = 0;

			for (final ReplaceClauseContext replaceClause : replaceClauses) {
				final CobolReplacementMapping replaceableReplacement = new CobolReplacementMapping();

				replaceableReplacement.replaceable = replaceClause.replaceable();
				replaceableReplacement.replacement = replaceClause.replacement();

				currentReplaceableReplacements[i] = replaceableReplacement;
				i++;
			}
		}
	}

  public int getLength(){
    return outputBuffer.length();
  }

	public void write(final String text) {
		outputBuffer.append(text);
	}
}
