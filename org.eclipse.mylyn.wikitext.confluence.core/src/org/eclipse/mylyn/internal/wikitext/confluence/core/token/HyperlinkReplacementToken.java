/*******************************************************************************
 * Copyright (c) 2007, 2008 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.internal.wikitext.confluence.core.token;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.LinkAttributes;
import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElement;
import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElementProcessor;

/**
 * 
 * 
 * @author David Green
 */
public class HyperlinkReplacementToken extends PatternBasedElement {

	@Override
	protected String getPattern(int groupOffset) {
        return "(^|[^\\{\\\\])\\{link:([^\\}]+)\\}"; //$NON-NLS-1$
	}

	@Override
	protected int getPatternGroupCount() {
		return 2;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new HyperlinkReplacementTokenProcessor();
	}

	private static class HyperlinkReplacementTokenProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
            getBuilder().characters(group(1));
    	    String linkComposite = group(2);
			String[] parts = linkComposite.split("\\s*\\|\\s*(?=([^\\!]*\\![^\\!]*\\!)*[^\\!]*$)"); //$NON-NLS-1$
                // split on '\s*\|\s*' but make sure the | is not included inside a !..! block
                // see http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes/1757107#1757107
			String text = parts.length > 1 ? parts[0] : null;
			if (text != null) {
				text = text.trim();
			}
			String href = parts.length > 1 ? parts[1] : parts[0];
			if (href != null) {
				href = href.trim();
			}
			String tip = parts.length > 2 ? parts[2] : null;
			if (tip != null) {
				tip = tip.trim();
			}
			if (text == null || text.length() == 0) {
				text = href;
                if (text.charAt(0) == '#') {
                    text = text.substring(1);
                }
			}
			Attributes attributes = new LinkAttributes();
			attributes.setTitle(tip);
			getBuilder().link(attributes, href, text);
		}
	}
}
