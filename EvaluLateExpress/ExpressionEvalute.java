package EvaluLateExpress;

import java.util.Scanner;
import java.util.Stack;

public class ExpressionEvalute {

    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        System.out.println("Please input a expression:");
        String str = input.nextLine();
        input.close();
        StringBuffer output = translate(str);      // Converts an infix expression to a postfix expression
        String posExpress = output.toString();
        int result = compute(posExpress);
        System.out.println(result);
    }

    /**
     * Converts an infix expression to a postfix expression
     * @param expression string whick input
     * @return postfix expression
     */
    public static StringBuffer translate(String expression) {
        StringBuffer result = new StringBuffer();       //store numbers
        Stack<Character> opStack = new Stack<>();

        for (int i = 0; i < expression.length(); i++){
            char ch = expression.charAt(i);
            if (! isRight(ch)){
                throw new RuntimeException("Wrong char" + ch);
            }

            //if number, append to result directly
            if (isNumber(ch)){      
                result.append(ch);
            }

            //if '(', push to opStack
            else if (ch == '(') {
                opStack.push(ch);
            }
            /**
             * if ch = '+' or '-'
             * pop the opterator from stack until '(' 
             * add the pop to result
             * push ch to opStack
             */
            else if (ch == '+' || ch == '-'){   
                while (!opStack.empty() && opStack.peek() != '('){
                        result.append(opStack.pop());
                }
                opStack.push(ch);
            }
            /**
             * if ch = '*' or '/'
             * pop the opterator from stack until '(' or '+', '-'
             * push ch to opStack
             */
            else if (ch == '*' || ch == '/'){
                while (!opStack.empty() && (opStack.peek() == '*' || opStack.peek() == '/')){
                    result.append(opStack.pop());
                }
                opStack.push(ch);
            }
            
            /**
             * if ch = ')'
             * pop the opterator from stack until '('
             * push ch to opStack
             */
            else if (ch == ')'){
                while (!opStack.empty() && opStack.peek() != '('){
                    result.append(opStack.pop());
                }
                opStack.pop();      //if meet '(', give up it.
            }
        }

        /**
         * add rest of opStack to result
         * give up '(' and ')'
         */
        while (!opStack.empty()){
            if (opStack.peek() == '(' || opStack.peek() == ')')
                opStack.pop();
            else
                result.append(opStack.pop());
        }
        return result;
    }

    /**
     * Evaluate postfix expression
     */
    public static int compute(String expression){
        Stack<Integer> numStack = new Stack<>();

        for (int i = 0; i < expression.length(); i++){
            char ch = expression.charAt(i);
            if (isNumber(ch)){
                numStack.push(ch - '0');
            }
            else{
                numStack.push(calc(ch, numStack.pop(), numStack.pop()));
            }
        }
        return numStack.pop();
    }

    /**
     * Determines if the character is a number.
     * @param value The character to test.
     * @return if the character is a number, return true; else return false.
     */
    private static boolean isNumber(char value){
        return value >= '0' && value <= '9';
    }

    /**
     * Determines if the character is right.
     * @param value The character to test.
     * @return if right, return true; else return false.
     */
    private static boolean isRight(char value){
        switch (value) {
            case '+':
            case '-':
            case '*':
            case '/':
            case '(':
            case ')':
                return true;
        }
        return isNumber(value);
    }

    /**
     * Calculate a op b
     * @param op    operator
     * @param a     int a
     * @param b     int a
     * @return  a result which a op b
     */
    private static int calc(char op, int a, int b){
        if (op == '+') return b + a;
        else if (op == '-') return b - a;
        else if (op == '*') return b * a;
        else if (op == '/') return b / a;
        else return 0;
    }
}
