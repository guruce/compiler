compiler
========

Using 'recursive descent parser' 


Grammer
-------

P → DL
D→BN; D|BN;
B → int | float
N → N , id | id L→S;L|S;
S → id = E | E
E → E+T | T T→T×F|F
F → ( E ) | num | id
