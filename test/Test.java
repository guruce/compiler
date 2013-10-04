
package test;

import java.io.IOException;
import lexer.*;
import parser.*;

public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // System.out.println("compiler design");   // TODO code application logic here

        Parser p = new Parser();
        p.expr();
//        Lexer l=new Lexer();
//        Token t=l.scan();
//        try{
//            System.out.println(t.toString());
//        }catch(Exception e){
//            System.out.println("exception");
//        }
    }
}
