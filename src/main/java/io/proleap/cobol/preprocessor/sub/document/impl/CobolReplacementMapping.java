/*
 * Copyright (C) 2017, Ulrich Wolffgang <ulrich.wolffgang@proleap.io>
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package io.proleap.cobol.preprocessor.sub.document.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.BufferedTokenStream;

import io.proleap.cobol.CobolPreprocessorParser.PseudoTextContext;
import io.proleap.cobol.CobolPreprocessorParser.ReplaceableContext;
import io.proleap.cobol.CobolPreprocessorParser.ReplacementContext;
import io.proleap.cobol.preprocessor.sub.util.TokenUtils;

/**
 * A mapping from a replaceable to a replacement.
 */
public class CobolReplacementMapping implements Comparable<CobolReplacementMapping> {

	public ReplaceableContext replaceable;

	public ReplacementContext replacement;

	@Override
	public int compareTo(final CobolReplacementMapping o) {
		return o.replaceable.getText().length() - replaceable.getText().length();
	}

	private String extractPseudoText(final PseudoTextContext pseudoTextCtx, final BufferedTokenStream tokens) {
		final String pseudoText = TokenUtils.getTextIncludingHiddenTokens(pseudoTextCtx, tokens).trim();
		final String content = pseudoText.replaceAll("^==", "").replaceAll("==$", "").trim();
		return content;
	}

	/**
	 * Whitespace in Cobol replaceables matches line breaks. Hence, the replaceable
	 * search string has to be enhanced to a regex, which is returned by this
	 * function.
	 */
	private String getRegexFromReplaceable(final String replaceable) {
		final String result;

		if (replaceable == null) {
			result = null;
		} else {
			final String[] parts = replaceable.split("\\s+");
			final String[] regexParts = new String[parts.length];
			final String regexSeparator = "[\\r\\n\\s]+";

			for (int i = 0; i < parts.length; i++) {
				final String part = parts[i];
				regexParts[i] = Pattern.quote(part);
			}

			result = String.join(regexSeparator, regexParts);
		}

		return result;
	}

	private String getText(final ReplaceableContext ctx, final BufferedTokenStream tokens) {
		final String result;

		if (ctx.pseudoText() != null) {
			result = extractPseudoText(ctx.pseudoText(), tokens);
		} else if (ctx.charDataLine() != null) {
			result = TokenUtils.getTextIncludingHiddenTokens(ctx, tokens);
		} else if (ctx.cobolWord() != null) {
			result = ctx.getText();
		} else if (ctx.literal() != null) {
			result = ctx.literal().getText();
		} else {
			result = null;
		}

		return result;
	}

	private String getText(final ReplacementContext ctx, final BufferedTokenStream tokens) {
		final String result;

		if (ctx.pseudoText() != null) {
			result = extractPseudoText(ctx.pseudoText(), tokens);
		} else if (ctx.charDataLine() != null) {
			result = TokenUtils.getTextIncludingHiddenTokens(ctx, tokens);
		} else if (ctx.cobolWord() != null) {
			result = ctx.getText();
		} else if (ctx.literal() != null) {
			result = ctx.literal().getText();
		} else {
			result = null;
		}

		return result;
	}

  protected boolean traceOn = false;

  public void describe(final BufferedTokenStream tokens) {
    final String replaceableString = getText(replaceable, tokens);
    final String replacementString = getText(replacement, tokens);
    System.err.printf("replacement of %s with %s\n",replaceableString,replacementString);
  }

	protected String replace(final String string, final BufferedTokenStream tokens) {
                
		final String replaceableString = getText(replaceable, tokens);
		final String replacementString = getText(replacement, tokens);

		final String result;
                java.io.PrintStream log = System.out;
                final boolean isWordToWord = ((replaceable.cobolWord() != null) &&
                                              // replaceableString.equals("QA zz 10") &&
                                              (replacement.cobolWord() != null));
                traceOn = replacementString.indexOf("HSFS-SUMA-DATA") != -1;
                

		if (replaceableString != null && replacementString != null) {
                
                        if (isWordToWord){
                          if (traceOn){
                            log.printf("IBM WORD-TO-WORD %s to %s \n",
                                       replaceableString,replacementString);
                          }
                          String s = string;
                          int sLen = s.length();
                          int startPos = 0;
                          int replaceablePos = -1;
                          int replaceableLength = replaceableString.length();
                          int replacementLength = replacementString.length();
                          while ((replaceablePos = s.indexOf(replaceableString,startPos)) != -1){
                            if (traceOn){
                              log.printf("replaceablePos found at %d in \n%s\n",replaceablePos,s);
                            }
                            // here, what about prev char??
                            boolean isWordStart = ((replaceablePos == 0) || (s.charAt(replaceablePos-1) == ' '));
                            
                            if (/* isWordStart && */ (replaceablePos + replacementLength == sLen)){
                              // match whole word at end of string
                              s = s.substring(0,replaceablePos)+replacementString;
                              if (traceOn){
                                log.printf("case 1 s is now %s\n",s);
                              }
                              break;
                            }
                            char nextChar = s.charAt(replaceablePos+replaceableLength);
                            if (isWordStart && ((nextChar == '.' || nextChar == ' '))){
                              // word ends with space or dot, what about other punctuation as word-terminating??
                              s = (s.substring(0,replaceablePos)+
                                   replacementString+
                                   s.substring(replaceablePos+replaceableLength));
                              if (traceOn){
                                log.printf("case 2 s is now %s, isWordStart=%s\n",s,isWordStart);
                              }
                              startPos = replaceablePos + replacementLength + 1;
                            } else {
                              startPos = replaceablePos + replacementLength;
                              if (traceOn){
                                log.printf("case 3 startPos is %d\n",startPos);
                              }
                            }
                          }
                          result = s;
                        } else {
                          if (traceOn){
                            log.printf("GENERAL REPLACEMENT %s to %s \n",
                                       replaceableString,replacementString);
                          }
                          // regex for the replaceable
                          final String replaceableRegex = getRegexFromReplaceable(replaceableString);
                          // regex for the replacement
                          final String quotedReplacementRegex = Matcher.quoteReplacement(replacementString);
                          result = Pattern.compile(replaceableRegex).matcher(string).replaceAll(quotedReplacementRegex);
                        }
		} else {
			result = string;
		}

		return result;
	}

	@Override
	public String toString() {
		return replaceable.getText() + " -> " + replacement.getText();
	}
}
