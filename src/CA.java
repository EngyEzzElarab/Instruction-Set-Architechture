import java.io.*;
import java.util.*;

public class CA { 

	short[] instructionMemory = new short[1024];
	byte[] dataMemory = new byte[2048];
	byte[] registerFile = new byte[64];
	boolean[] statusRegister = new boolean[8];
	short pc;

	public CA() {
		Arrays.fill(instructionMemory, (short)-1);
	}
	
	public short fetch() {
		return instructionMemory[pc++];
	}

	public Object[] decode(short instruction){ 

		int opCodeTmp = instruction & 0b1111000000000000;
		int opCode = opCodeTmp >> 12;

		int r1Tmp = instruction & 0b0000111111000000;
		int r1 = r1Tmp >> 6;

		int r2Immediate = instruction & 0b0000000000111111;
		Object[] res = new Object[4];
		switch (opCode) {
		case 0:
		case 1:
		case 2:
		case 5:
		case 6:
		case 7:
			res[0] = opCode;
			res[1] = r1;
			res[2] = registerFile[r1];
			res[3] = registerFile[r2Immediate];
			return res;

		case 3:
		case 4:
		case 8:
		case 9:
		case 10:
		case 11:
			res[0] = opCode;
			res[1] = r1;
			res[2] = registerFile[r1];
			res[3] = r2Immediate;
			return res;
		default:
			System.out.println("The opcode is invalid");
		}
		return null;
	}

	public boolean execute(int opCode, int destination, byte r1, int r2Immediate) throws Exception {

		switch (opCode) {
		case 0:
			add(destination, r1, r2Immediate);
			break;
		case 1:
			subtract(destination, r1, r2Immediate);

			break;
		case 2:
			multiply(destination, r1, r2Immediate);
			break;
		case 3:
			registerFile[destination] = (byte) r2Immediate;
			break;
			case 4:
			if(r1 == 0){
				pc += r2Immediate - 1;
				return true;
			}
			break;
		case 5:
			and(destination, r1, r2Immediate);
			break;
		case 6:
			or(destination, r1, r2Immediate);
			break;
		case 7:
			pc = concatenate(r1, (byte)r2Immediate);
			return true;
		case 8:
			slc(destination, r1, r2Immediate);
			break;

		case 9:
			src(destination, r1, r2Immediate);
			break;

		case 10:
			if(r2Immediate<0)
				throw new Exception ("Immedediate value is negative");
			registerFile[destination] = dataMemory[r2Immediate];
			break;

		case 11:
			if(r2Immediate<0)
				throw new Exception ("Immedediate value is negative");
			dataMemory[r2Immediate] = r1;
			break;
		}
		return false;
	}

	public void add(int destination, byte r1, int r2Immediate) {

		registerFile[destination] = (byte) (r1 + r2Immediate);

		// C
		statusRegister[4] = r1 + r2Immediate > Byte.MAX_VALUE;

		// V
		statusRegister[3] = (byte) (r1 + r2Immediate) != (r1 + r2Immediate);

		// N
		statusRegister[2] = (byte) (r1 + r2Immediate) < 0;

		// S
		statusRegister[1] = xor(statusRegister[2], statusRegister[3]);

		// Z
		statusRegister[0] = registerFile[destination] == 0;

		statusRegister[5] = false;

		statusRegister[6] = false;

		statusRegister[7] = false;
	}

	public void subtract(int destination, byte r1, int r2Immediate) {

		registerFile[destination] = (byte) (r1 - r2Immediate);

		// C
		statusRegister[4] = r1 - r2Immediate > Byte.MAX_VALUE;

		// V
		statusRegister[3] = (byte) (r1 - r2Immediate) != (r1 - r2Immediate);

		// N
		statusRegister[2] = (byte) (r1 - r2Immediate) < 0;

		// S
		statusRegister[1] = xor(statusRegister[2], statusRegister[3]);

		// Z
		statusRegister[0] = registerFile[destination] == 0;

		statusRegister[5] = false;

		statusRegister[6] = false;

		statusRegister[7] = false;
	}

