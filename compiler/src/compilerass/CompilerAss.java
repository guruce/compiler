/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compilerass;

import java.io.IOException;
import parser.RecursiveDescentParser;

/**
 *
 * @author Gurutharshan Nadarajah <gurutharshan@gmail.com>
 */
public class CompilerAss {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        RecursiveDescentParser parser = new RecursiveDescentParser();
        parser.P();
        
    }
}
