import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class FirstFollowGather {

    public TreeSet<Character> noTerminal = new TreeSet<>();        // 非终结符
    public TreeSet<Character> isTerminal = new TreeSet<>();        // 终结符
    private HashMap<Character, TreeSet<Character>> firstGather = new HashMap<>();       // First集合
    private HashMap<Character, TreeSet<Character>> followGather = new HashMap<>();       // Follow集合
    private HashMap<Character, ArrayList<String>> expressionMap = new HashMap<Character, ArrayList<String>>();  //初始化LL(1)文法
    private Character startChar;

    /**
     * 获取终结符和非终结符
     * @param LL(1)文法
     * @return 终结符和非终结符
     */
    public void getTerminal(ArrayList<String> llgrammarArray){
        // 获取非终结符
        for (String grammar : llgrammarArray){
           noTerminal.add(grammar.split("->")[0].charAt(0));
        }
        // 获取终结符
        for (String grammar : llgrammarArray){
            for (char ch : grammar.split("->")[1].toCharArray()){
                if (!noTerminal.contains(ch))
                    isTerminal.add(ch);
            }
        }
        System.out.println("终结符：");
        System.out.println(isTerminal);
        System.out.println("非终结符：");
        System.out.println(noTerminal);
    }

    /**
     * 初始化LL(1)文法
     * @param grammarArray LL(1)文法
     * @return  LL(1)文法
     */
    public HashMap<Character, ArrayList<String>> initGrammarArray(ArrayList<String> llgrammarArray){
        for (String grammar : llgrammarArray){
            String[] left_right = grammar.split("->");
            Character left = left_right[0].charAt(0);
            String right = left_right[1];
            ArrayList<String> list = expressionMap.containsKey(left) ? expressionMap.get(left) : new ArrayList<>();
            list.add(right);
            expressionMap.put(left, list);
        }
        return expressionMap;
    }

    /**
     * 计算 First 集合
     */
    public HashMap<Character, TreeSet<Character>> getFirstGather(){
        Iterator<Character> iterator = noTerminal.iterator();
        while(iterator.hasNext()){
            Character noterminalChar = iterator.next();     // 得到一个非终结符
            TreeSet<Character> firstSet = firstGather.get(noterminalChar);
            if (firstSet == null){
                firstSet = new TreeSet<Character>(); // 新建非终结符的First集合
            }
                    
            ArrayList<String> rights = expressionMap.get(noterminalChar);       // 获取该非终结符可以到达的状态
            for (String right : rights){
                Character itemChar = right.charAt(0);
                if (!itemChar.equals(noterminalChar))       
                    calcFirst(noterminalChar, firstSet, itemChar);              // 求E->T, 判断T是否为非终结符
                else
                    continue;
            }
        }
        return firstGather;
    }

    /**
     * 计算 Fitst 集合
     * @param noterminalChar    待求First集合的非终结符
     * @param firstSet          该非终结符的 First 集合
     * @param itemChar          需要考虑是否加入 First 集合的字符
     * @return  因为函数一定会找到 First 集合，所以返回为 true
     */
    public boolean calcFirst(Character noterminalChar, TreeSet<Character> firstSet, Character itemChar){
        // 如果是终结符或为空
        // 直接添加到 First 集合中
        if (itemChar == 'ε' || isTerminal.contains(itemChar)){
            firstSet.add(itemChar);
            firstGather.put(noterminalChar, firstSet);
            return true;    
        }
        // 如果是非终结符
        // 求取该非终结符的First集合
        else if(noTerminal.contains(itemChar)){
            ArrayList<String> arrayList = expressionMap.get(itemChar);
            for (String right : arrayList){
                Character firstChar = right.charAt(0);
                if (!firstChar.equals(itemChar))        // 不能与当前 left 相同，否则死循环
                    calcFirst(noterminalChar, firstSet, firstChar);         // T 为非终结符，求取 T的First
                else
                    continue;
            }
        }
        return true;
    }

    /**
     * 求取 Follow 集合
     * @param startString
     */
    public HashMap<Character, TreeSet<Character>> getFollowGather(Character start){
        for (Character tempKey : noTerminal){      // 先建立 Follow 集合
            TreeSet<Character> tempSet = new TreeSet<>();
            followGather.put(tempKey, tempSet);
        }
        startChar = start;
        Iterator<Character> iterator = noTerminal.iterator();
        while (iterator.hasNext()) {
            Character no_terminal = iterator.next();
            //System.out.println("此时非终结符为：" + no_terminal);
            TreeSet<Character> followSet = followGather.get(no_terminal);
            if (followSet.isEmpty()){
                // 对每个非终结符，遍历每一条文法，求取其 Follow 集
                Set<Character> keyLeft = expressionMap.keySet();
                for (Character left : keyLeft) {
                    ArrayList<String> rights = expressionMap.get(left);
                    for (String right : rights) {
                        calcFollow(no_terminal, followSet, left, right);
                    }
                }
            }
        }
        return followGather;
    }

    /**
     * 如果非终结符不是起始符
     * @param no_terminal   非终结符
     * @param startString   起始符
     */
    private void calcFollow(Character no_terminal, TreeSet<Character> followSet, Character left, String right){
        // 如果非终结符是起始符
        if (no_terminal.equals(startChar)) {
            followSet.add('$');
            followGather.put(no_terminal, followSet);
        }
        // 如果 right 中有 B->aAb 且 b 是终结符，直接添加
        if (FollowUtil.b_isTerminal(isTerminal, right, no_terminal)) {
            Character ALastString = FollowUtil.getANext(right, no_terminal);
            followSet.add(ALastString);
            followGather.put(no_terminal, followSet);
        }

        // B->aAC 且 C 是非终结符，Follow(A) = First(C)-ε
        else if (FollowUtil.C_isnoTerminal(noTerminal, right, no_terminal)){
            // 作为非终结符计算一次Follow集合
            Character nextStr = FollowUtil.getANext(right, no_terminal);
            TreeSet<Character> firstSet = firstGather.get(nextStr);    // 得到 First 集
            followSet.addAll(firstSet);
            if (firstSet.contains('ε')){
                followSet.remove('ε');
            }
            followGather.put(no_terminal, followSet);

            // 如果非终结符包含空要计算一次Follow集合
            if (FollowUtil.C_isNull(right, no_terminal, expressionMap)){
                // 求 left 的 Follow
                if (! left.equals(no_terminal)){
                    if (!followGather.get(left).isEmpty()) {
                        TreeSet<Character> left_followSet = followGather.get(left);
                        followSet.addAll(left_followSet);
                        followGather.put(no_terminal, followSet);
                    }
                    else {
                        TreeSet<Character> left_followSet = followGather.get(left);
                        Set<Character> keyLeft = expressionMap.keySet();
                        for (Character leftItem : keyLeft) {
                            ArrayList<String> rights = expressionMap.get(leftItem);
                            for (String rightItem : rights) {
                                calcFollow(left, left_followSet, leftItem, rightItem);
                            }
                        }
                        followSet.addAll(left_followSet);
                        followGather.put(no_terminal, followSet);
                    }
                }
            }
        }

        // B后面为空Follow(B)=Follow(A)
        else if (FollowUtil.B_isEnd(right, no_terminal)){
            if (!left.equals(no_terminal)){     // 待求不等于left
                if (! followGather.get(left).isEmpty()){
                    TreeSet<Character> left_followSet = followGather.get(left);
                    followSet.addAll(left_followSet);
                    followGather.put(no_terminal, followSet);
                }
                else{
                    TreeSet<Character> left_followSet = followGather.get(left);
                    Set<Character> keyLeft = expressionMap.keySet();
                    // 对每个非终结符，遍历每一条文法，求取其 Follow 集
                    for (Character leftItem : keyLeft) {
                        ArrayList<String> rights = expressionMap.get(leftItem);
                        for (String rightItem : rights) {
                            calcFollow(left, left_followSet, leftItem, rightItem); // 求A的Follow
                        }
                    }
                    followSet.addAll(left_followSet);
                    followGather.put(no_terminal, followSet);
                }
            }
        }

    }
}