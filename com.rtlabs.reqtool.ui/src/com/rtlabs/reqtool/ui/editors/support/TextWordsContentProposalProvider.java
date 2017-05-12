package com.rtlabs.reqtool.ui.editors.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import com.google.common.collect.ImmutableSet;

public class TextWordsContentProposalProvider implements IContentProposalProvider {
	private Pattern wordSplitterPattern = Pattern.compile("[^-\\w\\d]+");
	
	 // The provided proposals
	private List<String> standardProposals = new ArrayList<>();

	 // The proposals mapped to IContentProposal. Cached for speed in the case where filtering is not used.
	private IContentProposal[] proposalsCache;
	private String contentsCache = null;
	private Set<String> wordsCache = null;
	private String currentWordCache = null;

	public TextWordsContentProposalProvider(String... proposals) {
		this.standardProposals.addAll(Arrays.asList(proposals));;
	}

	private String getCurrentWordPrefix(String contents, int position) {
		if (position < 1) return "";
		
		StringBuilder b = new StringBuilder(8);
		Matcher m = wordSplitterPattern.matcher("");
		
		while (true) {
			if (position < 1) break;
			String curChar = contents.substring(position - 1, position);
			m.reset(curChar);
			if (m.find()) break;
			b.append(curChar);
			position--;
		}
		
		b.reverse();
		return b.toString();
	}
	
	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		String currentWord = getCurrentWordPrefix(contents, position);

		if (!contents.equalsIgnoreCase(contentsCache)) {
			wordsCache = null;
			proposalsCache = null;
		} else if (!currentWord.equalsIgnoreCase(currentWordCache)) { 
			proposalsCache = null;
		}
		
		contentsCache = contents;
		currentWordCache = currentWord.toLowerCase();

		if (wordsCache == null) {
			wordsCache = Stream.concat(
					standardProposals.stream(),
					wordSplitterPattern.splitAsStream(contents)
						.filter(w -> w.length() > 3)
						.map(w -> w.toLowerCase()))
				.collect(toImmutableSet());
		}
		
		if (proposalsCache == null) {
			proposalsCache = wordsCache.stream()
				.filter(w -> !w.equals(currentWordCache))
				.filter(w -> w.startsWith(currentWordCache))
				.map(w -> {
					String content = w.substring(currentWord.length());
					return new ContentProposal(content, currentWord + content, null, content.length());
				})
				.toArray(IContentProposal[]::new);
		}
		
		return proposalsCache;
	}

	private static <T> Collector<T, ?, Set<T>> toImmutableSet() {
		return Collector.of(
			() -> ImmutableSet.<T>builder(), 
			(b, e) -> b.add(e), 
			(a, b) -> a.addAll(b.build()),
			b -> b.build());
	}
}