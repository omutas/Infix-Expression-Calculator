import java.io.*;
import java.util.*;

/**
 * A class to evaluate postfix and infix expressions
 */

public class Main2 {

	public static Hashtable<String, Integer> variables = new Hashtable<String, Integer>();
	public static int line;
	public static boolean compiler;
	public static BufferedWriter bw;

	/**
	 * Evaluate infix expressions
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// Hashtable is filled with default value
		//System.out.println(args[0].toString().contains(".ac"));
		for (char ch = 'A'; ch <= 'Z'; ++ch) {
			variables.put(String.valueOf(ch), 0);
		}
		for (char ch = 'a'; ch <= 'z'; ++ch) {
			variables.put(String.valueOf(ch), 0);
		}
		//System.out.println("Type an infix expression (to quit, type <ctrl-d>)");

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		String input = null; // line input by the user
		line = 0;
		if (args.length == 0) {
			// interpreter mode
			compiler = false;
		} else{
			// compiler mode
			String file = args[0];
			in = new BufferedReader(new FileReader(file));
			compiler = true;
			file = file.replaceAll(".ac","");
			file = file.replaceAll(".txt","");
			bw = new BufferedWriter(new FileWriter(file+".asm"));
			bw.write("CODE SEGMENT\n");
		}

		do {
			try {
				//System.out.print(">"); // enter symbol as shown in description
				input = in.readLine();
				line++;
				//space replace causes some faults that's why removed
				//input = input.replaceAll("\\s", "");
				if (!input.contains("=")) {
					String postfix = toPostfix(input);
					//System.out.println("postfix =" + postfix);
					if (compiler) {
						calcPostfix(postfix);
						bw.write("\tcall print\n");
					}else{
						System.out.println(calcPostfix(postfix));
					}
				} else {
					//System.out.println("contains = " + input);
					String key = input.substring(0, input.indexOf("="));
					key = key.replaceAll("\\s", "");
					//System.out.println(key);
					//System.out.println(input.substring(input.indexOf("=") + 1));
					int result = calcPostfix(toPostfix(input.substring(input.indexOf("=") + 1)));
					//System.out.println(result);
					if (variables.containsKey(key)) {
						variables.put(key, result);
					}else{
						System.out.println("ERROR:You can't use "+key+" as variable.Use letters of English alphabet");
						throw new Exception();
					}
					if (compiler) {
						char a = key.charAt(0);
							if ((int)a>96) {
								/*if (variables.get(token)==0) {
									bw.write("\tMOV bx," + token + "sc\n");
									bw.write("\tMOV [bx], 0d\n");
								}*/
								bw.write("\tPOP ax\n");
								bw.write("\tPUSH offset " + key + "sc\n");
								bw.write("\tPOP bx\n");
								bw.write("\tMOV [bx],ax \n");
							}else{
								/*if (variables.get(token)==0) {
									bw.write("\tMOV bx," + token + "bc\n");
									bw.write("\tMOV [bx], 0d\n");
								}*/
								bw.write("POP ax");
								bw.write("\tPUSH offset " + key + "bc\n");
								bw.write("\tPOP bx\n");
								bw.write("\tMOV [bx],ax \n");
							}
					}
					//System.out.println(variables.toString());
				}
			} catch (Exception e) {
				if(input != null){
					//System.out.println("Invalid expression");
				}else{

				}
			}
		} while (input != null);
		if (compiler) {
			//bw.write("\tint 20h\n\tret\nprint:\n\tmov si,10d\n\txor dx,dx\n\tpush ' ' \n\tmov cx,1d\n\tmov bx,8000h\n\tand bx,ax\n\tshr bx,15\n\tjz positive\nnegative:\n\tnot ax\n\tinc ax\npositive:\n\tdiv si\n\tadd dx,48d\n\tpush dx\n\tinc cx\n\txor dx,dx\n\tcmp ax,0h\n\tjne positive\n\tdec bx\n\tjnz writeloop\n\tpush'-'\n\tinc cx\nwriteloop:\n\tpop dx\n\tmov ah,02h\n\tint 21h\n\tdec cx\n\tjnz writeloop\n\tret\n");
			bw.write("\tint 20h\n\tret\nprint: \n"
                            +"   mov    si,10d\n"
                            +"   xor    dx,dx\n"
                            +"   push   ' '  \n"
                            +"   mov    cx,1d\n"
                            +"   mov bx,ax\n"
                            +"   shr bx,15\n"
                            +"   jz nonzero\n"
                            +"   negative:\n"
                            +"   not ax\n"
                            +"   add ax,1d\n"
                            +"     nonzero:\n"
                            +"   div    si\n"
                            +"   add    dx,48d\n"
                            +"   push   dx\n"
                            +"   inc    cx\n"
                            +"   xor    dx,dx\n"
                            +"   cmp    ax,0h\n"
                            +"   jne    nonzero\n"
                            +"      dec bx\n"
                            +"   jnz writeloop\n"
                            +"   push '-'\n"
                            +"   inc cx\n"
                            +"   writeloop:\n"
                            +"   pop    dx\n"
                            +"   mov    ah,02h\n"
                            +"   int    21h\n"
                            +"   dec    cx\n"
                            +"   jnz    writeloop\n"
                            +"   mov dl, 10 \n"
                            +"   mov ah, 02h\n"
                            +"   int 21h\n"
                            +"   mov dl, 13\n"
                            +"   mov ah, 02h\n"
                            +"   int 21h\n"
                            +"   ret\n");
			for (char ch = 'A'; ch <= 'Z'; ++ch) {
				bw.write(ch + "bc: dw 0\n");
			}
			for (char ch = 'a'; ch <= 'z'; ++ch) {
				bw.write(ch + "sc: dw 0\n");
			}
			bw.write("code ends");
			bw.close();
		}
		
	}

	/*
	 * Takes infix string and turn it to postfix
	 * Returns postfix string 
	 * Gives error if infix has different char than our input set
	 */
	public static String toPostfix(String infix) throws Exception {

		try {
			String postfix = "";
			Stack<String> stack = new Stack<String>();
			StringTokenizer st = new StringTokenizer(infix, "()+-/* ", true);
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				// System.out.println("1"+token+"2");
				
				if (token.equals(" ")) { // skip any empty token
				 //System.out.println("space"); 
				} else if (token.equals("(")) {
					stack.push(token);
				} else if (token.equals(")")) {
					String op;
					while (!(op = stack.pop()).equals("(")) {
						// space will be used to identified tokens later
						// System.out.println("until (" + op +
						// stack.toString());
						postfix += " " + op;
					}
					// an operator
				} else if (token.equals("*") || token.equals("+") || token.equals("-") || token.equals("/")) {
					//first take tokens priority
					int p = priority(token);
					//then check is there any high prioritized operator exist
					while (!stack.isEmpty() && !stack.peek().equals("(") && priority(stack.peek()) >= p) {
						String op = stack.pop();
						//if it exists then add it to postfix
						// space will be used to identified tokens later
						postfix += " " + op;
					}
					//if it doesn't exist push it to stack
					stack.push(token);
				} else { // an operand
					//System.out.println(token.getClass().getName());
					if (variables.containsKey(token)) {
						if (compiler) {
							postfix += " " + token;
						}else { 
							postfix += " " + variables.get(token);
						}
					} else {
						//System.out.println("number format b " +token);
						postfix += " " + Integer.parseInt(token); // just to
						//System.out.println("number format b " +token);											// check
																	// that
					}
					// it is a number
					// If not a number, an exception is
					// thrown
					// space will be used to identified tokens later
					// postfix += " " + token;
				}
			}
			while (!stack.isEmpty()) {
				// System.out.println("!!!1");
				String op = stack.pop();
				// System.out.println(op);
				postfix += " " + op;
			}
			return postfix;
		} catch (EmptyStackException ese) {
			if(compiler){
				System.out.println("ERROR:" + line + " Stack is empty!!! or you add more closing parenthesis then opening ones");
			}else{
				System.out.println("ERROR:Stack is empty!!! or you add more closing parenthesis then opening ones");
			}
			throw new Exception();
		} catch (NumberFormatException nfe) {
			if(compiler){
				System.out.println("ERROR:" + line + " Please use our input set.(English alphabet letters, integer numbers, parenthesis \"(\" and \")\" and =, +, -, *, and / operators)");
			}else {
				System.out.println("ERROR:Please use our input set.(English alphabet letters, integer numbers, parenthesis \"(\" and \")\" and =, +, -, *, and / operators)");
			}
			throw new Exception();
		}
	}

	/*
	 * Return priority level of operation
	 * "/" and "*" has higher priority that's why they return 1
	 * "-" and "+" has lower priority that's why they return 0
	 */
	private static int priority(String operator) throws Exception {
		if (operator.equals("*") || operator.equals("/")) {
			return 1;
		} else if (operator.equals("-") || operator.equals("+")) {
			return 0;
		} else {
			if(compiler){
				System.out.println("ERROR:" + line + "Operator not in format +, -, /, *");
			}else{
				System.out.println("ERROR:Operator not in format +, -, /, *");
			}
			throw new Exception();
		}
	}

	/*
	 * Takes postfix string, reads its tokens if token is not an operator push it to stack and if operator send it to makeOperation method
	 * At the end return last value of stack as a result
	 * If stack is empty somehow or stack is not empty at the and send ERROR
	 *
	 */
	public static int calcPostfix(String postfix) throws Exception {
		try {
			Stack<Integer> stack = new Stack<Integer>();
			StringTokenizer st = new StringTokenizer(postfix);
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				// push operands to stack if see a operator make its operation
				if (token.equals("*") || token.equals("+") || token.equals("-") || token.equals("/")) {
					makeOperation(token, stack);
				} else {
					if (compiler) {
						if (variables.containsKey(token)) {
							stack.push(variables.get(token));
							char a = token.charAt(0);
							if ((int)a>96) {
								/*if (variables.get(token)==0) {
									bw.write("\tMOV bx," + token + "sc\n");
									bw.write("\tMOV [bx], 0d\n");
								}*/
								bw.write("\tPUSH " + token + "sc w\n");
							}else{
								/*if (variables.get(token)==0) {
									bw.write("\tMOV bx," + token + "bc\n");
									bw.write("\tMOV [bx], 0d\n");
								}*/
								bw.write("\tPUSH " + token + "bc w\n");
							}
						}else{
							bw.write("\tPUSH " + token + "\n");
							stack.push(new Integer(token));
						}
					}else{
						stack.push(new Integer(token));
					}
				}
			}
			// pop the last value stay at stack it is result
			// .intValue for boxing
			int result = ((Integer) stack.pop()).intValue();
			// check to be sure there is nothing left
			if (!stack.isEmpty()) { // the stack should be empty
				throw new Exception();
			}
			/*if (compiler) {
				bw.write("\tPUSH "+result+"\n");
			}*/
			return result;
		} catch (EmptyStackException ese) {
			if(compiler){
				System.out.println("ERROR:" + line + " Stack is empty something is wrong, Probably type much more operator then needed");
			}else {
				System.out.println("ERROR:Stack is empty something is wrong, Probably type much more operator then needed");
			}
			throw new Exception();
		} catch (NumberFormatException nfe) {
			if(compiler){
				System.out.println("ERROR:" + line + " Number format problem, Probably there a problem with parenthesis");
			}else{
				System.out.println("ERROR:Number format problem, Probably there a problem with parenthesis");
			}
			throw new Exception();
		} catch (Exception e){
			if(compiler){
				System.out.println("ERROR:" + line + " Missing operator or space is added between digits of a number");
			}else{
				System.out.println("ERROR:Missing operator or space is added between digits of a number");
			}
			throw new Exception();
		}
	}

	/*
	 *Calculate operation which is sended by calcPostfix and push its result to stack
	 *İf there is an operator which is not in out operator set give error
	 */
	private static void makeOperation(String operator, Stack<Integer> s) throws IOException {
		int result;
		int secondNumber = s.pop();
		int firstNumber = s.pop();
		if(compiler){
			bw.write("\tPOP bx\n");
			bw.write("\tPOP ax\n");
		}
		if (operator.equals("+")) {
			result = firstNumber + secondNumber;
			if(compiler){
				bw.write("\tADD ax, bx\n");
		    }
		} else if (operator.equals("-")) {
			result = firstNumber - secondNumber;
			if(compiler){
				bw.write("\tSUB ax, bx\n");
			}
		} else if (operator.equals("/")) {
			result = firstNumber / secondNumber;
			if(compiler){
				bw.write("\tDIV bx\n");
			}
		} else if (operator.equals("*")) {
			result = firstNumber * secondNumber;
			if(compiler){
				bw.write("\tMUL bx\n");
			}
		} else {
			if(compiler){
				System.out.println("ERROR:" + line + " Couldn't find this operator, Please just use +, -, *, /");
			}else{
				System.out.println("ERROR:Couldn't find this operator, Please just use +, -, *, /");
			}
			throw new IllegalArgumentException();
		}
		s.push(result);
		if(compiler){
			//bw.write("\tPUSH " + result + "\n");
			bw.write("\tPUSH ax\n");
		}
	}
}
