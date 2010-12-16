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
package org.eclipse.mylyn.internal.wikitext.confluence.core.block;

import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.core.parser.builder.AbstractXmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.markup.Block;

/**
 * List block, matches blocks that start with <code>*</code>, <code>#</code> or <code>-</code>
 * 
 * @author David Green
 */
public class ListBlock extends Block {

	private static final int LINE_REMAINDER_GROUP_OFFSET = 2;

	static final Pattern startPattern = Pattern.compile("((?:(?:\\*)|(?:#)|(?:-))+)\\s(.*+)"); //$NON-NLS-1$

	private int blockLineCount = 0;

	protected int builderLevel = -1;

	private Matcher matcher;

	private Stack<ListState> listState;

    private boolean nesting = false;

	public ListBlock() {
	}

	@Override
	public int processLineContent(String line, int offset) {
		if (line.matches("\\s*")) {
			// A blank line terminates the list.
			setClosed(true);
			return 0;
		}
        if (!startPattern.matcher(line).matches()) {
            nesting = true;
            return 0;
        }
        nesting = false;

		if (blockLineCount == 0) {
			listState = new Stack<ListState>();
			Attributes attributes = new Attributes();
			String listSpec = matcher.group(1);
			int level = calculateLevel(listSpec);
			BlockType type = calculateType(listSpec);

			if (type == BlockType.BULLETED_LIST && "-".equals(listSpec)) { //$NON-NLS-1$
				attributes.setCssStyle("list-style: square"); //$NON-NLS-1$
			}

			// 0-offset matches may start with the "*** " prefix.
			offset = matcher.start(LINE_REMAINDER_GROUP_OFFSET);

			listState.push(new ListState(1, type));
			builder.beginBlock(type, attributes);

			adjustLevel(listSpec, level, type);

			ListState listState = this.listState.peek();
			if (listState.openItem) {
				builder.endBlock();
			}
			listState.openItem = true;
			builder.beginBlock(BlockType.LIST_ITEM, new Attributes());
			if (builder instanceof AbstractXmlDocumentBuilder) {
				builderLevel = ((AbstractXmlDocumentBuilder) builder).getElementNestLevel();
			}
		} else if ((matcher = startPattern.matcher(line)).matches()) {
			String listSpec = matcher.group(1);
			int level = calculateLevel(listSpec);
			BlockType type = calculateType(listSpec);

			offset = matcher.start(LINE_REMAINDER_GROUP_OFFSET);

			adjustLevel(listSpec, level, type);

			ListState listState = this.listState.peek();
			if (listState.openItem) {
				builder.endBlock();
			}
			listState.openItem = true;
			builder.beginBlock(BlockType.LIST_ITEM, new Attributes());
			if (builder instanceof AbstractXmlDocumentBuilder) {
				builderLevel = ((AbstractXmlDocumentBuilder) builder).getElementNestLevel();
			}
		}
		++blockLineCount;

		markupLanguage.emitMarkupLine(getParser(), state, line, offset);

        return -1;
	}

	private void adjustLevel(String listSpec, int level, BlockType type) {
		for (ListState previousState = listState.peek(); level != previousState.level || previousState.type != type; previousState = listState.peek()) {

			if (level > previousState.level) {
				if (!previousState.openItem) {
					builder.beginBlock(BlockType.LIST_ITEM, new Attributes());
					previousState.openItem = true;
				}

				Attributes blockAttributes = new Attributes();
				if (type == BlockType.BULLETED_LIST && "-".equals(listSpec)) { //$NON-NLS-1$
					blockAttributes.setCssStyle("list-style: square"); //$NON-NLS-1$
				}
				listState.push(new ListState(previousState.level + 1, type));
				builder.beginBlock(type, blockAttributes);
			} else {
				closeOne();
				if (listState.isEmpty()) {
					Attributes blockAttributes = new Attributes();
					if (type == BlockType.BULLETED_LIST && "-".equals(listSpec)) { //$NON-NLS-1$
						blockAttributes.setCssStyle("list-style: square"); //$NON-NLS-1$
					}

					listState.push(new ListState(1, type));
					builder.beginBlock(type, blockAttributes);
				}
			}
		}
	}

	private int calculateLevel(String listSpec) {
		return listSpec.length();
	}

	private BlockType calculateType(String listSpec) {
		return listSpec.charAt(listSpec.length() - 1) == '#' ? BlockType.NUMERIC_LIST : BlockType.BULLETED_LIST;
	}

    @Override
    public Block clone() {
        return null;
    }

    @Override
	public boolean canStart(String line, int lineOffset) {
		if (lineOffset == 0) {
			matcher = startPattern.matcher(line);
			if (matcher.matches()) {
				setClosed(false);
				return true;
			}
		}
        return false;
	}

	@Override
	public void setClosed(boolean closed) {
		if (closed && !isClosed()) {
			while (listState != null && !listState.isEmpty()) {
				closeOne();
			}
			listState = null;
            blockLineCount = 0;
            builderLevel = -1;
		}
		super.setClosed(closed);
	}

	private void closeOne() {
		ListState e = listState.pop();
		if (e.openItem) {
			builder.endBlock();
		}
		builder.endBlock();
	}

	@Override
	public boolean beginNesting() {
		return nesting;
	}

	@Override
	public int findCloseOffset(String line, int lineOffset) {
		int closeOffset = -1;
		if (builderLevel != -1) {
			if ((builderLevel >= ((AbstractXmlDocumentBuilder) builder).getElementNestLevel())) {
				if (line.matches("\\s*")) {
					closeOffset = 0;
				}
			}
		}
		return closeOffset;
	}

	private static class ListState {
		int level;

		BlockType type;

		boolean openItem;

		private ListState(int level, BlockType type) {
			super();
			this.level = level;
			this.type = type;
		}

	}
}
