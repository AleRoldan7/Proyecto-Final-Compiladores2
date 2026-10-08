package assembler.runtime;

import assembler.generador.ASMContexto;

public class RuntimeRiscV {

    public RuntimeRiscV() {
    }

    public static void emitir(ASMContexto asmContexto) {
        asmContexto.crudo("\n# ================= RUNTIME =================\n");
        asmContexto.crudo(STRLEN);
        asmContexto.crudo(CONCAT);
        asmContexto.crudo(NUM_A_TEXTO);
        asmContexto.crudo(TEXTO_IGUAL);
        asmContexto.crudo(LEER_TEXTO);
        asmContexto.crudo(CHAR_A_TEXTO);
        asmContexto.crudo(LEER_CARACTER);
    }

    // a0 = texto -> a0 = longitud
    private static final String STRLEN = """
            rt_strlen:
                mv t0, a0
            rt_strlen_bucle:
                lb t1, 0(t0)
                beqz t1, rt_strlen_fin
                addi t0, t0, 1
                j rt_strlen_bucle
            rt_strlen_fin:
                sub a0, t0, a0
                jr ra

            """;

    // a0 = texto1, a1 = texto2 -> a0 = texto nuevo
    private static final String CONCAT = """
            rt_concat:
                addi sp, sp, -16
                sw ra, 12(sp)
                sw a0, 8(sp)
                sw a1, 4(sp)
                jal ra, rt_strlen
                sw a0, 0(sp)
                lw a0, 4(sp)
                jal ra, rt_strlen
                lw t0, 0(sp)
                add a0, a0, t0
                addi a0, a0, 1
                li a7, 9
                ecall
                mv t3, a0
                lw t0, 8(sp)
            rt_concat_copia1:
                lb t1, 0(t0)
                beqz t1, rt_concat_pasa2
                sb t1, 0(t3)
                addi t0, t0, 1
                addi t3, t3, 1
                j rt_concat_copia1
            rt_concat_pasa2:
                lw t0, 4(sp)
            rt_concat_copia2:
                lb t1, 0(t0)
                sb t1, 0(t3)
                beqz t1, rt_concat_fin
                addi t0, t0, 1
                addi t3, t3, 1
                j rt_concat_copia2
            rt_concat_fin:
                lw ra, 12(sp)
                addi sp, sp, 16
                jr ra

            """;

    // a0 = entero -> a0 = texto
    private static final String NUM_A_TEXTO = """
            rt_num_a_texto:
                addi sp, sp, -16
                sw ra, 12(sp)
                sw a0, 8(sp)
                li a0, 16
                li a7, 9
                ecall
                addi t4, a0, 15
                sb zero, 0(t4)
                lw t0, 8(sp)
                li t5, 0
                bgez t0, rt_num_positivo
                neg t0, t0
                li t5, 1
            rt_num_positivo:
                li t6, 10
                bnez t0, rt_num_digitos
                addi t4, t4, -1
                li t1, 48
                sb t1, 0(t4)
                j rt_num_signo
            rt_num_digitos:
                beqz t0, rt_num_signo
                rem t1, t0, t6
                div t0, t0, t6
                addi t1, t1, 48
                addi t4, t4, -1
                sb t1, 0(t4)
                j rt_num_digitos
            rt_num_signo:
                beqz t5, rt_num_fin
                addi t4, t4, -1
                li t1, 45
                sb t1, 0(t4)
            rt_num_fin:
                mv a0, t4
                lw ra, 12(sp)
                addi sp, sp, 16
                jr ra

            """;

    // a0 = texto1, a1 = texto2 -> a0 = 1 si son iguales, 0 si no
    private static final String TEXTO_IGUAL = """
            rt_texto_igual:
                lb t0, 0(a0)
                lb t1, 0(a1)
                bne t0, t1, rt_texto_distinto
                beqz t0, rt_texto_mismo
                addi a0, a0, 1
                addi a1, a1, 1
                j rt_texto_igual
            rt_texto_mismo:
                li a0, 1
                jr ra
            rt_texto_distinto:
                li a0, 0
                jr ra

            """;

    // -> a0 = texto leído (sin el salto de línea)
    private static final String LEER_TEXTO = """
            rt_leer_texto:
                addi sp, sp, -16
                sw ra, 12(sp)
                li a0, 256
                li a7, 9
                ecall
                sw a0, 8(sp)
                li a1, 256
                li a7, 8
                ecall
                lw a0, 8(sp)
                mv t0, a0
            rt_leer_bucle:
                lb t1, 0(t0)
                beqz t1, rt_leer_fin
                li t2, 10
                beq t1, t2, rt_leer_corta
                addi t0, t0, 1
                j rt_leer_bucle
            rt_leer_corta:
                sb zero, 0(t0)
            rt_leer_fin:
                lw ra, 12(sp)
                addi sp, sp, 16
                jr ra

            """;

    // a0 = carácter -> a0 = texto de 1 letra
    private static final String CHAR_A_TEXTO = """
            rt_char_a_texto:
                mv t1, a0
                li a0, 2
                li a7, 9
                ecall
                sb t1, 0(a0)
                sb zero, 1(a0)
                jr ra

            """;

    // -> a0 = primer carácter de la línea leída (0 si la línea está vacía)
    private static final String LEER_CARACTER = """
            rt_leer_caracter:
                addi sp, sp, -16
                sw ra, 12(sp)
                jal ra, rt_leer_texto
                lb a0, 0(a0)
                lw ra, 12(sp)
                addi sp, sp, 16
                jr ra

            """;
}
