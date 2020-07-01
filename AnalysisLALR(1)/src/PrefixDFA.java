import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

public class PrefixDFA {

    // 字符串表示的 DFA
    public class DFA{
        ArrayList<String> src;
        ArrayList<String> dst;
        char symbol;
        boolean flag;       // 判断 src 和 dst 是否相等，否则会陷入无限循环
    }

    // 数字表示的 DFA
    public class numberDFA{
        Integer src;
        Integer dst;
        char symbol;
    }

    private ArrayList<DFA> dfaList = new ArrayList<>(); // 存储DFA
    private ArrayList<DFA> lalrDFAlist = new ArrayList<>();     // 存储lalrDFA
    private ArrayList<numberDFA> numberDFAList = new ArrayList<>();     // 存储数字表示DFA
    private String[][] slrTable;     // LR(0) 分析表
    private HashMap<ArrayList<String>, Integer> dfaMap = new HashMap<>();   // 建立节点的数字表示映射
    public TreeSet<Character> noTerminal = new TreeSet<>(); // 非终结符
    public TreeSet<Character> isTerminal = new TreeSet<>(); // 终结符
    private HashMap<Character, TreeSet<Character>> firstGather = new HashMap<>();

    public void getFirstGather(HashMap<Character, TreeSet<Character>> firstGathers){
        firstGather = firstGathers;
    }

    /**
     * 计算识别文法活前缀的DFA
     * @param outGrammList
     * @return
     */
    public ArrayList<String> prefixDFA(ArrayList<String> outGrammList) {
        /**
         * 计算起始 src 
         */ 
        String firstGrammar = outGrammList.get(0);
        ArrayList<String> startSrc = new ArrayList<>(); //起始src
        String addPointStr = initialPoint(firstGrammar);
        String addFirstStr = addFirstChar(addPointStr, "$");
        startSrc.add(addFirstStr);     // 将加点后的文法加入

        ArrayList<Character> hasClosure = new ArrayList<>(); // 存储已经闭包过的非终结符
        for (int i = 0; i < startSrc.size(); i++){
            String pointStr = startSrc.get(i);
            if (checkAfterPointIsNoTerminal(pointStr)) { // 如果点后面是非终结符
                char state = getAfterPointChar(pointStr);
                if (!hasClosure.contains(state)) { // 如果该非终结符在该组里面没有被求过
                    ArrayList<String> closures = closure(outGrammList, state); // 求闭包
                    for (String str : closures) {
                        String addPointGrammar = initialPoint(str);
                        String first =  getFirstChar(pointStr, firstGather);
                        String addFirstGrammar = addFirstChar(addPointGrammar, first);
                        startSrc.add(addFirstGrammar);
                    }
                    hasClosure.add(state);
                }
            }
        }
       

        calcDFA(startSrc, outGrammList);
        for (int i = 0; i < dfaList.size(); i++){
            DFA dfa = dfaList.get(i);
            if (! isEqualSrcDst(dfa.src, dfa.dst) && ! isInDFAlist(dfa.dst)){     // 如果 src 不等于 dst 并且dst没有被转换过
                ArrayList<String> temp = dfa.dst;
                calcDFA(temp, outGrammList);
            }
        }

        /**
         * 由于 calcDFA 中没有考虑经过状态转换后，可能有多个文法可以移动点
         * 所以要合并哪些src相等，symbol相等，dst不等的DFA
         */
        //mergeDFA();    
        System.out.println("输出DFA：");
        for (DFA dfa : dfaList) {
            System.out.println(dfa.src);
            System.out.println(dfa.symbol);
            System.out.println(dfa.dst);
            System.out.println("---------------");
        }
        return startSrc;
    }

    public void mergeDFA(){
        // 对每个dfa去掉后缀
        for (DFA dfa : dfaList){
            ArrayList<String> src = dfa.src;
            ArrayList<String> dst = dfa.dst;
            ArrayList<String> newSrc = dropEndStr(src);
            ArrayList<String> newDst = dropEndStr(dst);
            DFA lalrdfa = new DFA();
            lalrdfa.src = newSrc;
            lalrdfa.dst = newDst;
            lalrdfa.symbol = dfa.symbol;
            lalrdfa.flag = false;
        }
    }

