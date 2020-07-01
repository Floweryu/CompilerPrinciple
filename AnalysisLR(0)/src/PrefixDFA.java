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
    private ArrayList<numberDFA> numberDFAList = new ArrayList<>();     // 存储数字表示DFA
    private String[][] lrTable;     // LR(0) 分析表
    private HashMap<ArrayList<String>, Integer> dfaMap = new HashMap<>();   // 建立节点的数字表示映射
    public TreeSet<Character> noTerminal = new TreeSet<>(); // 非终结符
    public TreeSet<Character> isTerminal = new TreeSet<>(); // 终结符

    /**
     * 计算识别文法活前缀的DFA
     * 
     * @param outGrammList
     * @return
     */
    public ArrayList<String> prefixDFA(ArrayList<String> outGrammList) {
        /**
         * 计算起始 src
         */
        String firstGrammar = outGrammList.get(0);
        ArrayList<String> startSrc = new ArrayList<>(); // 起始src
        String addPointStr = initialPoint(firstGrammar);
        startSrc.add(addPointStr); // 将加点后的文法加入

        ArrayList<Character> hasClosure = new ArrayList<>(); // 存储已经闭包过的非终结符
        for (int i = 0; i < startSrc.size(); i++) {
            String pointStr = startSrc.get(i);
            if (checkAfterPointIsNoTerminal(pointStr)) { // 如果点后面是非终结符
                char state = getAfterPointChar(pointStr);
                if (!hasClosure.contains(state)) { // 如果该非终结符在该组里面没有被求过
                    ArrayList<String> closures = closure(outGrammList, state); // 求闭包
                    for (String str : closures) {
                        String addPointGrammar = initialPoint(str);
                        startSrc.add(addPointGrammar);
                    }
                    hasClosure.add(state);
                }
            }
        }

        calcDFA(startSrc, outGrammList);
        for (int i = 0; i < dfaList.size(); i++) {
            DFA dfa = dfaList.get(i);
            if (!isEqualSrcDst(dfa.src, dfa.dst) && !isInDFAlist(dfa.dst)) { // 如果 src 不等于 dst 并且dst没有被转换过
                ArrayList<String> temp = dfa.dst;
                calcDFA(temp, outGrammList);
            }
        }

        /**
         * 由于 calcDFA 中没有考虑经过状态转换后，可能有多个文法可以移动点 所以要合并哪些src相等，symbol相等，dst不等的DFA
         */
        // mergeDFA();
        System.out.println("输出DFA：");
        for (DFA dfa : dfaList) {
            System.out.println(dfa.src);
            System.out.println(dfa.symbol);
            System.out.println(dfa.dst);
            System.out.println("---------------");
        }
        return startSrc;
    }

    /**
     * 计算DFA
     * 
     * @param pointGrammar 要求的DFA的src，也就是前面DFA的dst
     * @param outGrammList
     */
    public void calcDFA(ArrayList<String> pointGrammar, ArrayList<String> outGrammList) {
        for (String grammar : pointGrammar) { // 遍历文法
            if (!checkPointIsEnd(grammar)) { // 如果该文法的点没有到最后
                ArrayList<String> item = new ArrayList<String>(); // 存储 dst
                char ch = getAfterPointChar(grammar); // 得到转移状态
                if (ch == 'ε')
                    continue;

                String movePoint = movePoint(grammar); // 移动点后文法
                item.add(movePoint);

                if (!checkPointIsEnd(movePoint)) { // 如果点移动后不在最后
                    ArrayList<Character> hasClosure = new ArrayList<>(); // 存储已经闭包过的非终结符
                    for (int c = 0; c < item.size(); c++) {
                        String pointStr = item.get(c);
                        if (checkAfterPointIsNoTerminal(pointStr)) { // 如果点后面的是非终结符
                            char terminal = getAfterPointChar(pointStr);
                            if (!hasClosure.contains(terminal)) {
                                ArrayList<String> closures = closure(outGrammList, terminal); // 求非终结符的闭包
                                for (String str : closures) {
                                    String addPointGrammar = initialPoint(str); // 加点
                                    item.add(addPointGrammar);
                                }
                                hasClosure.add(terminal);
                            }
                        }
                    }
                }
                if (hasExistDFA(pointGrammar, ch)) {// 如果前面有DFA是经过该状态转换的
                    for (DFA dfa : dfaList) {
                        if (dfa.src.containsAll(pointGrammar) && pointGrammar.containsAll(dfa.src)
                                && dfa.symbol == ch) {
                            dfa.dst.addAll(item);
                            break;
                        }
                    }
                } else {// 没有就创建新的DFA
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
     * 合并DFA
     */
    public void mergeDFA(){
        for (int i = 0; i < dfaList.size(); i++){
            for (int j = 0; j < dfaList.size(); j++){
                if (j != i){
                    DFA oneDFA = dfaList.get(i);
                    DFA twoDFA = dfaList.get(j);
                    // 如果两个DFA的起始符合转换符相等，但是终止符不一样，应该合并
                    if (isEqualSrcDst(oneDFA.src, twoDFA.src) && oneDFA.symbol == twoDFA.symbol
                             && ! isEqualSrcDst(oneDFA.dst, twoDFA.dst)){
                        oneDFA.dst.addAll(twoDFA.dst);     // 添加到 i 上    
                        dfaList.remove(j); // 把j 的那个删掉
                        dfaList.get(i).dst = oneDFA.dst;
                        for (DFA dfa : dfaList){
                            if (isEqualSrcDst(dfa.src, twoDFA.dst)){
                                dfa.src = oneDFA.dst; 
                            }
                        }
                    }

                }
            }
        }
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

    public String[][] analyzeTable(ArrayList<String> newGrammarArray){
        Object[] isTer = isTerminal.toArray();
        Object[] noTer = noTerminal.toArray();

        int length = isTer.length + noTer.length + 1;       // 还要加上 "$" 和 00处
        lrTable = new String[dfaMap.size() + 1][length];
        // 初始化文法分析表，设置第一行和第一列
        lrTable[0][0] = "State";
        // 第一行，先添加终结符
        for (int i = 0; i < isTer.length; i++){
            lrTable[0][i + 1] = isTer[i].toString();
        }

        lrTable[0][isTer.length + 1] = "$";

        // 第一行， 非终结符
        for (int i = isTer.length + 2; i < length; i++){
            if (noTer[i - isTer.length - 1].toString().charAt(0) == '@')
                continue;
            lrTable[0][i] = noTer[i - isTer.length - 1].toString();
        }
        // 第一列, 根据状态数设置
        for (int i = 0; i < dfaMap.size(); i++){
            lrTable[i + 1][0] = String.valueOf(i);
        }

        for (int i = 0; i < dfaMap.size(); i++){    // 一行一行的分析
            Set<Entry<ArrayList<String>, Integer>> mst = dfaMap.entrySet();
			for (Entry<ArrayList<String>, Integer> entry : mst) {
                if (entry.getValue() == i){         // 找到第i个序号对应的状态
                    ArrayList<String> end = entry.getKey();
                    // 如果是带@的状态，则 acc
                    if (end.size() == 1 && end.get(0).contains("@")){
                        for (int j = 1; j < length; j++){
                            if (lrTable[0][j] == "$"){
                                lrTable[i + 1][j] = "acc";
                            }
                        }
                    }
                    // 如果是结尾状态，进行规约
                    else if (end.size() == 1 && checkPointIsEnd(end.get(0))){   
                        String dropPoint = end.get(0).replace(".", "");     // 去掉点
                        int index = newGrammarArray.indexOf(dropPoint);     // 在文法中找到去掉点后的文法位置
                        for (int j = 1; j <= isTer.length + 1; j++)          // 在非终结符部分规约
                            lrTable[i + 1][j] = "r" + String.valueOf(index);
                    }
                    // 如果都不是，写转换序号
                    else{
                        for (numberDFA dfa : numberDFAList) {
                            if (dfa.src.equals(i)) { // 找到该序号的DFA
                                for (int j = 1; j < length; j++) {
                                    if (lrTable[0][j].charAt(0) == dfa.symbol) { // 确定纵坐标
                                        String target = "";
                                        if (Character.isUpperCase(dfa.symbol)) { // 如果是大写字母
                                            target += String.valueOf(dfa.dst); // 要填入的字符不加 S
                                        } else {
                                            target += "s" + String.valueOf(dfa.dst); // 要填入的字符加 S
                                        }
                                        lrTable[i + 1][j] = target;
                                        break;
                                    }
                                }

                            }
                        }
                    }
                }
            }
            
        }
        return lrTable;
    }

    /**
     * 输出LR(0)分析表
     * 
     * @param lrTable
     */
    public String displayLLTable(String[][] lrTable) {
        String output = "";
        for (int i = 0; i < lrTable.length; i++) {
            for (int j = 0; j < lrTable[i].length; j++) {
                if (lrTable[i][j] == null) {
                    output += String.format("%5s", " ");
                    System.out.printf("%10s", " ");
                } else {
                    output += String.format("%5s", lrTable[i][j]);
                    System.out.printf("%10s", lrTable[i][j]);
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
     * 判断是否已经存在DFA状态
     */
    public boolean hasExistDFA(ArrayList<String> pointGrammar, char symbol) {
        for (DFA dfa : dfaList) {
            if (dfa.src.containsAll(pointGrammar) && pointGrammar.containsAll(dfa.src) && dfa.symbol == symbol) {
                return true;
            }
        }
        return false;
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
     * 检查点是否已经移动到最后一位
     * @param pointGrammar  文法
     * @return  到最后一位————true，否则————false
     */
    public boolean checkPointIsEnd(String pointGrammar){
        int pointIndex = pointGrammar.indexOf(".");
        if (pointIndex == pointGrammar.length() - 1){
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
     * @param LL(1)文法
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