	public void multiply(int destination, byte r1, int r2Immediate) {

		registerFile[destination] = (byte) (r1 * r2Immediate);

		// C
		statusRegister[4] = r1 * r2Immediate > Byte.MAX_VALUE;

		// N
		statusRegister[2] = (byte) (r1 - r2Immediate) < 0;

		// Z
		statusRegister[0] = registerFile[destination] == 0;

		statusRegister[5] = false;

		statusRegister[6] = false;

		statusRegister[7] = false;

	}

	public void and(int destination, byte r1, int r2Immediate) {

		registerFile[destination] = (byte) (r1 & r2Immediate);

		// N
		statusRegister[2] = (byte) (r1 & r2Immediate) < 0;

		// Z
		statusRegister[0] = registerFile[destination] == 0;

		statusRegister[5] = false;

		statusRegister[6] = false;

		statusRegister[7] = false;

	}

	public void or(int destination, byte r1, int r2Immediate) {

		registerFile[destination] = (byte) (r1 | r2Immediate);

		// N
		statusRegister[2] = (byte) (r1 | r2Immediate) < 0;

		// Z
		statusRegister[0] = registerFile[destination] == 0;

		statusRegister[5] = false;

		statusRegister[6] = false;

		statusRegister[7] = false;

	}

	public void slc(int destination, byte r1, int r2Immediate) {

		registerFile[destination] = (byte) (r1 << r2Immediate | r1 >> 8 - r2Immediate);

		// N
		statusRegister[2] = (byte) (r1 - r2Immediate) < 0;

		// Z
		statusRegister[0] = registerFile[destination] == 0;

		statusRegister[5] = false;

		statusRegister[6] = false;

		statusRegister[7] = false;
	}

	public void src(int destination, byte r1, int r2Immediate) {

		registerFile[destination] = (byte) (r1 >> r2Immediate | r1 << 8 - r2Immediate);

		// N
		statusRegister[2] = (byte) (r1 - r2Immediate) < 0;

		// Z
		statusRegister[0] = registerFile[destination] == 0;

		statusRegister[5] = false;

		statusRegister[6] = false;

		statusRegister[7] = false;
	}

	public static boolean xor(boolean x, boolean y) {
		return x != y;
	}

	public void run() throws Exception {
		Queue<Short> decode = new LinkedList<Short>();
		Queue<Object[]> execute = new LinkedList<>();
		boolean branch = false;
		for (int clockCycle = 0; clockCycle < (instructionMemory.length + 2); clockCycle++) {
			short instruction = 0;
			if (!execute.isEmpty()) {
				Object[] arr = execute.poll();
				try {
					branch = execute((int) arr[0], (int) arr[1], (byte) arr[2], (int) arr[3]);
				} catch (ClassCastException c) {
					branch = execute((int) arr[0], (int) arr[1], (byte) arr[2], (byte) arr[3]);
				}
			}
			if (!decode.isEmpty()) {
				if(branch){
					decode.clear();
					branch = false;
				}
				else{
					Object[]arr = decode(decode.poll());
					execute.add(arr);
				}
			}
			if (pc < instructionMemory.length)
				instruction = fetch();
			else
				return;
			if(instruction != -1)
				decode.add(instruction);
		}

	}


	public void printRegister() {
		for (int i = 0; i < registerFile.length; i++)
			System.out.println("R" + i + " -> " + registerFile[i]);
	}

	public void printRegister(int registerNumber) {
		System.out.println("R" + registerNumber + ": " + registerFile[registerNumber]);
	}