    /**
     * 去掉每个DFA状态的后缀
     * @param state
     * @return
     */
    public ArrayList<String> dropEndStr(ArrayList<String> state){
        ArrayList<String> newState = new ArrayList<>();
        for (String str : state){
            int index = str.indexOf(",");
            String newStr = str.substring(0, index);
            newState.add(newStr);
        }
        return newState;
    }

    /**
     * 计算DFA
     * @param pointGrammar  要求的DFA的src，也就是前面DFA的dst
     * @param outGrammList
     */
    public void calcDFA(ArrayList<String> pointGrammar, ArrayList<String> outGrammList){
        for (String grammar : pointGrammar) { // 遍历文法
            if (!checkPointIsEnd(grammar)) {    // 如果该文法的点没有到最后
                ArrayList<String> item = new ArrayList<String>();       // 存储 dst
                char ch = getAfterPointChar(grammar); // 得到转移状态
                if (ch == 'ε')
                    continue;
                
                String movePoint = movePoint(grammar); // 移动点后文法
                item.add(movePoint);

                if (!checkPointIsEnd(movePoint)) {  // 如果点移动后不在最后
                    ArrayList<Character> hasClosure = new ArrayList<>(); // 存储已经闭包过的非终结符
                    for (int c = 0; c < item.size(); c++){
                        String pointStr = item.get(c);
                        if (checkAfterPointIsNoTerminal(pointStr)) { // 如果点后面的是非终结符
                            char terminal = getAfterPointChar(pointStr);
                            if (!hasClosure.contains(terminal)){
                                ArrayList<String> closures = closure(outGrammList, terminal); // 求非终结符的闭包
                                for (String str : closures) {
                                    String addPointGrammar = initialPoint(str); // 加点
                                    String first = getFirstChar(pointStr, firstGather);
                                    String addFirstGrammar = addFirstChar(addPointGrammar, first);
                                    item.add(addFirstGrammar);
                                }
                                hasClosure.add(terminal);
                            }
                        } 
                    }
                }
                if(hasExistDFA(pointGrammar, ch)){// 如果前面有DFA是经过该状态转换的
                    for (DFA dfa : dfaList) {
                        if (dfa.src.containsAll(pointGrammar) && pointGrammar.containsAll(dfa.src)
                                && dfa.symbol == ch) {
                            dfa.dst.addAll(item);
                            break;
                        }
                    }
                }
                else{// 没有就创建新的DFA
                    DFA newDFA = new DFA(); // 创建新的 DFA
                    newDFA.src = pointGrammar;
                    newDFA.symbol = ch;
                    newDFA.dst = item;
                    newDFA.flag = isEqualSrcDst(newDFA.src, newDFA.dst);
                    dfaList.add(newDFA);
                }
            }
        }
    }
    
    /**
     * 得到后面第一个字符的 first 集合
     * @param waitingStr    待求语法
     * @return  求得的 first
     */
    public String getFirstChar(String waitingStr, HashMap<Character, TreeSet<Character>> firstGather){
        int pointIndex = waitingStr.indexOf(".");
        String str = waitingStr.substring(pointIndex + 1, pointIndex + 2);
        if (str.equals(",")){   // 如果点后面就是逗号
            return waitingStr.substring(pointIndex + 2, waitingStr.length());
        }
        else {     
            Character nextChar = waitingStr.charAt(pointIndex + 2);
            str = waitingStr.substring(pointIndex + 2, pointIndex + 3); // 取移动一位后的点后面的字符
            if (str.equals(",")){       // 如果移动一位后，点后面还是逗号
                return waitingStr.substring(pointIndex + 3, waitingStr.length());
            }
            else{   // 移动一位后，点后面不是逗号

                if (noTerminal.contains(nextChar)){      // 如果移动点后面是非终结符
                    TreeSet<Character> firstSet = firstGather.get(nextChar);
                    return firstSet.toString();
                }
                else{
                    return str;
                }
            }
        }
    }
    /**
     * 添加 first 集合
     * @param pointStr  加点后的文法
     * @param first first 集合
     * @return  添加first后的文法
     */
    public String addFirstChar(String pointStr, String first){
        String res = pointStr + "," + first;
        return res;
    }   

