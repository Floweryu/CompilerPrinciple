import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class AnalyzeLLTable {

    private String[][] llTable;

    /**
     * 计算 LL(1)分析表
     * @param isTerminal
     * @param noTerminal
     * @param llgrammarArray
     * @param firstGather
     * @param followGather
     * @return LL(1)分析表
     */
    public String[][] analyzeTable(TreeSet<Character> isTerminal, TreeSet<Character> noTerminal, ArrayList<String> llgrammarArray, 
            HashMap<Character, TreeSet<Character>> firstGather, 
            HashMap<Character, TreeSet<Character>> followGather){
        // 得到 LL(1) 文法分析表
        llTable = new String[noTerminal.size() + 1][isTerminal.size() + 1];
        Object[] isTer = isTerminal.toArray();
        Object[] noTer = noTerminal.toArray();

        // 初始化文法分析表
        llTable[0][0] = "@";
        for (int i = 0; i < isTer.length; i++) {
            llTable[0][i + 1] = (isTer[i].toString().charAt(0) == 'ε') ? "$" : isTer[i].toString();
        }
        for (int i = 0; i < noTer.length; i++) {
            llTable[i + 1][0] = noTer[i].toString();
        }
        for (String grammar : llgrammarArray) {
            String[] splitGrammar = grammar.split("->");
            Character left = splitGrammar[0].charAt(0);
            String right = splitGrammar[1];
            Character rightFirst = right.charAt(0);

            // int grammarIndex = llgrammarArray.indexOf(grammar);
            // String index = String.valueOf(grammarIndex);

            if (isTerminal.contains(rightFirst)){   // 如果rightFirst是终结符
                if (rightFirst == 'ε'){     // 如果为空，填入Follow集
                    TreeSet<Character> followSet = followGather.get(left);
                    for (Character a : followSet) {
                        insertTable(isTer, noTer, left, a, right, llgrammarArray);
                    }
                }
                else    // 不为空，填入 First 集
                    insertTable(isTer, noTer, left, rightFirst, right, llgrammarArray);
            }
            else if (noTerminal.contains(rightFirst)){  // 如果是非终结符, 填入该非终结符的First集
                TreeSet<Character> firstSet = firstGather.get(rightFirst);
                for (Character a : firstSet) {
                    insertTable(isTer, noTer, left, a, right, llgrammarArray);
                }
            }
        }
        return llTable;
    }

    /**
     * 向 LL(1) 分析表中插入语法右侧
     * @param isTerminal
     * @param noTerminal
     * @param left      // 横坐标定位
     * @param a         // 纵坐标定位
     * @param right     // 待插入语法
     */
    private void insertTable(Object[] isTerminal, Object[] noTerminal, 
            Character left, Character a, String right, ArrayList<String> llgrammarArray){
        for (int i = 1; i < noTerminal.length + 1; i++) {
            if (llTable[i][0].charAt(0) == left) { // 定位横坐标
                for (int j = 1; j < isTerminal.length + 1; j++) {
                    if (llTable[0][j].charAt(0) == a){ // 定位纵坐标
                        llTable[i][j] = right;
                        return;
                    }
                }
            }
        }
    }

    /**
     * 输出LL(1)分析表
     * @param llTable
     */
    public String displayLLTable(String[][] llTable){
        String output = "";
        for (int i = 0; i < llTable.length; i++){
            for (int j = 0; j < llTable[i].length; j++){
                if (llTable[i][j] == null) {
                    output += String.format("%10s", " ");
                    System.out.printf("%10s", " ");
                } else {
                    output += String.format("%10s", llTable[i][j]);
                    System.out.printf("%10s", llTable[i][j]);
                }
            }
            output += "\n";
            System.out.print("\n");
        }
        return output;
    }
}