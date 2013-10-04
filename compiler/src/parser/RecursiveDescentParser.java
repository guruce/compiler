
package parser;

import inter.Id;
import java.io.IOException;
import java.util.Stack;
import lexer.*;
import symbols.*;

/**
 *
 * @author Gurutharshan Nadarajah <gurutharshan@gmail.com>
 */
public class RecursiveDescentParser {
    
    Token lookahead, nextSymbol;
    Lexer lexer;
    boolean backtracted;
    Type type;
    int used = 0;
    Env top; //Symbol table at the top -- here only one
    Stack stack;

    public RecursiveDescentParser() throws IOException {
        lexer = new Lexer();
        lookahead = lexer.scan();
        top = new Env(null);
        stack = new Stack();
    }     
    
    public void P() throws IOException {
        D();
        L();
    }
    
    /**
     * Have to look first set D -> BN;D | BN;
     */
    void D() throws IOException {
        B();
        N();
        match(new Token(';'));
        if (lookahead.tag == Tag.BASIC) //checking first set to choose production rule
             D();
    }
    
    /**
     * special care on first sets L -> S;L | S;
     */
    void L() throws IOException {
        S();
        match(new Token(';'));
        if (lookahead.tag == Tag.ID || lookahead.tag == '(' || lookahead.tag == Tag.NUM)
            L();        
        /* if next symbol present then call L else nothing */
    }
    
    void B() throws IOException {
        if (lookahead.tag == Tag.BASIC){
            type = (Type)lookahead;
            match(new Token(Tag.BASIC));
        }
        else
            throw new Error("syntax error"); // advanced with line number and message
    }
    
    void N() throws IOException {
        //insert into symbol table
        Id id = new Id((Word)lookahead, type, used);
        top.put(lookahead, id);
        match(new Token(Tag.ID));
        N1();
    }
    
    void N1() throws IOException {
        if (lookahead.tag == ',') {
            match(new Token(','));
            //insert into symbol table
            Id id = new Id((Word)lookahead, type, used);
            top.put(lookahead, id);
            match(new Token(Tag.ID));
            N1();
        } else {
            type = null;
        }
        //else nothing
    }
    
    
    /* second part */
    void S() throws IOException {
            if (lookahead.tag == Tag.ID) {
            Token tempID = lookahead;
            stack.push(lookahead);              //postfix stack
            match(new Token(Tag.ID));
            System.out.print(tempID.toString());

            //backtracking part...
            if(lookahead.tag == '=') {
                match(new Token('='));
                E();
                Token rhs = (Token)stack.pop();
                Token lhs = (Token)stack.pop();
                Word wLHS = (Word)lhs;
                wLHS.value = rhs.toString();
                System.out.print("=");
                
                Type idType = top.get(lhs).type;
                if((idType.width == 8) && (rhs.tag == Tag.NUM)) {
                    //Widdnening
                } else if ((idType.width == 4) && (rhs.tag == Tag.REAL)) {
                    throw new Error("Semantic error");
                }
                
                System.out.println(wLHS.value);                
            } else {
                nextSymbol = lookahead;
                lookahead = tempID;
                backtracted = true;
                E();
            }
            
        }
    }
    
    void E() throws IOException {
        T();
        E1();
    }
    
    void E1() throws IOException {
        if (lookahead.tag == '+') {
            match(new Token('+'));
            T();
            E1();
            System.out.print("+");
        }    
        //else nothing
    }
    
    void T() throws IOException {
        F();
        T1();
    }
    
    void T1() throws IOException {
        if (lookahead.tag == '*') {
            match(new Token('*'));
            F();
            T1();
            System.out.print("*");
        }    
        //else nothing
    }
    
    void F() throws IOException  {
        if (lookahead.tag == '(') {
            match(new Token('('));
            E();
            match(new Token(')'));
        } else if((lookahead.tag == Tag.NUM) || (lookahead.tag == Tag.REAL)) {
            System.out.print(lookahead.toString());
            stack.push(lookahead);
            match(new Token(Tag.NUM));
        }else if(lookahead.tag == Tag.ID) {
            System.out.print(lookahead.toString());
            match(new Token(Tag.ID));
        }
    }
    
    /**
     * 
     * @param token 
     */
    void match(Token token) throws IOException {
        if (lookahead.tag == token.tag) {
//            System.out.print(lookahead.toString());
            if (!backtracted)
                lookahead = lexer.scan();
            else
                lookahead = nextSymbol;
        } else {
            throw new Error("syntax error");
        }
    }
    
}