	public void interpreter(String filePath) throws Exception {
		@SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		int index = 0;
		while (br.ready()){
			String instruction = br.readLine();
			instruction = instruction.toLowerCase();
			StringTokenizer st = new StringTokenizer(instruction);
			ArrayList<String> tokens = new ArrayList<String>();
			while(st.hasMoreTokens()) 
				tokens.add(st.nextToken());
			
			if(tokens.isEmpty() || tokens.get(0).charAt(0) == '#')
				continue;
			
			if(tokens.size() != 3)
				throw new Exception("Syntax Error!");
			
			String res = "";
			switch(tokens.get(0)) {
			case "add":
				res = "0000" + verifyRegister(tokens.get(1)) + verifyRegister(tokens.get(2));break;
			case "sub":
				res = "0001" + verifyRegister(tokens.get(1)) + verifyRegister(tokens.get(2));break;
			case "mul":
				res = "0010" + verifyRegister(tokens.get(1)) + verifyRegister(tokens.get(2));break;
			case "ldi":
				res = "0011" + verifyRegister(tokens.get(1)) + verifyNumber(tokens.get(2));break;
			case "beqz":
				res = "0100" + verifyRegister(tokens.get(1)) + verifyNumber(tokens.get(2));break;
			case "and":
				res = "0101" + verifyRegister(tokens.get(1)) + verifyRegister(tokens.get(2));break;
			case "or":
				res = "0110" + verifyRegister(tokens.get(1)) + verifyRegister(tokens.get(2));break;
			case "jr":
				res = "0111" + verifyRegister(tokens.get(1)) + verifyRegister(tokens.get(2));break;
			case "slc":
				res = "1000" + verifyRegister(tokens.get(1)) + verifyNumber(tokens.get(2));break;
			case "src":
				res = "1001" + verifyRegister(tokens.get(1)) + verifyNumber(tokens.get(2));break;
			case "lb":
				res = "1010" + verifyRegister(tokens.get(1)) + verifyNumber(tokens.get(2));break;
			case "sb":
				res = "1011" + verifyRegister(tokens.get(1)) + verifyNumber(tokens.get(2));break;
			default: throw new Exception("Syntax Error");
			}
			
			instructionMemory[index++] = binaryToDecimal(res);	
		}
		br.close();
		run();
	}

	public static String flipBits(String bits) {

		String res = "";
		for (int i = 0; i < bits.length(); i++)
			if (bits.charAt(i) == '1')
				res += "0";
			else if (bits.charAt(i) == '0')
				res += "1";
			else
				throw new NumberFormatException("The string is not binary");

		return res;
	}

	public static short binaryToDecimal(String number) {
		try {
			return Short.parseShort(number, 2);
		} catch (NumberFormatException e) {
			number = flipBits(number);
			short res = Short.parseShort(number, 2);
			return (short) (-(res + 1));
		}
	}

	public static String verifyRegister(String register) throws Exception {
		int num;
		try {
			num = Integer.parseInt(register.substring(1));
		} catch (Exception e) {
			throw new Exception("Syntax Error!");
		}

		if (register.charAt(0) != 'r' || (num < 0 || num > 63))
			throw new Exception("Syntax Error!");
		
		String res = Integer.toBinaryString(num);
		for(int i = res.length() ; i < 6 ; i++)
			res = "0" + res;
		return res;
	}

	public static String verifyNumber(String num)throws Exception {

		int number;
		try {
			number = Integer.parseInt(num);
		} catch (Exception e) {
			throw new Exception("Syntax Error!");
		}
		String res = Integer.toBinaryString(number);
		for(int i = res.length() ; i < 6 ; i++)
			res = "0" + res;
		return res;
	}

	public static short concatenate(byte x, byte y){
		String b1 = Integer.toBinaryString(x);
		String b2 = Integer.toBinaryString(y);
		String res = b1 + b2;
		return binaryToDecimal(res);
	}

	public static void main(String[] args) throws Exception{


		CA ca = new CA();
		ca.printRegister(1);
		ca.printRegister(2);
		
		ca.interpreter("program.txt");
		
		ca.printRegister(1);
		ca.printRegister(2);
		System.out.println(Arrays.toString(ca.statusRegister));	
	}

}
