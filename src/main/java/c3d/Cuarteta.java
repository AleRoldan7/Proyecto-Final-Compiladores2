package c3d;

import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cuarteta {

    private final String operador;
    private final String arg1;
    private final String arg2;
    private final String resultado;
    private final TipoDato tipoDeclarado;

    public Cuarteta(String operador, String arg1, String arg2, String resultado, TipoDato tipoDeclarado) {
        this.operador = operador;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.resultado = resultado;
        this.tipoDeclarado = tipoDeclarado;
    }

    @Override
    public String toString() {
        return switch (operador) {
            case "label"    -> resultado + ":";
            case "goto"     -> "goto " + resultado;
            case "if_true"  -> "if " + arg1 + " goto " + resultado;
            case "if_false" -> "if_false " + arg1 + " goto " + resultado;
            case "="        -> resultado + " = " + arg1;
            case "fbegin"  -> "fbegin " + arg1;
            case "fend"    -> "fend " + arg1;
            case "fparam"  -> "fparam " + arg1;
            case "ldecl"   -> "local " + arg1 + " : " + tipoDeclarado;
            case "gdecl"   -> "global " + arg1 + " : " + tipoDeclarado;
            case "call"    -> (resultado == null || "-".equals(resultado))
                    ? "call " + arg1 + ", " + arg2
                    : resultado + " = call " + arg1 + ", " + arg2;
            case "param"    -> "param " + arg1;
            case "return"   -> arg1 == null ? "return" : "return " + arg1;
            case "print"    -> "print " + arg1;
            case "read"     -> resultado + " = read";
            case "halt"     -> "halt";

            case "neg"      -> resultado + " = -" + arg1;
            case "!"      -> resultado + " = !" + arg1;

            case "index_get" -> resultado + " = " + arg1 + "[" + arg2 + "]";
            case "index_set" -> resultado + "[" + arg1 + "] = " + arg2;

            case "new"      -> resultado + " = new " + arg1;

            case "param_decl"  -> "param " + arg1 + " " + arg2;
            case "new_array"   -> resultado + " = new " + arg1 + "[" + arg2 + "]";
            case "field_set"   -> resultado + "." + arg1 + " = " + arg2;
            case "attr_get"    -> resultado + " = " + arg1 + "." + arg2;

            case "if_<"   -> "if " + arg1 + " < " + arg2 + " goto " + resultado;
            case "if_>"   -> "if " + arg1 + " > " + arg2 + " goto " + resultado;
            case "if_<="  -> "if " + arg1 + " <= " + arg2 + " goto " + resultado;
            case "if_>="  -> "if " + arg1 + " >= " + arg2 + " goto " + resultado;
            case "if_=="  -> "if " + arg1 + " == " + arg2 + " goto " + resultado;
            case "if_!="  -> "if " + arg1 + " != " + arg2 + " goto " + resultado;

            case "comment" -> "// " + resultado;

            default -> resultado + " = " + arg1 + " " + operador + " " + arg2;
        };
    }
}