package visitor.piton;

import org.antlr.v4.runtime.*;
import compi2.proyectofinal.GrammarPythonLexer;
import java.util.*;

public class LexerIndentacionY implements TokenSource {

    private final GrammarPythonLexer lexerBase;
    private final Deque<Integer> pilaIndentacion = new ArrayDeque<>(List.of(0));
    private final Queue<Token> colaTokens = new LinkedList<>();

    private Integer indentacionPendiente = null;

    public LexerIndentacionY(GrammarPythonLexer lexerBase) {
        this.lexerBase = lexerBase;
    }

    @Override
    public Token nextToken() {

        if (!colaTokens.isEmpty()) {
            return colaTokens.poll();
        }

        Token token = lexerBase.nextToken();

        if (token.getType() == GrammarPythonLexer.NEWLINE) {

            int nivel = contarIndentacion(token.getText());

            indentacionPendiente = nivel;

            CommonToken newline = new CommonToken(token);
            newline.setText("<NEWLINE>");
            return newline;
        }

        if (token.getType() == Token.EOF) {

            if (indentacionPendiente != null) {
                aplicarIndentacion(indentacionPendiente, token);
                indentacionPendiente = null;
                if (!colaTokens.isEmpty()) {
                    colaTokens.add(token);
                    return colaTokens.poll();
                }
            }

            while (pilaIndentacion.peek() > 0) {
                pilaIndentacion.pop();
                colaTokens.add(crearTokenSintetico(GrammarPythonLexer.DEDENT, token));
            }
            colaTokens.add(token);
            return colaTokens.poll();
        }

        if (indentacionPendiente != null) {

            int nivel = indentacionPendiente;
            indentacionPendiente = null;

            aplicarIndentacion(nivel, token);

            colaTokens.add(token);
            return colaTokens.poll();
        }

        return token;
    }

    private void aplicarIndentacion(int nivel, Token origen) {

        int actual = pilaIndentacion.peek();

        if (nivel > actual) {
            pilaIndentacion.push(nivel);
            colaTokens.add(crearTokenSintetico(GrammarPythonLexer.INDENT, origen));
        } else {
            while (nivel < pilaIndentacion.peek()) {
                pilaIndentacion.pop();
                colaTokens.add(crearTokenSintetico(GrammarPythonLexer.DEDENT, origen));
            }
        }
    }

    private int contarIndentacion(String textoNewline) {

        int indentacion = 0;

        for (char c : textoNewline.toCharArray()) {
            if (c == '\t') {
                indentacion += 4;
            } else if (c == ' ') {
                indentacion++;
            }
        }

        return indentacion;
    }

    private CommonToken crearTokenSintetico(int tipo, Token origen) {
        CommonToken t = new CommonToken(origen);
        t.setType(tipo);
        t.setText(tipo == GrammarPythonLexer.INDENT ? "<INDENT>" : "<DEDENT>");
        return t;
    }


    @Override
    public int getLine() {
        return lexerBase.getLine();
    }

    @Override
    public int getCharPositionInLine() {
        return lexerBase.getCharPositionInLine();
    }

    @Override
    public CharStream getInputStream() {
        return lexerBase.getInputStream();
    }

    @Override
    public String getSourceName() {
        return lexerBase.getSourceName();
    }

    @Override
    public void setTokenFactory(TokenFactory<?> factory) {
        lexerBase.setTokenFactory(factory);
    }

    @Override
    public TokenFactory<?> getTokenFactory() {
        return lexerBase.getTokenFactory();
    }
}