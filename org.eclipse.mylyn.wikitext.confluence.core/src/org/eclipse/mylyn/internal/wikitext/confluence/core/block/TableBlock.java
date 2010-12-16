/*******************************************************************************
 * Copyright (c) 2007, 2008 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *     Fintan Bolton - modified for use in Confdoc plugin
 *******************************************************************************/
package org.eclipse.mylyn.internal.wikitext.confluence.core.block;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.XmlTableAttributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.core.parser.builder.AbstractXmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.markup.Block;

/**
 * Table block, matches blocks that start with <code>table. </code> or those that start with a table row.
 * 
 * @author David Green
 */
public class TableBlock extends Block {
    private final Logger log = Logger.getLogger(TableBlock.class.getName());

    static final Pattern startPattern = Pattern.compile("(?:\\s*)(\\|(.*)?(\\|\\s*$))");
    static final Pattern CONT_CELL_PATTERN = Pattern.compile("^(?:\\s*)((?:(?:[^\\|\\[]*)(?:\\[[^\\]]*\\])?)*)");
    static final Pattern NEXT_CELL_PATTERN = Pattern.compile("\\|(\\|)?" + "((?:(?:[^\\|\\[\\]]*)(?:\\[[^\\]]*\\])?)*)");

    // + "(\\|\\|?\\s*$)?");
    // static final Pattern END_ROW_PATTERN = Pattern.compile("(\\|\\|?\\s*$)");

    protected int blockLineCount = -1;
    protected int builderLevel = -1;
    protected int rowCount = 0;
    protected int headerColCount = 0;
    protected int colCount;
    protected boolean isHeaderRow = false;

    private Matcher matcher;

    private enum State {
        INACTIVE, IN_TABLE, IN_ROW, IN_CELL
    };

    private State tableState;


    public TableBlock() {
        tableState = State.INACTIVE;
        colCount = 0;
    }

    @Override
    public int processLineContent(String line, int offset) {
        if (tableState == State.INACTIVE) {
            // Put code here that executes at very start of table
            blockLineCount = 0;
            headerColCount = 0;
            colCount = 0;
            rowCount = 0;
            builderLevel = -1;
            super.setClosed(false);
            log.fine("Table started.");
            Attributes attr = new Attributes();
            attr.setCssClass("table-wrap");
            builder.beginBlock(BlockType.DIV, attr);
            XmlTableAttributes attributes = new XmlTableAttributes();
            attributes.setCssClass("confluenceTable");
            builder.beginBlock(BlockType.TABLE, attributes);
            tableState = State.IN_TABLE;
        } else if (markupLanguage.isEmptyLine(line) || !startPattern.matcher(line).matches()) {
            // [End of Table]
            log.fine("End of table");
            setClosed(true);
            return 0;
        }
        ++blockLineCount;

        if (offset == line.length()) {
            return -1;
        }

        String textileLine = (offset == 0) ? line : line.substring(offset);

        Matcher contCellMatcher = CONT_CELL_PATTERN.matcher(textileLine);
        contCellMatcher.find();
        String cellText = contCellMatcher.group(1);
        if (log.isLoggable(Level.FINE)) {
            log.fine("Matching cellText = [" + cellText + "]");
            log.fine("offset = " + offset);
            log.fine("textileLine = " + textileLine);
        }

        int cellOffset = offset;
        int lineOffset = offset + ((cellText != null) ? contCellMatcher.start(1) : 0);
        textileLine = line.substring(lineOffset);
        Matcher nextCellMatcher = NEXT_CELL_PATTERN.matcher(textileLine);

        while (nextCellMatcher.find()) {
            if (cellText != null && !cellText.equals("")) {
                if (tableState == State.IN_CELL) {
                    // Emit *preceding* cell text
                	// (no nesting blocks)
                    markupLanguage.emitMarkupLine(getParser(), state, cellOffset, cellText, 0);
                    cellText = null;
                } else {
                    // [End of Table]
                    // If we get here, it means the line started without a pipe, |,
                    // symbol.
                    log.fine("End of table");
                    setClosed(true);
                    return 0;
                }
            }
            if (tableState == State.IN_CELL) {
                // [Close current cell]
                log.fine("End of current cell");
                if (isHeaderRow) {
                    headerColCount++;
                } else {
                    colCount++;
                }
                closeToLevel(State.IN_ROW);
            } else {
                // [Start new row]
                log.fine("Start new row");
                String headerIndicator = nextCellMatcher.group(1);
                isHeaderRow = (headerIndicator != null) && "|".equals(headerIndicator);
                if (isHeaderRow) {
                    headerColCount = 0;
                    colCount = 0;
                } else {
                    colCount = 0;
                }
                builder.beginBlock(BlockType.TABLE_ROW, new Attributes());
                tableState = State.IN_ROW;
            }

            // Test for end of row
            int currOffset = nextCellMatcher.start();
            String restOfLine = textileLine.substring(currOffset);
            boolean foundEndRowMarker = restOfLine.matches("\\|(\\|)?\\s*$");
            boolean reachedColLimit = (colCount != 0) ? (colCount >= headerColCount) : false;
            log.fine("foundEndRowMarker = " + Boolean.toString(foundEndRowMarker));
            log.fine("reachedColLimit = " + Boolean.toString(reachedColLimit));

            cellText = nextCellMatcher.group(2);
            if (!foundEndRowMarker && !reachedColLimit) {
                // [Start a new cell]
                cellText = cellText.replaceFirst("\\\\\\\\\\s*$", "");
                cellOffset = lineOffset + nextCellMatcher.start(2);
                log.fine("Start new cell: cellText = [" + cellText + "]" + ", cellOffset = [" + cellOffset + "]");
                Attributes attr = new Attributes();
                attr.setCssClass(isHeaderRow ? "confluenceTh" : "confluenceTd");
                builder.beginBlock(isHeaderRow ? BlockType.TABLE_CELL_HEADER : BlockType.TABLE_CELL_NORMAL, attr);
                tableState = State.IN_CELL;
                if (builder instanceof AbstractXmlDocumentBuilder) {
                    builderLevel = ((AbstractXmlDocumentBuilder) builder).getElementNestLevel();
                }
            }

            if (foundEndRowMarker || reachedColLimit) {
                // [End of row]
                log.fine("End of row");
                closeToLevel(State.IN_TABLE);
                rowCount++;
                break;
            }
        }

        if (cellText != null && tableState == State.IN_CELL) {
        	if (isNestingEnabled()) {
        	    // Multi-line cell
                // Let the parent parser process the trailing cell
                return cellOffset;
        	}
        	else {
        	    // Simple cell (no nesting blocks)
                markupLanguage.emitMarkupLine(getParser(), state, cellOffset, cellText, 0);
                return -1;
        	}
        }
        else {
            // Line was completely consumed
            return -1;
        }
    }

