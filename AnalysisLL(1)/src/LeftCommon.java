import java.util.ArrayList;

public class LeftCommon {

    private String arrows = "->";

    public void removeLeftCommon(ArrayList<String> grammarArray, ArrayList<String> removeLeftCommon){
        for (String grammar : grammarArray){
            String[] splitgrammar = grammar.split("->");
            Character left = splitgrammar[0].charAt(0); // 取 -> 左侧
            String leftString = left.toString();
            String right = splitgrammar[1]; // 取 -> 右侧
            String[] littleRight = right.split("\\|"); // 以 "|" 分隔

            boolean isLeftCommon = true;

            String common = littleRight[0].substring(0, 1);
            if (littleRight.length >= 2){
                for (int i = 1; i < littleRight.length; i++){
                    if (! littleRight[i].startsWith(common)){
                        isLeftCommon = false;
                    }
                }
            }else{ 
                isLeftCommon = false;
            }
            if (isLeftCommon){
                boolean epsilonFlag = false;
                String oneRight = "", twoRight = "";
                oneRight += common.concat(common + "'");        //提取 公因子 + ' 作为右边
                for (int i = 0; i < littleRight.length; i++){
                    if (littleRight[i].replaceFirst(common, "").equals("")){      
                        epsilonFlag = true;
                    }
                    else
                        twoRight += littleRight[i].replaceFirst(common, "").concat("|");
                }
                String one_newGrammar = leftString + "->" + oneRight;
                String two_newGrammar = "";
                if (epsilonFlag)
                    two_newGrammar += common + "'" + "->" + twoRight + "ε";
                else
                    two_newGrammar += common + "'" + "->" + twoRight.substring(0, twoRight.length() - 1);
                removeLeftCommon.add(one_newGrammar);
                removeLeftCommon.add(two_newGrammar);
            }
            else {
                removeLeftCommon.add(grammar);
            }
        }
    }

    /**
     * 改变文法的形式，把文法分离为最小单元，并以 String 为单位存储
     * @param removeLeftCommon 消除左递归和左因子后的文法
     * @return 改进的后文法，二维字符串数组
     */
    public ArrayList<ArrayList<String>> changeForm(ArrayList<String> removeLeftCommon){
        ArrayList<ArrayList<String>> newGrammarArray = new ArrayList<ArrayList<String>>();
        for (String grammar : removeLeftCommon){
            String left = grammar.substring(0, grammar.indexOf("->"));
            String rights = grammar.substring(grammar.indexOf("->") + 2);   
            if (rights.contains("|")){     // 如果有 或 运算，则分开
                String[] littleString = rights.split("\\|");
                for (String str : littleString){
                    ArrayList<String> newGrammar = new ArrayList<>();
                    newGrammar.add(left);
                    newGrammar.add(arrows);
                    newGrammar.add(str);
                    newGrammarArray.add(newGrammar);
                }
            } else{
                ArrayList<String> newGrammar = new ArrayList<>();
                newGrammar.add(left);
                newGrammar.add(arrows);
                newGrammar.add(rights);
                newGrammarArray.add(newGrammar);
            }
        }
        return newGrammarArray;
    }
}