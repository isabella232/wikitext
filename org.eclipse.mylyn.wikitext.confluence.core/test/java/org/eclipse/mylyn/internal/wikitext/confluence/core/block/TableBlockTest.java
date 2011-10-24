package org.eclipse.mylyn.internal.wikitext.confluence.core.block;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.mylyn.wikitext.confluence.core.ConfluenceLanguage;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;


/**
 * Unit test for simple App.
 */
public class TableBlockTest
    extends TestCase
{
    private final Logger log = Logger.getLogger(TableBlockTest.class.getName());
	private MarkupParser markupParser;
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TableBlockTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TableBlockTest.class );
    }

    /**
     * Rigorous Test :-)
     */
    public void testTableBlock()
    {
        String sample_in;
        String sample_out;
        String expected;
    	markupParser = new MarkupParser(new ConfluenceLanguage());
    	try {
    		java.net.URL inputUrl = getClass().getResource("test-table.conf");
    		sample_in = convertStreamToString(inputUrl.openStream());
    		sample_out = convertWikitextToDocBook(sample_in);
    		log.info("Page generated from test-table.conf is:");
    		log.info(sample_out);
    	}
    	catch (Exception e) {
    		log.log(Level.SEVERE, e.toString());
    		assertTrue(false);
    		return;
    	}

    	// Read expected result from file.
        try {
            java.net.URL inputUrl = getClass().getResource("test-table.html");
            expected = convertStreamToString(inputUrl.openStream());
            log.info("Expected page content is:");
            log.info(expected);
        }
        catch (Exception e) {
            log.log(Level.SEVERE, e.toString());
            assertTrue(false);
            return;
        }
        // Make the newline character platform-independent, in case you are running
        // the test on a Windows platform.
        String normalized_expected = expected.replaceAll("(\r\n|\r)", "\n").trim();
    	assertTrue( normalized_expected.equals(sample_out.trim())  );
    }
    
    private String convertWikitextToDocBook(String wikiText)
    {
        StringWriter out = new StringWriter();
        
        markupParser.setBuilder(new HtmlDocumentBuilder(out));
        markupParser.parse(wikiText);
        markupParser.setBuilder(null);

        return out.toString();
    }

    public String convertStreamToString(InputStream is)
                throws IOException {
            /*
             * To convert the InputStream to String we use the
             * Reader.read(char[] buffer) method. We iterate until the
             * Reader return -1 which means there's no more data to
             * read. We use the StringWriter class to produce the string.
             */
            if (is != null) {
                Writer writer = new StringWriter();
     
                char[] buffer = new char[1024];
                try {
                    Reader reader = new BufferedReader(
                            new InputStreamReader(is, "UTF-8"));
                    int n;
                    while ((n = reader.read(buffer)) != -1) {
                        writer.write(buffer, 0, n);
                    }
                } finally {
                    is.close();
                }
                return writer.toString();
            } else {       
                return "";
            }
    }
}