    @Override
    public boolean canStart(String line, int lineOffset) {
        boolean isStart = false;
        if ((lineOffset == 0) && (tableState == State.INACTIVE)) {
            matcher = startPattern.matcher(line);
            isStart = matcher.matches();
        } else {
            matcher = null;
            isStart = false;
        }
        return isStart;
    }

    @Override
    public void setClosed(boolean closed) {
        if (closed && !isClosed()) {
            closeToLevel(State.INACTIVE);
        }
        blockLineCount = -1;
        builderLevel = -1;
        super.setClosed(closed);
    }

    @Override
    public boolean beginNesting() {
        return false;
    }
    
    public boolean isNestingEnabled() {
        return (builder instanceof AbstractXmlDocumentBuilder) ? true : false;
    }

    @Override
    public int findCloseOffset(String line, int lineOffset) {
        int closeOffset = -1;
        if (builderLevel != -1) {
            if ((builderLevel >= ((AbstractXmlDocumentBuilder) builder).getElementNestLevel())) {
                if (!startPattern.matcher(line.substring(lineOffset)).find()) {
                    closeOffset = 0;
                }
            }
        }
        return closeOffset;
    }

    @Override
    public Block clone() {
        return null;
    }

    protected boolean closeToLevel(State targetState) {
        if (targetState == State.IN_ROW) {
            if (tableState == State.IN_CELL) {
                builder.endBlock(); // [Close current cell]
                tableState = State.IN_ROW;
                return true;
            }
            if (tableState == State.IN_ROW) {
                return true;
            }
            return false;
        }
        if (targetState == State.IN_TABLE) {
            if (tableState == State.IN_CELL) {
                builder.endBlock(); // [Close current cell]
                builder.endBlock(); // [Close current row]
                tableState = State.IN_TABLE;
                return true;
            }
            if (tableState == State.IN_ROW) {
                builder.endBlock(); // [Close current row]
                tableState = State.IN_TABLE;
                return true;
            }
            if (tableState == State.IN_TABLE) {
                return true;
            }
            return false;
        }
        if (targetState == State.INACTIVE) {
            if (tableState == State.IN_CELL) {
                builder.endBlock(); // [Close current cell]
                builder.endBlock(); // [Close current row]
                builder.endBlock(); // [Close table]
                builder.endBlock(); // [Close div wrapper]
                tableState = State.INACTIVE;
                return true;
            }
            if (tableState == State.IN_ROW) {
                builder.endBlock(); // [Close current row]
                builder.endBlock(); // [Close table]
                builder.endBlock(); // [Close div wrapper]
                tableState = State.INACTIVE;
                return true;
            }
            if (tableState == State.IN_TABLE) {
                builder.endBlock(); // [Close table]
                builder.endBlock(); // [Close div wrapper]
                tableState = State.INACTIVE;
                return true;
            }
            if (tableState == State.INACTIVE) {
                return true;
            }
            return false;
        }
        return false;
    }
    
}
