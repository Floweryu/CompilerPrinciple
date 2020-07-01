import java.util.ArrayList;
import java.util.Stack;

public class AnalyzeStack {

    // 用下面两个栈来表示分析栈
    private Stack<Character> opStack = new Stack<Character>(); // 存放符号
    private Stack<Integer> stStack = new Stack<Integer>(); // 存放状态

    public String analyzeGrammar(String inputGrammar, String[][] analyzeTable, ArrayList<String> newGrammarArray) {
        String output = "";
        System.out.printf("%-15s %-15s %-15s %10s  |   %-25s %-10s\n", "Steps", "stStack", "opStack", "Input", "Opera",
                "Action");
        output += String.format("%-15s %-15s %-15s %10s  |   %-25s %-10s\n", "Steps", "stStack", "opStack", "Input",
                "Opera", "Action");
        opStack.push('$');
        stStack.push(0);
        inputGrammar += "$";
        int step = 1;
        for (int i = 0; i < inputGrammar.length(); i++) {

            Character itemTop = inputGrammar.charAt(i);
            int topSt = stStack.peek(); // 取出状态栈栈顶
            String target = findItem(analyzeTable, topSt, itemTop); // 找到对应在分析表中的值
            if (target == null) {
                System.out.println("Unaccept!!");
                return "Unaccept!!!";
            }

            int number;
            if (target.contains("s")) { // 移进
                System.out.printf("%-15s %-15s %-15s %10s  |   %-25s %-10s\n", getStep(step), getStStack(stStack),
                        getOpStack(opStack), inputGrammar.substring(i), "shift", target);
                output += String.format("%-15s %-15s %-15s %10s  |   %-25s %-10s\n", getStep(step), getStStack(stStack),
                        getOpStack(opStack), inputGrammar.substring(i), "shift", target);
                number = Integer.valueOf(target.substring(target.indexOf("s") + 1)); // 去掉s，得到数字
                stStack.push(number);
                opStack.push(itemTop);
                step++;
            } else if (target.contains("r")) { // 规约
                number = Integer.valueOf(target.substring(target.indexOf("r") + 1)); // 需要规约的文法的序号
                String grammar = newGrammarArray.get(number); // 要规约的文法
                System.out.printf("%-15s %-15s %-15s %10s  |   %-25s %-10s\n", getStep(step), getStStack(stStack),
                        getOpStack(opStack), inputGrammar.substring(i), "reduce(" + grammar + ")", target);
                output += String.format("%-15s %-15s %-15s %10s  |   %-25s %-10s\n", getStep(step), getStStack(stStack),
                        getOpStack(opStack), inputGrammar.substring(i), "reduce(" + grammar + ")", target);
                String[] splitGrammar = grammar.split("->");
                Character left = splitGrammar[0].charAt(0);
                String right = splitGrammar[1];
                for (int j = 0; j < right.length(); j++) { // -> 右边的个数就是要替换的个数
                    stStack.pop();
                    opStack.pop();
                }
                opStack.push(left); // -> 左边的就是要替换的
                int top = stStack.peek();
                String tar = findItem(analyzeTable, top, left); // 替换后要到达的状态
                int dst = Integer.valueOf(tar); // 由于非终结符在表中以数字出现,直接转换为整形
                stStack.push(dst);
                i--; // 没有移进
                step++;
            } else if (target.contains("acc")) {
                System.out.printf("%-15s %-15s %-15s %10s  |   %-25s %-10s\n", getStep(step), getStStack(stStack),
                        getOpStack(opStack), inputGrammar.substring(i), "Accept", target);
                output += String.format("%-15s %-15s %-15s %10s  |   %-25s %-10s\n", getStep(step), getStStack(stStack),
                        getOpStack(opStack), inputGrammar.substring(i), "Accept", target);
                step++;
            }
        }
        return output;
    }

    /**
     * 输出步数
     * 
     * @param step
     * @return
     */
    public String getStep(int step) {
        return String.valueOf(step);
    }

    /**
     * 获取字符栈内容
     * 
     * @param stack
     * @return
     */
    public String getOpStack(Stack<Character> stack) {
        String str = "";
        for (Character s : stack) {
            str += s.toString();
        }
        return str;
    }

    /**
     * 输出状态栈内容
     */
    public String getStStack(Stack<Integer> stack) {
        String str = "";
        for (Integer s : stack) {
            str += s.toString();
        }
        return str;
    }

    public String findItem(String[][] analyzeTable, int x, Character y) {
        for (int i = 1; i < analyzeTable.length; i++) {
            if (Integer.valueOf(analyzeTable[i][0]) == x) {
                for (int j = 1; j < analyzeTable[i].length; j++) {
                    if (analyzeTable[0][j].charAt(0) == y) {
                        return analyzeTable[i][j];
                    }
                }
            }
        }
        return null;
    }
}