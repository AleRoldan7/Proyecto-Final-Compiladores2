package visitor.piton;

import ast.NodoAST;
import ast.Programa;
import ast.clases.Clase;
import ast.declaraciones.*;
import ast.estructuras.Campo;
import ast.estructuras.Estructura;
import ast.estructuras.InicializacionEstructura;
import ast.expresiones.*;
import ast.sentencias.*;
import ast.tipos.Tipo;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.compi2.proyecto1compiladores2.GrammarPythonBaseVisitor;
import org.compi2.proyecto1compiladores2.GrammarPythonParser;
import semantico.AnalisisContexto;

import java.util.ArrayList;
import java.util.List;



public class VisitorPiton extends GrammarPythonBaseVisitor<NodoAST> {

    private final AnalisisContexto contexto;

    public VisitorPiton(AnalisisContexto contexto) {
        this.contexto = contexto;
    }


    @Override
    public NodoAST visitProgram(GrammarPythonParser.ProgramContext ctx) {

        List<Estructura> estructuras = new ArrayList<>();
        List<Clase> clases = new ArrayList<>();
        List<DeclaracionFuncion> funciones = new ArrayList<>();
        List<Declaracion> declaracionesGlobales = new ArrayList<>();

        if (ctx.seccionEstructuras() != null) {
            for (var e : ctx.seccionEstructuras().declaracionEstructura()) {
                NodoAST nodo = visit(e);
                if (nodo instanceof Estructura est) {
                    estructuras.add(est);
                }
            }
        }

        if (ctx.seccionFunciones() != null) {
            for (var f : ctx.seccionFunciones().declaracionFuncion()) {
                NodoAST nodo = visit(f);
                if (nodo instanceof DeclaracionFuncion func) {
                    funciones.add(func);
                }
            }
        }

        return new Programa(
                linea(ctx),
                columna(ctx),
                List.of(),
                estructuras,
                clases,
                funciones,
                declaracionesGlobales
        );
    }


    @Override
    public NodoAST visitDeclaracionFuncion(GrammarPythonParser.DeclaracionFuncionContext ctx) {

        String nombreFuncion = ctx.ID().getText();

        Tipo tipoRetorno = null;
        if (ctx.tipo() != null) {
            tipoRetorno = construirTipo(ctx.tipo());
        }

        List<Parametro> parametros = new ArrayList<>();

        if (ctx.listaParametros() != null) {

            for (var p : ctx.listaParametros().parametro()) {

                if (p instanceof GrammarPythonParser.ParametroSimpleContext simple) {

                    Tipo tipo = construirTipo(simple.tipo());

                    parametros.add(new Parametro(
                            linea(simple), columna(simple),
                            tipo,
                            simple.ID().getText(),
                            false,
                            false
                    ));

                } else if (p instanceof GrammarPythonParser.ParametroArregloContext arreglo) {

                    Tipo tipoBase = construirTipo(arreglo.tipo());

                    Tipo tipoArreglo = new Tipo(
                            linea(arreglo), columna(arreglo),
                            tipoBase.getNombre(),
                            true,
                            1
                    );

                    parametros.add(new Parametro(
                            linea(arreglo), columna(arreglo),
                            tipoArreglo,
                            arreglo.ID().getText(),
                            false,
                            true
                    ));

                } else if (p instanceof GrammarPythonParser.ParametroEstructuraContext estructura) {

                    Tipo tipo = construirTipo(estructura.tipo());

                    parametros.add(new Parametro(
                            linea(estructura), columna(estructura),
                            tipo,
                            estructura.ID().getText(),
                            false,
                            false
                    ));
                }
            }
        }

        Bloque cuerpoFuncion = (Bloque) visit(ctx.bloque());

        return new DeclaracionFuncion(
                linea(ctx), columna(ctx),
                nombreFuncion,
                tipoRetorno,
                parametros,
                cuerpoFuncion
        );
    }

    /* =========================================================
       DECLARACION DE ESTRUCTURA
       ========================================================= */