    /**
     * 判断该状态是否已经转换过
     * @param dst
     * @return 没有————false, 否则————true
     */
    public boolean isInDFAlist(ArrayList<String> dst){
        for (DFA dfa : dfaList){
            // 只用判断是否有 由该dst产生的DFA即可，也就是判断是否存在和它相同的src
            ArrayList<String> tempSrc = dfa.src;            
            if (dst.containsAll(tempSrc) && tempSrc.containsAll(dst)){
                return true;
            }
        }
        return false;
    }

    /**
     * 计算某个状态的闭包
     * 
     * @param newGrammar 新的文法形式
     * @param state      要计算的状态
     * @return 闭包的文法
     */
    public ArrayList<String> closure(ArrayList<String> newGrammar, char state) {
        ArrayList<String> closureList = new ArrayList<>();
        for (String grammar : newGrammar) {
            if (grammar.charAt(0) == state) {
                closureList.add(grammar);
            }
        }
        return closureList;
    }

    public String[][] analyzeTable(ArrayList<String> newGrammarArray, 
                        HashMap<Character, TreeSet<Character>> followGather){
        Object[] isTer = isTerminal.toArray();
        Object[] noTer = noTerminal.toArray();

        int length = isTer.length + noTer.length + 1;       // 还要加上 "$" 和 00处
        slrTable = new String[dfaMap.size() + 1][length];
        // 初始化文法分析表，设置第一行和第一列
        slrTable[0][0] = "State";
        // 第一行，先添加终结符
        for (int i = 0; i < isTer.length; i++){
            slrTable[0][i + 1] = isTer[i].toString();
        }

        slrTable[0][isTer.length + 1] = "$";

        // 第一行， 非终结符
        for (int i = isTer.length + 2; i < length; i++){
            if (noTer[i - isTer.length - 1].toString().charAt(0) == '@')
                continue;
            slrTable[0][i] = noTer[i - isTer.length - 1].toString();
        }
        // 第一列, 根据状态数设置
        for (int i = 0; i < dfaMap.size(); i++){
            slrTable[i + 1][0] = String.valueOf(i);
        }

        // 对每一个 DFA 状态
        for (int i = 0; i < dfaMap.size(); i++){    // 一行一行的分析
            Set<Entry<ArrayList<String>, Integer>> mst = dfaMap.entrySet();
			for (Entry<ArrayList<String>, Integer> entry : mst) {
                if (entry.getValue() == i){         // 找到第i个序号对应的状态
                    ArrayList<String> end = entry.getKey();

                    for (String grammar : end){

                        // 如果是带@并且最后为 . ，则在 $ 处 acc
                        if (grammar.contains("@") && checkPointIsEnd(grammar)){
                            for (int j = 1; j < length; j++) {
                                if (slrTable[0][j] == "$") {
                                    slrTable[i + 1][j] = "acc";
                                    break;
                                }
                            }
                        }
                        // 如果文法的点在最后，或者点后面跟着空，说明要规约
                        else if (checkPointIsEnd(grammar) || checkEafterPoint(grammar)){
                            int index1 = grammar.indexOf(",");
                            String dropStr = grammar.substring(0, index1);  // 先去掉逗号和其后面的内容
                            String endStr = grammar.substring(index1 + 1, grammar.length());    // 获得逗号后面的内容
                            String dropPoint = dropStr.replace(".", ""); // 去掉点
                            int index = newGrammarArray.indexOf(dropPoint); // 在文法中找到去掉点后的文法位置
                        
                            for (int m = 0; m < endStr.length(); m++){        // 只在 逗号后面 位置处规约
                                for (int j = 1; j < length; j++){
                                    if (slrTable[0][j].equals(String.valueOf(endStr.charAt(m)))){
                                        slrTable[i + 1][j] = "r" + String.valueOf(index);
                                    }
                                }
                            }
                        }
                        // 如果都不是，写转换序号
                        else {
                            for (numberDFA dfa : numberDFAList) {
                                if (dfa.src.equals(i)) { // 找到该序号的DFA
                                    for (int j = 1; j < length; j++) {
                                        if (slrTable[0][j].charAt(0) == dfa.symbol) { // 确定纵坐标
                                            String target = "";
                                            if (Character.isUpperCase(dfa.symbol)) { // 如果是大写字母
                                                target += String.valueOf(dfa.dst); // 要填入的字符不加 S
                                            } else {
                                                target += "s" + String.valueOf(dfa.dst); // 要填入的字符加 S
                                            }
                                            slrTable[i + 1][j] = target;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        
                    }
                    
                }
            }
            
        }
        return slrTable;
    }

    /**
     * 输出LR(1)分析表
     * 
     * @param slrTable
     */
    public String displayLLTable(String[][] slrTable) {
        String output = "";
        for (int i = 0; i < slrTable.length; i++) {
            for (int j = 0; j < slrTable[i].length; j++) {
                if (slrTable[i][j] == null) {
                    output += String.format("%5s", " ");
                    System.out.printf("%10s", " ");
                } else {
                    output += String.format("%5s", slrTable[i][j]);
                    System.out.printf("%10s", slrTable[i][j]);
                }
            }
            output += "\n";
            System.out.print("\n");
        }
        return output;
    }

    /**
     * 建立状态转换映射，把状态变成用数字表示
     * @param startStr  //起始状态
     * @return  映射关系
     */
    public HashMap<ArrayList<String>, Integer> mapStateOfDFA(ArrayList<String> startStr){
        Integer staticNumber = 1;
        dfaMap.put(startStr, 0);    // 起始节点设置为0
        for (DFA dfa : dfaList){
            ArrayList<String> tempSrc = dfa.src;
            ArrayList<String> tempDst = dfa.dst;
            if (! dfaMap.containsKey(tempSrc)){     // 如果不包含该键，说明还没有映射
                dfaMap.put(tempSrc, staticNumber++);
            }
            if (! dfaMap.containsKey(tempDst)){
                dfaMap.put(tempDst, staticNumber++);
            }
        }
        return dfaMap;
    }

    /**
     * DFA的数字表示
     */
    public void changeToNumber(){
        for (DFA dfa : dfaList){
            numberDFA newDFA = new numberDFA();
            newDFA.src = dfaMap.get(dfa.src);
            newDFA.dst = dfaMap.get(dfa.dst);
            newDFA.symbol = dfa.symbol;
            numberDFAList.add(newDFA);
        }
        for (numberDFA dfa : numberDFAList){
            System.out.println(dfa.src + "-------->" + dfa.dst + "-----" + dfa.symbol);
        }
    }

    /**
     * 判断是否已经存在DFA状态
     */
    public boolean hasExistDFA(ArrayList<String> pointGrammar, char symbol){
        for (DFA dfa : dfaList) {
            if (dfa.src.containsAll(pointGrammar) && pointGrammar.containsAll(dfa.src) && dfa.symbol == symbol) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断 src 和 dst 是否相等
     * @param src   
     * @param dst
     * @return  相等————true，否则————false
     */
    public boolean isEqualSrcDst(ArrayList<String> src, ArrayList<String> dst){
        if (src.containsAll(dst) && dst.containsAll(src)){
            return true;
        }
        return false;
    }
    /**
     * 返回点后面的字符
     * @param pointGrammar  文法
     * @return  字符
     */
    public char getAfterPointChar(String pointGrammar){
        int pointIndex = pointGrammar.indexOf(".");
        return pointGrammar.charAt(pointIndex + 1);
    }
    /**
     * 初始化点的位置
     * 
     * @param grammar 一条文法
     * @return 初始化点后文法
     */
    public String initialPoint(String grammar) {
        StringBuffer str = new StringBuffer(grammar);
        int index = str.indexOf("->") + 2;
        str.insert(index, ".");
        return str.toString();
    }

    /**
     * 移动点的位置到下一位
     * @param grammar
     * @return  移动后的文法
     */
    public String movePoint(String grammar) {
        StringBuffer str = new StringBuffer(grammar);
        int pointIndex = str.indexOf(".");
        str.deleteCharAt(pointIndex);
        str.insert(pointIndex + 1, ".");
        return str.toString();
    }

    /**
     * 检查点的下一位字符是终结符还是非终结符
     * @param ch  要检查的符号
     * @return  是非终结符————true, 否则————false
     */
    public boolean checkAfterPointIsNoTerminal(String pointGrammar){
        int pointIndex = pointGrammar.indexOf(".");
        char ch = pointGrammar.charAt(pointIndex + 1);
        if (isTerminal.contains(ch)){
            return false;
        }
        return true;
    }

    /**
     * 特殊情况，检查点后面是不是空
     */
    public boolean checkEafterPoint(String pointGrammar){
        int pointIndex = pointGrammar.indexOf(".");
        char ch = pointGrammar.charAt(pointIndex + 1);
        if (ch == 'ε'){
            return true;
        }
        return false;
    }

    /**
     * 检查点是否已经移动到最后一位
     * @param pointGrammar  文法
     * @return  到最后一位————true，否则————false
     */
    public boolean checkPointIsEnd(String pointGrammar){
        int pointIndex = pointGrammar.indexOf(".");
        if (pointGrammar.charAt(pointIndex + 1) == ','){        // 如果点后面是逗号，说明在最后一位了
            return true;
        }
        return false;
    }

    /**
     * 显示识别文法活前缀活DFA
     * @param startSrc
     */
    public void displayDFA(ArrayList<String> startSrc){
        GraphViz gViz = new GraphViz("../images", 
        "D:\\ToolsSoftware\\Graphviz\\bin\\dot.exe", "prefixDFA", "prefixDFACode.txt");
        gViz.start_graph();
        gViz.addCircle();
        String start = changeToString(startSrc);
        gViz.addStart(start);
        for (DFA dfa : dfaList){
            String tempSrc = changeToString(dfa.src);
            String tempDst = changeToString(dfa.dst);
            gViz.addEdge(tempSrc, tempDst, dfa.symbol);
        }
        gViz.end_graph();
        try {
            gViz.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 把DFA状态变成String，以便画图
     */
    public String changeToString(ArrayList<String> startSrc){
        String res = "\"";
        for (String str : startSrc){
            res += str.concat("\n");
        }
        res += "\"";
        return res;
    }

    /**
     * 改变文法的形式，把文法分离为最小单元，并以 String 为单位存储
     * 
     * @param removeLeftCommon 消除左递归和左因子后的文法
     * @return 改进的后文法，二维字符串数组
     */
    public ArrayList<String> changeForm(ArrayList<String> removeLeftCommon) {
        ArrayList<String> newGrammarArray = new ArrayList<String>();
        for (String grammar : removeLeftCommon) {
            String left = grammar.substring(0, grammar.indexOf("->"));
            String rights = grammar.substring(grammar.indexOf("->") + 2);
            if (rights.contains("|")) { // 如果有 或 运算，则分开
                String[] littleString = rights.split("\\|");
                for (String str : littleString) {
                    String newGrammar = "";
                    newGrammar += left;
                    newGrammar += "->";
                    newGrammar += str;
                    newGrammarArray.add(newGrammar);
                }
            } else {
                String newGrammar = "";
                newGrammar += left;
                newGrammar += "->";
                newGrammar += rights;
                newGrammarArray.add(newGrammar);
            }
        }
        return newGrammarArray;
    }

    /**
     * 获取终结符和非终结符
     * 
     * @param LR(1)文法
     * @return 终结符和非终结符
     */
    public void getTerminal(ArrayList<String> llgrammarArray) {
        // 获取非终结符
        for (String grammar : llgrammarArray) {
            noTerminal.add(grammar.split("->")[0].charAt(0));
        }
        // 获取终结符
        for (String grammar : llgrammarArray) {
            for (char ch : grammar.split("->")[1].toCharArray()) {
                if (!noTerminal.contains(ch))
                    isTerminal.add(ch);
            }
        }
        System.out.println("终结符：");
        System.out.println(isTerminal);
        System.out.println("非终结符：");
        System.out.println(noTerminal);
    }
}