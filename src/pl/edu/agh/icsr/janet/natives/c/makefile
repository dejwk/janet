include ../../../../../../../makefile-common

#all: main
all: StatementsParser.java BlockParser.java ExpressionParser.java PureCParser.java TokenTypes.java

c_statements.tab.c: c_statements.y
	bison c_statements.y

c_block.tab.c: c_block.y
	bison c_block.y

c_expression.tab.c: c_expression.y
	bison c_expression.y

c_pure.tab.c: c_pure.y
	bison c_pure.y

StatementsParser.java TokenTypes.java: c_statements.tab.c ../Parser.template makefile
	$(JAVA) jbf.jb \
		-YYlex Lexer \
		-YYparse StatementsParser \
		-yyerror parseError \
		-YYtokentypes TokenTypes \
		-YYlocation YYLocation \
		-package pl.edu.agh.icsr.janet.natives.c \
		-tokentype Object \
		c_statements.tab.c \
		../Parser.template StatementsParser.java \
		../../yytokentypes.template TokenTypes.java

BlockParser.java: c_block.tab.c ../Parser.template makefile
	$(JAVA) jbf.jb \
		-YYlex Lexer \
		-YYparse BlockParser \
		-yyerror parseError \
		-YYtokentypes TokenTypes \
		-YYlocation YYLocation \
		-package pl.edu.agh.icsr.janet.natives.c \
		-tokentype Object \
		c_block.tab.c \
		../Parser.template BlockParser.java

ExpressionParser.java: c_expression.tab.c ../Parser.template makefile
	$(JAVA) jbf.jb \
		-YYlex Lexer \
		-YYparse ExpressionParser \
		-yyerror parseError \
		-YYtokentypes TokenTypes \
		-YYlocation YYLocation \
		-package pl.edu.agh.icsr.janet.natives.c \
		-tokentype Object \
		c_expression.tab.c \
		../Parser.template ExpressionParser.java

PureCParser.java: c_pure.tab.c ../Parser.template makefile
	$(JAVA) jbf.jb \
		-YYlex Lexer \
		-YYparse PureCParser \
		-yyerror parseError \
		-YYtokentypes TokenTypes \
		-YYlocation YYLocation \
		-package pl.edu.agh.icsr.janet.natives.c \
		-tokentype Object \
		c_pure.tab.c \
		../Parser.template PureCParser.java

clean:
	rm StatementsParser.java BlockParser.java ExpressionParser.java PureCParser.java TokenTypes.java
	rm *.tab.c
	mv *~ \#*\# backup















































