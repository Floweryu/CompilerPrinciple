import java.util.ArrayList;

public class NFAtoDFA {

    /**
     * NFA 的一个单元
     */
    public static class NFA{
        int src;
        char edge;
        int dst;
    }

    /**
     * 新的 DFA 单元
     */
    public static class DFA{
        ArrayList<Integer> src;
        char edge;
        ArrayList<Integer> dst;
    }

    public static ArrayList<NFA> nfaList = new ArrayList<>();
    public static ArrayList<DFA> dfaList = new ArrayList<>();

    /**
     * 获得表达式的字母表
     * @param originalString
     * @return
     */
    public static String getAlphabet(String originalString){
        StringBuffer alphabet = new StringBuffer();
        int stringLength = originalString.length();
        for (int i = 0; i < stringLength; i++){
            char ch = originalString.charAt(i);
            String ch1 = String.valueOf(ch);
            if (Character.isLetter(ch) && !alphabet.toString().contains(ch1)){      // 找到字符并且没有添加
                alphabet.append(ch);
            }
        }
        return alphabet.toString();
    }
    /**
     * 将NFA单元简单化
     * @param nfaCell
     * @return NFA
     */
    public static void transNFA(RPNtoNFA.NFAcell nfaCell){
        int nfaLength = nfaCell.edgeList.size();
        for (int i = 0; i < nfaLength; i++){
            NFA newNFA = new NFA();
            RPNtoNFA.Edge nfaEdge = nfaCell.edgeList.get(i);
            newNFA.src = nfaEdge.startName;
            newNFA.dst = nfaEdge.endName;
            newNFA.edge = nfaEdge.transSymbol;
            nfaList.add(newNFA);
        }
    }

