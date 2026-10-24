.data
heap_ptr: .word 0
.align 3
g_banco: .space 8
.align 3
g_c1: .space 8
.align 3
g_c2: .space 8
.align 3
g_c3: .space 8
.align 3
g_buscada: .space 8
.align 3
g_ok: .space 8
.align 3
g_monto: .space 8
.align 3
g_bitacora: .space 8
.align 3
dbl_0: .double 1000.0
.align 3
dbl_1: .double 0.01
.align 3
dbl_2: .double 100.0
.align 3
dbl_3: .double 1.5
.align 3
dbl_4: .double 0.0
.align 3
dbl_5: .double 0.05
.align 3
dbl_6: .double 500.0
.align 3
dbl_7: .double 2000.0
.align 3
dbl_8: .double 300.0
.align 3
dbl_9: .double 1200.0
.align 3
dbl_10: .double 200.0
.align 3
dbl_11: .double 150.0
.align 3
dbl_12: .double 50.0
str_0: .asciz "===== BITACORA ====="
str_1: .asciz "Movimientos anotados: "
str_2: .asciz "  #"
str_3: .asciz ": "
str_4: .asciz "Total movido: "
str_5: .asciz "Monto invalido"
str_6: .asciz " deposita "
str_7: .asciz " -> saldo "
str_8: .asciz ": fondos insuficientes para retirar "
str_9: .asciz " retira "
str_10: .asciz " intenta transferir "
str_11: .asciz " a "
str_12: .asciz ": sin cambios a fin de mes"
str_13: .asciz "["
str_14: .asciz "] "
str_15: .asciz " ("
str_16: .asciz ") saldo: "
str_17: .asciz " | movimientos: "
str_18: .asciz "Sin titular"
str_19: .asciz "Basica"
str_20: .asciz "=== FIN DE MES ==="
str_21: .asciz "=== RESUMEN DEL BANCO ==="
str_22: .asciz " recibe intereses: "
str_23: .asciz "Ahorro"
str_24: .asciz ": excede el limite de sobregiro, no puede retirar "
str_25: .asciz " (sobregiro permitido) -> saldo "
str_26: .asciz " paga cargo por sobregiro: "
str_27: .asciz ": sin sobregiro este mes"
str_28: .asciz " | limite sobregiro: "
str_29: .asciz "Corriente"
str_30: .asciz "Ana"
str_31: .asciz "Beto"
str_32: .asciz "Carla"
str_33: .asciz "=== BANCO ZETARIANO ==="
str_34: .asciz "--- Depositos iniciales ---"
str_35: .asciz "--- Operaciones ---"
str_36: .asciz "Comision sugerida: "
str_37: .asciz "Retiro rechazado"
str_38: .asciz "Total depositado en el banco: "
str_39: .asciz "--- Busquedas ---"
str_40: .asciz "Cuenta 1002 no encontrada"
str_41: .asciz "Cuenta 1002 pertenece a "
str_42: .asciz "Cuenta 9999 no encontrada"
str_43: .asciz "Cuenta 9999 pertenece a "

.text
.globl _start
_start:
    li t0, -16
    and sp, sp, t0
    li a0, 1048576
    li a7, 9
    ecall
    la t0, heap_ptr
    sw a0, 0(t0)
    jal ra, __init
    jal ra, main
    li a7, 10
    ecall

comision__1:
    addi sp, sp, -48
    sw s0, 44(sp)
    sw ra, 40(sp)
    addi s0, sp, 44
    fsd fa0, -12(s0)
    fld ft0, -12(s0)
    la t6, dbl_0
    fld ft1, 0(t6)
    flt.d t2, ft1, ft0
    sw t2, -16(s0)
    lw t0, -16(s0)
    bnez t0, _skip0
    j .L1
