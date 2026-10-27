.data
heap_ptr: .word 0
.align 3
g_p: .space 8
.align 3
g_q: .space 8
.align 3
g_s: .space 8
.align 3
g_m: .space 8
.align 3
g_r: .space 8
.align 3
g_vals: .space 8
str_0: .asciz "Par("
str_1: .asciz ", "
str_2: .asciz ")"
str_3: .asciz " "
str_4: .asciz ","
str_5: .asciz "suma valores: "
str_6: .asciz "p.a tras modificar m: "
str_7: .asciz "directo: "

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

crearPar__2:
    addi sp, sp, -32
    sw s0, 28(sp)
    sw ra, 24(sp)
    addi s0, sp, 28
    sw a0, -8(s0)
    sw a1, -12(s0)
    lw t0, -16(s0)
    lw t1, -8(s0)
    sw t1, 0(t0)
    lw t0, -16(s0)
    lw t1, -12(s0)
    sw t1, 8(t0)
    lw a0, -16(s0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 32
    ret
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 32
    ret
sumarPares__2:
    addi sp, sp, -48
    sw s0, 44(sp)
    sw ra, 40(sp)
    addi s0, sp, 44
    sw a0, -8(s0)
    sw a1, -12(s0)
    lw t0, -8(s0)
    lw t1, 0(t0)
    sw t1, -20(s0)
    lw t0, -12(s0)
    lw t1, 0(t0)
    sw t1, -24(s0)
    lw t0, -20(s0)
    lw t1, -24(s0)
    add t2, t0, t1
    sw t2, -28(s0)
    lw t0, -16(s0)
    lw t1, -28(s0)
    sw t1, 0(t0)
    lw t0, -8(s0)
    lw t1, 8(t0)
    sw t1, -32(s0)
    lw t0, -12(s0)
    lw t1, 8(t0)
    sw t1, -36(s0)
    lw t0, -32(s0)
    lw t1, -36(s0)
    add t2, t0, t1
    sw t2, -40(s0)
    lw t0, -16(s0)
    lw t1, -40(s0)
    sw t1, 8(t0)
    lw a0, -16(s0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
mayor__2:
    addi sp, sp, -32
    sw s0, 28(sp)
    sw ra, 24(sp)
    addi s0, sp, 28
    sw a0, -8(s0)
    sw a1, -12(s0)
    lw t0, -8(s0)
    lw t1, 0(t0)
    sw t1, -16(s0)
    lw t0, -12(s0)
    lw t1, 0(t0)
    sw t1, -20(s0)
    lw t0, -16(s0)
    lw t1, -20(s0)
    slt t2, t0, t1
    xori t2, t2, 1
    sw t2, -24(s0)
    lw t0, -24(s0)
    bnez t0, _skip0
    j .L1
_skip0:
    lw a0, -8(s0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 32
    ret
    j .L0
.L1:
.L0:
    lw a0, -12(s0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 32
    ret
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 32
    ret
crearRango__2:
    addi sp, sp, -48
    sw s0, 44(sp)
    sw ra, 40(sp)
    addi s0, sp, 44
    sw a0, -8(s0)
    sw a1, -12(s0)
    lw t0, -16(s0)
    lw t1, -8(s0)
    sw t1, 0(t0)
    lw t0, -16(s0)
    lw t1, -12(s0)
    sw t1, 8(t0)
    li t0, 0
    sw t0, -20(s0)
.L2:
    lw t0, -20(s0)
    li t1, 3
    bge t0, t1, _skip1
    j .L3
_skip1:
    j .L5
.L3:
    lw t0, -8(s0)
    lw t1, -20(s0)
    add t2, t0, t1
    sw t2, -24(s0)
    lw t0, -16(s0)
    lw t1, 16(t0)
    sw t1, -28(s0)
    lw t0, -28(s0)
    lw t1, -20(s0)
    slli t1, t1, 2
    add t0, t0, t1
    lw t2, -24(s0)
    sw t2, 0(t0)
.L4:
    lw t0, -20(s0)
    sw t0, -32(s0)
    lw t0, -20(s0)
    li t1, 1
    add t2, t0, t1
    sw t2, -36(s0)
    lw t0, -36(s0)
    sw t0, -20(s0)
    j .L2
.L5:
    lw a0, -16(s0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
sumaRango__1:
    addi sp, sp, -48
    sw s0, 44(sp)
    sw ra, 40(sp)
    addi s0, sp, 44
    sw a0, -8(s0)
    li t0, 0
    sw t0, -12(s0)
    li t0, 0
    sw t0, -16(s0)
.L6:
    lw t0, -16(s0)
    li t1, 3
    bge t0, t1, _skip2
    j .L7
_skip2:
    j .L9
.L7:
    lw t0, -8(s0)
    lw t1, 16(t0)
    sw t1, -20(s0)
    lw t0, -20(s0)
    lw t1, -16(s0)
    slli t1, t1, 2
    add t0, t0, t1
    lw t2, 0(t0)
    sw t2, -24(s0)
    lw t0, -12(s0)
    lw t1, -24(s0)
    add t2, t0, t1
    sw t2, -28(s0)
    lw t0, -28(s0)
    sw t0, -12(s0)
.L8:
    lw t0, -16(s0)
    sw t0, -32(s0)
    lw t0, -16(s0)
    li t1, 1
    add t2, t0, t1
    sw t2, -36(s0)
    lw t0, -36(s0)
    sw t0, -16(s0)
    j .L6
.L9:
    lw a0, -12(s0)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
imprimirPar__1:
    addi sp, sp, -48
    sw s0, 44(sp)
    sw ra, 40(sp)
    addi s0, sp, 44
    sw a0, -8(s0)
    lw t0, -8(s0)
    lw t1, 0(t0)
    sw t1, -12(s0)
    la a0, str_0
    lw a1, -12(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo3:
    lbu t2, 0(t1)
    beqz t2, _cad_fin4
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo3
_cad_fin4:
    mv t0, a1
    bgez t0, _ent_pos5
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos5:
    mv t1, t4
_ent_dig6:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig6
    addi t5, t4, -1
_ent_inv7:
    bge t1, t5, _ent_invfin8
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv7
_ent_invfin8:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -16(s0)
    lw a0, -16(s0)
    la a1, str_1
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo9:
    lbu t2, 0(t1)
    beqz t2, _cad_fin10
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo9
_cad_fin10:
    mv t1, a1
_cad_ciclo11:
    lbu t2, 0(t1)
    beqz t2, _cad_fin12
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo11
_cad_fin12:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -20(s0)
    lw t0, -8(s0)
    lw t1, 8(t0)
    sw t1, -24(s0)
    lw a0, -20(s0)
    lw a1, -24(s0)
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo13:
    lbu t2, 0(t1)
    beqz t2, _cad_fin14
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo13
_cad_fin14:
    mv t0, a1
    bgez t0, _ent_pos15
    neg t0, t0
    li t5, 45
    sb t5, 0(t4)
    addi t4, t4, 1
_ent_pos15:
    mv t1, t4
_ent_dig16:
    li t5, 10
    rem t2, t0, t5
    div t0, t0, t5
    addi t2, t2, 48
    sb t2, 0(t4)
    addi t4, t4, 1
    bnez t0, _ent_dig16
    addi t5, t4, -1
_ent_inv17:
    bge t1, t5, _ent_invfin18
    lbu t2, 0(t1)
    lbu t0, 0(t5)
    sb t0, 0(t1)
    sb t2, 0(t5)
    addi t1, t1, 1
    addi t5, t5, -1
    j _ent_inv17
_ent_invfin18:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -28(s0)
    lw a0, -28(s0)
    la a1, str_2
    la t6, heap_ptr
    lw t3, 0(t6)
    mv t4, t3
    mv t1, a0
_cad_ciclo19:
    lbu t2, 0(t1)
    beqz t2, _cad_fin20
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo19
_cad_fin20:
    mv t1, a1
_cad_ciclo21:
    lbu t2, 0(t1)
    beqz t2, _cad_fin22
    sb t2, 0(t4)
    addi t1, t1, 1
    addi t4, t4, 1
    j _cad_ciclo21
_cad_fin22:
    sb zero, 0(t4)
    addi t4, t4, 8
    andi t4, t4, -8
    la t6, heap_ptr
    sw t4, 0(t6)
    mv a0, t3
    sw a0, -32(s0)
    lw a0, -32(s0)
    li a7, 4
    ecall
    li a0, 10
    li a7, 11
    ecall
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 48
    ret
__init:
    addi sp, sp, -32
    sw s0, 28(sp)
    sw ra, 24(sp)
    addi s0, sp, 28
    li t0, 1
    li t1, 3
    mul t2, t0, t1
    sw t2, -8(s0)
    lw t3, -8(s0)
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
    la t6, g_vals
    sw a0, 0(t6)
    la t6, g_vals
    lw t0, 0(t6)
    li t1, 0
    slli t1, t1, 2
    add t0, t0, t1
    li t2, 0
    sw t2, 0(t0)
    la t6, g_vals
    lw t0, 0(t6)
    li t1, 1
    slli t1, t1, 2
    add t0, t0, t1
    li t2, 0
    sw t2, 0(t0)
    la t6, g_vals
    lw t0, 0(t6)
    li t1, 2
    slli t1, t1, 2
    add t0, t0, t1
    li t2, 0
    sw t2, 0(t0)
    la t6, heap_ptr
    lw a0, 0(t6)
    addi a0, a0, 7
    andi a0, a0, -8
    addi t4, a0, 16
    sw t4, 0(t6)
    sw a0, -12(s0)
    lw t0, -12(s0)
    li t1, 0
    sw t1, 0(t0)
    lw t0, -12(s0)
    li t1, 0
    sw t1, 8(t0)
    lw t0, -12(s0)
    la t6, g_p
    sw t0, 0(t6)
    la t6, heap_ptr
    lw a0, 0(t6)
    addi a0, a0, 7
    andi a0, a0, -8
    addi t4, a0, 16
    sw t4, 0(t6)
    sw a0, -16(s0)
    lw t0, -16(s0)
    li t1, 0
    sw t1, 0(t0)
    lw t0, -16(s0)
    li t1, 0
    sw t1, 8(t0)
    lw t0, -16(s0)
    la t6, g_q
    sw t0, 0(t6)
    la t6, heap_ptr
    lw a0, 0(t6)
    addi a0, a0, 7
    andi a0, a0, -8
    addi t4, a0, 16
    sw t4, 0(t6)
    sw a0, -20(s0)
    lw t0, -20(s0)
    li t1, 0
    sw t1, 0(t0)
    lw t0, -20(s0)
    li t1, 0
    sw t1, 8(t0)
    lw t0, -20(s0)
    la t6, g_s
    sw t0, 0(t6)
    la t6, heap_ptr
    lw a0, 0(t6)
    addi a0, a0, 7
    andi a0, a0, -8
    addi t4, a0, 16
    sw t4, 0(t6)
    sw a0, -24(s0)
    lw t0, -24(s0)
    li t1, 0
    sw t1, 0(t0)
    lw t0, -24(s0)
    li t1, 0
    sw t1, 8(t0)
    lw t0, -24(s0)
    la t6, g_m
    sw t0, 0(t6)
    la t6, heap_ptr
    lw a0, 0(t6)
    addi a0, a0, 7
    andi a0, a0, -8
    addi t4, a0, 24
    sw t4, 0(t6)
    sw a0, -28(s0)
    lw t0, -28(s0)
    li t1, 0
    sw t1, 0(t0)
    lw t0, -28(s0)
    li t1, 0
    sw t1, 8(t0)
    lw t0, -28(s0)
    la t6, g_vals
    lw t1, 0(t6)
    sw t1, 16(t0)
    lw t0, -28(s0)
    la t6, g_r
    sw t0, 0(t6)
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 32
    ret
main:
    addi sp, sp, -112
    sw s0, 108(sp)
    sw ra, 104(sp)
    addi s0, sp, 108
    li a0, 3
    li a1, 4
    jal ra, crearPar__2
    sw a0, -8(s0)
    lw t0, -8(s0)
    la t6, g_p
    sw t0, 0(t6)
    li a0, 10
    li a1, 20
    jal ra, crearPar__2
    sw a0, -12(s0)
    lw t0, -12(s0)
    la t6, g_q
    sw t0, 0(t6)
    la t6, g_p
    lw a0, 0(t6)
    jal ra, imprimirPar__1
    sw a0, -16(s0)
    la t6, g_q
    lw a0, 0(t6)
    jal ra, imprimirPar__1
    sw a0, -20(s0)
    la t6, g_p
    lw a0, 0(t6)
    la t6, g_q
    lw a1, 0(t6)
    jal ra, sumarPares__2
    sw a0, -24(s0)
    lw t0, -24(s0)
    la t6, g_s
    sw t0, 0(t6)
    la t6, g_s
    lw a0, 0(t6)
    jal ra, imprimirPar__1
    sw a0, -28(s0)
    la t6, g_p
    lw a0, 0(t6)
    jal ra, imprimirPar__1
    sw a0, -32(s0)
    li a0, 100
    li a1, 200
    jal ra, crearPar__2
    sw a0, -36(s0)
    lw t0, -36(s0)
    la t6, g_p
    sw t0, 0(t6)
    la t6, g_p
    lw a0, 0(t6)
    jal ra, imprimirPar__1
    sw a0, -40(s0)
    la t6, g_s
    lw a0, 0(t6)
    jal ra, imprimirPar__1
    sw a0, -44(s0)
    li a0, 5
    li a1, 8
    jal ra, crearRango__2
    sw a0, -48(s0)
    lw t0, -48(s0)
    la t6, g_r
    sw t0, 0(t6)
    la t6, g_r
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -52(s0)
    la t6, g_r
    lw t0, 0(t6)
    lw t1, 8(t0)
    sw t1, -56(s0)
    la t6, g_r
    lw t0, 0(t6)
    lw t1, 16(t0)
    sw t1, -60(s0)
    lw t0, -60(s0)
    li t1, 0
    slli t1, t1, 2
    add t0, t0, t1
    lw t2, 0(t0)
    sw t2, -64(s0)
    la t6, g_r
    lw t0, 0(t6)
    lw t1, 16(t0)
    sw t1, -68(s0)
    lw t0, -68(s0)
    li t1, 1
    slli t1, t1, 2
    add t0, t0, t1
    lw t2, 0(t0)
    sw t2, -72(s0)
    la t6, g_r
    lw t0, 0(t6)
    lw t1, 16(t0)
    sw t1, -76(s0)
    lw t0, -76(s0)
    li t1, 2
    slli t1, t1, 2
    add t0, t0, t1
    lw t2, 0(t0)
    sw t2, -80(s0)
    lw a0, -52(s0)
    li a7, 1
    ecall
    la a0, str_3
    li a7, 4
    ecall
    lw a0, -56(s0)
    li a7, 1
    ecall
    la a0, str_3
    li a7, 4
    ecall
    lw a0, -64(s0)
    li a7, 1
    ecall
    la a0, str_4
    li a7, 4
    ecall
    lw a0, -72(s0)
    li a7, 1
    ecall
    la a0, str_4
    li a7, 4
    ecall
    lw a0, -80(s0)
    li a7, 1
    ecall
    li a0, 10
    li a7, 11
    ecall
    la t6, g_r
    lw a0, 0(t6)
    jal ra, sumaRango__1
    sw a0, -84(s0)
    la a0, str_5
    li a7, 4
    ecall
    lw a0, -84(s0)
    li a7, 1
    ecall
    li a0, 10
    li a7, 11
    ecall
    la t6, g_p
    lw a0, 0(t6)
    la t6, g_s
    lw a1, 0(t6)
    jal ra, mayor__2
    sw a0, -88(s0)
    lw t0, -88(s0)
    la t6, g_m
    sw t0, 0(t6)
    la t6, g_m
    lw t0, 0(t6)
    li t1, 777
    sw t1, 0(t0)
    la t6, g_p
    lw t0, 0(t6)
    lw t1, 0(t0)
    sw t1, -92(s0)
    la a0, str_6
    li a7, 4
    ecall
    lw a0, -92(s0)
    li a7, 1
    ecall
    li a0, 10
    li a7, 11
    ecall
    li a0, 8
    li a1, 9
    jal ra, crearPar__2
    sw a0, -96(s0)
    la a0, str_7
    li a7, 4
    ecall
    lw a0, -96(s0)
    li a7, 1
    ecall
    li a0, 10
    li a7, 11
    ecall
    lw ra, -4(s0)
    lw s0, 0(s0)
    addi sp, sp, 112
    ret
