/*
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
*/
package assembler.runtime;

import assembler.generador.ASMContexto;

public class RuntimeRiscV {

    private RuntimeRiscV() {
    }

    public static void emitir(ASMContexto asmContexto) {

        // ---- buffers estáticos del runtime ----
        asmContexto.dato("# ---- buffers del runtime ----");
        asmContexto.dato(".align 4");
        asmContexto.dato("rt_buffer: .space 256");
        asmContexto.dato(".align 3");
        asmContexto.dato("rt_char_buffer: .space 16");
        asmContexto.dato(".align 3");
        asmContexto.dato("rt_num_buffer: .space 256");
        asmContexto.dato(".align 2");
        asmContexto.dato("rt_num_offset: .word 0");
        asmContexto.dato(".align 3");
        asmContexto.dato("rt_concat_buffer: .space 8192");
        asmContexto.dato(".align 2");
        asmContexto.dato("rt_concat_offset: .word 0");

        asmContexto.crudo("\n# ================= RUNTIME =================\n");
        asmContexto.crudo(STRLEN);
        asmContexto.crudo(CONCAT);
        asmContexto.crudo(NUM_A_TEXTO);
        asmContexto.crudo(TEXTO_IGUAL);
        asmContexto.crudo(LEER_TEXTO);
        asmContexto.crudo(CHAR_A_TEXTO);
        asmContexto.crudo(LEER_CARACTER);
        asmContexto.crudo(ATOI);
        asmContexto.crudo(ATOF);
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

    // a0 = texto1, a1 = texto2 -> a0 = texto nuevo (buffer circular de 8 KB)
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
            # obtener offset actual
            la t6, rt_concat_offset
            lw t4, 0(t6)
            la t5, rt_concat_buffer
            add t5, t5, t4
            # copiar primer string
            mv t3, t5
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
            # actualizar offset circular
            addi t4, t4, 512
            li t6, 8191
            and t4, t4, t6
            la t6, rt_concat_offset
            sw t4, 0(t6)
            mv a0, t5
            lw ra, 12(sp)
            addi sp, sp, 16
            jr ra

        """;

    // a0 = entero -> a0 = texto (buffer circular de 256 B)
    private static final String NUM_A_TEXTO = """
            rt_num_a_texto:
                addi sp, sp, -16
                sw ra, 12(sp)
                sw a0, 8(sp)
                la t6, rt_num_offset
                lw t5, 0(t6)
                la a0, rt_num_buffer
                add a0, a0, t5
                addi t5, t5, 16
                andi t5, t5, 255
                la t6, rt_num_offset
                sw t5, 0(t6)
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

    // -> a0 = texto leído (buffer estático, sin fugas)
    private static final String LEER_TEXTO = """
            rt_leer_texto:
                addi sp, sp, -16
                sw ra, 12(sp)
                la a0, rt_buffer
                li a1, 256
                li a7, 8
                ecall
                la a0, rt_buffer
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
                la a0, rt_buffer
                lw ra, 12(sp)
                addi sp, sp, 16
                jr ra

            """;

    // a0 = carácter -> a0 = texto de 1 letra (buffer estático)
    private static final String CHAR_A_TEXTO = """
            rt_char_a_texto:
                la t3, rt_char_buffer
                sb a0, 0(t3)
                sb zero, 1(t3)
                mv a0, t3
                jr ra

            """;

    // -> a0 = primer carácter de la línea leída (0 si vacía)
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

    private static final String ATOI = """
        rt_atoi:
            # a0 = string, devuelve a0 = entero
            mv t0, a0
            li a0, 0          # resultado
            li t1, 0          # signo (0 = positivo, 1 = negativo)
            lb t2, 0(t0)
            li t3, 45         # '-'
            bne t2, t3, rt_atoi_digitos
            li t1, 1
            addi t0, t0, 1
        rt_atoi_digitos:
            lb t2, 0(t0)
            beqz t2, rt_atoi_fin
            li t3, 48         # '0'
            blt t2, t3, rt_atoi_fin
            li t3, 57         # '9'
            bgt t2, t3, rt_atoi_fin
            li t3, 48
            sub t2, t2, t3    # t2 = digito
            li t3, 10
            mul a0, a0, t3
            add a0, a0, t2
            addi t0, t0, 1
            j rt_atoi_digitos
        rt_atoi_fin:
            beqz t1, rt_atoi_ok
            neg a0, a0
        rt_atoi_ok:
            jr ra

        """;

    private static final String ATOF = """
        rt_atof:
            # a0 = string, devuelve fa0 = double
            addi sp, sp, -16
            sw ra, 12(sp)
            sw s0, 8(sp)
            sw s1, 4(sp)
            
            mv s0, a0          # s0 = string
            li s1, 0           # s1 = signo (0 = positivo, 1 = negativo)
            
            # Verificar signo
            lb t0, 0(s0)
            li t1, 45          # '-'
            bne t0, t1, rt_atof_int
            li s1, 1
            addi s0, s0, 1
            
        rt_atof_int:
            # Parte entera
            li t2, 0
        rt_atof_int_loop:
            lb t0, 0(s0)
            li t1, 48
            blt t0, t1, rt_atof_check_dot
            li t1, 57
            bgt t0, t1, rt_atof_check_dot
            li t1, 48
            sub t0, t0, t1
            li t1, 10
            mul t2, t2, t1
            add t2, t2, t0
            addi s0, s0, 1
            j rt_atof_int_loop
            
        rt_atof_check_dot:
            fcvt.d.w ft0, t2   # ft0 = parte entera
            
            lb t0, 0(s0)
            li t1, 46          # '.'
            bne t0, t1, rt_atof_sign
            
            addi s0, s0, 1
            
            # Parte fraccionaria
            li t3, 0
            li t4, 0
        rt_atof_frac_loop:
            lb t0, 0(s0)
            li t1, 48
            blt t0, t1, rt_atof_frac_done
            li t1, 57
            bgt t0, t1, rt_atof_frac_done
            li t1, 48
            sub t0, t0, t1
            li t1, 10
            mul t3, t3, t1
            add t3, t3, t0
            addi t4, t4, 1
            addi s0, s0, 1
            j rt_atof_frac_loop
            
        rt_atof_frac_done:
            # Convertir fracción a double
            fcvt.d.w ft1, t3
            li t5, 10
            fcvt.d.w ft2, t5
            li t6, 0
        rt_atof_div_loop:
            bge t6, t4, rt_atof_add
            fdiv.d ft1, ft1, ft2
            addi t6, t6, 1
            j rt_atof_div_loop
            
        rt_atof_add:
            fadd.d ft0, ft0, ft1
            
        rt_atof_sign:
            beqz s1, rt_atof_fin
            fneg.d ft0, ft0
            
        rt_atof_fin:
            fmv.d fa0, ft0
            lw s1, 4(sp)
            lw s0, 8(sp)
            lw ra, 12(sp)
            addi sp, sp, 16
            jr ra

        """;
}