    public static void displayDFA(ArrayList<DFA> dfaList, ArrayList<Integer> dfaStartState, ArrayList<ArrayList<Integer>> dfaEndState){
        GraphViz gViz = new GraphViz("LexicalAnalysis/images",
                "D:\\ToolsSoftware\\Graphviz\\bin\\dot.exe", "dfaGif", "dfaCode.txt");
        gViz.start_graph();
        gViz.addDoubleCircle();     // 添加终止节点，双层圆
        int endLength = dfaEndState.size();
        for (int i = 0; i < endLength; i++){
            String tempEnd = String.valueOf(dfaEndState.get(i));
            //System.out.println(tempEnd.replace("[", "\"").replace("]", "\""));
            // 给字符串去掉中括号，并两边加上引号，以满足 dot 语法要求
            gViz.addDoubleNode(tempEnd.replace("[", "\"").replace("]", "\""));
        }
        gViz.addCircle();       //添加单层圆
        String tempStart = String.valueOf(dfaStartState);
        String newStart = tempStart.replace("[", "\"").replace("]", "\"");
        gViz.addStart(newStart);
        int dfaLength = dfaList.size();
        for (int i = 0; i < dfaLength; i++){
            DFA dfa = dfaList.get(i);
            String tempSrc = String.valueOf(dfa.src);
            String tempDst = String.valueOf(dfa.dst);
            //给字符串去掉中括号，并两边加上引号，以满足 dot 语法要求
            String newDst = tempDst.replace("[", "\"").replace("]", "\"");
            String newSrc = tempSrc.replace("[", "\"").replace("]", "\"");
            
            gViz.addEdge(newSrc, newDst, dfa.edge);
        }
        gViz.end_graph();
        try {
            gViz.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 由起始节点开始闭包，求出过程中各个闭包的闭包
     * 
     * @param setp  起始节点的数组形式
     * @param alphabet 正则表达式的字母表
     * @return 所有闭包的状态节点，是一个二维数组
     */
    public static ArrayList<ArrayList<Integer>> allClosure(ArrayList<Integer> setp, String alphabet){
        ArrayList<ArrayList<Integer>> storeState = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> state = NFAtoDFA.closure(setp);
        storeState.add(state);
        for (int k = 0; k < storeState.size(); k++) {
            for (int i = 0; i < alphabet.length(); i++) {
                // 先进行字母闭包
                ArrayList<Integer> alphaState = NFAtoDFA.moveTo(storeState.get(k), alphabet.charAt(i));
                // System.out.println("对字母进行的闭包结果");
                // showAlphaClosure(alphaState);

                // 再进行空字符闭包
                ArrayList<Integer> emptyState = NFAtoDFA.closure(alphaState);
                // System.out.println("再对空字符串进行闭包的结果");
                // showEmptyClosure(emptyState);

                // 如果闭包后不为空，则添加到DFA表中
                if (!emptyState.isEmpty()) {
                    NFAtoDFA.DFA dfa = new NFAtoDFA.DFA();
                    dfa.src = storeState.get(k);
                    dfa.edge = alphabet.charAt(i);
                    dfa.dst = emptyState;
                    dfaList.add(dfa);
                }

                if (!NFAtoDFA.checkInMerge(storeState, emptyState) && ! emptyState.isEmpty()) {
                    storeState.add(emptyState);
                }

            }
        }
        return storeState;
    }

    /**
     * 获取包含NFA终止节点的状态集合
     * @param dfaList   DFA 集合
     * @param nfaEndState   NFA 终止节点
     * @return  包含NFA终止节点的集合
     */
    public static ArrayList<ArrayList<Integer>> selectEndlist(ArrayList<DFA> dfaList, int nfaEndState){
        ArrayList<ArrayList<Integer>> dfaEndState = new ArrayList<ArrayList<Integer>>();
        int dfaLength = dfaList.size();
        for (int i = 0; i < dfaLength; i++){
            ArrayList<Integer> src = dfaList.get(i).src;
            ArrayList<Integer> dst = dfaList.get(i).dst;
            if (src.contains(nfaEndState) && !dfaEndState.contains(src)){
                dfaEndState.add(src);
            }
            if (dst.contains(nfaEndState) && !dfaEndState.contains(dst)){
                dfaEndState.add(dst);
            }
        }
        return dfaEndState;
    }

    /**
     * 获取 DFA 的初态，包含 NFA 起始状态的闭包
     * @param setp  NFA 的起始状态
     * @return DFA 的初态
     */
    public static ArrayList<Integer> selectStartList(int setp){
        ArrayList<Integer> temp = new ArrayList<>();
        temp.add(setp);
        ArrayList<Integer> state = NFAtoDFA.closure(temp);
        return state;
    }

    /**
     * 输出简洁型的NFA
     */
    public static void showNFA(){
        int nfaLength = nfaList.size();
        for (int i = 0; i < nfaLength; i++){
            NFA nfa = nfaList.get(i);
            System.out.printf(nfa.src + "——>" + nfa.dst + "————" + nfa.edge + "\n");
        }
    }

    /**
     * 输出 字母 的闭包结果
     * @param alphaState
     */
    public static void showAlphaClosure(ArrayList<Integer> alphaState){
        int length = alphaState.size();
        for (int j = 0; j < length; j++) {
            System.out.printf(alphaState.get(j) + " zf ");
        }
        System.out.printf("\n");
    }
    
    /**
     * 输出 空字符 的闭包结果
     * @param emptyState
     */
    public static void showEmptyClosure(ArrayList<Integer> emptyState){
        for (int j = 0; j < emptyState.size(); j++) {
            System.out.printf(emptyState.get(j) + " ");
        }
        System.out.printf("\n");
    }

    /**
     * 输出存储新 DFA 闭包节点的内容
     * @param storeState
     */
    public static void showStoreState(ArrayList<ArrayList<Integer>> storeState){
        int storeLength = storeState.size();
        for (int i = 0; i < storeLength; i++) {
            System.out.printf(storeState.get(i) + "\n");
        }
    }

    /**
     * 输出 DFA 形式
     * @param dfaList
     */
    public static void showDFA(ArrayList<DFA> dfaList){
        int dfaLength = dfaList.size();
        for (int i = 0; i < dfaLength; i++) {
            NFAtoDFA.DFA newDFA = dfaList.get(i);
            System.out.printf(newDFA.src + "————>" + newDFA.dst + "   " + newDFA.edge + "\n");
        }
    }
    /**
     * 判断节点是否被包含在合并态中
     * @param setp 合并的状态
     * @param state 要判断的节点
     * @return  被包含————true，否则————false
     */
    public static boolean check(ArrayList<Integer> setp, int dst){
        if (setp.contains(dst))
            return true;
        return false;
    }

    /**
     * 判断闭包后是否原本就已经存在
     * @param storeState    所有闭包的集合
     * @param setp  闭包结果
     * @return  本来就在————true，否则————false
     */
    public static boolean checkInMerge(ArrayList<ArrayList<Integer>> storeState, ArrayList<Integer> setp){
        if (storeState.contains(setp))
            return true;
        return false;
    }

    /**
     * 状态节点关于 "#" 的闭包
     * @param mergeState 要计算闭包的节点集合
     * @return 对 空 的闭包，允许对增加的新元素也闭包, 所以在对字母闭包结果的基础上再闭包
     */
    public static ArrayList<Integer> closure(ArrayList<Integer> mergeState){
        int nfaLength = nfaList.size();
        for (int i = 0; i < mergeState.size(); i++){
            for (int j = 0; j < nfaLength; j++){
                NFA nfa = nfaList.get(j);
                // 在nfaList中找到与 setp 节点状态相同的起始节点，并且转换状态是 '#'
                if (mergeState.get(i) == nfa.src && nfa.edge == '#'){
                    if (! check(mergeState, nfa.dst)){  // 若终止节点不在mergeState中， 这里
                        mergeState.add(nfa.dst);
                    }
                }
            }
        }
        return mergeState;
    }

    /**
     * 状态节点关于 字母表 的闭包
     * @param mergeState 要计算闭包的节点集合
     * @param symbol     转换字符
     * @return 对字符的闭包，存储在新的集合中，否则会把新加入节点也进行闭包运算
     */
    public static ArrayList<Integer> moveTo(ArrayList<Integer> mergeState, char symbol){
        ArrayList<Integer> newMerge = new ArrayList<>();
        int mergeLength = mergeState.size();
        int nfaLength = nfaList.size();
        for (int i = 0; i < mergeLength; i++){
            for (int j = 0; j < nfaLength; j++){
                NFA nfa = nfaList.get(j);
                if (mergeState.get(i) == nfa.src && nfa.edge == symbol){    //如果存在
                    if (! check(newMerge, nfa.dst)){    // 在新节点集合中检查是否已经存在
                        newMerge.add(nfa.dst);      
                    }
                }
            }
        }
        return newMerge;
    }

}