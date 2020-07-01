import java.util.ArrayList;

public class EliminateRecursive {

    /**
     * 消除直接左递归
     */
    public void removeDirect(ArrayList<String> grammarArray, ArrayList<String> removeDirectGrammar){
        for (String grammar : grammarArray)
        {
            String[] splitgrammar = grammar.split("->");
            Character left = splitgrammar[0].charAt(0);          // 取 -> 左侧
            String leftString = left.toString();
            String right = splitgrammar[1];                 // 取 -> 右侧
            String[] littleRight = right.split("\\|");        // 以 "|" 分隔

            boolean isLeftRecursion = false;
            if (littleRight.length >= 2){
                for (int i = 0; i < littleRight.length; i++) {
                    if (littleRight[i].startsWith(leftString)) {       // 找到符合左递归条件的语句
                        isLeftRecursion = true;
                    }
                }
            }

            // 如果是左递归
            if (isLeftRecursion){
                String oneRight = "", twoRight = "";
                for (int i = 0; i < littleRight.length; i++){
                    if (!littleRight[i].startsWith(leftString)){
                        oneRight += (littleRight[i].concat(leftString + "'"));
                    } else{
                        twoRight += (littleRight[i].replaceFirst(       // 把 left 消除
                                leftString, "").concat(            
                                leftString + "'") + "|");          // 在后面链接上 left'
                    }
                }
                String one_newgrammar = leftString + "->" + oneRight;
                String two_newgrammar = leftString + "'" + "->" + twoRight + "ε";
                removeDirectGrammar.add(one_newgrammar);
                removeDirectGrammar.add(two_newgrammar);
            }
            else {
                removeDirectGrammar.add(grammar);
            }
        }
    }

    public void showGrammar(ArrayList<String> removeDirectGrammar){
        for (String grammar : removeDirectGrammar){
            System.out.println(grammar);
        }
    }
}