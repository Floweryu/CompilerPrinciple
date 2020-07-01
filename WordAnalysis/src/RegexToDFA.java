import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class RegexToDFA extends JFrame{
    private static final long serialVersionUID = 1L;
    
    public static void main(String[] args) {
        RegexToDFA reToDFA = new RegexToDFA();
        reToDFA.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        reToDFA.setVisible(true);
    }

    //返回一个首选窗口
    private static final int DEFAULT_WIDTH = 1000;
    private static final int DEFAULT_HEIGHT = 700;
    public Dimension getPreferredSize() {return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);}

    private static String regex;    // 添加 "·" 符号后的表达式
    private static String rpnRegex; // 逆波兰表达式
    public static ArrayList<Integer> setp = new ArrayList<>(); // 存储起始状态节点

    public RegexToDFA(){

        // 添加输入框
        JTextField inputField = new JTextField();      //输入正则表达式文本域
        inputField.setFont(new Font(null, Font.BOLD, 15));

        JTextField connectField = new JTextField();   //输出加入连接符后的Regex
        connectField.setEditable(false);              //设置不可编辑
        connectField.setFont(new Font("微软雅黑", Font.BOLD, 15));
        
        JTextField rpnField = new JTextField();        //输出逆波兰表达式
        rpnField.setEditable(false);
        rpnField.setFont(new Font("微软雅黑", Font.BOLD, 15));

        // 添加按钮
        JButton getConnect = new JButton("AddConnect");     // 添加连接按钮
        JButton getRPN = new JButton("GetRPN");         // 添加 RPN 按钮
        JButton nfaButton = new JButton("NFA图像");     // 显示 NFA 图像的按钮
        JButton dfaButton = new JButton("DFA图像");     // 显示 DFA 图像的按钮
        JButton minidfaButton = new JButton("miniDFA图像"); // 显示 DFA 图像的按钮

        // 添加组件到 northPanel 上
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(4, 3));

        northPanel.add(new JLabel("输入正则表达式: ", SwingConstants.CENTER));
        northPanel.add(inputField);
        northPanel.add(new JLabel());

        northPanel.add(new JLabel("添加连接符: ", SwingConstants.CENTER));
        northPanel.add(connectField);
        northPanel.add(getConnect);         // 添加连接按钮

        northPanel.add(new JLabel("逆波兰表达式: ", SwingConstants.CENTER));
        northPanel.add(rpnField);
        northPanel.add(getRPN);

        northPanel.add(nfaButton);
        northPanel.add(dfaButton);
        northPanel.add(minidfaButton);

        add(northPanel, BorderLayout.NORTH);

        // 添加 ok 按钮，把所有结果先运算出来
        JPanel southPanel = new JPanel();
        JButton okButton = new JButton("OK");
        southPanel.add(okButton);
        add(southPanel, BorderLayout.SOUTH);

        /**
         * 设置各个按钮事件响应
         */
        // okButton 计算所有需要的数据
        okButton.addActionListener(e -> {
            String originalRegex = inputField.getText();    // 得到输入的正则表达式
            String alphabet = NFAtoDFA.getAlphabet(originalRegex);  // 得到字母表
            //System.out.println(alphabet);
            StringBuffer newRegex = RegexToRPN.addConnectOP(originalRegex); // 添加 "·" 符号
            regex = newRegex.toString();    

            StringBuffer rpnExpress = RegexToRPN.ReversePolishNotation(regex); // 转换成逆波兰表达式
            rpnRegex = rpnExpress.toString();

            RPNtoNFA.NFAcell nfaCell = RPNtoNFA.RPNexprssToNFA(rpnRegex); // 逆波兰式转换成 NFA
            RPNtoNFA.displayNFA(nfaCell);       // 可视化 NFA 图像

            System.out.println("由逆波兰式产生的NFA：");
            RPNtoNFA.showNFA(nfaCell);      //显示原始 NFA
            NFAtoDFA.transNFA(nfaCell);     // 将NFA转换成简单形式
            System.out.println("符号表示 NFA:");
            NFAtoDFA.showNFA();             // 显示简单形式的NFA

            setp.add(nfaCell.startState); // 从起始状态开始
            ArrayList<ArrayList<Integer>> storeState = NFAtoDFA.allClosure(setp, alphabet); // 计算闭包

            System.out.println("闭包后DFA的所有状态：");
            NFAtoDFA.showStoreState(storeState);        // 显示闭包后的节点
            System.out.println("DFA的状态转换关系：");
            NFAtoDFA.showDFA(NFAtoDFA.dfaList);         // 显示DFA

            ArrayList<ArrayList<Integer>> dfaEndState = NFAtoDFA.selectEndlist(NFAtoDFA.dfaList, nfaCell.endState); // 计算终止DFA节点
            System.out.println("DFA终止状态：");
            NFAtoDFA.showStoreState(dfaEndState);

            ArrayList<Integer> dfaStartState = NFAtoDFA.selectStartList(nfaCell.startState); // 计算起始节点
            System.out.println("DFA起始状态：");
            System.out.println(dfaStartState);

            ArrayList<DFAtoMiniDFA.newDFA> newdfa = DFAtoMiniDFA.changeDFA(NFAtoDFA.dfaList, storeState);   //化简DFA的状态
            DFAtoMiniDFA.startIndex = DFAtoMiniDFA.getStartIndex(storeState, dfaStartState); // 计算化简后的起始状态
            DFAtoMiniDFA.EndIndex = DFAtoMiniDFA.getEndIndex(storeState, dfaEndState);      // 计算化简后的终止状态  

            System.out.println("化简后的DFA结构：");
            DFAtoMiniDFA.shownewDFA(newdfa);

            NFAtoDFA.displayDFA(NFAtoDFA.dfaList, dfaStartState, dfaEndState);      // 可视化 DFA 

            ArrayList<Integer> endSet = DFAtoMiniDFA.nonendState(storeState, nfaCell.endState, true);
            System.out.println("化简后DFA的终止集：");
            System.out.println(endSet);

            ArrayList<Integer> nonSet = DFAtoMiniDFA.nonendState(storeState, nfaCell.endState, false);
            System.out.println("化简后DFA的非终止集：");
            System.out.println(nonSet);

            // DFA 最小化过程
            DFAtoMiniDFA.MiniDFA_Group.add(endSet);
            DFAtoMiniDFA.MiniDFA_Group.add(nonSet);
            // System.out.println("终结态和非终结态分组");
            // for (ArrayList<Integer> group : DFAtoMiniDFA.MiniDFA_Group) {
            //     System.out.println(group);
            // }
            DFAtoMiniDFA.miniDFA(newdfa, alphabet);     // DFA 最小化

            System.out.println("miniDFA 的状态转换：");
            DFAtoMiniDFA.shownewDFA(DFAtoMiniDFA.MiniDFA);
            System.out.println("miniDFA 的起始状态：");
            System.out.println(DFAtoMiniDFA.miniStart);
            System.out.println("miniDFA 的终止状态：");
            System.out.println(DFAtoMiniDFA.miniEnd);

            //可视化miniDFA
            DFAtoMiniDFA.displayMiniDFA(DFAtoMiniDFA.MiniDFA, DFAtoMiniDFA.miniStart, DFAtoMiniDFA.miniEnd);
        });

        //设置链接符号按钮响应，显示添加 "·" 后的表达式
        getConnect.addActionListener(e -> {
            connectField.setText(regex); // 显示
        });

        //设置逆波兰式按钮响应， 显示逆波兰表达式
        getRPN.addActionListener(e -> {
            rpnField.setText(rpnRegex);
        });

        /**
         * 显示图像的画板
         */
        JPanel centerJPanel = new JPanel();
        centerJPanel.setLayout(new BorderLayout());
        nfaButton.addActionListener(e -> {
            ImageIcon imageIcon = new ImageIcon("LexicalAnalysis/images/nfaGif.gif");
            JLabel imgLabel = new JLabel();
            imgLabel.setIcon(imageIcon);
            centerJPanel.add(imgLabel, BorderLayout.NORTH);
            centerJPanel.updateUI();
        });
        
        dfaButton.addActionListener(e -> {
            ImageIcon imageIcon = new ImageIcon("LexicalAnalysis/images/dfaGif.gif");
            JLabel imgLabel = new JLabel();
            imgLabel.setIcon(imageIcon);
            centerJPanel.add(imgLabel, BorderLayout.CENTER);
            centerJPanel.updateUI();
        });

        minidfaButton.addActionListener(e -> {
            ImageIcon imageIcon = new ImageIcon("LexicalAnalysis/images/mini-dfaGif.gif");
            JLabel imgLabel = new JLabel();
            imgLabel.setIcon(imageIcon);
            centerJPanel.add(imgLabel, BorderLayout.SOUTH);
            centerJPanel.updateUI();
        });

        JScrollPane centerJScroll = new JScrollPane(centerJPanel);
        add(centerJScroll);
        pack();
    }


}
