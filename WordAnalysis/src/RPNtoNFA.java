import java.util.ArrayList;
import java.util.Stack;

public class RPNtoNFA {
    /**
     * NFA的一个单元，并不是单独一个结点
     */
    public static class NFAcell{
        ArrayList<Edge> edgeList = new ArrayList<Edge>();   //存储边的动态数组
        int startState;     //NFA的起始节点的标记
        int endState;       //NFA的结束节点的标记        
    }
    private static int stateNum = 0;

    /**
     * 边的结构，空转换符用 '#' 表示
     */
    public static class Edge{
        int startName;           //边的起始节点名
        int endName;             //边的终止节点名
        char transSymbol;        //边上的转换符
    }
   
    private static Stack<NFAcell> stack = new Stack<NFAcell>(); // 存储NFA单元的栈
    
    public static NFAcell RPNexprssToNFA(String rpnString){

        NFAcell nfaCell = new NFAcell();
        for (int i = 0; i < rpnString.length(); i++){
            char element = rpnString.charAt(i);
            System.out.println(element);
            switch (element) {
                case '|':   //如果是选择运算
                    nfaCell = addMergeCell(element);
                    //showNFA(nfaCell);
                    stack.push(nfaCell);
                    break;
                case '*':   //如果是闭包运算
                    nfaCell = addClosureCell(element);
                    //showNFA(nfaCell);
                    stack.push(nfaCell);
                    break;
                case '·':   //如果是连接运算
                    nfaCell = addConnectCell(element);
                    //showNFA(nfaCell);
                    stack.push(nfaCell);
                    break;
                default:        // 如果是字母
                    nfaCell = addAlphaCell(element);
                    //showNFA(nfaCell);
                    stack.push(nfaCell);    //入栈
                    break;
            }
        }
        nfaCell = stack.pop();
        return nfaCell;
    }

