package semantico.dialecto;

import enums.TipoDato;

import java.util.Map;


public abstract class DialectoBase implements Dialecto {

    protected abstract Map<String, TipoDato> diccionarioDeTipos();

    protected abstract Map<TipoDato, String> nombresDeTipos();

    @Override
    public TipoDato tipoPrimitivo(String nombreEnElLenguaje) {

        if (nombreEnElLenguaje == null) {
            return null;
        }

        return diccionarioDeTipos().get(nombreEnElLenguaje);
    }

    @Override
    public String nombrarTipo(TipoDato tipoDato) {

        if (tipoDato == null) {
            return "?";
        }

        return nombresDeTipos().getOrDefault(tipoDato, tipoDato.name().toLowerCase());
    }

    @Override
    public boolean permiteModulo() {
        return false;
    }

    @Override
    public boolean asignable(TipoDato esperado, TipoDato real) {

        if (esperado == null || real == null) {
            return false;
        }

        if (esperado == real) {
            return true;
        }

        // Promoción implícita: un entero cabe donde se espera un decimal.
        if (esperado == TipoDato.DECIMAL && real == TipoDato.ENTERO) {
            return true;
        }

        // Un caracter cabe donde se espera un entero
        if (esperado == TipoDato.ENTERO && real == TipoDato.CARACTER) {
            return true;
        }

        return false;
    }

    @Override
    public TipoDato resultadoDe(String operador, TipoDato izquierda, TipoDato derecha) {

        if (operador == null || izquierda == null || derecha == null) {
            return null;
        }

        return switch (operador) {

            case "+" -> suma(izquierda, derecha);

            case "-", "*", "/" -> aritmetica(izquierda, derecha);

            case "%" -> permiteModulo() && izquierda == TipoDato.ENTERO && derecha == TipoDato.ENTERO
                    ? TipoDato.ENTERO
                    : null;

            /*
             * Relacionales de orden: solo tienen sentido entre cosas
             * ordenables (números y caracteres).
             */
            case "<", ">", "<=", ">=" -> ordenable(izquierda) && ordenable(derecha)
                    ? TipoDato.BOOLEANO
                    : null;

            /*
             * Igualdad: se permite entre tipos compatibles en cualquier
             * dirección, y contra null para tipos por referencia.
             */
            case "==", "!=" -> comparables(izquierda, derecha)
                    ? TipoDato.BOOLEANO
                    : null;

            case "&&", "||" -> izquierda == TipoDato.BOOLEANO && derecha == TipoDato.BOOLEANO
                    ? TipoDato.BOOLEANO
                    : null;

            default -> null;
        };
    }


    private TipoDato suma(TipoDato izquierda, TipoDato derecha) {

        if (izquierda == TipoDato.TEXTO || derecha == TipoDato.TEXTO) {
            return TipoDato.TEXTO;
        }

        return aritmetica(izquierda, derecha);
    }

    private TipoDato aritmetica(TipoDato izquierda, TipoDato derecha) {

        if (!ordenable(izquierda) || !ordenable(derecha)) {
            return null;
        }

        if (izquierda == TipoDato.DECIMAL || derecha == TipoDato.DECIMAL) {
            return TipoDato.DECIMAL;
        }

        return TipoDato.ENTERO;
    }

    private boolean ordenable(TipoDato tipoDato) {
        return tipoDato == TipoDato.ENTERO
                || tipoDato == TipoDato.DECIMAL
                || tipoDato == TipoDato.CARACTER;
    }

    private boolean comparables(TipoDato izquierda, TipoDato derecha) {

        if (izquierda == derecha) {
            return true;
        }

        if (ordenable(izquierda) && ordenable(derecha)) {
            return true;
        }

        // Comparar un objeto/estructura contra null es válido.
        return izquierda.esReferencia() || derecha.esReferencia();
    }
}