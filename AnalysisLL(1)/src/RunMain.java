
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.Map.Entry;

public class RunMain extends JFrame {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        RunMain runMain = new RunMain();
        runMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        runMain.setVisible(true);
    }

    private ArrayList<String> grammarArray = new ArrayList<>();         // 输入文法
    private ArrayList<String> llgrammarArray = new ArrayList<>();       // LL(1)文法
    private ArrayList<String> removeDirectGrammar = new ArrayList<>();  // 消除左递归后文法
    private ArrayList<String> removeLeftCommon = new ArrayList<>();     // 提取左因子后文法
    private TreeSet<Character> noTerminal; // 非终结符
    private TreeSet<Character> isTerminal; // 终结符
    private HashMap<Character, TreeSet<Character>> firstGather = new HashMap<>();  // First集合
    private HashMap<Character, TreeSet<Character>> followGather = new HashMap<>(); // Follow集合
    private HashMap<Character, ArrayList<String>> expressionMap = new HashMap<>(); // LL(1)文法
    private String[][] llTable;
    private ArrayList<ArrayList<String>> newGrammarArray = new ArrayList<ArrayList<String>>();  // 改进后的文法形式
    private String outDirectGrammar = ""; // 输出显示消除左递归后的文法
    private String outCommonGrammar = ""; // 输出显示提取左因子后
    private String outChangeGrammar = ""; // 输出改进后的文法
    private String outFirstSet = "";      // 输出First集合
    private String outFollowSet = "";     // 输出Follow集合
    private String outLLGrammar = "";     // 输出LL(1)文法

    public Character startChar; // 开始符

    public RunMain() {
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new GridLayout());
        JTextArea inputArea = new JTextArea(10, 40);
        inputArea.setBorder(BorderFactory.createLoweredBevelBorder());
        inputArea.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        inputArea.setLineWrap(true);
        textPanel.add(inputArea);
        JTextArea outputArea = new JTextArea(10, 40);
        outputArea.setBorder(BorderFactory.createLoweredBevelBorder());
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        outputArea.setLineWrap(true);
        textPanel.add(outputArea);
        add(textPanel, BorderLayout.NORTH);

        JPanel southPanel = new JPanel();
        JTextArea llOutputArea = new JTextArea(20, 40);
        llOutputArea.setBorder(BorderFactory.createLoweredBevelBorder());
        llOutputArea.setEditable(false);
        llOutputArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        llOutputArea.setLineWrap(true);
        southPanel.setLayout(new BorderLayout());
        southPanel.add(llOutputArea, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        // 添加按钮，把所有结果先运算出来
        JLabel label1 = new JLabel();
        label1.setText("改写文法功能：");
        label1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        buttonPanel.add(label1);

        JButton changeButton = new JButton("改写文法");
        buttonPanel.add(changeButton);

        JButton recursiveButton = new JButton("消除左递归");
        buttonPanel.add(recursiveButton);

        JButton commonButton = new JButton("提取左因子");
        buttonPanel.add(commonButton);

        JButton grammarButton = new JButton("LL(1)文法");
        buttonPanel.add(grammarButton);

        JLabel label2 = new JLabel();
        label2.setText("分析文法功能：");
        label2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        buttonPanel.add(label2);

        JButton analyseButton = new JButton("分析文法");
        buttonPanel.add(analyseButton);

        JButton firstButton = new JButton("First集合");
        buttonPanel.add(firstButton);

        JButton followButton = new JButton("Follow集合");
        buttonPanel.add(followButton);

        JButton LL1_Button = new JButton("LL1分析表");
        buttonPanel.add(LL1_Button);

        add(buttonPanel, BorderLayout.CENTER);

        // 添加 改写文法的按钮事件，处理普通文法到LL(1)文法
        changeButton.addActionListener(e -> {
            String[] inputGrammar = inputArea.getText().split("\n");
            for (int i = 0; i < inputGrammar.length; i++) {
                grammarArray.add(inputGrammar[i]);
            }

            // 消除左递归
            EliminateRecursive removeRecursive = new EliminateRecursive();
            removeRecursive.removeDirect(grammarArray, removeDirectGrammar);
            for (int i = 0; i < removeDirectGrammar.size(); i++) {
                outDirectGrammar += removeDirectGrammar.get(i).concat("\n");
            }

            // 提取公因子
            LeftCommon removeCommon = new LeftCommon();
            removeCommon.removeLeftCommon(removeDirectGrammar, removeLeftCommon);
            for (int i = 0; i < removeLeftCommon.size(); i++) {
                outCommonGrammar += removeLeftCommon.get(i).concat("\n");
            }

            // 改进后的文法形式
            newGrammarArray = removeCommon.changeForm(removeLeftCommon);
            for (int i = 0; i < newGrammarArray.size(); i++){
                ArrayList<String> grammars = newGrammarArray.get(i);
                for (int j = 0; j < grammars.size(); j++){
                    outChangeGrammar += grammars.get(j);
                }
                outChangeGrammar += "\n";
            }
        });

        // 添加分析文法按钮，处理LL(1)文法的分析
        analyseButton.addActionListener(e->{
            String[] inputGrammar = inputArea.getText().split("\n");
            for (int i = 0; i < inputGrammar.length; i++) {
                llgrammarArray.add(inputGrammar[i]);
            }
            // 获取 First 集合
            FirstFollowGather getFirstFollowGather = new FirstFollowGather();
            getFirstFollowGather.getTerminal(llgrammarArray); // 计算终结符和非终结符
            expressionMap = getFirstFollowGather.initGrammarArray(llgrammarArray); // 初始化为LL(1)文法存储形式
            System.out.println("文法:");
            Set<Entry<Character, ArrayList<String>>> ex = expressionMap.entrySet();
            for (Entry<Character, ArrayList<String>> entry : ex) {
                System.out.println(entry.getKey() + "=" + entry.getValue());
            }

            firstGather = getFirstFollowGather.getFirstGather(); // 计算 First 集合
            Set<Entry<Character, TreeSet<Character>>> mst = firstGather.entrySet();
            for (Entry<Character, TreeSet<Character>> entry : mst) {
                outFirstSet += "First(" + entry.getKey() + ")" + "=" + entry.getValue() + "\n";
            }

            // 获取Follow集合
            startChar = llgrammarArray.get(0).charAt(0);
            System.out.println("起始状态：");
            System.out.println(startChar);
            followGather = getFirstFollowGather.getFollowGather(startChar);
            Set<Entry<Character, TreeSet<Character>>> msw = followGather.entrySet();
            for (Entry<Character, TreeSet<Character>> entry : msw) {
                outFollowSet += "Follow(" + entry.getKey() + ")" + "=" + entry.getValue() + "\n";
                System.out.println("Follow(" + entry.getKey() + ")" + "=" + entry.getValue() + "\n");
            }

            // 得到终结符和非终结符
            isTerminal = getFirstFollowGather.isTerminal;
            noTerminal = getFirstFollowGather.noTerminal;
            // 计算LL(1)分析表
            AnalyzeLLTable analyzeLLTable = new AnalyzeLLTable();
            llTable = analyzeLLTable.analyzeTable(isTerminal, noTerminal, llgrammarArray, firstGather, followGather);
            outLLGrammar = analyzeLLTable.displayLLTable(llTable);
        });

        /**
         * 点击显示消除递归后的文法
         */
        recursiveButton.addActionListener(e->{
            outputArea.setText(outDirectGrammar);
        });

        /**
         * 点击显示提取左因子后的文法
         */
        commonButton.addActionListener(e->{
            outputArea.setText(outCommonGrammar);
        });

        /**
         * 点击显示改进后的LL(1)文法
         */
        grammarButton.addActionListener(e->{
            outputArea.setText(outChangeGrammar);
        });

        /**
         * 点击显示 First 集合
         */
        firstButton.addActionListener(e->{
            outputArea.setText(outFirstSet);
        });

        /**
         * 点击显示 Follow 集合
         */
        followButton.addActionListener(e->{
            outputArea.setText(outFollowSet);
        });
        
        /**
         * 点击显示 LL1 分析表
         */
        LL1_Button.addActionListener(e->{
            llOutputArea.setText(outLLGrammar);
        });
        pack();
    }

    // 返回一个首选窗口
    private static final int DEFAULT_WIDTH = 1000;
    private static final int DEFAULT_HEIGHT = 700;

    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
}