_skip0:
    fld ft0, -12(s0)
    la t6, dbl_1
    fld ft1, 0(t6)
    fmul.d ft2, ft0, ft1
    fsd ft2, -28(s0)
    fld fa0, -28(s0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
    j .L0
.L1:
    fld ft0, -12(s0)
    la t6, dbl_2
    fld ft1, 0(t6)
    flt.d t2, ft1, ft0
    sw t2, -32(s0)
    lw t0, -32(s0)
    bnez t0, _skip1
    j .L2
_skip1:
    la t6, dbl_3
    fld fa0, 0(t6)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
    j .L0
.L2:
    la t6, dbl_4
    fld fa0, 0(t6)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
.L0:
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
anotar__2:
    addi sp, sp, -64
    sw s0, 60(sp)
    sw ra, 56(sp)
    addi s0, sp, 60
    sw a0, -8(s0)
    fsd fa1, -20(s0)
    lw t0, -8(s0)
    lw t1, 0(t0)
    sw t1, -24(s0)
    lw t0, -24(s0)
    li t1, 10
    slt t2, t0, t1
    sw t2, -28(s0)
    lw t0, -28(s0)
    bnez t0, _skip2
    j .L4
_skip2:
    lw t0, -8(s0)
    lw t1, 8(t0)
    sw t1, -32(s0)
    lw t0, -8(s0)
    lw t1, 0(t0)
    sw t1, -36(s0)
    lw t0, -32(s0)
    lw t1, -36(s0)
    slli t1, t1, 3
    add t0, t0, t1
    fld ft0, -20(s0)
    fsd ft0, 0(t0)
    lw t0, -8(s0)
    lw t1, 0(t0)
    sw t1, -40(s0)
    lw t0, -40(s0)
    sw t0, -44(s0)
    lw t0, -40(s0)
    li t1, 1
    add t2, t0, t1
    sw t2, -48(s0)
    lw t0, -8(s0)
    lw t1, -48(s0)
    sw t1, 0(t0)
    j .L3
.L4:
.L3:
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 64
    ret
totalBitacora__1:
    addi sp, sp, -64
    sw s0, 60(sp)
    sw ra, 56(sp)
    addi s0, sp, 60
    sw a0, -8(s0)
    la t6, dbl_4
    fld ft0, 0(t6)
    fsd ft0, -20(s0)
    li t0, 0
    sw t0, -24(s0)
.L5:
    lw t0, -8(s0)
    lw t1, 0(t0)
    sw t1, -28(s0)
    lw t0, -24(s0)
    lw t1, -28(s0)
    bge t0, t1, _skip3
    j .L6
_skip3:
    j .L8
.L6:
    lw t0, -8(s0)
    lw t1, 8(t0)
    sw t1, -32(s0)
    lw t0, -32(s0)
    lw t1, -24(s0)
    slli t1, t1, 3
    add t0, t0, t1
    fld ft0, 0(t0)
    fsd ft0, -44(s0)
    fld ft0, -20(s0)
    fld ft1, -44(s0)
    fadd.d ft2, ft0, ft1
    fsd ft2, -52(s0)
    fld ft0, -52(s0)
    fsd ft0, -20(s0)
.L7:
    lw t0, -24(s0)
    sw t0, -56(s0)
    lw t0, -24(s0)
    li t1, 1
    add t2, t0, t1
    sw t2, -60(s0)
    lw t0, -60(s0)
    sw t0, -24(s0)
    j .L5
.L8:
    fld fa0, -20(s0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 64
    ret
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 64
    ret
imprimirBitacora__1:
    addi sp, sp, -80
    sw s0, 76(sp)
    sw ra, 72(sp)
    addi s0, sp, 76
    sw a0, -8(s0)
    la a0, str_0
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    lw t0, -8(s0)
    lw t1, 0(t0)
    sw t1, -16(s0)
    la a0, str_1
    lw a1, -16(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo4:
    lbu t2, 0(t1)
    beqz t2, _cad_fin5
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo4
_cad_fin5:
    mv t0, a1
    bgez t0, _ent_pos6
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos6:
    mv t1, t4
_ent_dig7:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig7
    addi t5, t4, -1
_ent_inv8:
    bge t1, t5, _ent_invfin9
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv8
_ent_invfin9:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -20(s0)
    lw a0, -20(s0)
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    li t0, 0
    sw t0, -12(s0)
.L9:
    lw t0, -8(s0)
    lw t1, 0(t0)
    sw t1, -24(s0)
    lw t0, -12(s0)
    lw t1, -24(s0)
    bge t0, t1, _skip10
    j .L10
_skip10:
    j .L12
.L10:
    la a0, str_2
    lw a1, -12(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo11:
    lbu t2, 0(t1)
    beqz t2, _cad_fin12
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo11
_cad_fin12:
    mv t0, a1
    bgez t0, _ent_pos13
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos13:
    mv t1, t4
_ent_dig14:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig14
    addi t5, t4, -1
_ent_inv15:
    bge t1, t5, _ent_invfin16
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv15
_ent_invfin16:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -28(s0)
    lw a0, -28(s0)
    la a1, str_3
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo17:
    lbu t2, 0(t1)
    beqz t2, _cad_fin18
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo17
_cad_fin18:
    mv t1, a1
_cad_ciclo19:
    lbu t2, 0(t1)
    beqz t2, _cad_fin20
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo19
_cad_fin20:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -32(s0)
    lw t0, -8(s0)
    lw t1, 8(t0)
    sw t1, -36(s0)
    lw t0, -36(s0)
    lw t1, -12(s0)
    slli t1, t1, 3
    add t0, t0, t1
    fld ft0, 0(t0)
    fsd ft0, -44(s0)
    lw a0, -32(s0)
    fld fa1, -44(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo21:
    lbu t2, 0(t1)
    beqz t2, _cad_fin22
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo21
_cad_fin22:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos23
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos23:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos24
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos24:
    mv t1, t4
_ent_dig25:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig25
    addi t5, t4, -1
_ent_inv26:
    bge t1, t5, _ent_invfin27
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv26
_ent_invfin27:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -48(s0)
    lw a0, -48(s0)
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
.L11:
    lw t0, -12(s0)
    sw t0, -52(s0)
    lw t0, -12(s0)
    li t1, 1
    add t2, t0, t1
    sw t2, -56(s0)
    lw t0, -56(s0)
    sw t0, -12(s0)
    j .L9
.L12:
    lw a0, -8(s0)
    jal ra, totalBitacora__1
    fsd fa0, -68(s0)
    la a0, str_4
    fld fa1, -68(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo28:
    lbu t2, 0(t1)
    beqz t2, _cad_fin29
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo28
_cad_fin29:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos30
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos30:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos31
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos31:
    mv t1, t4
_ent_dig32:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig32
    addi t5, t4, -1
_ent_inv33:
    bge t1, t5, _ent_invfin34
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv33
_ent_invfin34:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -72(s0)
    lw a0, -72(s0)
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 80
    ret
Cuenta_getSaldo__1:
    addi sp, sp, -32
    sw s0, 28(sp)
    sw ra, 24(sp)
    addi s0, sp, 28
    sw a0, -8(s0)
    lw t0, -8(s0)
    fld ft0, 8(t0)
    fsd ft0, -20(s0)
    fld fa0, -20(s0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 32
    ret
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 32
    ret
Cuenta_getTitular__1:
    addi sp, sp, -16
    sw s0, 12(sp)
    sw ra, 8(sp)
    addi s0, sp, 12
    sw a0, -8(s0)
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -12(s0)
    lw a0, -12(s0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 16
    ret
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 16
    ret
Cuenta_getNumero__1:
    addi sp, sp, -16
    sw s0, 12(sp)
    sw ra, 8(sp)
    addi s0, sp, 12
    sw a0, -8(s0)
    lw t0, -8(s0)
    lw t1, 24(t0)
    sw t1, -12(s0)
    lw a0, -12(s0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 16
    ret
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 16
    ret
Cuenta_ajustarSaldo__2:
    addi sp, sp, -48
    sw s0, 44(sp)
    sw ra, 40(sp)
    addi s0, sp, 44
    sw a0, -8(s0)
    fsd fa1, -20(s0)
    lw t0, -8(s0)
    fld ft0, 8(t0)
    fsd ft0, -28(s0)
    fld ft0, -28(s0)
    fld ft1, -20(s0)
    fadd.d ft2, ft0, ft1
    fsd ft2, -36(s0)
    lw t0, -8(s0)
    fld ft0, -36(s0)
    fsd ft0, 8(t0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
Cuenta_depositar__2:
    addi sp, sp, -96
    sw s0, 92(sp)
    sw ra, 88(sp)
    addi s0, sp, 92
    sw a0, -8(s0)
    fsd fa1, -20(s0)
    fld ft0, -20(s0)
    li t5, 0
    fcvt.d.w ft1, t5
    fle.d t2, ft0, ft1
    sw t2, -24(s0)
    lw t0, -24(s0)
    bnez t0, _skip35
    j .L14
_skip35:
    la a0, str_5
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 96
    ret
    j .L13
.L14:
.L13:
    lw t0, -8(s0)
    fld ft0, 8(t0)
    fsd ft0, -36(s0)
    fld ft0, -36(s0)
    fld ft1, -20(s0)
    fadd.d ft2, ft0, ft1
    fsd ft2, -44(s0)
    lw t0, -8(s0)
    fld ft0, -44(s0)
    fsd ft0, 8(t0)
    lw t0, -8(s0)
    lw t1, 40(t0)
    sw t1, -48(s0)
    lw t0, -48(s0)
    sw t0, -52(s0)
    lw t0, -48(s0)
    li t1, 1
    add t2, t0, t1
    sw t2, -56(s0)
    lw t0, -8(s0)
    lw t1, -56(s0)
    sw t1, 40(t0)
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -60(s0)
    lw a0, -60(s0)
    la a1, str_6
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo36:
    lbu t2, 0(t1)
    beqz t2, _cad_fin37
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo36
_cad_fin37:
    mv t1, a1
_cad_ciclo38:
    lbu t2, 0(t1)
    beqz t2, _cad_fin39
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo38
_cad_fin39:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -64(s0)
    lw a0, -64(s0)
    fld fa1, -20(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo40:
    lbu t2, 0(t1)
    beqz t2, _cad_fin41
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo40
_cad_fin41:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos42
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos42:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos43
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos43:
    mv t1, t4
_ent_dig44:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig44
    addi t5, t4, -1
_ent_inv45:
    bge t1, t5, _ent_invfin46
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv45
_ent_invfin46:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -68(s0)
    lw a0, -68(s0)
    la a1, str_7
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo47:
    lbu t2, 0(t1)
    beqz t2, _cad_fin48
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo47
_cad_fin48:
    mv t1, a1
_cad_ciclo49:
    lbu t2, 0(t1)
    beqz t2, _cad_fin50
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo49
_cad_fin50:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -72(s0)
    lw t0, -8(s0)
    fld ft0, 8(t0)
    fsd ft0, -84(s0)
    lw a0, -72(s0)
    fld fa1, -84(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo51:
    lbu t2, 0(t1)
    beqz t2, _cad_fin52
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo51
_cad_fin52:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos53
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos53:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos54
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos54:
    mv t1, t4
_ent_dig55:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig55
    addi t5, t4, -1
_ent_inv56:
    bge t1, t5, _ent_invfin57
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv56
_ent_invfin57:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -88(s0)
    lw a0, -88(s0)
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 96
    ret
Cuenta_retirar__2:
    addi sp, sp, -128
    sw s0, 124(sp)
    sw ra, 120(sp)
    addi s0, sp, 124
    sw a0, -8(s0)
    fsd fa1, -20(s0)
    fld ft0, -20(s0)
    li t5, 0
    fcvt.d.w ft1, t5
    fle.d t2, ft0, ft1
    sw t2, -24(s0)
    lw t0, -8(s0)
    fld ft0, 8(t0)
    fsd ft0, -36(s0)
    fld ft0, -20(s0)
    fld ft1, -36(s0)
    flt.d t2, ft1, ft0
    sw t2, -40(s0)
    lw t0, -24(s0)
    lw t1, -40(s0)
    or t2, t0, t1
    sw t2, -44(s0)
    lw t0, -44(s0)
    bnez t0, _skip58
    j .L16
_skip58:
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -48(s0)
    lw a0, -48(s0)
    la a1, str_8
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo59:
    lbu t2, 0(t1)
    beqz t2, _cad_fin60
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo59
_cad_fin60:
    mv t1, a1
_cad_ciclo61:
    lbu t2, 0(t1)
    beqz t2, _cad_fin62
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo61
_cad_fin62:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -52(s0)
    lw a0, -52(s0)
    fld fa1, -20(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo63:
    lbu t2, 0(t1)
    beqz t2, _cad_fin64
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo63
_cad_fin64:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos65
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos65:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos66
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos66:
    mv t1, t4
_ent_dig67:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig67
    addi t5, t4, -1
_ent_inv68:
    bge t1, t5, _ent_invfin69
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv68
_ent_invfin69:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -56(s0)
    lw a0, -56(s0)
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    li a0, 0
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 128
    ret
    j .L15
.L16:
.L15:
    lw t0, -8(s0)
    fld ft0, 8(t0)
    fsd ft0, -68(s0)
    fld ft0, -68(s0)
    fld ft1, -20(s0)
    fsub.d ft2, ft0, ft1
    fsd ft2, -76(s0)
    lw t0, -8(s0)
    fld ft0, -76(s0)
    fsd ft0, 8(t0)
    lw t0, -8(s0)
    lw t1, 40(t0)
    sw t1, -80(s0)
    lw t0, -80(s0)
    sw t0, -84(s0)
    lw t0, -80(s0)
    li t1, 1
    add t2, t0, t1
    sw t2, -88(s0)
    lw t0, -8(s0)
    lw t1, -88(s0)
    sw t1, 40(t0)
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -92(s0)
    lw a0, -92(s0)
    la a1, str_9
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo70:
    lbu t2, 0(t1)
    beqz t2, _cad_fin71
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo70
_cad_fin71:
    mv t1, a1
_cad_ciclo72:
    lbu t2, 0(t1)
    beqz t2, _cad_fin73
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo72
_cad_fin73:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -96(s0)
    lw a0, -96(s0)
    fld fa1, -20(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo74:
    lbu t2, 0(t1)
    beqz t2, _cad_fin75
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo74
_cad_fin75:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos76
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos76:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos77
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos77:
    mv t1, t4
_ent_dig78:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig78
    addi t5, t4, -1
_ent_inv79:
    bge t1, t5, _ent_invfin80
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv79
_ent_invfin80:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -100(s0)
    lw a0, -100(s0)
    la a1, str_7
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo81:
    lbu t2, 0(t1)
    beqz t2, _cad_fin82
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo81
_cad_fin82:
    mv t1, a1
_cad_ciclo83:
    lbu t2, 0(t1)
    beqz t2, _cad_fin84
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo83
_cad_fin84:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -104(s0)
    lw t0, -8(s0)
    fld ft0, 8(t0)
    fsd ft0, -116(s0)
    lw a0, -104(s0)
    fld fa1, -116(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo85:
    lbu t2, 0(t1)
    beqz t2, _cad_fin86
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo85
_cad_fin86:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos87
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos87:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos88
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos88:
    mv t1, t4
_ent_dig89:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig89
    addi t5, t4, -1
_ent_inv90:
    bge t1, t5, _ent_invfin91
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv90
_ent_invfin91:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -120(s0)
    lw a0, -120(s0)
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    li a0, 1
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 128
    ret
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 128
    ret
Cuenta_transferir__3:
    addi sp, sp, -144
    sw s0, 140(sp)
    sw ra, 136(sp)
    addi s0, sp, 140
    sw a0, -8(s0)
    sw a1, -12(s0)
    fsd fa2, -20(s0)
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -24(s0)
    lw a0, -24(s0)
    la a1, str_10
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo92:
    lbu t2, 0(t1)
    beqz t2, _cad_fin93
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo92
_cad_fin93:
    mv t1, a1
_cad_ciclo94:
    lbu t2, 0(t1)
    beqz t2, _cad_fin95
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo94
_cad_fin95:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -28(s0)
    lw a0, -28(s0)
    fld fa1, -20(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo96:
    lbu t2, 0(t1)
    beqz t2, _cad_fin97
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo96
_cad_fin97:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos98
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos98:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos99
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos99:
    mv t1, t4
_ent_dig100:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig100
    addi t5, t4, -1
_ent_inv101:
    bge t1, t5, _ent_invfin102
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv101
_ent_invfin102:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -32(s0)
    lw a0, -32(s0)
    la a1, str_11
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo103:
    lbu t2, 0(t1)
    beqz t2, _cad_fin104
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo103
_cad_fin104:
    mv t1, a1
_cad_ciclo105:
    lbu t2, 0(t1)
    beqz t2, _cad_fin106
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo105
_cad_fin106:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -36(s0)
    lw t0, -12(s0)
    lw t1, 0(t0)
    sw t1, -40(s0)
    lw t0, -40(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -44(s0)
    lw t0, -44(s0)
    bnez t0, _skip107
    j .L18
_skip107:
    lw a0, -12(s0)
    jal ra, Cuenta_getTitular__1
    sw a0, -48(s0)
    lw t0, -48(s0)
    sw t0, -52(s0)
    j .L17
.L18:
    lw t0, -40(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -56(s0)
    lw t0, -56(s0)
    bnez t0, _skip108
    j .L19
_skip108:
    lw a0, -12(s0)
    jal ra, Cuenta_getTitular__1
    sw a0, -60(s0)
    lw t0, -60(s0)
    sw t0, -52(s0)
    j .L17
.L19:
    lw t0, -40(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -64(s0)
    lw t0, -64(s0)
    bnez t0, _skip109
    j .L20
_skip109:
    lw a0, -12(s0)
    jal ra, Cuenta_getTitular__1
    sw a0, -68(s0)
    lw t0, -68(s0)
    sw t0, -52(s0)
    j .L17
.L20:
    lw a0, -12(s0)
    jal ra, Cuenta_getTitular__1
    sw a0, -72(s0)
    lw t0, -72(s0)
    sw t0, -52(s0)
.L17:
    lw a0, -36(s0)
    lw a1, -52(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo110:
    lbu t2, 0(t1)
    beqz t2, _cad_fin111
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo110
_cad_fin111:
    mv t1, a1
_cad_ciclo112:
    lbu t2, 0(t1)
    beqz t2, _cad_fin113
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo112
_cad_fin113:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -76(s0)
    lw a0, -76(s0)
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    lw t0, -8(s0)
    lw t1, 0(t0)
    sw t1, -80(s0)
    lw t0, -80(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -84(s0)
    lw t0, -84(s0)
    bnez t0, _skip114
    j .L24
_skip114:
    lw a0, -8(s0)
    fld fa1, -20(s0)
    jal ra, Cuenta_retirar__2
    sw a0, -88(s0)
    lw t0, -88(s0)
    sw t0, -92(s0)
    j .L23
.L24:
    lw t0, -80(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -96(s0)
    lw t0, -96(s0)
    bnez t0, _skip115
    j .L25
_skip115:
    lw a0, -8(s0)
    fld fa1, -20(s0)
    jal ra, Cuenta_retirar__2
    sw a0, -100(s0)
    lw t0, -100(s0)
    sw t0, -92(s0)
    j .L23
.L25:
    lw t0, -80(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -104(s0)
    lw t0, -104(s0)
    bnez t0, _skip116
    j .L26
_skip116:
    lw a0, -8(s0)
    fld fa1, -20(s0)
    jal ra, CuentaCorriente_retirar__2
    sw a0, -108(s0)
    lw t0, -108(s0)
    sw t0, -92(s0)
    j .L23
.L26:
    lw a0, -8(s0)
    fld fa1, -20(s0)
    jal ra, Cuenta_retirar__2
    sw a0, -112(s0)
    lw t0, -112(s0)
    sw t0, -92(s0)
.L23:
    lw t0, -92(s0)
    bnez t0, _skip117
    j .L22
_skip117:
    lw t0, -12(s0)
    lw t1, 0(t0)
    sw t1, -116(s0)
    lw t0, -116(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -120(s0)
    lw t0, -120(s0)
    bnez t0, _skip118
    j .L28
_skip118:
    lw a0, -12(s0)
    fld fa1, -20(s0)
    jal ra, Cuenta_depositar__2
    j .L27
.L28:
    lw t0, -116(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -124(s0)
    lw t0, -124(s0)
    bnez t0, _skip119
    j .L29
_skip119:
    lw a0, -12(s0)
    fld fa1, -20(s0)
    jal ra, Cuenta_depositar__2
    j .L27
.L29:
    lw t0, -116(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -128(s0)
    lw t0, -128(s0)
    bnez t0, _skip120
    j .L30
_skip120:
    lw a0, -12(s0)
    fld fa1, -20(s0)
    jal ra, Cuenta_depositar__2
    j .L27
.L30:
    lw a0, -12(s0)
    fld fa1, -20(s0)
    jal ra, Cuenta_depositar__2
.L27:
    li a0, 1
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 144
    ret
    j .L21
.L22:
.L21:
    li a0, 0
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 144
    ret
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 144
    ret
Cuenta_finDeMes__1:
    addi sp, sp, -32
    sw s0, 28(sp)
    sw ra, 24(sp)
    addi s0, sp, 28
    sw a0, -8(s0)
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -12(s0)
    lw a0, -12(s0)
    la a1, str_12
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo121:
    lbu t2, 0(t1)
    beqz t2, _cad_fin122
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo121
_cad_fin122:
    mv t1, a1
_cad_ciclo123:
    lbu t2, 0(t1)
    beqz t2, _cad_fin124
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo123
_cad_fin124:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -16(s0)
    lw a0, -16(s0)
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 32
    ret
Cuenta_describir__1:
    addi sp, sp, -80
    sw s0, 76(sp)
    sw ra, 72(sp)
    addi s0, sp, 76
    sw a0, -8(s0)
    lw t0, -8(s0)
    lw t1, 24(t0)
    sw t1, -12(s0)
    la a0, str_13
    lw a1, -12(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo125:
    lbu t2, 0(t1)
    beqz t2, _cad_fin126
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo125
_cad_fin126:
    mv t0, a1
    bgez t0, _ent_pos127
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos127:
    mv t1, t4
_ent_dig128:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig128
    addi t5, t4, -1
_ent_inv129:
    bge t1, t5, _ent_invfin130
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv129
_ent_invfin130:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -16(s0)
    lw a0, -16(s0)
    la a1, str_14
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo131:
    lbu t2, 0(t1)
    beqz t2, _cad_fin132
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo131
_cad_fin132:
    mv t1, a1
_cad_ciclo133:
    lbu t2, 0(t1)
    beqz t2, _cad_fin134
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo133
_cad_fin134:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -20(s0)
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -24(s0)
    lw a0, -20(s0)
    lw a1, -24(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo135:
    lbu t2, 0(t1)
    beqz t2, _cad_fin136
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo135
_cad_fin136:
    mv t1, a1
_cad_ciclo137:
    lbu t2, 0(t1)
    beqz t2, _cad_fin138
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo137
_cad_fin138:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -28(s0)
    lw a0, -28(s0)
    la a1, str_15
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo139:
    lbu t2, 0(t1)
    beqz t2, _cad_fin140
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo139
_cad_fin140:
    mv t1, a1
_cad_ciclo141:
    lbu t2, 0(t1)
    beqz t2, _cad_fin142
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo141
_cad_fin142:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -32(s0)
    lw t0, -8(s0)
    lw t1, 32(t0)
    sw t1, -36(s0)
    lw a0, -32(s0)
    lw a1, -36(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo143:
    lbu t2, 0(t1)
    beqz t2, _cad_fin144
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo143
_cad_fin144:
    mv t1, a1
_cad_ciclo145:
    lbu t2, 0(t1)
    beqz t2, _cad_fin146
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo145
_cad_fin146:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -40(s0)
    lw a0, -40(s0)
    la a1, str_16
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo147:
    lbu t2, 0(t1)
    beqz t2, _cad_fin148
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo147
_cad_fin148:
    mv t1, a1
_cad_ciclo149:
    lbu t2, 0(t1)
    beqz t2, _cad_fin150
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo149
_cad_fin150:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -44(s0)
    lw t0, -8(s0)
    fld ft0, 8(t0)
    fsd ft0, -52(s0)
    lw a0, -44(s0)
    fld fa1, -52(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo151:
    lbu t2, 0(t1)
    beqz t2, _cad_fin152
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo151
_cad_fin152:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos153
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos153:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos154
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos154:
    mv t1, t4
_ent_dig155:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig155
    addi t5, t4, -1
_ent_inv156:
    bge t1, t5, _ent_invfin157
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv156
_ent_invfin157:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -56(s0)
    lw a0, -56(s0)
    la a1, str_17
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo158:
    lbu t2, 0(t1)
    beqz t2, _cad_fin159
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo158
_cad_fin159:
    mv t1, a1
_cad_ciclo160:
    lbu t2, 0(t1)
    beqz t2, _cad_fin161
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo160
_cad_fin161:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -60(s0)
    lw t0, -8(s0)
    lw t1, 40(t0)
    sw t1, -64(s0)
    lw a0, -60(s0)
    lw a1, -64(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo162:
    lbu t2, 0(t1)
    beqz t2, _cad_fin163
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo162
_cad_fin163:
    mv t0, a1
    bgez t0, _ent_pos164
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos164:
    mv t1, t4
_ent_dig165:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig165
    addi t5, t4, -1
_ent_inv166:
    bge t1, t5, _ent_invfin167
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv166
_ent_invfin167:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -68(s0)
    lw a0, -68(s0)
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 80
    ret
init_Cuenta__1:
    addi sp, sp, -16
    sw s0, 12(sp)
    sw ra, 8(sp)
    addi s0, sp, 12
    sw a0, -8(s0)
    lw t0, -8(s0)
    la t6, dbl_4
    fld ft0, 0(t6)
    fsd ft0, 8(t0)
    lw t0, -8(s0)
    la t1, str_18
    sw t1, 16(t0)
    lw t0, -8(s0)
    li t1, 0
    sw t1, 24(t0)
    lw t0, -8(s0)
    la t1, str_19
    sw t1, 32(t0)
    lw t0, -8(s0)
    li t1, 0
    sw t1, 40(t0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 16
    ret
init_Cuenta__3:
    addi sp, sp, -32
    sw s0, 28(sp)
    sw ra, 24(sp)
    addi s0, sp, 28
    sw a0, -8(s0)
    sw a1, -12(s0)
    sw a2, -16(s0)
    lw t0, -8(s0)
    lw t1, -12(s0)
    sw t1, 16(t0)
    lw t0, -8(s0)
    lw t1, -16(s0)
    sw t1, 24(t0)
    lw t0, -8(s0)
    la t6, dbl_4
    fld ft0, 0(t6)
    fsd ft0, 8(t0)
    lw t0, -8(s0)
    la t1, str_19
    sw t1, 32(t0)
    lw t0, -8(s0)
    li t1, 0
    sw t1, 40(t0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 32
    ret
Banco_agregar__2:
    addi sp, sp, -48
    sw s0, 44(sp)
    sw ra, 40(sp)
    addi s0, sp, 44
    sw a0, -8(s0)
    sw a1, -12(s0)
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -16(s0)
    lw t0, -16(s0)
    li t1, 10
    slt t2, t0, t1
    xori t2, t2, 1
    sw t2, -20(s0)
    lw t0, -20(s0)
    bnez t0, _skip168
    j .L32
_skip168:
    li a0, 0
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
    j .L31
.L32:
.L31:
    lw t0, -8(s0)
    lw t1, 8(t0)
    sw t1, -24(s0)
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -28(s0)
    lw t0, -24(s0)
    lw t1, -28(s0)
    slli t1, t1, 2
    add t0, t0, t1
    lw t2, -12(s0)
    sw t2, 0(t0)
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -32(s0)
    lw t0, -32(s0)
    sw t0, -36(s0)
    lw t0, -32(s0)
    li t1, 1
    add t2, t0, t1
    sw t2, -40(s0)
    lw t0, -8(s0)
    lw t1, -40(s0)
    sw t1, 16(t0)
    li a0, 1
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
Banco_buscar__2:
    addi sp, sp, -96
    sw s0, 92(sp)
    sw ra, 88(sp)
    addi s0, sp, 92
    sw a0, -8(s0)
    sw a1, -12(s0)
    li t0, 0
    sw t0, -16(s0)
.L33:
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -20(s0)
    lw t0, -16(s0)
    lw t1, -20(s0)
    bge t0, t1, _skip169
    j .L34
_skip169:
    j .L36
.L34:
    lw t0, -8(s0)
    lw t1, 8(t0)
    sw t1, -24(s0)
    lw t0, -24(s0)
    lw t1, -16(s0)
    slli t1, t1, 2
    add t0, t0, t1
    lw t2, 0(t0)
    sw t2, -28(s0)
    lw t0, -28(s0)
    lw t1, 0(t0)
    sw t1, -32(s0)
    lw t0, -32(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -36(s0)
    lw t0, -36(s0)
    bnez t0, _skip170
    j .L40
_skip170:
    lw a0, -28(s0)
    jal ra, Cuenta_getNumero__1
    sw a0, -40(s0)
    lw t0, -40(s0)
    sw t0, -44(s0)
    j .L39
.L40:
    lw t0, -32(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -48(s0)
    lw t0, -48(s0)
    bnez t0, _skip171
    j .L41
_skip171:
    lw a0, -28(s0)
    jal ra, Cuenta_getNumero__1
    sw a0, -52(s0)
    lw t0, -52(s0)
    sw t0, -44(s0)
    j .L39
.L41:
    lw t0, -32(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -56(s0)
    lw t0, -56(s0)
    bnez t0, _skip172
    j .L42
_skip172:
    lw a0, -28(s0)
    jal ra, Cuenta_getNumero__1
    sw a0, -60(s0)
    lw t0, -60(s0)
    sw t0, -44(s0)
    j .L39
.L42:
    lw a0, -28(s0)
    jal ra, Cuenta_getNumero__1
    sw a0, -64(s0)
    lw t0, -64(s0)
    sw t0, -44(s0)
.L39:
    lw t0, -44(s0)
    lw t1, -12(s0)
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -68(s0)
    lw t0, -68(s0)
    bnez t0, _skip173
    j .L38
_skip173:
    lw t0, -8(s0)
    lw t1, 8(t0)
    sw t1, -72(s0)
    lw t0, -72(s0)
    lw t1, -16(s0)
    slli t1, t1, 2
    add t0, t0, t1
    lw t2, 0(t0)
    sw t2, -76(s0)
    lw a0, -76(s0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 96
    ret
    j .L37
.L38:
.L37:
.L35:
    lw t0, -16(s0)
    li t1, 1
    add t2, t0, t1
    sw t2, -80(s0)
    lw t0, -80(s0)
    sw t0, -16(s0)
    j .L33
.L36:
    li a0, 0
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 96
    ret
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 96
    ret
Banco_totalDepositado__1:
    addi sp, sp, -128
    sw s0, 124(sp)
    sw ra, 120(sp)
    addi s0, sp, 124
    sw a0, -8(s0)
    la t6, dbl_4
    fld ft0, 0(t6)
    fsd ft0, -20(s0)
    li t0, 0
    sw t0, -24(s0)
.L43:
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -28(s0)
    lw t0, -24(s0)
    lw t1, -28(s0)
    bge t0, t1, _skip174
    j .L44
_skip174:
    j .L46
.L44:
    lw t0, -8(s0)
    lw t1, 8(t0)
    sw t1, -32(s0)
    lw t0, -32(s0)
    lw t1, -24(s0)
    slli t1, t1, 2
    add t0, t0, t1
    lw t2, 0(t0)
    sw t2, -36(s0)
    lw t0, -36(s0)
    lw t1, 0(t0)
    sw t1, -40(s0)
    lw t0, -40(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -44(s0)
    lw t0, -44(s0)
    bnez t0, _skip175
    j .L48
_skip175:
    lw a0, -36(s0)
    jal ra, Cuenta_getSaldo__1
    fsd fa0, -52(s0)
    fld ft0, -52(s0)
    fsd ft0, -60(s0)
    j .L47
.L48:
    lw t0, -40(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -64(s0)
    lw t0, -64(s0)
    bnez t0, _skip176
    j .L49
_skip176:
    lw a0, -36(s0)
    jal ra, Cuenta_getSaldo__1
    fsd fa0, -76(s0)
    fld ft0, -76(s0)
    fsd ft0, -60(s0)
    j .L47
.L49:
    lw t0, -40(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -80(s0)
    lw t0, -80(s0)
    bnez t0, _skip177
    j .L50
_skip177:
    lw a0, -36(s0)
    jal ra, Cuenta_getSaldo__1
    fsd fa0, -92(s0)
    fld ft0, -92(s0)
    fsd ft0, -60(s0)
    j .L47
.L50:
    lw a0, -36(s0)
    jal ra, Cuenta_getSaldo__1
    fsd fa0, -100(s0)
    fld ft0, -100(s0)
    fsd ft0, -60(s0)
.L47:
    fld ft0, -20(s0)
    fld ft1, -60(s0)
    fadd.d ft2, ft0, ft1
    fsd ft2, -108(s0)
    fld ft0, -108(s0)
    fsd ft0, -20(s0)
.L45:
    lw t0, -24(s0)
    li t1, 1
    add t2, t0, t1
    sw t2, -112(s0)
    lw t0, -112(s0)
    sw t0, -24(s0)
    j .L43
.L46:
    fld fa0, -20(s0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 128
    ret
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 128
    ret
Banco_finDeMes__1:
    addi sp, sp, -48
    sw s0, 44(sp)
    sw ra, 40(sp)
    addi s0, sp, 44
    sw a0, -8(s0)
    la a0, str_20
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    li t0, 0
    sw t0, -12(s0)
.L51:
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -16(s0)
    lw t0, -12(s0)
    lw t1, -16(s0)
    bge t0, t1, _skip178
    j .L52
_skip178:
    j .L54
.L52:
    lw t0, -8(s0)
    lw t1, 8(t0)
    sw t1, -20(s0)
    lw t0, -20(s0)
    lw t1, -12(s0)
    slli t1, t1, 2
    add t0, t0, t1
    lw t2, 0(t0)
    sw t2, -24(s0)
    lw t0, -24(s0)
    lw t1, 0(t0)
    sw t1, -28(s0)
    lw t0, -28(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -32(s0)
    lw t0, -32(s0)
    bnez t0, _skip179
    j .L56
_skip179:
    lw a0, -24(s0)
    jal ra, Cuenta_finDeMes__1
    j .L55
.L56:
    lw t0, -28(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -36(s0)
    lw t0, -36(s0)
    bnez t0, _skip180
    j .L57
_skip180:
    lw a0, -24(s0)
    jal ra, CuentaAhorro_finDeMes__1
    j .L55
.L57:
    lw t0, -28(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -40(s0)
    lw t0, -40(s0)
    bnez t0, _skip181
    j .L58
_skip181:
    lw a0, -24(s0)
    jal ra, CuentaCorriente_finDeMes__1
    j .L55
.L58:
    lw a0, -24(s0)
    jal ra, Cuenta_finDeMes__1
.L55:
.L53:
    lw t0, -12(s0)
    li t1, 1
    add t2, t0, t1
    sw t2, -44(s0)
    lw t0, -44(s0)
    sw t0, -12(s0)
    j .L51
.L54:
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
Banco_imprimirResumen__1:
    addi sp, sp, -48
    sw s0, 44(sp)
    sw ra, 40(sp)
    addi s0, sp, 44
    sw a0, -8(s0)
    la a0, str_21
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    li t0, 0
    sw t0, -12(s0)
.L59:
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -16(s0)
    lw t0, -12(s0)
    lw t1, -16(s0)
    bge t0, t1, _skip182
    j .L60
_skip182:
    j .L62
.L60:
    lw t0, -8(s0)
    lw t1, 8(t0)
    sw t1, -20(s0)
    lw t0, -20(s0)
    lw t1, -12(s0)
    slli t1, t1, 2
    add t0, t0, t1
    lw t2, 0(t0)
    sw t2, -24(s0)
    lw t0, -24(s0)
    lw t1, 0(t0)
    sw t1, -28(s0)
    lw t0, -28(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -32(s0)
    lw t0, -32(s0)
    bnez t0, _skip183
    j .L64
_skip183:
    lw a0, -24(s0)
    jal ra, Cuenta_describir__1
    j .L63
.L64:
    lw t0, -28(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -36(s0)
    lw t0, -36(s0)
    bnez t0, _skip184
    j .L65
_skip184:
    lw a0, -24(s0)
    jal ra, Cuenta_describir__1
    j .L63
.L65:
    lw t0, -28(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -40(s0)
    lw t0, -40(s0)
    bnez t0, _skip185
    j .L66
_skip185:
    lw a0, -24(s0)
    jal ra, CuentaCorriente_describir__1
    j .L63
.L66:
    lw a0, -24(s0)
    jal ra, Cuenta_describir__1
.L63:
.L61:
    lw t0, -12(s0)
    li t1, 1
    add t2, t0, t1
    sw t2, -44(s0)
    lw t0, -44(s0)
    sw t0, -12(s0)
    j .L59
.L62:
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
init_Banco__1:
    addi sp, sp, -16
    sw s0, 12(sp)
    sw ra, 8(sp)
    addi s0, sp, 12
    sw a0, -8(s0)
    li t3, 10
    li t1, 4
    mul t3, t3, t1
    addi t3, t3, 7
    andi t3, t3, -8
    la t6, heap_ptr
    lw a0, 0(t6)
    addi a0, a0, 7
    andi a0, a0, -8
    add t4, a0, t3
    sw t4, 0(t6)
    sw a0, -12(s0)
    lw t0, -8(s0)
    lw t1, -12(s0)
    sw t1, 8(t0)
    lw t0, -8(s0)
    li t1, 0
    sw t1, 16(t0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 16
    ret
CuentaAhorro_finDeMes__1:
    addi sp, sp, -80
    sw s0, 76(sp)
    sw ra, 72(sp)
    addi s0, sp, 76
    sw a0, -8(s0)
    lw a0, -8(s0)
    jal ra, Cuenta_getSaldo__1
    fsd fa0, -28(s0)
    lw t0, -8(s0)
    fld ft0, 48(t0)
    fsd ft0, -36(s0)
    fld ft0, -28(s0)
    fld ft1, -36(s0)
    fmul.d ft2, ft0, ft1
    fsd ft2, -44(s0)
    fld ft0, -44(s0)
    fsd ft0, -20(s0)
    lw a0, -8(s0)
    fld fa1, -20(s0)
    jal ra, Cuenta_ajustarSaldo__2
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -48(s0)
    lw a0, -48(s0)
    la a1, str_22
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo186:
    lbu t2, 0(t1)
    beqz t2, _cad_fin187
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo186
_cad_fin187:
    mv t1, a1
_cad_ciclo188:
    lbu t2, 0(t1)
    beqz t2, _cad_fin189
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo188
_cad_fin189:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -52(s0)
    lw a0, -52(s0)
    fld fa1, -20(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo190:
    lbu t2, 0(t1)
    beqz t2, _cad_fin191
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo190
_cad_fin191:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos192
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos192:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos193
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos193:
    mv t1, t4
_ent_dig194:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig194
    addi t5, t4, -1
_ent_inv195:
    bge t1, t5, _ent_invfin196
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv195
_ent_invfin196:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -56(s0)
    lw a0, -56(s0)
    la a1, str_7
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo197:
    lbu t2, 0(t1)
    beqz t2, _cad_fin198
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo197
_cad_fin198:
    mv t1, a1
_cad_ciclo199:
    lbu t2, 0(t1)
    beqz t2, _cad_fin200
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo199
_cad_fin200:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -60(s0)
    lw a0, -8(s0)
    jal ra, Cuenta_getSaldo__1
    fsd fa0, -68(s0)
    lw a0, -60(s0)
    fld fa1, -68(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo201:
    lbu t2, 0(t1)
    beqz t2, _cad_fin202
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo201
_cad_fin202:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos203
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos203:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos204
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos204:
    mv t1, t4
_ent_dig205:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig205
    addi t5, t4, -1
_ent_inv206:
    bge t1, t5, _ent_invfin207
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv206
_ent_invfin207:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -72(s0)
    lw a0, -72(s0)
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 80
    ret
init_CuentaAhorro__4:
    addi sp, sp, -32
    sw s0, 28(sp)
    sw ra, 24(sp)
    addi s0, sp, 28
    sw a0, -8(s0)
    sw a1, -12(s0)
    sw a2, -16(s0)
    fsd fa3, -28(s0)
    lw t0, -8(s0)
    lw t1, -12(s0)
    sw t1, 16(t0)
    lw t0, -8(s0)
    lw t1, -16(s0)
    sw t1, 24(t0)
    lw t0, -8(s0)
    fld ft0, -28(s0)
    fsd ft0, 48(t0)
    lw t0, -8(s0)
    la t1, str_23
    sw t1, 32(t0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 32
    ret
CuentaCorriente_retirar__2:
    addi sp, sp, -144
    sw s0, 140(sp)
    sw ra, 136(sp)
    addi s0, sp, 140
    sw a0, -8(s0)
    fsd fa1, -20(s0)
    fld ft0, -20(s0)
    li t5, 0
    fcvt.d.w ft1, t5
    fle.d t2, ft0, ft1
    sw t2, -24(s0)
    lw a0, -8(s0)
    jal ra, Cuenta_getSaldo__1
    fsd fa0, -36(s0)
    lw t0, -8(s0)
    fld ft0, 48(t0)
    fsd ft0, -44(s0)
    fld ft0, -36(s0)
    fld ft1, -44(s0)
    fadd.d ft2, ft0, ft1
    fsd ft2, -52(s0)
    fld ft0, -20(s0)
    fld ft1, -52(s0)
    flt.d t2, ft1, ft0
    sw t2, -56(s0)
    lw t0, -24(s0)
    lw t1, -56(s0)
    or t2, t0, t1
    sw t2, -60(s0)
    lw t0, -60(s0)
    bnez t0, _skip208
    j .L68
_skip208:
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -64(s0)
    lw a0, -64(s0)
    la a1, str_24
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo209:
    lbu t2, 0(t1)
    beqz t2, _cad_fin210
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo209
_cad_fin210:
    mv t1, a1
_cad_ciclo211:
    lbu t2, 0(t1)
    beqz t2, _cad_fin212
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo211
_cad_fin212:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -68(s0)
    lw a0, -68(s0)
    fld fa1, -20(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo213:
    lbu t2, 0(t1)
    beqz t2, _cad_fin214
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo213
_cad_fin214:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos215
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos215:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos216
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos216:
    mv t1, t4
_ent_dig217:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig217
    addi t5, t4, -1
_ent_inv218:
    bge t1, t5, _ent_invfin219
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv218
_ent_invfin219:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -72(s0)
    lw a0, -72(s0)
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    li a0, 0
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 144
    ret
    j .L67
.L68:
.L67:
    li t5, 0
    fcvt.d.w ft0, t5
    fld ft1, -20(s0)
    fsub.d ft2, ft0, ft1
    fsd ft2, -84(s0)
    lw a0, -8(s0)
    fld fa1, -84(s0)
    jal ra, Cuenta_ajustarSaldo__2
    lw t0, -8(s0)
    lw t1, 40(t0)
    sw t1, -88(s0)
    lw t0, -88(s0)
    sw t0, -92(s0)
    lw t0, -88(s0)
    li t1, 1
    add t2, t0, t1
    sw t2, -96(s0)
    lw t0, -8(s0)
    lw t1, -96(s0)
    sw t1, 40(t0)
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -100(s0)
    lw a0, -100(s0)
    la a1, str_9
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo220:
    lbu t2, 0(t1)
    beqz t2, _cad_fin221
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo220
_cad_fin221:
    mv t1, a1
_cad_ciclo222:
    lbu t2, 0(t1)
    beqz t2, _cad_fin223
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo222
_cad_fin223:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -104(s0)
    lw a0, -104(s0)
    fld fa1, -20(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo224:
    lbu t2, 0(t1)
    beqz t2, _cad_fin225
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo224
_cad_fin225:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos226
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos226:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos227
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos227:
    mv t1, t4
_ent_dig228:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig228
    addi t5, t4, -1
_ent_inv229:
    bge t1, t5, _ent_invfin230
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv229
_ent_invfin230:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -108(s0)
    lw a0, -108(s0)
    la a1, str_25
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo231:
    lbu t2, 0(t1)
    beqz t2, _cad_fin232
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo231
_cad_fin232:
    mv t1, a1
_cad_ciclo233:
    lbu t2, 0(t1)
    beqz t2, _cad_fin234
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo233
_cad_fin234:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -112(s0)
    lw a0, -8(s0)
    jal ra, Cuenta_getSaldo__1
    fsd fa0, -124(s0)
    lw a0, -112(s0)
    fld fa1, -124(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo235:
    lbu t2, 0(t1)
    beqz t2, _cad_fin236
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo235
_cad_fin236:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos237
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos237:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos238
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos238:
    mv t1, t4
_ent_dig239:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig239
    addi t5, t4, -1
_ent_inv240:
    bge t1, t5, _ent_invfin241
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv240
_ent_invfin241:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -128(s0)
    lw a0, -128(s0)
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    li a0, 1
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 144
    ret
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 144
    ret
CuentaCorriente_finDeMes__1:
    addi sp, sp, -96
    sw s0, 92(sp)
    sw ra, 88(sp)
    addi s0, sp, 92
    sw a0, -8(s0)
    lw a0, -8(s0)
    jal ra, Cuenta_getSaldo__1
    fsd fa0, -28(s0)
    fld ft0, -28(s0)
    li t5, 0
    fcvt.d.w ft1, t5
    flt.d t2, ft0, ft1
    sw t2, -32(s0)
    lw t0, -32(s0)
    bnez t0, _skip242
    j .L70
_skip242:
    lw a0, -8(s0)
    jal ra, Cuenta_getSaldo__1
    fsd fa0, -44(s0)
    fld ft0, -44(s0)
    la t6, dbl_5
    fld ft1, 0(t6)
    fmul.d ft2, ft0, ft1
    fsd ft2, -52(s0)
    fld ft0, -52(s0)
    fsd ft0, -20(s0)
    lw a0, -8(s0)
    fld fa1, -20(s0)
    jal ra, Cuenta_ajustarSaldo__2
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -56(s0)
    lw a0, -56(s0)
    la a1, str_26
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo243:
    lbu t2, 0(t1)
    beqz t2, _cad_fin244
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo243
_cad_fin244:
    mv t1, a1
_cad_ciclo245:
    lbu t2, 0(t1)
    beqz t2, _cad_fin246
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo245
_cad_fin246:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -60(s0)
    lw a0, -60(s0)
    fld fa1, -20(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo247:
    lbu t2, 0(t1)
    beqz t2, _cad_fin248
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo247
_cad_fin248:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos249
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos249:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos250
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos250:
    mv t1, t4
_ent_dig251:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig251
    addi t5, t4, -1
_ent_inv252:
    bge t1, t5, _ent_invfin253
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv252
_ent_invfin253:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -64(s0)
    lw a0, -64(s0)
    la a1, str_7
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo254:
    lbu t2, 0(t1)
    beqz t2, _cad_fin255
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo254
_cad_fin255:
    mv t1, a1
_cad_ciclo256:
    lbu t2, 0(t1)
    beqz t2, _cad_fin257
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo256
_cad_fin257:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -68(s0)
    lw a0, -8(s0)
    jal ra, Cuenta_getSaldo__1
    fsd fa0, -76(s0)
    lw a0, -68(s0)
    fld fa1, -76(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo258:
    lbu t2, 0(t1)
    beqz t2, _cad_fin259
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo258
_cad_fin259:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos260
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos260:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos261
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos261:
    mv t1, t4
_ent_dig262:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig262
    addi t5, t4, -1
_ent_inv263:
    bge t1, t5, _ent_invfin264
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv263
_ent_invfin264:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -80(s0)
    lw a0, -80(s0)
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    j .L69
.L70:
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -84(s0)
    lw a0, -84(s0)
    la a1, str_27
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo265:
    lbu t2, 0(t1)
    beqz t2, _cad_fin266
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo265
_cad_fin266:
    mv t1, a1
_cad_ciclo267:
    lbu t2, 0(t1)
    beqz t2, _cad_fin268
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo267
_cad_fin268:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -88(s0)
    lw a0, -88(s0)
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
.L69:
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 96
    ret
CuentaCorriente_describir__1:
    addi sp, sp, -96
    sw s0, 92(sp)
    sw ra, 88(sp)
    addi s0, sp, 92
    sw a0, -8(s0)
    lw t0, -8(s0)
    lw t1, 24(t0)
    sw t1, -12(s0)
    la a0, str_13
    lw a1, -12(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo269:
    lbu t2, 0(t1)
    beqz t2, _cad_fin270
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo269
_cad_fin270:
    mv t0, a1
    bgez t0, _ent_pos271
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos271:
    mv t1, t4
_ent_dig272:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig272
    addi t5, t4, -1
_ent_inv273:
    bge t1, t5, _ent_invfin274
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv273
_ent_invfin274:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -16(s0)
    lw a0, -16(s0)
    la a1, str_14
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo275:
    lbu t2, 0(t1)
    beqz t2, _cad_fin276
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo275
_cad_fin276:
    mv t1, a1
_cad_ciclo277:
    lbu t2, 0(t1)
    beqz t2, _cad_fin278
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo277
_cad_fin278:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -20(s0)
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -24(s0)
    lw a0, -20(s0)
    lw a1, -24(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo279:
    lbu t2, 0(t1)
    beqz t2, _cad_fin280
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo279
_cad_fin280:
    mv t1, a1
_cad_ciclo281:
    lbu t2, 0(t1)
    beqz t2, _cad_fin282
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo281
_cad_fin282:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -28(s0)
    lw a0, -28(s0)
    la a1, str_15
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo283:
    lbu t2, 0(t1)
    beqz t2, _cad_fin284
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo283
_cad_fin284:
    mv t1, a1
_cad_ciclo285:
    lbu t2, 0(t1)
    beqz t2, _cad_fin286
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo285
_cad_fin286:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -32(s0)
    lw t0, -8(s0)
    lw t1, 32(t0)
    sw t1, -36(s0)
    lw a0, -32(s0)
    lw a1, -36(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo287:
    lbu t2, 0(t1)
    beqz t2, _cad_fin288
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo287
_cad_fin288:
    mv t1, a1
_cad_ciclo289:
    lbu t2, 0(t1)
    beqz t2, _cad_fin290
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo289
_cad_fin290:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -40(s0)
    lw a0, -40(s0)
    la a1, str_16
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo291:
    lbu t2, 0(t1)
    beqz t2, _cad_fin292
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo291
_cad_fin292:
    mv t1, a1
_cad_ciclo293:
    lbu t2, 0(t1)
    beqz t2, _cad_fin294
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo293
_cad_fin294:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -44(s0)
    lw a0, -8(s0)
    jal ra, Cuenta_getSaldo__1
    fsd fa0, -52(s0)
    lw a0, -44(s0)
    fld fa1, -52(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo295:
    lbu t2, 0(t1)
    beqz t2, _cad_fin296
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo295
_cad_fin296:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos297
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos297:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos298
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos298:
    mv t1, t4
_ent_dig299:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig299
    addi t5, t4, -1
_ent_inv300:
    bge t1, t5, _ent_invfin301
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv300
_ent_invfin301:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -56(s0)
    lw a0, -56(s0)
    la a1, str_28
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo302:
    lbu t2, 0(t1)
    beqz t2, _cad_fin303
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo302
_cad_fin303:
    mv t1, a1
_cad_ciclo304:
    lbu t2, 0(t1)
    beqz t2, _cad_fin305
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo304
_cad_fin305:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -60(s0)
    lw t0, -8(s0)
    fld ft0, 48(t0)
    fsd ft0, -68(s0)
    lw a0, -60(s0)
    fld fa1, -68(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo306:
    lbu t2, 0(t1)
    beqz t2, _cad_fin307
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo306
_cad_fin307:
    li t5, 10
    fcvt.d.w ft7, t5
    fmul.d ft7, fa1, ft7
    fcvt.w.d t0, ft7, rtz
    bgez t0, _dec_pos308
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_dec_pos308:
    li t5, 10
    rem t6, t0, t5
    div t0, t0, t5
    mv t0, t0
    bgez t0, _ent_pos309
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos309:
    mv t1, t4
_ent_dig310:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig310
    addi t5, t4, -1
_ent_inv311:
    bge t1, t5, _ent_invfin312
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv311
_ent_invfin312:
    li t5, 46
    sb t5, 0(t4)
    addi t4, t4, 1
    addi t6, t6, 48
    sb t6, 0(t4)
    addi t4, t4, 1
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -72(s0)
    lw a0, -72(s0)
    la a1, str_17
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo313:
    lbu t2, 0(t1)
    beqz t2, _cad_fin314
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo313
_cad_fin314:
    mv t1, a1
_cad_ciclo315:
    lbu t2, 0(t1)
    beqz t2, _cad_fin316
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo315
_cad_fin316:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -76(s0)
    lw t0, -8(s0)
    lw t1, 40(t0)
    sw t1, -80(s0)
    lw a0, -76(s0)
    lw a1, -80(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo317:
    lbu t2, 0(t1)
    beqz t2, _cad_fin318
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo317
_cad_fin318:
    mv t0, a1
    bgez t0, _ent_pos319
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos319:
    mv t1, t4
_ent_dig320:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig320
    addi t5, t4, -1
_ent_inv321:
    bge t1, t5, _ent_invfin322
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv321
_ent_invfin322:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -84(s0)
    lw a0, -84(s0)
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 96
    ret
init_CuentaCorriente__4:
    addi sp, sp, -32
    sw s0, 28(sp)
    sw ra, 24(sp)
    addi s0, sp, 28
    sw a0, -8(s0)
    sw a1, -12(s0)
    sw a2, -16(s0)
    fsd fa3, -28(s0)
    lw t0, -8(s0)
    lw t1, -12(s0)
    sw t1, 16(t0)
    lw t0, -8(s0)
    lw t1, -16(s0)
    sw t1, 24(t0)
    lw t0, -8(s0)
    fld ft0, -28(s0)
    fsd ft0, 48(t0)
    lw t0, -8(s0)
    la t1, str_29
    sw t1, 32(t0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 32
    ret
__init:
    addi sp, sp, -64
    sw s0, 60(sp)
    sw ra, 56(sp)
    addi s0, sp, 60
    la t6, heap_ptr
    lw a0, 0(t6)
    addi a0, a0, 7
    andi a0, a0, -8
    addi t4, a0, 24
    sw t4, 0(t6)
    sw a0, -8(s0)
    lw t0, -8(s0)
    li t1, 4
    sw t1, 0(t0)
    lw a0, -8(s0)
    jal ra, init_Banco__1
    sw a0, -12(s0)
    lw t0, -8(s0)
    la t6, g_banco
    sw t0, 0(t6)
    la t6, heap_ptr
    lw a0, 0(t6)
    addi a0, a0, 7
    andi a0, a0, -8
    addi t4, a0, 56
    sw t4, 0(t6)
    sw a0, -16(s0)
    lw t0, -16(s0)
    li t1, 2
    sw t1, 0(t0)
    lw a0, -16(s0)
    la a1, str_30
    li a2, 1001
    la t6, dbl_5
    fld fa3, 0(t6)
    jal ra, init_CuentaAhorro__4
    sw a0, -20(s0)
    lw t0, -16(s0)
    la t6, g_c1
    sw t0, 0(t6)
    la t6, heap_ptr
    lw a0, 0(t6)
    addi a0, a0, 7
    andi a0, a0, -8
    addi t4, a0, 56
    sw t4, 0(t6)
    sw a0, -24(s0)
    lw t0, -24(s0)
    li t1, 3
    sw t1, 0(t0)
    lw a0, -24(s0)
    la a1, str_31
    li a2, 1002
    la t6, dbl_6
    fld fa3, 0(t6)
    jal ra, init_CuentaCorriente__4
    sw a0, -28(s0)
    lw t0, -24(s0)
    la t6, g_c2
    sw t0, 0(t6)
    la t6, heap_ptr
    lw a0, 0(t6)
    addi a0, a0, 7
    andi a0, a0, -8
    addi t4, a0, 48
    sw t4, 0(t6)
    sw a0, -32(s0)
    lw t0, -32(s0)
    li t1, 1
    sw t1, 0(t0)
    lw a0, -32(s0)
    la a1, str_32
    li a2, 1003
    jal ra, init_Cuenta__3
    sw a0, -36(s0)
    lw t0, -32(s0)
    la t6, g_c3
    sw t0, 0(t6)
    li t0, 0
    la t6, g_buscada
    sw t0, 0(t6)
    li t0, 0
    la t6, g_ok
    sw t0, 0(t6)
    la t6, dbl_4
    fld ft0, 0(t6)
    la t6, g_monto
    fsd ft0, 0(t6)
    li t0, 1
    li t1, 10
    mul t2, t0, t1
    sw t2, -40(s0)
    lw t3, -40(s0)
    li t1, 4
    mul t3, t3, t1
    addi t3, t3, 7
    andi t3, t3, -8
    la t6, heap_ptr
    lw a0, 0(t6)
    addi a0, a0, 7
    andi a0, a0, -8
    add t4, a0, t3
    sw t4, 0(t6)
    sw a0, -44(s0)
    lw t0, -44(s0)
    li t1, 0
    slli t1, t1, 3
    add t0, t0, t1
    la t6, dbl_4
    fld ft0, 0(t6)
    fsd ft0, 0(t0)
    lw t0, -44(s0)
    li t1, 1
    slli t1, t1, 3
    add t0, t0, t1
    la t6, dbl_4
    fld ft0, 0(t6)
    fsd ft0, 0(t0)
    lw t0, -44(s0)
    li t1, 2
    slli t1, t1, 3
    add t0, t0, t1
    la t6, dbl_4
    fld ft0, 0(t6)
    fsd ft0, 0(t0)
    lw t0, -44(s0)
    li t1, 3
    slli t1, t1, 3
    add t0, t0, t1
    la t6, dbl_4
    fld ft0, 0(t6)
    fsd ft0, 0(t0)
    lw t0, -44(s0)
    li t1, 4
    slli t1, t1, 3
    add t0, t0, t1
    la t6, dbl_4
    fld ft0, 0(t6)
    fsd ft0, 0(t0)
    lw t0, -44(s0)
    li t1, 5
    slli t1, t1, 3
    add t0, t0, t1
    la t6, dbl_4
    fld ft0, 0(t6)
    fsd ft0, 0(t0)
    lw t0, -44(s0)
    li t1, 6
    slli t1, t1, 3
    add t0, t0, t1
    la t6, dbl_4
    fld ft0, 0(t6)
    fsd ft0, 0(t0)
    lw t0, -44(s0)
    li t1, 7
    slli t1, t1, 3
    add t0, t0, t1
    la t6, dbl_4
    fld ft0, 0(t6)
    fsd ft0, 0(t0)
    lw t0, -44(s0)
    li t1, 8
    slli t1, t1, 3
    add t0, t0, t1
    la t6, dbl_4
    fld ft0, 0(t6)
    fsd ft0, 0(t0)
    lw t0, -44(s0)
    li t1, 9
    slli t1, t1, 3
    add t0, t0, t1
    la t6, dbl_4
    fld ft0, 0(t6)
    fsd ft0, 0(t0)
    la t6, heap_ptr
    lw a0, 0(t6)
    addi a0, a0, 7
    andi a0, a0, -8
    addi t4, a0, 16
    sw t4, 0(t6)
    sw a0, -48(s0)
    lw t0, -48(s0)
    li t1, 0
    sw t1, 0(t0)
    lw t0, -48(s0)
    lw t1, -44(s0)
    sw t1, 8(t0)
    lw t0, -48(s0)
    la t6, g_bitacora
    sw t0, 0(t6)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 64
    ret
main:
    addi sp, sp, -544
    sw s0, 540(sp)
    sw ra, 536(sp)
    addi s0, sp, 540
    la a0, str_33
    li a7, 4
    ecall
    la t6, g_banco
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -8(s0)
    lw t0, -8(s0)
    li t1, 4
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -12(s0)
    lw t0, -12(s0)
    bnez t0, _skip323
    j .L72
_skip323:
    la t6, g_banco
    lw a0, 0(t6)
    la t6, g_c1
    lw a1, 0(t6)
    jal ra, Banco_agregar__2
    sw a0, -16(s0)
    lw t0, -16(s0)
    sw t0, -20(s0)
    j .L71
.L72:
    la t6, g_banco
    lw a0, 0(t6)
    la t6, g_c1
    lw a1, 0(t6)
    jal ra, Banco_agregar__2
    sw a0, -24(s0)
    lw t0, -24(s0)
    sw t0, -20(s0)
.L71:
    la t6, g_banco
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -28(s0)
    lw t0, -28(s0)
    li t1, 4
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -32(s0)
    lw t0, -32(s0)
    bnez t0, _skip324
    j .L74
_skip324:
    la t6, g_banco
    lw a0, 0(t6)
    la t6, g_c2
    lw a1, 0(t6)
    jal ra, Banco_agregar__2
    sw a0, -36(s0)
    lw t0, -36(s0)
    sw t0, -40(s0)
    j .L73
.L74:
    la t6, g_banco
    lw a0, 0(t6)
    la t6, g_c2
    lw a1, 0(t6)
    jal ra, Banco_agregar__2
    sw a0, -44(s0)
    lw t0, -44(s0)
    sw t0, -40(s0)
.L73:
    la t6, g_banco
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -48(s0)
    lw t0, -48(s0)
    li t1, 4
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -52(s0)
    lw t0, -52(s0)
    bnez t0, _skip325
    j .L76
_skip325:
    la t6, g_banco
    lw a0, 0(t6)
    la t6, g_c3
    lw a1, 0(t6)
    jal ra, Banco_agregar__2
    sw a0, -56(s0)
    lw t0, -56(s0)
    sw t0, -60(s0)
    j .L75
.L76:
    la t6, g_banco
    lw a0, 0(t6)
    la t6, g_c3
    lw a1, 0(t6)
    jal ra, Banco_agregar__2
    sw a0, -64(s0)
    lw t0, -64(s0)
    sw t0, -60(s0)
.L75:
    la a0, str_34
    li a7, 4
    ecall
    la t6, g_c1
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -68(s0)
    lw t0, -68(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -72(s0)
    lw t0, -72(s0)
    bnez t0, _skip326
    j .L78
_skip326:
    la t6, g_c1
    lw a0, 0(t6)
    la t6, dbl_7
    fld fa1, 0(t6)
    jal ra, Cuenta_depositar__2
    j .L77
.L78:
    lw t0, -68(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -76(s0)
    lw t0, -76(s0)
    bnez t0, _skip327
    j .L79
_skip327:
    la t6, g_c1
    lw a0, 0(t6)
    la t6, dbl_7
    fld fa1, 0(t6)
    jal ra, Cuenta_depositar__2
    j .L77
.L79:
    lw t0, -68(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -80(s0)
    lw t0, -80(s0)
    bnez t0, _skip328
    j .L80
_skip328:
    la t6, g_c1
    lw a0, 0(t6)
    la t6, dbl_7
    fld fa1, 0(t6)
    jal ra, Cuenta_depositar__2
    j .L77
.L80:
    la t6, g_c1
    lw a0, 0(t6)
    la t6, dbl_7
    fld fa1, 0(t6)
    jal ra, Cuenta_depositar__2
.L77:
    la t6, g_c2
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -84(s0)
    lw t0, -84(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -88(s0)
    lw t0, -88(s0)
    bnez t0, _skip329
    j .L82
_skip329:
    la t6, g_c2
    lw a0, 0(t6)
    la t6, dbl_8
    fld fa1, 0(t6)
    jal ra, Cuenta_depositar__2
    j .L81
.L82:
    lw t0, -84(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -92(s0)
    lw t0, -92(s0)
    bnez t0, _skip330
    j .L83
_skip330:
    la t6, g_c2
    lw a0, 0(t6)
    la t6, dbl_8
    fld fa1, 0(t6)
    jal ra, Cuenta_depositar__2
    j .L81
.L83:
    lw t0, -84(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -96(s0)
    lw t0, -96(s0)
    bnez t0, _skip331
    j .L84
_skip331:
    la t6, g_c2
    lw a0, 0(t6)
    la t6, dbl_8
    fld fa1, 0(t6)
    jal ra, Cuenta_depositar__2
    j .L81
.L84:
    la t6, g_c2
    lw a0, 0(t6)
    la t6, dbl_8
    fld fa1, 0(t6)
    jal ra, Cuenta_depositar__2
.L81:
    la t6, g_c3
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -100(s0)
    lw t0, -100(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -104(s0)
    lw t0, -104(s0)
    bnez t0, _skip332
    j .L86
_skip332:
    la t6, g_c3
    lw a0, 0(t6)
    la t6, dbl_2
    fld fa1, 0(t6)
    jal ra, Cuenta_depositar__2
    j .L85
.L86:
    lw t0, -100(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -108(s0)
    lw t0, -108(s0)
    bnez t0, _skip333
    j .L87
_skip333:
    la t6, g_c3
    lw a0, 0(t6)
    la t6, dbl_2
    fld fa1, 0(t6)
    jal ra, Cuenta_depositar__2
    j .L85
.L87:
    lw t0, -100(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -112(s0)
    lw t0, -112(s0)
    bnez t0, _skip334
    j .L88
_skip334:
    la t6, g_c3
    lw a0, 0(t6)
    la t6, dbl_2
    fld fa1, 0(t6)
    jal ra, Cuenta_depositar__2
    j .L85
.L88:
    la t6, g_c3
    lw a0, 0(t6)
    la t6, dbl_2
    fld fa1, 0(t6)
    jal ra, Cuenta_depositar__2
.L85:
    la a0, str_35
    li a7, 4
    ecall
    la t6, dbl_6
    fld ft0, 0(t6)
    la t6, g_monto
    fsd ft0, 0(t6)
    la t6, g_c1
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -116(s0)
    lw t0, -116(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -120(s0)
    lw t0, -120(s0)
    bnez t0, _skip335
    j .L90
_skip335:
    la t6, g_c1
    lw a0, 0(t6)
    la t6, g_c2
    lw a1, 0(t6)
    la t6, g_monto
    fld fa2, 0(t6)
    jal ra, Cuenta_transferir__3
    sw a0, -124(s0)
    lw t0, -124(s0)
    sw t0, -128(s0)
    j .L89
.L90:
    lw t0, -116(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -132(s0)
    lw t0, -132(s0)
    bnez t0, _skip336
    j .L91
_skip336:
    la t6, g_c1
    lw a0, 0(t6)
    la t6, g_c2
    lw a1, 0(t6)
    la t6, g_monto
    fld fa2, 0(t6)
    jal ra, Cuenta_transferir__3
    sw a0, -136(s0)
    lw t0, -136(s0)
    sw t0, -128(s0)
    j .L89
.L91:
    lw t0, -116(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -140(s0)
    lw t0, -140(s0)
    bnez t0, _skip337
    j .L92
_skip337:
    la t6, g_c1
    lw a0, 0(t6)
    la t6, g_c2
    lw a1, 0(t6)
    la t6, g_monto
    fld fa2, 0(t6)
    jal ra, Cuenta_transferir__3
    sw a0, -144(s0)
    lw t0, -144(s0)
    sw t0, -128(s0)
    j .L89
.L92:
    la t6, g_c1
    lw a0, 0(t6)
    la t6, g_c2
    lw a1, 0(t6)
    la t6, g_monto
    fld fa2, 0(t6)
    jal ra, Cuenta_transferir__3
    sw a0, -148(s0)
    lw t0, -148(s0)
    sw t0, -128(s0)
.L89:
    lw t0, -128(s0)
    la t6, g_ok
    sw t0, 0(t6)
    la t6, g_ok
    lw t0, 0(t6)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -152(s0)
    lw t0, -152(s0)
    bnez t0, _skip338
    j .L94
_skip338:
    la t6, g_bitacora
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, anotar__2
    sw a0, -156(s0)
    la t6, g_monto
    fld fa0, 0(t6)
    jal ra, comision__1
    fsd fa0, -164(s0)
    la a0, str_36
    li a7, 4
    ecall
    fld fa0, -164(s0)
    li a7, 3
    ecall
    j .L93
.L94:
.L93:
    la t6, dbl_9
    fld ft0, 0(t6)
    la t6, g_monto
    fsd ft0, 0(t6)
    la t6, g_c2
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -168(s0)
    lw t0, -168(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -172(s0)
    lw t0, -172(s0)
    bnez t0, _skip339
    j .L96
_skip339:
    la t6, g_c2
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, Cuenta_retirar__2
    sw a0, -176(s0)
    lw t0, -176(s0)
    sw t0, -180(s0)
    j .L95
.L96:
    lw t0, -168(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -184(s0)
    lw t0, -184(s0)
    bnez t0, _skip340
    j .L97
_skip340:
    la t6, g_c2
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, Cuenta_retirar__2
    sw a0, -188(s0)
    lw t0, -188(s0)
    sw t0, -180(s0)
    j .L95
.L97:
    lw t0, -168(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -192(s0)
    lw t0, -192(s0)
    bnez t0, _skip341
    j .L98
_skip341:
    la t6, g_c2
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, CuentaCorriente_retirar__2
    sw a0, -196(s0)
    lw t0, -196(s0)
    sw t0, -180(s0)
    j .L95
.L98:
    la t6, g_c2
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, Cuenta_retirar__2
    sw a0, -200(s0)
    lw t0, -200(s0)
    sw t0, -180(s0)
.L95:
    lw t0, -180(s0)
    la t6, g_ok
    sw t0, 0(t6)
    la t6, g_ok
    lw t0, 0(t6)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -204(s0)
    lw t0, -204(s0)
    bnez t0, _skip342
    j .L100
_skip342:
    la t6, g_bitacora
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, anotar__2
    sw a0, -208(s0)
    la t6, g_monto
    fld fa0, 0(t6)
    jal ra, comision__1
    fsd fa0, -220(s0)
    la a0, str_36
    li a7, 4
    ecall
    fld fa0, -220(s0)
    li a7, 3
    ecall
    j .L99
.L100:
    la a0, str_37
    li a7, 4
    ecall
.L99:
    la t6, dbl_10
    fld ft0, 0(t6)
    la t6, g_monto
    fsd ft0, 0(t6)
    la t6, g_c2
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -224(s0)
    lw t0, -224(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -228(s0)
    lw t0, -228(s0)
    bnez t0, _skip343
    j .L102
_skip343:
    la t6, g_c2
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, Cuenta_retirar__2
    sw a0, -232(s0)
    lw t0, -232(s0)
    sw t0, -236(s0)
    j .L101
.L102:
    lw t0, -224(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -240(s0)
    lw t0, -240(s0)
    bnez t0, _skip344
    j .L103
_skip344:
    la t6, g_c2
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, Cuenta_retirar__2
    sw a0, -244(s0)
    lw t0, -244(s0)
    sw t0, -236(s0)
    j .L101
.L103:
    lw t0, -224(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -248(s0)
    lw t0, -248(s0)
    bnez t0, _skip345
    j .L104
_skip345:
    la t6, g_c2
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, CuentaCorriente_retirar__2
    sw a0, -252(s0)
    lw t0, -252(s0)
    sw t0, -236(s0)
    j .L101
.L104:
    la t6, g_c2
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, Cuenta_retirar__2
    sw a0, -256(s0)
    lw t0, -256(s0)
    sw t0, -236(s0)
.L101:
    lw t0, -236(s0)
    la t6, g_ok
    sw t0, 0(t6)
    la t6, g_ok
    lw t0, 0(t6)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -260(s0)
    lw t0, -260(s0)
    bnez t0, _skip346
    j .L106
_skip346:
    la t6, g_bitacora
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, anotar__2
    sw a0, -264(s0)
    j .L105
.L106:
    la a0, str_37
    li a7, 4
    ecall
.L105:
    la t6, dbl_11
    fld ft0, 0(t6)
    la t6, g_monto
    fsd ft0, 0(t6)
    la t6, g_c3
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -268(s0)
    lw t0, -268(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -272(s0)
    lw t0, -272(s0)
    bnez t0, _skip347
    j .L108
_skip347:
    la t6, g_c3
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, Cuenta_retirar__2
    sw a0, -276(s0)
    lw t0, -276(s0)
    sw t0, -280(s0)
    j .L107
.L108:
    lw t0, -268(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -284(s0)
    lw t0, -284(s0)
    bnez t0, _skip348
    j .L109
_skip348:
    la t6, g_c3
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, Cuenta_retirar__2
    sw a0, -288(s0)
    lw t0, -288(s0)
    sw t0, -280(s0)
    j .L107
.L109:
    lw t0, -268(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -292(s0)
    lw t0, -292(s0)
    bnez t0, _skip349
    j .L110
_skip349:
    la t6, g_c3
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, CuentaCorriente_retirar__2
    sw a0, -296(s0)
    lw t0, -296(s0)
    sw t0, -280(s0)
    j .L107
.L110:
    la t6, g_c3
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, Cuenta_retirar__2
    sw a0, -300(s0)
    lw t0, -300(s0)
    sw t0, -280(s0)
.L107:
    lw t0, -280(s0)
    la t6, g_ok
    sw t0, 0(t6)
    la t6, g_ok
    lw t0, 0(t6)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -304(s0)
    lw t0, -304(s0)
    bnez t0, _skip350
    j .L112
_skip350:
    la t6, g_bitacora
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, anotar__2
    sw a0, -308(s0)
    j .L111
.L112:
    la a0, str_37
    li a7, 4
    ecall
.L111:
    la t6, dbl_12
    fld ft0, 0(t6)
    la t6, g_monto
    fsd ft0, 0(t6)
    la t6, g_c3
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -312(s0)
    lw t0, -312(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -316(s0)
    lw t0, -316(s0)
    bnez t0, _skip351
    j .L114
_skip351:
    la t6, g_c3
    lw a0, 0(t6)
    la t6, g_c1
    lw a1, 0(t6)
    la t6, g_monto
    fld fa2, 0(t6)
    jal ra, Cuenta_transferir__3
    sw a0, -320(s0)
    lw t0, -320(s0)
    sw t0, -324(s0)
    j .L113
.L114:
    lw t0, -312(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -328(s0)
    lw t0, -328(s0)
    bnez t0, _skip352
    j .L115
_skip352:
    la t6, g_c3
    lw a0, 0(t6)
    la t6, g_c1
    lw a1, 0(t6)
    la t6, g_monto
    fld fa2, 0(t6)
    jal ra, Cuenta_transferir__3
    sw a0, -332(s0)
    lw t0, -332(s0)
    sw t0, -324(s0)
    j .L113
.L115:
    lw t0, -312(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -336(s0)
    lw t0, -336(s0)
    bnez t0, _skip353
    j .L116
_skip353:
    la t6, g_c3
    lw a0, 0(t6)
    la t6, g_c1
    lw a1, 0(t6)
    la t6, g_monto
    fld fa2, 0(t6)
    jal ra, Cuenta_transferir__3
    sw a0, -340(s0)
    lw t0, -340(s0)
    sw t0, -324(s0)
    j .L113
.L116:
    la t6, g_c3
    lw a0, 0(t6)
    la t6, g_c1
    lw a1, 0(t6)
    la t6, g_monto
    fld fa2, 0(t6)
    jal ra, Cuenta_transferir__3
    sw a0, -344(s0)
    lw t0, -344(s0)
    sw t0, -324(s0)
.L113:
    lw t0, -324(s0)
    la t6, g_ok
    sw t0, 0(t6)
    la t6, g_ok
    lw t0, 0(t6)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -348(s0)
    lw t0, -348(s0)
    bnez t0, _skip354
    j .L118
_skip354:
    la t6, g_bitacora
    lw a0, 0(t6)
    la t6, g_monto
    fld fa1, 0(t6)
    jal ra, anotar__2
    sw a0, -352(s0)
    la t6, g_monto
    fld fa0, 0(t6)
    jal ra, comision__1
    fsd fa0, -364(s0)
    la a0, str_36
    li a7, 4
    ecall
    fld fa0, -364(s0)
    li a7, 3
    ecall
    j .L117
.L118:
.L117:
    la t6, g_banco
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -368(s0)
    lw t0, -368(s0)
    li t1, 4
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -372(s0)
    lw t0, -372(s0)
    bnez t0, _skip355
    j .L120
_skip355:
    la t6, g_banco
    lw a0, 0(t6)
    jal ra, Banco_finDeMes__1
    j .L119
.L120:
    la t6, g_banco
    lw a0, 0(t6)
    jal ra, Banco_finDeMes__1
.L119:
    la t6, g_banco
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -376(s0)
    lw t0, -376(s0)
    li t1, 4
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -380(s0)
    lw t0, -380(s0)
    bnez t0, _skip356
    j .L122
_skip356:
    la t6, g_banco
    lw a0, 0(t6)
    jal ra, Banco_imprimirResumen__1
    j .L121
.L122:
    la t6, g_banco
    lw a0, 0(t6)
    jal ra, Banco_imprimirResumen__1
.L121:
    la t6, g_banco
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -384(s0)
    lw t0, -384(s0)
    li t1, 4
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -388(s0)
    lw t0, -388(s0)
    bnez t0, _skip357
    j .L124
_skip357:
    la t6, g_banco
    lw a0, 0(t6)
    jal ra, Banco_totalDepositado__1
    fsd fa0, -396(s0)
    fld ft0, -396(s0)
    fsd ft0, -404(s0)
    j .L123
.L124:
    la t6, g_banco
    lw a0, 0(t6)
    jal ra, Banco_totalDepositado__1
    fsd fa0, -412(s0)
    fld ft0, -412(s0)
    fsd ft0, -404(s0)
.L123:
    la a0, str_38
    li a7, 4
    ecall
    fld fa0, -404(s0)
    li a7, 3
    ecall
    la a0, str_39
    li a7, 4
    ecall
    la t6, g_banco
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -416(s0)
    lw t0, -416(s0)
    li t1, 4
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -420(s0)
    lw t0, -420(s0)
    bnez t0, _skip358
    j .L126
_skip358:
    la t6, g_banco
    lw a0, 0(t6)
    li a1, 1002
    jal ra, Banco_buscar__2
    sw a0, -424(s0)
    lw t0, -424(s0)
    sw t0, -428(s0)
    j .L125
.L126:
    la t6, g_banco
    lw a0, 0(t6)
    li a1, 1002
    jal ra, Banco_buscar__2
    sw a0, -432(s0)
    lw t0, -432(s0)
    sw t0, -428(s0)
.L125:
    lw t0, -428(s0)
    la t6, g_buscada
    sw t0, 0(t6)
    la t6, g_buscada
    lw t0, 0(t6)
    li t1, 0
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -436(s0)
    lw t0, -436(s0)
    bnez t0, _skip359
    j .L128
_skip359:
    la a0, str_40
    li a7, 4
    ecall
    j .L127
.L128:
    la t6, g_buscada
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -440(s0)
    lw t0, -440(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -444(s0)
    lw t0, -444(s0)
    bnez t0, _skip360
    j .L130
_skip360:
    la t6, g_buscada
    lw a0, 0(t6)
    jal ra, Cuenta_getTitular__1
    sw a0, -448(s0)
    lw t0, -448(s0)
    sw t0, -452(s0)
    j .L129
.L130:
    lw t0, -440(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -456(s0)
    lw t0, -456(s0)
    bnez t0, _skip361
    j .L131
_skip361:
    la t6, g_buscada
    lw a0, 0(t6)
    jal ra, Cuenta_getTitular__1
    sw a0, -460(s0)
    lw t0, -460(s0)
    sw t0, -452(s0)
    j .L129
.L131:
    lw t0, -440(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -464(s0)
    lw t0, -464(s0)
    bnez t0, _skip362
    j .L132
_skip362:
    la t6, g_buscada
    lw a0, 0(t6)
    jal ra, Cuenta_getTitular__1
    sw a0, -468(s0)
    lw t0, -468(s0)
    sw t0, -452(s0)
    j .L129
.L132:
    la t6, g_buscada
    lw a0, 0(t6)
    jal ra, Cuenta_getTitular__1
    sw a0, -472(s0)
    lw t0, -472(s0)
    sw t0, -452(s0)
.L129:
    la a0, str_41
    li a7, 4
    ecall
    lw a0, -452(s0)
    li a7, 4
    ecall
.L127:
    la t6, g_banco
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -476(s0)
    lw t0, -476(s0)
    li t1, 4
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -480(s0)
    lw t0, -480(s0)
    bnez t0, _skip363
    j .L134
_skip363:
    la t6, g_banco
    lw a0, 0(t6)
    li a1, 9999
    jal ra, Banco_buscar__2
    sw a0, -484(s0)
    lw t0, -484(s0)
    sw t0, -488(s0)
    j .L133
.L134:
    la t6, g_banco
    lw a0, 0(t6)
    li a1, 9999
    jal ra, Banco_buscar__2
    sw a0, -492(s0)
    lw t0, -492(s0)
    sw t0, -488(s0)
.L133:
    lw t0, -488(s0)
    la t6, g_buscada
    sw t0, 0(t6)
    la t6, g_buscada
    lw t0, 0(t6)
    li t1, 0
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -496(s0)
    lw t0, -496(s0)
    bnez t0, _skip364
    j .L136
_skip364:
    la a0, str_42
    li a7, 4
    ecall
    j .L135
.L136:
    la t6, g_buscada
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -500(s0)
    lw t0, -500(s0)
    li t1, 1
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -504(s0)
    lw t0, -504(s0)
    bnez t0, _skip365
    j .L138
_skip365:
    la t6, g_buscada
    lw a0, 0(t6)
    jal ra, Cuenta_getTitular__1
    sw a0, -508(s0)
    lw t0, -508(s0)
    sw t0, -512(s0)
    j .L137
.L138:
    lw t0, -500(s0)
    li t1, 2
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -516(s0)
    lw t0, -516(s0)
    bnez t0, _skip366
    j .L139
_skip366:
    la t6, g_buscada
    lw a0, 0(t6)
    jal ra, Cuenta_getTitular__1
    sw a0, -520(s0)
    lw t0, -520(s0)
    sw t0, -512(s0)
    j .L137
.L139:
    lw t0, -500(s0)
    li t1, 3
    xor t2, t0, t1
    seqz t2, t2
    sw t2, -524(s0)
    lw t0, -524(s0)
    bnez t0, _skip367
    j .L140
_skip367:
    la t6, g_buscada
    lw a0, 0(t6)
    jal ra, Cuenta_getTitular__1
    sw a0, -528(s0)
    lw t0, -528(s0)
    sw t0, -512(s0)
    j .L137
.L140:
    la t6, g_buscada
    lw a0, 0(t6)
    jal ra, Cuenta_getTitular__1
    sw a0, -532(s0)
    lw t0, -532(s0)
    sw t0, -512(s0)
.L137:
    la a0, str_43
    li a7, 4
    ecall
    lw a0, -512(s0)
    li a7, 4
    ecall
.L135:
    la t6, g_bitacora
    lw a0, 0(t6)
    jal ra, imprimirBitacora__1
    sw a0, -536(s0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 544
    ret
