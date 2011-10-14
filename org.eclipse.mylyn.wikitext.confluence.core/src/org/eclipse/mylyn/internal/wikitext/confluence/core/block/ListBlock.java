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
	
	protected boolean nesting = false;

	private Matcher matcher;

	private Stack<ListState> listState;

	public ListBlock() {
	}

	@Override
	public int processLineContent(String line, int offset) {
	    nesting = false;
	    cancelWantControlFlag();
	    
		if (line.matches("\\s*")) {
			// A blank line terminates the list.
			setClosed(true);
			return 0;
		}
		
		boolean isListInsideTable = checkIsListInsideTable();

        Matcher contCellMatcher = TableBlock.CONT_CELL_PATTERN.matcher(line);
        contCellMatcher.find();
        int tableCellOffset = contCellMatcher.end(1);
        boolean foundTableRow = (tableCellOffset < line.length());
		if (foundTableRow && isListInsideTable) {
            // Suppress creation of a table inside a list inside a table
		    if (tableCellOffset == offset) {
	            // Close list, enabling parent table to take control
	            setClosed(true);
	            return offset;
		    }
		}
		
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
		} else if (offset==0 && (matcher = startPattern.matcher(line)).matches()) {
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
		}
		++blockLineCount;

		if (isNestingEnabled()) {
	        // Enable nested processing of the line remainder
	        nesting = true;
	        return offset;
		}
		else {
            markupLanguage.emitMarkupLine(getParser(), state, line, offset);
            return -1;
		}
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
	public boolean canStart(String line, int lineOffset) {
		blockLineCount = 0;
		builderLevel = -1;
		listState = null;
		if (lineOffset == 0) {
			matcher = startPattern.matcher(line);
			return matcher.matches();
		} else {
			matcher = null;
			return false;
		}
	}

	@Override
	public void setClosed(boolean closed) {
		if (closed && !isClosed()) {
			while (listState != null && !listState.isEmpty()) {
				closeOne();
			}
			listState = null;
			blockLineCount =0;
			builderLevel = -1;
			nesting = false;
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
		return nesting && ((builder instanceof AbstractXmlDocumentBuilder) ? true : false);
	}

    public boolean isNestingEnabled() {
        if (getMarkupLanguage().blockWantsControl() != null
                && !getMarkupLanguage().blockWantsControl().equals("list")) {
            // Disable nesting, if another block type is trying to regain control
            return false;
        }
        else {
            // Enable nesting, if the current builder object supports it
            return beginNesting();
        }
    }
    
    protected boolean checkIsListInsideTable() {
        Stack<Block> nestedBlocks = getMarkupLanguage().getNestedBlocks();
        if (nestedBlocks != null && !nestedBlocks.isEmpty()) {
            if (nestedBlocks.peek() instanceof TableBlock) {
                // This list is nested inside a table
                return true;
            }
            else if (nestedBlocks.size() >= 2) {
                // Peek up to a depth of 2
                // i.e. check whether the current nesting has the form
                // Table > List > [CurrentBlock]
                if (nestedBlocks.elementAt(nestedBlocks.size()-2) instanceof TableBlock) {
                    return true;
                }
            }
        }
        return false;
    }

	@Override
	public int findCloseOffset(String line, int lineOffset) {
		int closeOffset = -1;
        AbstractXmlDocumentBuilder absXmlBuilder = (AbstractXmlDocumentBuilder) builder;
        if (!getMarkupLanguage().currentBlockHasLiteralLayout()) {
            if (line.matches("\\s*") ||
                    (lineOffset==0 && startPattern.matcher(line).matches()) ) {
                closeOffset = lineOffset;
                // Notify other nested blocks that list wants to
                // assume control of parsing
                getMarkupLanguage().setBlockWantsControl("list");
            }
            else if (checkIsListInsideTable()){
                // In order to give an enclosing table a chance at retaking control,
                // we must also make sure that the list pops off the stack of nested blocks
                // whenever it sees a table cell coming
                Matcher contCellMatcher = TableBlock.CONT_CELL_PATTERN.matcher(line);
                contCellMatcher.find();
                if (contCellMatcher.end(1) < line.length()) {
                    closeOffset = lineOffset;
                }
            }
        }
		return closeOffset;
	}

    protected void cancelWantControlFlag() {
        if (getMarkupLanguage().blockWantsControl() != null
                && getMarkupLanguage().blockWantsControl().equals("list")) {
            getMarkupLanguage().setBlockWantsControl(null);
        }
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
