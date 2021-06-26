# Instruction-Set-Architechture
Implementation of an ISA with 12 differenet instructions to emulate the implementation of the micro architecture. There are 64 registers named r0 till r63

# The language is not case sensitive 

# Syntax of program:
ADD R1 R2      ->  R1 = R1 + R2  
SUB R1 R2      ->  R1 = R1 - R2  
MULI R1 R2     ->  R1 = R1 * R2  
LDI R1 IMM     -> R1 = IMM  
BEQZ R1 IMM    -> IF(R1 == 0) {PC = PC+IMM}  
AND R1 R2      -> R1 = R1 & R2  
OR R1 R2       ->  R1 = R1 | R2  
JR R1 R2       ->  PC = R1 || R2  
SLC R1 IMM     ->  R1 = R1 << IMM | R1 >> 8 - IMM  
SRC R1 IMM     ->  R1 = R1 >> IMM | R1 << 8 - IMM  
LB R1 ADDRESS  -> R1 = MEM[ADDRESS]  
SB R1 ADDRESS  -> MEM[ADDRESS] = R1  
