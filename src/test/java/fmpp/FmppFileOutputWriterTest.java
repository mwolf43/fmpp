package fmpp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;

/**
 * Test the FMPPFileOutputWriter implementation.
 */
public class FmppFileOutputWriterTest {
	
	//Some commonly used test variables
    Engine engine;
    final String enc = "UTF-8";
    File outputFile1 = new File("target/test/file1.txt");

	@Before
	public void setUp() throws Exception{
		engine = new Engine(Engine.DEFAULT_RECOMMENDED_DEFAULTS);
        File outputRoot = new File("target/test/");
        engine.setOutputRoot(outputRoot);

        assertTrue(ensureDelete(outputFile1));
	}

    /**
     * Write a simple, small text to an output file and read it again.
     * @throws Exception
     */
    @Test
    public void testSimpleWriting() throws Exception {
        String outputText = "Output for file1";
        
        FmppFileOutputWriter writer = new FmppFileOutputWriter(engine, outputFile1, enc);
        writer.append(outputText);
        writer.close();
        
        assertTrue(outputFile1.exists());
        assertEquals(outputText, readFile(outputFile1, enc));
    }

    /**
     * Write nothing to the output and test if the output file was created.
     * Expected result: output file is created empty
     * @throws Exception
     */
    @Test
    public void testNoWriting() throws Exception {
        
        FmppFileOutputWriter writer = new FmppFileOutputWriter(engine, outputFile1, enc);
        writer.close();
        
        assertTrue(outputFile1.exists());
        assertEquals(0, outputFile1.length());
    }

    /**
     * Test handling of nested output and dropping of the first output.
     * @throws Exception
     */
    @Test
    public void testNestingAndDropping() throws Exception {
    	
    	//Nested files are specified relative to the original file.
        String outputFile2 = "file2.txt";
        String content1 = "Output shold be dropped!";
        String content2 = "Output for file2";

        //Real file is defined relative to original output file.
        //So the output needs to be constructed.
        File realOutputFile2 = new File(outputFile1.getParentFile(), outputFile2);

        assertTrue(ensureDelete(realOutputFile2));
        
        FmppFileOutputWriter writer = new FmppFileOutputWriter(engine, outputFile1, enc);
        
        writer.append(content1);
        
        //Tell the writer that output to file #1 is to be dropped completely.
        //So already produced and future output are to be ignored!
        writer.dropOutputFile();

        //Try again...
        writer.append(content1);

        //Switch to output file #2
        writer.nestOutputFileBegin(outputFile2, false);
        writer.append(content2);
        
        //Switch back to output file #1
        writer.nestOutputFileEnd(false);
        assertEquals(outputFile1, writer.getOutputFile());
        
        //Output should still be ignored since file1 is dropped!
        writer.append("Some random output");
        writer.close();
        
        //First output file does not exist.
        assertFalse(outputFile1.exists());
        
        assertTrue(realOutputFile2.exists());
        assertEquals(content2, readFile(realOutputFile2, enc));
    }

    /**
     * Helper method to read the whole content of a file
     * into one big String.
     * @param file The file to read.
     * @param encoding The encoding to use.
     * @return The String representing the file content.
     * @throws IOException
     */
    private static String readFile(File file, String encoding) throws IOException {
    	StringBuilder b = new StringBuilder();
    	Charset cs = Charset.forName(encoding);
    	try(Reader r = new FileReader(file, cs)) {
    		char[] buffer = new char[1024]; //some small buffer
    		int len = 0;
    		while((len = r.read(buffer)) >= 0) {
    			b.append(buffer, 0, len);
    		}
    		return b.toString();
    	}
    }
    
    /**
     * Helper method to ensure that the given file does not exist.
     * @param file The file to delete.
     * @return Success
     */
    private static boolean ensureDelete(File file) {
    	if(!file.exists()) return true;
    	return file.delete();
    }
}
