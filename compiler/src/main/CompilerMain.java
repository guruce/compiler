/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.IOException;
import parser.RecursiveDescentParser;

/**
 *
 * @author Gurutharshan Nadarajah <gurutharshan@gmail.com>
 */
public class CompilerMain {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        RecursiveDescentParser parser = new RecursiveDescentParser();
        parser.program();
    }
}