    @Override
    public NodoAST visitDeclaracionEstructura(
            GrammarPythonParser.DeclaracionEstructuraContext ctx) {

        List<Campo> campos = new ArrayList<>();

        for (var campo : ctx.campoEstructura()) {

            Tipo tipo = construirTipo(campo.tipo());

            if (campo.dimension() != null) {

                int dimensiones =
                        campo.dimension().CORCHETE_ABRE().size();

                List<Integer> tamanos = new ArrayList<>();

                for (var num : campo.dimension().NUMERO_ENTERO()) {
                    tamanos.add(Integer.parseInt(num.getText()));
                }

                Tipo tipoArreglo = new Tipo(
                        linea(campo),
                        columna(campo),
                        tipo.getNombre(),
                        true,
                        dimensiones,
                        tamanos
                );

                campos.add(new Campo(
                        linea(campo),
                        columna(campo),
                        tipoArreglo,
                        campo.ID().getText()
                ));

            } else {

                campos.add(new Campo(
                        linea(campo),
                        columna(campo),
                        tipo,
                        campo.ID().getText()
                ));
            }
        }

        return new Estructura(
                linea(ctx),
                columna(ctx),
                ctx.ID().getText(),
                campos
        );
    }



    @Override
    public NodoAST visitDeclaracionVariable(GrammarPythonParser.DeclaracionVariableContext ctx) {

        Tipo tipo = construirTipo(ctx.tipo());
        Expresion valorInicial = ctx.expresion() != null
                ? (Expresion) visit(ctx.expresion())
                : null;

        if (ctx.dimension() != null) {


            List<Expresion> dims = new ArrayList<>();
            List<Integer> tamanos = new ArrayList<>();

            for (var num : ctx.dimension().NUMERO_ENTERO()) {
                int valor = Integer.parseInt(num.getText());
                tamanos.add(valor);
                dims.add(new Literal(
                        linea(ctx), columna(ctx),
                        valor,
                        "entero"
                ));
            }

            int numDims = ctx.dimension().CORCHETE_ABRE().size();

            Tipo tipoArreglo = new Tipo(
                    linea(ctx), columna(ctx),
                    tipo.getNombre(),
                    true,
                    numDims

            );

            List<Expresion> valores = new ArrayList<>();
            if (valorInicial instanceof InicializacionEstructura ie) {
                valores = ie.getValores();
            } else if (valorInicial != null) {
                valores.add(valorInicial);
            }

            return new DeclaracionArreglo(
                    linea(ctx), columna(ctx),
                    tipoArreglo,
                    ctx.ID().getText(),
                    dims,
                    valores
            );
        }

        return new DeclaracionVariable(
                linea(ctx), columna(ctx),
                tipo,
                ctx.ID().getText(),
                valorInicial
        );
    }

    /* =========================================================
       EXPRESIONES — OPERADORES BINARIOS
       ========================================================= */

    @Override
    public NodoAST visitExpAditiva(GrammarPythonParser.ExpAditivaContext ctx) {
        return new ExpresionBinaria(
                linea(ctx), columna(ctx),
                (Expresion) visit(ctx.expresion(0)),
                ctx.op.getText(),
                (Expresion) visit(ctx.expresion(1))
        );
    }

    @Override
    public NodoAST visitExpMultiplicativa(GrammarPythonParser.ExpMultiplicativaContext ctx) {
        return new ExpresionBinaria(
                linea(ctx), columna(ctx),
                (Expresion) visit(ctx.expresion(0)),
                ctx.op.getText(),
                (Expresion) visit(ctx.expresion(1))
        );
    }

    @Override
    public NodoAST visitExpRelacional(GrammarPythonParser.ExpRelacionalContext ctx) {
        return new ExpresionBinaria(
                linea(ctx), columna(ctx),
                (Expresion) visit(ctx.expresion(0)),
                ctx.op.getText(),
                (Expresion) visit(ctx.expresion(1))
        );
    }

    @Override
    public NodoAST visitExpIgualdad(GrammarPythonParser.ExpIgualdadContext ctx) {
        return new ExpresionBinaria(
                linea(ctx), columna(ctx),
                (Expresion) visit(ctx.expresion(0)),
                ctx.op.getText(),
                (Expresion) visit(ctx.expresion(1))
        );
    }

    @Override
    public NodoAST visitExpAnd(GrammarPythonParser.ExpAndContext ctx) {
        return new ExpresionBinaria(
                linea(ctx), columna(ctx),
                (Expresion) visit(ctx.expresion(0)),
                "&&",
                (Expresion) visit(ctx.expresion(1))
        );
    }

