
package parser;

import java.io.IOException;
import lexer.*;

public class Parser {

    static int lookahead;
    Lexer lexer;
    Token token;

    public Parser() throws IOException {
        lexer = new Lexer();
        token = lexer.scan();
        if (token.tag == Tag.NUM) {
            lookahead = ((Num)token).value;
        }               
    }

    public void expr() throws IOException {
        term();
        while (true) {
            if (lookahead == '+') {
                match('+');
                term();
                System.out.print('+');
                continue;
            } else if (lookahead == '-') {
                match('-');
                term();
                System.out.print('-');
                continue;
            } else {
                return;
            }
        }
    }

    void term() throws IOException {
//        if (Character.isDigit(lookahead)) {
            System.out.print(lookahead);
            match(lookahead);
//        } else {
//            throw new Error("syntax error");
//        }
    }

    void match(int t) throws IOException {
        if (lookahead == t) {
            token = lexer.scan();
            if (token.tag == Tag.NUM) {
                lookahead = ((Num) token).value;
            } else {
                lookahead = token.tag;
            }
        } else {
            throw new Error("syntax error");
        }

    }
}
