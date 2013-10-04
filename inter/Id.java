
package inter;

import lexer.*;
import symbols.*;

public class Id {

    public int offset; // relative address
    public Token op;
    public Type type;

    public Id(Word id, Type p, int b) {
        op = id;
        type = p;
        offset = b;
    }
}