    @Override
    public NodoAST visitExpOr(GrammarPythonParser.ExpOrContext ctx) {
        return new ExpresionBinaria(
                linea(ctx), columna(ctx),
                (Expresion) visit(ctx.expresion(0)),
                "||",
                (Expresion) visit(ctx.expresion(1))
        );
    }

    /* =========================================================
       EXPRESIONES — UNARIAS
       ========================================================= */

    @Override
    public NodoAST visitExpNegacionLogica(GrammarPythonParser.ExpNegacionLogicaContext ctx) {
        return new ExpresionUnaria(
                linea(ctx), columna(ctx),
                "!",
                (Expresion) visit(ctx.expresion()),
                true
        );
    }

    @Override
    public NodoAST visitExpNegativo(GrammarPythonParser.ExpNegativoContext ctx) {
        return new ExpresionUnaria(
                linea(ctx), columna(ctx),
                "-",
                (Expresion) visit(ctx.expresion()),
                true
        );
    }

    @Override
    public NodoAST visitExpPreIncremento(GrammarPythonParser.ExpPreIncrementoContext ctx) {
        return new ExpresionUnaria(
                linea(ctx), columna(ctx),
                "++",
                new Identificador(linea(ctx), columna(ctx), ctx.ID().getText()),
                true
        );
    }

    @Override
    public NodoAST visitExpPreDecremento(GrammarPythonParser.ExpPreDecrementoContext ctx) {
        return new ExpresionUnaria(
                linea(ctx), columna(ctx),
                "--",
                new Identificador(linea(ctx), columna(ctx), ctx.ID().getText()),
                true
        );
    }

    @Override
    public NodoAST visitExpPostIncremento(GrammarPythonParser.ExpPostIncrementoContext ctx) {
        return new ExpresionUnaria(
                linea(ctx), columna(ctx),
                "++",
                new Identificador(linea(ctx), columna(ctx), ctx.ID().getText()),
                false
        );
    }

    @Override
    public NodoAST visitExpPostDecremento(GrammarPythonParser.ExpPostDecrementoContext ctx) {
        return new ExpresionUnaria(
                linea(ctx), columna(ctx),
                "--",
                new Identificador(linea(ctx), columna(ctx), ctx.ID().getText()),
                false
        );
    }

    /* =========================================================
       EXPRESIONES — PRIMARIAS
       ========================================================= */

    @Override
    public NodoAST visitExpParentesis(GrammarPythonParser.ExpParentesisContext ctx) {
        return visit(ctx.expresion());
    }

    @Override
    public NodoAST visitExpLiteral(GrammarPythonParser.ExpLiteralContext ctx) {
        return construirLiteral(ctx.literal());
    }

    @Override
    public NodoAST visitExpAcceso(GrammarPythonParser.ExpAccesoContext ctx) {
        return visit(ctx.accesoVariable());
    }

    @Override
    public NodoAST visitExpLlamada(GrammarPythonParser.ExpLlamadaContext ctx) {
        return visit(ctx.llamadaFuncion());
    }

    @Override
    public NodoAST visitExpLeer(GrammarPythonParser.ExpLeerContext ctx) {
        return new LlamadaFuncion(
                linea(ctx), columna(ctx),
                "leer",
                List.of()
        );
    }

    @Override
    public NodoAST visitExpListaValores(GrammarPythonParser.ExpListaValoresContext ctx) {

        List<Expresion> valores = new ArrayList<>();

        GrammarPythonParser.ListaValoresContext listaValores = ctx.listaValores();

        if (listaValores != null) {
            for (var expr : listaValores.expresion()) {
                valores.add((Expresion) visit(expr));
            }
        }

        return new InicializacionEstructura(
                linea(ctx), columna(ctx),
                null,
                valores
        );
    }

    /* =========================================================
       LLAMADA A FUNCION
       ========================================================= */

    @Override
    public NodoAST visitLlamadaFuncion(GrammarPythonParser.LlamadaFuncionContext ctx) {

        String nombre = ctx.ID().getText();
        List<Expresion> argumentos = new ArrayList<>();

        if (ctx.listaArgumentos() != null) {
            for (var expr : ctx.listaArgumentos().expresion()) {
                argumentos.add((Expresion) visit(expr));
            }
        }

        return new LlamadaFuncion(
                linea(ctx), columna(ctx),
                nombre,
                argumentos
        );
    }



