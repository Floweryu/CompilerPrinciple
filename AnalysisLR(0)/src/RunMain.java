import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.*;

public class RunMain {

	public static void main(String[] args) {
		MainFrame mainFrame = new MainFrame();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
		mainFrame.pack();
	}

}

class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private ArrayList<String> grammarArray = new ArrayList<>(); // 输入文法
	private ArrayList<String> tempGrammar = new ArrayList<>(); // 拓广文法
	private ArrayList<String> newGrammarArray = new ArrayList<String>(); // 改进后的文法形式
	private ArrayList<String> startStr = new ArrayList<>();		// 活性DFA起始
	private HashMap<ArrayList<String>, Integer> dfaMap = new HashMap<>(); // 建立节点的数字表示映射
	private String[][] lrTable;
	private ImageIcon imageIcon;

	private String outputNewGrammar = "";		// 输出拓广文法
	private String outLrAnalyzeTable;		// 输出LR(0)分析表
	private String outAnalyzeStack;			// 输出分析表

	public MainFrame(){

		JTextArea inputArea = new JTextArea(11, 30);
		inputArea.setBorder(BorderFactory.createLoweredBevelBorder());
		inputArea.setFont(new Font("Consolas", Font.PLAIN, 15));
		inputArea.setLineWrap(true);
		JScrollPane inputScroll = new JScrollPane(inputArea);
		
		JTextArea outputArea = new JTextArea(11, 60);
		outputArea.setBorder(BorderFactory.createLoweredBevelBorder());
		outputArea.setEditable(false);
		outputArea.setFont(new Font("Consolas", Font.PLAIN, 15));
		JScrollPane outputScroll = new JScrollPane(outputArea);
		// 添加分隔条
		JSplitPane splitPane = new JSplitPane();
		splitPane.setLeftComponent(inputScroll);
		splitPane.setRightComponent(outputScroll);
		splitPane.setContinuousLayout(true);		// 拖动分隔条连续重绘组件
		splitPane.setDividerLocation(250);			// 设置分隔条的起始位置
		add(splitPane, BorderLayout.NORTH);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		JLabel label = new JLabel("输入测试文法");
		buttonPanel.add(label);
		JTextField inputStr = new JTextField(50);
		inputStr.setFont(new Font("Consolas", Font.PLAIN, 15));
		buttonPanel.add(inputStr);
		JButton analyzeButton = new JButton("分析文法");
		analyzeButton.setBackground(Color.lightGray);
		analyzeButton.setForeground(Color.RED);
		buttonPanel.add(analyzeButton);
		JButton outReachButton = new JButton("拓广文法");
		buttonPanel.add(outReachButton);
		JButton dfaButton = new JButton("显示DFA");
		buttonPanel.add(dfaButton);
		JButton tableButton = new JButton("LR(0)分析表");
		buttonPanel.add(tableButton);
		JButton okButton = new JButton("OK");
		okButton.setBackground(Color.LIGHT_GRAY);
		okButton.setForeground(Color.RED);
		buttonPanel.add(okButton);
		add(buttonPanel, BorderLayout.SOUTH);

		JPanel paintPanel = new JPanel();
		dfaButton.addActionListener(e->{
			imageIcon = new ImageIcon("../images/prefixDFA.gif");
			JLabel imgLabel = new JLabel();
			imgLabel.setIcon(imageIcon);
			paintPanel.add(imgLabel);
			paintPanel.updateUI();
		});
		JScrollPane paintJScroll = new JScrollPane(paintPanel);
		add(paintJScroll);
		/**
		 * 计算所有结果
		 */
		okButton.addActionListener(e->{
			String[] inputGrammar = inputArea.getText().split("\n");
			for (int i = 0; i < inputGrammar.length; i++) {
				grammarArray.add(inputGrammar[i]);
			}

			OutReach outReach = new OutReach();
			tempGrammar = outReach.outReachGrammar(grammarArray);
			for (String grammar : tempGrammar) {
				outputNewGrammar += grammar.concat("\n");
			}

			PrefixDFA prefixDFA = new PrefixDFA();
			newGrammarArray = prefixDFA.changeForm(tempGrammar);	// 改变文法形式
			System.out.println("改变文法形势：");
			System.out.println(newGrammarArray);

			prefixDFA.getTerminal(newGrammarArray);					// 获取终结符和非终结符

			startStr = prefixDFA.prefixDFA(newGrammarArray);		// 计算活前缀DFA

			prefixDFA.displayDFA(startStr);							// 显示DFA

			dfaMap = prefixDFA.mapStateOfDFA(startStr);				// DFA的状态转换为数字
			System.out.println("DFA状态对应的数字表示：");
			Set<Entry<ArrayList<String>, Integer>> mst = dfaMap.entrySet();
			for (Entry<ArrayList<String>, Integer> entry : mst) {
				System.out.println(entry.getKey() + "-------->" + entry.getValue());
			}

			System.out.println("DFA的数字表示：");					 // DFA转化为数字表示
			prefixDFA.changeToNumber();

			lrTable = prefixDFA.analyzeTable(newGrammarArray);		// 计算分析表
			System.out.println("LR(0)分析表：");
			outLrAnalyzeTable = prefixDFA.displayLLTable(lrTable);	// 显示分析表
		});	

		/**
		 * 拓广文法按钮输出
		 */
		outReachButton.addActionListener(e->{
			outputArea.setText(outputNewGrammar);
		});

		/**
		 * LR(0)分析表按钮输出
		 */
		tableButton.addActionListener(e->{
			outputArea.setText(outLrAnalyzeTable);
		});

		/**
		 * 分析文法按钮
		 */
		analyzeButton.addActionListener(e->{
			String input = inputStr.getText();
			AnalyzeStack analyzeStack = new AnalyzeStack();
			System.out.println("文法分析过程:");
			outAnalyzeStack = analyzeStack.analyzeGrammar(input, lrTable, newGrammarArray);
			outputArea.setText(outAnalyzeStack);
		});
	}

	private static final int DEFAULT_LENGTH = 1000;
	private static final int DEFAULT_WIDTH = 700;

	public Dimension getPreferredSize() {
		return new Dimension(DEFAULT_LENGTH, DEFAULT_WIDTH);
	}
}