    /**
     * 用 dot 画图实现 NFA 可视化
     */
    public static void displayNFA(NFAcell nfaCell){
        GraphViz gViz = new GraphViz("LexicalAnalysis/images",
                "D:\\ToolsSoftware\\Graphviz\\bin\\dot.exe", "nfaGif", "nfaCode.txt");
        String tempEnd;
        tempEnd = String.valueOf(nfaCell.endState);
        gViz.start_graph();
        gViz.addDoubleCircle();     //添加双层圆
        gViz.addDoubleNode(tempEnd);   //添加双层圆节点
        gViz.addCircle();           // 添加单层圆
        String tempStart;
        tempStart = String.valueOf(nfaCell.startState);
        gViz.addStart(tempStart);
        // 添加单层元节点和标签
        int nfaLength = nfaCell.edgeList.size();
        for (int i = 0; i < nfaLength; i++){
            Edge nfaEdge = nfaCell.edgeList.get(i);
            String start, end;
            char symbol;
            start = String.valueOf(nfaEdge.startName);
            end = String.valueOf(nfaEdge.endName);
            symbol = nfaEdge.transSymbol;
            gViz.addEdge(start, end, symbol);   // 添加边
        }
        gViz.end_graph();
        try {
            gViz.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果是字母，创建NFA单元
     * @param element ——字母
     * @return  创建好的NFA单元
     */
    public static NFAcell addAlphaCell(char element){
        // 构建边
        Edge newEdge = new Edge();
        // 设置边两边节点的编号
        int startState = newStateNode();
        int endState = newStateNode();
        newEdge.startName = startState;
        newEdge.endName = endState;
        newEdge.transSymbol = element;

        // 构建NFA单元
        NFAcell newCell = new NFAcell();
        newCell.edgeList.add(newEdge);      //将边添加到NFA单元边表中
        // 设置单元的起始和终止节点
        newCell.startState = newCell.edgeList.get(0).startName;
        newCell.endState = newCell.edgeList.get(0).endName;
        return newCell;
    }

    /**
     * 如果是 '|' 连接符，则出栈两个NFA单元相连
     * @param element '|'连接符
     * @return 新的NFA单元
     */
    public static NFAcell addMergeCell(char element){
        NFAcell left = stack.pop();
        NFAcell right = stack.pop();

        Edge edge1 = new Edge();
        Edge edge2 = new Edge();
        Edge edge3 = new Edge();
        Edge edge4 = new Edge();

        int startState = newStateNode();        // edge1, edge2 连接同一个节点
        // 新建节点 ——> left 起始节点
        edge1.startName = startState; 
        edge1.endName = left.startState; // 新边结尾与 left 的第一个结点相连
        edge1.transSymbol = '#';    // 表示空串

        // 新建节点 ——> right 起始节点
        edge2.startName = startState;             // 和 edge1 连接的同一个节点
        edge2.endName = right.startState;    // 新边结尾与 right 的第一个结点相连
        edge2.transSymbol = '#';    

        int endState = newStateNode();          // edge3, edge4 连接同一个节点
        // left 终止节点 ——> 新建终止节点
        edge3.startName = left.endState; // 与 left 单元最后一个结点相连
        edge3.endName = endState;    
        edge3.transSymbol = '#';

        // right 终止节点 ——> 新建终止节点
        edge4.startName = right.endState;   // 与 right 单元最后一个结点相连
        edge4.endName = endState;     
        edge4.transSymbol = '#';

        NFAcell newCell = new NFAcell();
        // 把出栈的两个 nfa 单元的边添加到新 nfa 单元中
        addItsEdge(newCell, left);          
        addItsEdge(newCell, right);

        // 将新建的四条边添加到 nfa 单元中
        newCell.edgeList.add(edge1);
        newCell.edgeList.add(edge2);
        newCell.edgeList.add(edge3);
        newCell.edgeList.add(edge4);
        // 设置新 nfa 的起始终止序号
        newCell.startState = startState;
        newCell.endState = endState;

        return newCell;
    }

    /**
     * 如果是 '*' 运算，出栈一个 NFA 单元
     * @param element '*' 运算符
     * @return  一个新的 NFA 单元
     */
    public static NFAcell addClosureCell(char element){
        NFAcell nfaCell = stack.pop();

        Edge edge1 = new Edge();
        Edge edge2 = new Edge();
        Edge edge3 = new Edge();
        Edge edge4 = new Edge();

        // 获取两个新状态节点
        int startState = newStateNode();
        int endState = newStateNode();

        // 由新建起始节点 ——> nfa 起始节点
        edge1.startName = startState;
        edge1.endName = nfaCell.startState;
        edge1.transSymbol = '#';

        // 由新建起始节点 ——> 新建终止节点
        edge2.startName = startState;
        edge2.endName = endState;
        edge2.transSymbol = '#';

        // 由nfa最后节点 ——> 新建终止节点
        edge3.startName = nfaCell.endState;
        edge3.endName = endState;
        edge3.transSymbol = '#';

        // 由新建终止节点 ——> nfa 起始节点
        edge4.startName = nfaCell.endState;
        edge4.endName = nfaCell.startState;
        edge4.transSymbol = '#';

        //添加边到新 nfa 单元中
        NFAcell newCell = new NFAcell();
        addItsEdge(newCell, nfaCell);
        newCell.edgeList.add(edge1);        
        newCell.edgeList.add(edge2);        
        newCell.edgeList.add(edge3);        
        newCell.edgeList.add(edge4);        

        // 设置新的起始终止节点
        newCell.startState = startState;
        newCell.endState = endState;

        return newCell;
    }

    /**
     * 如果是 连接 运算， 出栈两个 NFA 单元
     * @param element '·' 运算符
     * @return 一个新的 NFA 单元
     */
    public static NFAcell addConnectCell(char element){
        NFAcell right = stack.pop();    // 先出栈的应该连接在后出栈的后面，所以是 right
        NFAcell left = stack.pop();

        //将  left 的终止节点和 right 的起始节点结合
        int length_right = right.edgeList.size();
        for (int i = 0; i < length_right; i++){
            // 该边的 起始节点 指向 NFA 单元开始
            if (right.edgeList.get(i).startName == right.startState){ 
                right.edgeList.get(i).startName = left.endState;    // 边的起始节点指向 left 单元的终止节点
            }
            // 该边的 终止节点指向 NFA 单元开始
            else if (right.edgeList.get(i).endName == right.startState){
                right.edgeList.get(i).endName = left.endState;      // 边的终止节点指向 left 单元的终止节点
            } 
        }
        right.startState = left.endState;       //right 单元的起始节点与left 终止节点相连
        addItsEdge(left, right);        // 将 right 单元的边复制到 left
        left.endState = right.endState;     // left 单元终止节点指向right终止节点

        return left;
    }

    /**
     * 输出 NFA 转换
     * @param nfaCell  NFA结构单元
     */
    public static void showNFA(NFAcell nfaCell){
        int nfaLength = nfaCell.edgeList.size();
        for (int i = 0; i < nfaLength; i++){
            Edge nfaEdge = nfaCell.edgeList.get(i);
            System.out.print("起始状态   ");
            System.out.print(nfaEdge.startName);

            System.out.print("    终止状态   ");
            System.out.print(nfaEdge.endName);

            System.out.print("    转换符   ");
            System.out.print(nfaEdge.transSymbol);
            System.out.print("\n");
        }
    }
    /**
     * 把出栈的 nfa 单元的边添加到新生成的 nfa 上
     */
    private static void addItsEdge(NFAcell newCell, NFAcell nfaCell){
        int edgesCount = nfaCell.edgeList.size();
        for (int i = 0; i < edgesCount; i++){
            Edge temp = nfaCell.edgeList.get(i);
            newCell.edgeList.add(temp);
        }
    }

    /**
     * 新增加一个结点
     * @return  节点序号
     */
    private static int newStateNode(){
        return ++stateNum;
    }
}