    @Override
    public NodoAST visitAccesoVariable(GrammarPythonParser.AccesoVariableContext ctx) {

        List<String> ids = new ArrayList<>();
        for (var id : ctx.ID()) {
            ids.add(id.getText());
        }

        // Acceso simple
        if (ids.size() == 1 && ctx.expresion().isEmpty()) {
            return new Identificador(linea(ctx), columna(ctx), ids.get(0));
        }

        // Acceso compuesto
        Expresion actual = new Identificador(linea(ctx), columna(ctx), ids.get(0));

        int idIndex = 1;
        int exprIndex = 0;

        for (int i = 1; i < ctx.getChildCount(); i++) {

            ParseTree hijo = ctx.getChild(i);
            String texto = hijo.getText();

            if (texto.equals(".") && idIndex < ids.size()) {
                actual = new AccesoAtributo(
                        linea(ctx), columna(ctx),
                        actual,
                        ids.get(idIndex++)
                );
            } else if (texto.equals("[") && exprIndex < ctx.expresion().size()) {
                Expresion indice = (Expresion) visit(ctx.expresion().get(exprIndex++));
                actual = new AccesoArreglo(
                        linea(ctx), columna(ctx),
                        actual,
                        List.of(indice)
                );
            }
        }

        return actual;
    }

    @Override
    public NodoAST visitSentAsignacion(GrammarPythonParser.SentAsignacionContext ctx) {
        return visit(ctx.asignacion());
    }

    @Override
    public NodoAST visitAsignacion(GrammarPythonParser.AsignacionContext ctx) {

        Expresion destino = (Expresion) visit(ctx.accesoVariable());
        Expresion valor = (Expresion) visit(ctx.expresion());

        return new Asignacion(
                linea(ctx), columna(ctx),
                destino,
                valor
        );
    }



    @Override
    public NodoAST visitSentIncrDecrPostfijo(GrammarPythonParser.SentIncrDecrPostfijoContext ctx) {

        Expresion destino = (Expresion) visit(ctx.accesoVariable());
        String operador = ctx.INCREMENTO() != null ? "++" : "--";

        return new SentenciaExpresion(
                linea(ctx), columna(ctx),
                new ExpresionUnaria(linea(ctx), columna(ctx), operador, destino, false)
        );
    }

    @Override
    public NodoAST visitSentIncrDecrPrefijo(GrammarPythonParser.SentIncrDecrPrefijoContext ctx) {

        Expresion destino = (Expresion) visit(ctx.accesoVariable());
        String operador = ctx.INCREMENTO() != null ? "++" : "--";

        return new SentenciaExpresion(
                linea(ctx), columna(ctx),
                new ExpresionUnaria(linea(ctx), columna(ctx), operador, destino, true)
        );
    }


    @Override
    public NodoAST visitSentDeclaracionVariable(GrammarPythonParser.SentDeclaracionVariableContext ctx) {
        return visit(ctx.declaracionVariable());
    }

    @Override
    public NodoAST visitSentLlamadaFuncion(GrammarPythonParser.SentLlamadaFuncionContext ctx) {
        return new SentenciaExpresion(
                linea(ctx), columna(ctx),
                (Expresion) visit(ctx.llamadaFuncion())
        );
    }

    @Override
    public NodoAST visitSentImprimir(GrammarPythonParser.SentImprimirContext ctx) {
        return visit(ctx.imprimirStmt());
    }

    @Override
    public NodoAST visitImprimirStmt(GrammarPythonParser.ImprimirStmtContext ctx) {

        List<Expresion> argumentos = new ArrayList<>();
        argumentos.add((Expresion) visit(ctx.expresion()));

        return new SentenciaExpresion(
                linea(ctx), columna(ctx),
                new LlamadaFuncion(linea(ctx), columna(ctx), "imprimir", argumentos)
        );
    }

    @Override
    public NodoAST visitSentLeer(GrammarPythonParser.SentLeerContext ctx) {
        return visit(ctx.leerStmt());
    }

    @Override
    public NodoAST visitLeerStmt(GrammarPythonParser.LeerStmtContext ctx) {
        return new SentenciaExpresion(
                linea(ctx), columna(ctx),
                new LlamadaFuncion(linea(ctx), columna(ctx), "leer", List.of())
        );
    }

