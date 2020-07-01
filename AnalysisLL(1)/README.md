### 程序说明：
**先说程序最大的不足**：
就是在将普通文法转换为LL(1)文法后，状态符的表示上采用了加 ' 的方式，这给后序的LL(1)文法的分析造成了很大困难（水平不够）。所以将程序分为了两种输入，每种输入对应不同结果。

**两部分输入分别对应于**：
- 普通文法转换为LL(1)文法：输入普通文法后，点击界面的改写文法按钮，程序进行计算，然后才点击后面按钮以获取改写结果。
- LL(1)文法的分析：输入为LL(1)文法，点击界面的文法分析按钮，程序进行计算，然后点击后面按钮得到需要结果。

另外，文法的状态表示用的是一个字符，所以对于普遍文法来说，要化简才能输入计算。
#### 程序目录：
- RunMain.java
- EliminateRecursive.java
- LeftCommon.java
- FirstFollowGather.java
- FollowUtil.java
- AnalyzeLLTable.java

#### 各个程序解释：
##### RunMain.java ——主运行程序
程序的界面，按钮响应，操作。
***
**下面程序在主界面输入的文法不是LL(1)文法即可，程序实现的是普通文法到LL(1)文法的转换。**
##### EliminateRecursive.java ——消除左递归
- `public void removeDirect(ArrayList<String> grammarArray, ArrayList<String> removeDirectGrammar)`
  
  **消除思想**：判断每条文法是否含有左递归，然后消除。

  **注意**：消除左递归后，文法中会出现包含 ' 字符的状态，这是消除左递归出现的状态。这也是该程序的不足，因为我还没有想到用其它更好的字符代替新的状态，所以给后面造成了麻烦。

- `public void showGrammar(ArrayList<String> removeDirectGrammar)`
  
  显示消除左递归后的文法

##### LeftCommon.java ——提取左因子
- `public void removeLeftCommon(ArrayList<String> grammarArray, ArrayList<String> removeLeftCommon)`
  
  **消除思想**：判断每条文法是否有左因子，然后提取；

  **注意**：提取左因子后文法后面可能会出现空字符，要处理好空字符在文法后面的连接方式。传递的文法是消除左递归后的文法。
- `public ArrayList<ArrayList<String>> changeForm(ArrayList<String> removeLeftCommon)`
  
  消除左因子后，改写文法的表现和存储方式。

***
**下面程序在主界面输入的是LL(1)文法，程序实现的是LL(1)文法的分析过程：**
- 求First和Follow集合
- 求LL(1)分析表
  
##### FirstFollowGather.java ——— 求取First和Follow集
- `public void getTerminal(ArrayList<String> llgrammarArray)`
  
  获得终结符和非终结符。

  **计算思想**：先求非终结符(在->左边)，然后遍历文法，不是非终结符的就是终结符
- `public HashMap<Character, ArrayList<String>> initGrammarArray(ArrayList<String> llgrammarArray)`
  
  把LL(1)文法用HashMap形式表示，这样方便遍历每一条文法
- `public HashMap<Character, TreeSet<Character>> getFirstGather()`
  
  计算First集合。求取方法见**calcFirst**;
- `public boolean calcFirst(Character noterminalChar, TreeSet<Character> firstSet, Character itemChar)`
  
  只求取了非终结符的First集合。

  **计算思想：**
  1. 直接求取。遇到终结符或空，直接添加到First集合中。
  2. 反复求取。遇到非终结符，则First集合一直传递下去，知道遇到终结符或空，或者遇到已经求好的First集合
- `public HashMap<Character, TreeSet<Character>> getFollowGather(Character start)`
  
  求取非终结符的Follow集合。

  **计算思想：**
  1. 如果非终结符是起始符，直接添加 "$" 到Follow集合。
  2. 如果有 B——> a A b，Follow(A) = First(b) - 空。
  3. 如果有 B——> a A b，并且 b 中有空，则 Follow(A) = Follow(B)。
   
   也就是说，A如果在最后，则A的Follow集合等于 B的Follow集合。
   这里在计算时，第二点要先把b不为空时，A的Follow集合求出来，然后才求b为空时的Follow集合。这样在第三点就可以直接求取A是最后字符时的Follow集合。

   **注意**：程序中用到了递归，注意要求取Follow集合的存储目标。

##### FollowUtil.java ——求取Follow集合时用到的文法判断工具

判断几种Follow的形式

##### AnalyzeLLTable.java ——计算LL(1)分析表
**算法思想**：

对于G中的每一个产生式， A -> α ,执行以下2步：
1. for  ∀ a ∈ FIRST(α)， 将 A -> α 填入 M [A, a ];

2. if（ε ∈ FIRST(α)）
 ∀ a ∈ FOLLOW (A) ， 将 A -> ε 填入 M [A, a ];

程序实现时：
1. 如果文法右侧第一个状态是终结符。判断如果是空，根据A的Follow集；如果不是空，根据该终结符填入。
2. 如果文法右侧第一个状态是非终结符。根据该非终结符的First集合填入。