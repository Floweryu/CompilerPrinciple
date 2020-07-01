import java.util.Stack;

/**
 * 把正则表达式添加连接符，并转变成逆波兰式
 */
public class RegexToRPN {

    /**
     * 添加连接符
     * @param regex 原始正则表达式(没有连接符号)
     * @return 加入连接符后的正则表达式
     */
    public static StringBuffer addConnectOP(String regex) {
        StringBuffer newRegex = new StringBuffer();
        for (int i = 0; i < regex.length() - 1; i++) {
            char ch1 = regex.charAt(i);
            newRegex.append(ch1);
            char ch2 = regex.charAt(i + 1);
            if (Character.isLetter(ch1) && (ch2 == '(' || Character.isLetter(ch2))) {
                newRegex.append('·');
            } else if (ch1 == '*' && (ch2 == '(' || Character.isLetter(ch2))) {
                newRegex.append('·');
            } else if (ch1 == ')' && (ch2 == '(' || Character.isLetter(ch2))) {
                newRegex.append('·');
            }
        }
        newRegex.append(regex.charAt(regex.length() - 1)); // 添加最后一个字符
        return newRegex;
    }

    /**
     * 构建逆波兰表达式（RPN）
     * @param regex 加入连接符后的正则表达式
     * @return 逆波兰表达式
     */
    public static StringBuffer ReversePolishNotation(String regex) {
        StringBuffer rpnExpress = new StringBuffer();
        Stack<Character> opStack = new Stack<>();

        for (int i = 0; i < regex.length(); i++) {
            char ch = regex.charAt(i);
            if (Character.isLetter(ch)) {
                rpnExpress.append(ch);
            } else if (ch == '(') {
                opStack.push(ch);
            } else if (ch == '*') {
                while (!opStack.empty() && opStack.peek() == '*') {
                    rpnExpress.append(opStack.pop());
                }
                opStack.push(ch);
            } else if (ch == '·') {
                while (!opStack.empty() && (opStack.peek() == '*' || opStack.peek() == '·')) {
                    rpnExpress.append(opStack.pop());
                }
                opStack.push(ch);
            } else if (ch == '|') {
                while (!opStack.empty() && (opStack.peek() != '(')) {
                    rpnExpress.append(opStack.pop());
                }
                opStack.push(ch);
            } else if (ch == ')') {
                while (!opStack.empty() && opStack.peek() != '(') {
                    rpnExpress.append(opStack.pop());
                }
                opStack.pop();
            }
        }

        while (!opStack.empty()) {
            if (opStack.peek() == '(' || opStack.peek() == ')')
                opStack.pop();
            else
                rpnExpress.append(opStack.pop());
        }
        return rpnExpress;
    }
    
}