    @Override
    public NodoAST visitSentRetorno(GrammarPythonParser.SentRetornoContext ctx) {

        Expresion valor = ctx.expresion() != null
                ? (Expresion) visit(ctx.expresion())
                : null;

        return new SentenciaReturn(linea(ctx), columna(ctx), valor);
    }

    @Override
    public NodoAST visitSentRomper(GrammarPythonParser.SentRomperContext ctx) {
        return new SentenciaBreak(linea(ctx), columna(ctx));
    }

    @Override
    public NodoAST visitSentContinuar(GrammarPythonParser.SentContinuarContext ctx) {
        return new SentenciaContinue(linea(ctx), columna(ctx));
    }

    @Override
    public NodoAST visitSentCondicional(GrammarPythonParser.SentCondicionalContext ctx) {
        return visit(ctx.condicional());
    }

    @Override
    public NodoAST visitSentSwitch(GrammarPythonParser.SentSwitchContext ctx) {
        return visit(ctx.switchCase());
    }

    @Override
    public NodoAST visitSentCicloPara(GrammarPythonParser.SentCicloParaContext ctx) {
        return visit(ctx.cicloPara());
    }

    @Override
    public NodoAST visitSentCicloMientras(GrammarPythonParser.SentCicloMientrasContext ctx) {
        return visit(ctx.cicloMientras());
    }

    @Override
    public NodoAST visitSentEstructuraLocal(GrammarPythonParser.SentEstructuraLocalContext ctx) {
        return visit(ctx.declaracionEstructura());
    }


    @Override
    public NodoAST visitBloque(GrammarPythonParser.BloqueContext ctx) {

        List<Sentencia> sentencias = new ArrayList<>();

        for (var s : ctx.sentencia()) {
            NodoAST nodo = visit(s);
            if (nodo instanceof Sentencia sentencia) {
                sentencias.add(sentencia);
            }
        }

        return new Bloque(linea(ctx), columna(ctx), sentencias);
    }

    @Override
    public NodoAST visitBloqueCaso(GrammarPythonParser.BloqueCasoContext ctx) {

        List<Sentencia> sentencias = new ArrayList<>();

        for (var s : ctx.sentencia()) {
            NodoAST nodo = visit(s);
            if (nodo instanceof Sentencia sentencia) {
                sentencias.add(sentencia);
            }
        }

        return new Bloque(linea(ctx), columna(ctx), sentencias);
    }


    @Override
    public NodoAST visitCondicional(GrammarPythonParser.CondicionalContext ctx) {

        Expresion condicion = (Expresion) visit(ctx.expresion(0));
        Bloque bloqueEntonces = (Bloque) visit(ctx.bloque(0));

        int cantidadExpresiones = ctx.expresion().size();
        int cantidadBloques = ctx.bloque().size();

        int cantidadElseIf = cantidadExpresiones - 1;

        List<CondicionIf> listaElseIf = new ArrayList<>();

        for (int i = 0; i < cantidadElseIf; i++) {

            Expresion condElseIf = (Expresion) visit(ctx.expresion(i + 1));
            Bloque bloqueElseIf = (Bloque) visit(ctx.bloque(i + 1));

            listaElseIf.add(new CondicionIf(
                    linea(ctx), columna(ctx),
                    condElseIf,
                    bloqueElseIf,
                    List.of(),
                    null
            ));
        }

        Bloque bloqueElse = null;
        if (cantidadBloques > cantidadExpresiones) {
            bloqueElse = (Bloque) visit(ctx.bloque(cantidadBloques - 1));
        }

        return new CondicionIf(
                linea(ctx), columna(ctx),
                condicion,
                bloqueEntonces,
                listaElseIf,
                bloqueElse
        );
    }


    @Override
    public NodoAST visitSwitchCase(GrammarPythonParser.SwitchCaseContext ctx) {

        Expresion expresionSwitch = (Expresion) visit(ctx.expresion());

        List<SentenciaCase> casos = new ArrayList<>();

        for (var caso : ctx.casoElegir()) {
            NodoAST nodo = visit(caso);
            if (nodo instanceof SentenciaCase sc) {
                casos.add(sc);
            }
        }

        Bloque bloqueDefault = null;

        if (ctx.SIEMPRE() != null && ctx.bloqueCaso() != null) {
            bloqueDefault = (Bloque) visit(ctx.bloqueCaso());
        }

        return new CondicionSwitch(
                linea(ctx), columna(ctx),
                expresionSwitch,
                casos,
                bloqueDefault
        );
    }

