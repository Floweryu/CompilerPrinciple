import java.util.ArrayList;
import java.util.LinkedList;

public class DFAtoMiniDFA {

    public static class newDFA{
        int src;
        char symbol;
        int dst;
    }

    public static LinkedList<ArrayList<Integer>> MiniDFA_Group = new LinkedList<ArrayList<Integer>>();; // 最小化DFA的分组
    public static ArrayList<newDFA> MiniDFA = new ArrayList<newDFA>();
    public static int startIndex;       // 未化简之前的起始节点的下标号(即用来代替起始节点的序号)
    public static int miniStart;        // miniDFA的起始节点
    public static ArrayList<Integer> EndIndex = new ArrayList<>();  // 未化简之前的终止节点的下标号(即用来代替终止节点的序号)
    public static ArrayList<Integer> miniEnd = new ArrayList<>();       // miniDFA 的终止节点

    /**
     * 可视化 mini-DFA
     * @param MiniDFA   
     * @param start
     * @param end
     */
    public static void displayMiniDFA(ArrayList<newDFA> MiniDFA, int start, ArrayList<Integer> end){
        GraphViz gViz = new GraphViz("LexicalAnalysis/images",
                "D:\\ToolsSoftware\\Graphviz\\bin\\dot.exe", "mini-dfaGif", "mini-dfaCode.txt");
        gViz.start_graph();
        gViz.addDoubleCircle();     // 添加终止节点
        int endLength = end.size();
        for (int i = 0; i < endLength; i++){
            String tempEnd = String.valueOf(end.get(i));
            gViz.addDoubleNode(tempEnd);
        }
        // 添加起始节点
        gViz.addCircle();       // 添加单层圆
        String tempStart = String.valueOf(start);
        gViz.addStart(tempStart);
        // 添加 miniDFA链接关系
        int dfaLength = MiniDFA.size();
        for (int i = 0; i < dfaLength; i++){
            newDFA dfa = MiniDFA.get(i);
            String src = String.valueOf(dfa.src);
            String dst = String.valueOf(dfa.dst);
            gViz.addEdge(src, dst, dfa.symbol);
        }
        gViz.end_graph();
        try {
            gViz.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * DFA最小化
     * @param newdfa    原来的dfa
     * @param alphabet  字母表
     */
    public static void miniDFA(ArrayList<newDFA> newdfa, String alphabet){
        boolean split = true;
        while (split) {
            split = false;
            for (ArrayList<Integer> group : MiniDFA_Group)
            {
                int alphaLength = alphabet.length();
                for (int i = 0; i < alphaLength; i++)
                {
                    char ch = alphabet.charAt(i);
                    ArrayList<ArrayList<Integer>>  splitGroup = groupUp(group, newdfa, ch);     // 分裂后的新组
                    
                    if (splitGroup.size() == 1){
                        continue;       // 未发生分裂
                    }
                    else {
                        split = true;   // 分裂
                        MiniDFA_Group.remove(group);    // 把旧的分组删除
                        for (ArrayList<Integer> newGroup : splitGroup){     //把新的分组添加到 MiniDFA_Group 中
                            MiniDFA_Group.add(newGroup);
                        }
                    }
                    if (split)      // 如果对一个字母来说已经发生了分裂，就直接跳出该次循环，进行下一轮分裂 
                        break;
                }
                if (split)  
                    break;
            }
        }

        int miniLength = MiniDFA_Group.size();
        for (int i = 0; i < miniLength; i++)
        {
            int stateIndex = MiniDFA_Group.get(i).get(0);       // 都分好组后，只用选取第一个状态作为代表就行
            int alphaLength = alphabet.length();

            for (int j = 0; j < alphaLength; j++)               // 对每一个输入状态  
            {
                char symbol = alphabet.charAt(j);
                int dfaLength = newdfa.size();
                for (int k = 0; k < dfaLength; k++)             // 在原来 DFA 中寻找转换状态
                {
                    newDFA dfa = newdfa.get(k);
                    if (dfa.src == stateIndex && dfa.symbol == symbol)
                    {  // 从 NFA 中找到该状态转换
                        int toIndex = dfa.dst;
                        for (int m = 0; m < miniLength; m++)     // 寻找转换状态所在的新分组的序号
                        {
                            if (MiniDFA_Group.get(m).contains(toIndex)){
                                // 以新分组的组号代替原来的状态号
                                newDFA miniDFA = new newDFA();  
                                miniDFA.src = i;
                                miniDFA.dst = m;
                                miniDFA.symbol = symbol;
                                MiniDFA.add(miniDFA);
                            }
                        }
                    }
                }
            }
        }

        // 在新分的组下，获取开始状态
        for (int i = 0; i < miniLength; i++){
            ArrayList<Integer> curGroup = MiniDFA_Group.get(i);
            if (curGroup.contains(startIndex)){  // 如果包含起始状态
                miniStart = i; 
                break;          // 起始状态只有一个     
            }
        }

        // 在新的分组下， 获取终结状态
        for (int i = 0; i < miniLength; i++){
            ArrayList<Integer> curGroup = MiniDFA_Group.get(i);
            for (int end : EndIndex){   
                if (curGroup.contains(end)){
                    miniEnd.add(i);
                    break;      // 只要心分组中包含一个终止节点，则该分组就是终止节点
                }
            }
        }

    }

    /**
     *  对原来的每一个分组，获取分裂后产生的新组
     * @param group     原来的分组
     * @param newdfa    原来序号的 dfa 
     * @param symbol    转换状态
     * @return      分裂后产生的新组
     */
    public static ArrayList<ArrayList<Integer>> groupUp(ArrayList<Integer> group, ArrayList<newDFA> newdfa, char symbol)
    {
        ArrayList<ArrayList<Integer>> groups = new ArrayList<ArrayList<Integer>>(); // 新产生的分组集合
        ArrayList<Integer> groupID = new ArrayList<Integer>(); // 在 group 上需要新产生的分组序号
        ArrayList<Integer> toList = new ArrayList<Integer>();  // 每次转换对应要去的组号————group 中有多少字符，对应的转换序号有多少个
        for (int state : group)
        {        // 对该状态区的每一个状态
            int toGroupID = -1;
            int dfaLength = newdfa.size();
            boolean flag = true;    // 判断是否从 dfa中找到转换状态

            for (int i = 0; i < dfaLength; i++)
            {
                newDFA dfa = newdfa.get(i);
                if (dfa.src == state && dfa.symbol == symbol){  // 如果在 dfa 中找到去向
                    // 遍历所有分区，找到该去向所在的分区号
                    for (int j = MiniDFA_Group.size() - 1; j >= 0; j--){    
                        if (MiniDFA_Group.get(j).contains(dfa.dst)){
                            toGroupID = j;
                            flag = false;
                            break;
                        }
                    }
                }
            }
            if (flag){    // 没找到
                toGroupID = -1;
            }
            toList.add(toGroupID);

            if (!groupID.contains(toGroupID)){   // 检查这个分组的ID是否已经存在
                groupID.add(toGroupID);         // 不存在的话，就添加ID
                groups.add(new ArrayList<Integer>());       // 创建一个新的组
            }
        }
        /**
         *      a   b
         *  1   6   3
         *  2   7   3   
         *  3   1   5
         *  4   4   6
         *  ---------
         *  5   7   3
         *  ····
         * 对上面例子，在对 a 的转换中：MiniDFA_Group 有两组，序号为 0 , 1
         *  toList ： 1, 1, 0, 0        // 长度和 group 相同
         *  groupID : 1, 0
         *  groups ： 两组
         * 也就是，把原来的 0 组新分成了 两组 , 新的分组序号由 groupID 的下标决定
         */
        for (int i = 0; i < toList.size(); i++){
            int index = groupID.indexOf(toList.get(i));     // 找到每个 dst 对应新的分组在 groupID 中的下标 作为新分组的序号
            groups.get(index).add(group.get(i));            // 将属于不同分组的状态添加到不同的组中
        }
        return groups;
    }

    /**
     * 获取起始节点在新的分组下的序号
     * 
     * @param storeState    原来的 DFA 的状态集合
     * @param dfaStartState 起始状态
     * @return 新分组下起始状态的序号
     */
    public static int getStartIndex(ArrayList<ArrayList<Integer>> storeState, ArrayList<Integer> dfaStartState) {
        return storeState.indexOf(dfaStartState);
    }

    /**
     * 获取终止状态在新分组下的序号
     * 
     * @param storeState 原来的 DFA 的状态集合————未化简的
     * @param endState   终止状态————未化简的
     * @return 新分组下的终止状态的序号
     */
    public static ArrayList<Integer> getEndIndex(ArrayList<ArrayList<Integer>> storeState,
            ArrayList<ArrayList<Integer>> endState) {
        ArrayList<Integer> endList = new ArrayList<>();
        for (ArrayList<Integer> end : endState) { // 对原来 DFA 的每个 终止状态
            int index = storeState.indexOf(end); // 找到对应的下标
            endList.add(index);
        }
        return endList;
    }

    /**
     * 将 DFA 的节点状态变得简单
     * @param dfaList   复杂的 DFA
     * @return 简单的 DFA 
     */
    public static ArrayList<newDFA> changeDFA(ArrayList<NFAtoDFA.DFA> dfaList, 
            ArrayList<ArrayList<Integer>> storeState)
    {
        ArrayList<newDFA> newdfaList = new ArrayList<>();
        int listLength = dfaList.size();
        for (int i = 0; i < listLength; i++)
        {
            newDFA dfa = new newDFA();
            NFAtoDFA.DFA oldDFA = dfaList.get(i);
            dfa.src = storeState.indexOf(oldDFA.src);
            dfa.dst = storeState.indexOf(oldDFA.dst);
            dfa.symbol = oldDFA.edge;
            newdfaList.add(dfa);
        }
        return newdfaList;
    }

    /**
     * 输出新的 newDFA
     */
    public static void shownewDFA(ArrayList<newDFA> newdfaList){
        int length = newdfaList.size();
        for (int i = 0; i < length; i++){
            newDFA dfa = newdfaList.get(i);
            System.out.printf(dfa.src + "————>" + dfa.dst + "    " + dfa.symbol + "\n");
        }
    }

    /**
     * 得到非终结状态 或 终结状态
     * 
     * @param storeState
     * @param end
     * @param flag       true——取终结状态， false——取非终结状态
     * @return true——返回终结状态， false——返回非终结状态取。原NFA状态的第一个字符
     */
    public static ArrayList<Integer> nonendState(ArrayList<ArrayList<Integer>> storeState, int end, boolean flag){
        int storeLength = storeState.size();
        ArrayList<Integer> endSet = new ArrayList<Integer>();
        for (int i = 0; i < storeLength; i++) {
            ArrayList<Integer> state = storeState.get(i);
            if (!flag && !state.contains(end) && ! state.isEmpty()) {
                endSet.add(storeState.indexOf(state));
            }
            if (flag && state.contains(end) && ! state.isEmpty()){
                endSet.add(storeState.indexOf(state));
            }
        }
        return endSet;
    }
    
}