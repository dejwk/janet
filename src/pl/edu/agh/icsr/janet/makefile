include ../../../../../makefile-common

#all: main
all: Parser.java TokenTypes.java EmbeddedParser.java

JAVA_TARGETS= /home/dawid/msc/classes/pl/edu/agh/icsr/j2n/TokenTypes.class /home/dawid/msc/classes/pl/edu/agh/icsr/j2n/YYToken.class /home/dawid/msc/classes/pl/edu/agh/icsr/j2n/YYLocation.class /home/dawid/msc/classes/pl/edu/agh/icsr/j2n/LexException.class /home/dawid/msc/classes/pl/edu/agh/icsr/j2n/ParseException.class /home/dawid/msc/classes/pl/edu/agh/icsr/j2n/CompileException.class /home/dawid/msc/classes/pl/edu/agh/icsr/j2n/InputBuffer.class /home/dawid/msc/classes/pl/edu/agh/icsr/j2n/JavaPreprocessor.class /home/dawid/msc/classes/pl/edu/agh/icsr/j2n/Lexer.class /home/dawid/msc/classes/pl/edu/agh/icsr/j2n/Parser.class
JAVA_SOURCES= TokenTypes.java YYToken.java YYLocation.java LexException.java ParseException.java CompileException.java InputBuffer.java JavaPreprocessor.java Lexer.java Parser.java

janet.tab.c: janet.y
	bison janet.y

Parser.java TokenTypes.java: janet.tab.c Parser.template
	$(JAVA) jbf.jb \
                -YYlex Lexer \
                -YYparse Parser \
                -yyerror parseError \
                -YYtokentypes TokenTypes \
                -YYlocation YYLocation \
                -package pl.edu.agh.icsr.janet \
                -tokentype Object \
                janet.tab.c \
                Parser.template Parser.java \
                yytokentypes.template TokenTypes.java

janet_embedded.tab.c: janet_embedded.y
	bison janet_embedded.y

EmbeddedParser.java: janet_embedded.tab.c EmbeddedParser.template
	$(JAVA) jbf.jb \
                -YYlex Lexer \
                -YYparse EmbeddedParser -implements IParser \
                -yyerror parseError \
                -YYtokentypes TokenTypes \
                -YYlocation YYLocation \
                -package pl.edu.agh.icsr.janet \
                -tokentype Object \
                janet_embedded.tab.c \
                EmbeddedParser.template EmbeddedParser.java


#include ../makefile-javarule

main: $(JAVA_PARSER) $(JAVA_TARGETS)

clean:
	rm -f $(JAVA_TARGETS)
	rm -f *.tab.c















