    @Override
    public NodoAST visitCasoElegir(GrammarPythonParser.CasoElegirContext ctx) {

        Expresion valor = construirLiteral(ctx.literal());
        Bloque cuerpo = (Bloque) visit(ctx.bloqueCaso());

        return new SentenciaCase(
                linea(ctx), columna(ctx),
                valor,
                cuerpo
        );
    }


    @Override
    public NodoAST visitCicloPara(GrammarPythonParser.CicloParaContext ctx) {

        Sentencia inicializacion = null;

        if (ctx.declaracionVariable() != null) {
            NodoAST nodo = visit(ctx.declaracionVariable());
            if (nodo instanceof Sentencia sentencia) inicializacion = sentencia;
        } else if (ctx.asignacion() != null) {
            NodoAST nodo = visit(ctx.asignacion());
            if (nodo instanceof Sentencia sentencia) inicializacion = sentencia;
        }

        Expresion condicion = null;
        Expresion incremento = null;

        List<ParseTree> hijos = ctx.children;
        int puntoYComaVistos = 0;

        for (ParseTree hijo : hijos) {

            String texto = hijo.getText();

            if (texto.equals(";")) {
                puntoYComaVistos++;
                continue;
            }

            if (hijo instanceof GrammarPythonParser.ExpresionContext expCtx) {

                Expresion exp = (Expresion) visit(expCtx);

                if (puntoYComaVistos == 0) {

                    condicion = exp;
                } else if (puntoYComaVistos == 1) {

                    condicion = exp;
                } else {
                    incremento = exp;
                }
            }
        }

        Bloque cuerpo = (Bloque) visit(ctx.bloque());

        return new CicloFor(
                linea(ctx), columna(ctx),
                inicializacion,
                condicion,
                incremento,
                cuerpo
        );
    }

    @Override
    public NodoAST visitCicloWhile(GrammarPythonParser.CicloWhileContext ctx) {

        Expresion condicion = (Expresion) visit(ctx.expresion());
        Bloque cuerpo = (Bloque) visit(ctx.bloque());

        return new CicloWhile(
                linea(ctx), columna(ctx),
                condicion,
                cuerpo
        );
    }

    @Override
    public NodoAST visitCicloDoWhile(GrammarPythonParser.CicloDoWhileContext ctx) {

        Bloque cuerpo = (Bloque) visit(ctx.bloque());
        Expresion condicion = (Expresion) visit(ctx.expresion());

        return new CicloDoWhile(
                linea(ctx), columna(ctx),
                cuerpo,
                condicion
        );
    }


    private Literal construirLiteral(GrammarPythonParser.LiteralContext lit) {

        if (lit.NUMERO_ENTERO() != null) {
            return new Literal(
                    linea(lit), columna(lit),
                    Integer.parseInt(lit.getText()),
                    "entero"
            );
        }

        if (lit.DECIMAL() != null) {
            return new Literal(
                    linea(lit), columna(lit),
                    Double.parseDouble(lit.getText()),
                    "flotante"
            );
        }

        if (lit.VERDADERO() != null || lit.FALSO() != null) {
            return new Literal(
                    linea(lit), columna(lit),
                    lit.VERDADERO() != null,
                    "bool"
            );
        }

        if (lit.COMILLASSIMPLES() != null) {
            return new Literal(
                    linea(lit), columna(lit),
                    lit.getText(),
                    "caracter"
            );
        }

        return new Literal(
                linea(lit), columna(lit),
                lit.getText(),
                "cadena"
        );
    }

    private Tipo construirTipo(GrammarPythonParser.TipoContext ctx) {

        if (ctx == null) {
            return null;
        }

        return new Tipo(
                linea(ctx), columna(ctx),
                ctx.getText(),
                false,
                0
        );
    }

    private int linea(ParserRuleContext ctx) {
        return ctx.getStart().getLine();
    }

    private int columna(ParserRuleContext ctx) {
        return ctx.getStart().getCharPositionInLine();
    }
}