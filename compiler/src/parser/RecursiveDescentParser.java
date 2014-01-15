
package parser;

import inter.Id;
import java.io.BufferedWriter;
import java.io.FileWriter;
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
    Env top;                    //Symbol table at the top -- here only one
    Stack stack;
    Stack stack3AC;
    int tempCount = 1;
    BufferedWriter output;
    
    public RecursiveDescentParser() throws IOException {
        lexer = new Lexer();
        lookahead = lexer.scan();
        top = new Env(null);
        stack = new Stack();
        stack3AC = new Stack();
        output = new BufferedWriter(new FileWriter("output.txt"));
    }     
    
    public void program() throws IOException {
        System.out.println("Postfix Notation");
        P();
        output.close();
        System.out.println("\n");
        System.out.println("Final Variable status");
        top.printVar();
        System.out.println("\n" + "See output.txt for three address code");
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
            throw new Error("syntax error at line "+lexer.line); // advanced with line number and message
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
            stack.push(lookahead);                  //postfix stack
            stack3AC.push(lookahead.toString());
            match(new Token(Tag.ID));
            System.out.print(tempID.toString()+" ");

            //backtracking part...
            if(lookahead.tag == '=') {              //choose S -> id = E production rule
                match(new Token('='));
                E();
                System.out.print("= ");
                assignOp();
            } else {                                //choose S -> E rule
                nextSymbol = lookahead;
                lookahead = tempID;
                backtracted = true;
                E();
            }
        }
    }
    
    void assignOp() throws IOException {
        Token rhs = (Token) stack.pop();
        Token lhs = (Token) stack.pop();
        String r_3ac = (String)stack3AC.pop();
        String l_3ac = (String)stack3AC.pop();
//        System.out.println(l_3ac + " = " + r_3ac);
        output.write(l_3ac + " = " + r_3ac);
        output.newLine();
        
        Word wLHS = (Word) lhs;
//        wLHS.value = rhs.toString();
        if(top.get(lhs) == null){
            throw new Error("Undefined variable "+lhs.toString()+" at line "+lexer.line);
        } else {
            Type idType = top.get(lhs).type;
            if (rhs.tag == Tag.ID) {
                Word wRHS = (Word) rhs;
                if(top.get(rhs) == null){
                    throw new Error("Undefined variable "+rhs.toString()+" at line "+lexer.line);
                } else {
                    Type rhsIdType = top.get(rhs).type;
                    if (((idType.width == 8) && (rhsIdType.width == 4))
                            || ((idType.width == 8) && (rhsIdType.width == 8))
                            || ((idType.width == 4) && (rhsIdType.width == 4))) {
                        //Widdnening
                        wLHS.value = wRHS.value;
                    } else if ((idType.width == 4) && (rhs.tag == Tag.REAL)) {
                        throw new Error("Type error at line "+lexer.line);
                    }
                }
            } else {    //rhs is a number FLOAT/INT value
                if (((idType.width == 8) && (rhs.tag == Tag.NUM))
                        || ((idType.width == 8) && (rhs.tag == Tag.REAL))
                        || ((idType.width == 4) && (rhs.tag == Tag.NUM))) {
                    //Widdnening
                    wLHS.value = rhs.toString();
                } else if ((idType.width == 4) && (rhs.tag == Tag.REAL)) {
                    throw new Error("Type error at line "+lexer.line);
                }
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
            System.out.print("+ ");
            addOp();
        }    
        //else nothing
    }
    
    // Adding operation for last two elements in the stack, and put the value.
    void addOp() throws IOException {
        Token rhs = (Token)stack.pop();
        Token lhs = (Token)stack.pop();
        String temp = "t"+tempCount;
        tempCount++;
        String r_3ac = (String)stack3AC.pop();
        String l_3ac = (String)stack3AC.pop();
        stack3AC.push(temp);
//        System.out.println(temp + " = " +l_3ac + " * " + r_3ac);
        output.write(temp + " = " +l_3ac + " + " + r_3ac);
        output.newLine();
        
        if((lhs.tag == Tag.ID) && (rhs.tag == Tag.ID)) {
            Type lhsType = top.get(lhs).type;
            Type rhsType = top.get(rhs).type;
            Word wLHS = (Word)lhs;
            Word wRHS = (Word)rhs;
            if ((lhsType.width == 8) || (rhsType.width == 8)) {
                float l = Float.parseFloat(wLHS.value);
                float r = Float.parseFloat(wRHS.value);
                Real result = new Real(l+r);
                stack.push(result);
            } else if((lhsType.width == 4) && (rhsType.width == 4)) {
                int l = Integer.parseInt(wLHS.value);
                int r = Integer.parseInt(wRHS.value);
                Num result = new Num(l+r);
                stack.push((Token)result);
            }
        } else if (lhs.tag == Tag.ID) {
            Type lhsType = top.get(lhs).type;
            Word wLHS = (Word)lhs;
            if((lhsType.width == 8) || (rhs.tag == Tag.REAL)) {
                float l = Float.parseFloat(wLHS.value);
                float r = Float.parseFloat(rhs.toString());
                Real result = new Real(l+r);
                stack.push(result);
            } else if ((lhsType.width == 4) && (rhs.tag == Tag.NUM)) {
                int l = Integer.parseInt(wLHS.value);
                int r = Integer.parseInt(rhs.toString());
                Num result = new Num(l+r);
                stack.push(result);
            }
        } else if (rhs.tag == Tag.ID) {
            Type rhsType = top.get(rhs).type;
            Word wRHS = (Word)rhs;
            if((rhsType.width == 8) || (lhs.tag == Tag.REAL)) {
                float l = Float.parseFloat(lhs.toString());
                float r = Float.parseFloat(wRHS.value);
                Real result = new Real(l+r);
                stack.push(result);
            } else if ((rhsType.width == 4) && (lhs.tag == Tag.NUM)) {
                int l = Integer.parseInt(lhs.toString());
                int r = Integer.parseInt(wRHS.value);
                Num result = new Num(l+r);
                stack.push(result);
            }
        } else {                                        // Both are Num/Real values
            if((rhs.tag == Tag.REAL) || (lhs.tag == Tag.REAL)) {
                float l = Float.parseFloat(lhs.toString());
                float r = Float.parseFloat(rhs.toString());
                Real result = new Real(l+r);
                stack.push(result);
            } else if ((rhs.tag == Tag.NUM) || (lhs.tag == Tag.NUM)) {
                int l = Integer.parseInt(lhs.toString());
                int r = Integer.parseInt(rhs.toString());
                Num result = new Num(l+r);
                stack.push(result);
            }
        }
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
            System.out.print("* ");
            multiOp();
        }    
        //else nothing
    }
    
    void multiOp() throws IOException {
        Token rhs = (Token)stack.pop();
        Token lhs = (Token)stack.pop();
        String temp = "t"+tempCount;
        tempCount++;
        String r_3ac = (String)stack3AC.pop();
        String l_3ac = (String)stack3AC.pop();
        stack3AC.push(temp);
//        System.out.println(temp + " = " +l_3ac + " + " + r_3ac);
        output.write(temp + " = " +l_3ac + " * " + r_3ac);
        output.newLine();
        
        if((lhs.tag == Tag.ID) && (rhs.tag == Tag.ID)) {
            Type lhsType = top.get(lhs).type;
            Type rhsType = top.get(rhs).type;
            Word wLHS = (Word)lhs;
            Word wRHS = (Word)rhs;
            if ((lhsType.width == 8) || (rhsType.width == 8)) {
                float l = Float.parseFloat(wLHS.value);
                float r = Float.parseFloat(wRHS.value);
                Real result = new Real(l*r);
                stack.push(result);
            } else if((lhsType.width == 4) && (rhsType.width == 4)) {
                int l = Integer.parseInt(wLHS.value);
                int r = Integer.parseInt(wRHS.value);
                Num result = new Num(l*r);
                stack.push((Token)result);
            }
        } else if (lhs.tag == Tag.ID) {
            Type lhsType = top.get(lhs).type;
            Word wLHS = (Word)lhs;
            if((lhsType.width == 8) || (rhs.tag == Tag.REAL)) {
                float l = Float.parseFloat(wLHS.value);
                float r = Float.parseFloat(rhs.toString());
                Real result = new Real(l*r);
                stack.push(result);
            } else if ((lhsType.width == 4) && (rhs.tag == Tag.NUM)) {
                int l = Integer.parseInt(wLHS.value);
                int r = Integer.parseInt(rhs.toString());
                Num result = new Num(l*r);
                stack.push(result);
            }
        } else if (rhs.tag == Tag.ID) {
            Type rhsType = top.get(rhs).type;
            Word wRHS = (Word)rhs;
            if((rhsType.width == 8) || (lhs.tag == Tag.REAL)) {
                float l = Float.parseFloat(lhs.toString());
                float r = Float.parseFloat(wRHS.value);
                Real result = new Real(l*r);
                stack.push(result);
            } else if ((rhsType.width == 4) && (lhs.tag == Tag.NUM)) {
                int l = Integer.parseInt(lhs.toString());
                int r = Integer.parseInt(wRHS.value);
                Num result = new Num(l*r);
                stack.push(result);
            }
        } else {                                        // Both are Num/Real values
            if((rhs.tag == Tag.REAL) || (lhs.tag == Tag.REAL)) {
                float l = Float.parseFloat(lhs.toString());
                float r = Float.parseFloat(rhs.toString());
                Real result = new Real(l*r);
                stack.push(result);
            } else if ((rhs.tag == Tag.NUM) || (lhs.tag == Tag.NUM)) {
                int l = Integer.parseInt(lhs.toString());
                int r = Integer.parseInt(rhs.toString());
                Num result = new Num(l*r);
                stack.push(result);
            }
        }
    }
    
    void F() throws IOException  {
        if (lookahead.tag == '(') {
            match(new Token('('));
            E();
            match(new Token(')'));
        } else if((lookahead.tag == Tag.NUM) || (lookahead.tag == Tag.REAL)) {
            System.out.print(lookahead.toString()+" ");
            stack.push(lookahead);
            stack3AC.push(lookahead.toString());
            if(lookahead.tag == Tag.NUM)
                match(new Token(Tag.NUM));
            else
                match(new Token(Tag.REAL));
        }else if(lookahead.tag == Tag.ID) {
            System.out.print(lookahead.toString()+" ");
            stack.push(lookahead);
            stack3AC.push(lookahead.toString());
            match(new Token(Tag.ID));
        }
    }
    
    /**
     * 
     * @param token 
     */
    void match(Token token) throws IOException {
        if (lookahead.tag == token.tag) {
            if (!backtracted)
                lookahead = lexer.scan();
            else
                lookahead = nextSymbol;
        } else {
            throw new Error("syntax error at line "+lexer.line);
        }
    }

}
