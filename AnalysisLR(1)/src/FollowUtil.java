import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class FollowUtil {

    /**
     * 判断 B->Ab ,  b 是不是终结符
     * @param isTerminal   终结符集
     * @param right        文法右侧
     * @param no_Terminal  要求Follow的目标
     * @return  是————true, 否————false
     */
    public static boolean b_isTerminal(TreeSet<Character> isTerminal, String right, Character no_terminal){
        String noStr = no_terminal.toString();
        if (right.contains(noStr)){
            int index = right.indexOf(no_terminal);
            String findStr;
            try{    
                findStr = right.substring(index + 1, index + 2);    
            }catch (Exception e){
                return false; 
            }

            if (isTerminal.contains(findStr.charAt(0))){  // 如果后一位符号是终结符
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    /**
     * 判断 B->aAC, C是非终结符
     * 
     * @param noTerminal    非终结符集
     * @param right         文法
     * @param no_terminal   待判定的字符
     * @return 若后一位是非终结符，，返回 true
     */
    public static boolean C_isnoTerminal(TreeSet<Character> noTerminal, String right, Character no_terminal){
        String noStr = no_terminal.toString();
        if (right.contains(noStr)){
            int index = right.indexOf(no_terminal);
            String findStr;
            try{
                findStr = right.substring(index + 1, index + 2);
            }catch(Exception e){
                return false;
            }

            if (noTerminal.contains(findStr.charAt(0)))
                return true;
            else
                return false;
        } else {
            return false;
        }
    }

    /**
     * 判断 B->aAC, C是否包含 ε
     * 
     * @param right     // 文法
     * @param no_terminal       //待求的Follow
     * @param expressionMap
     * @return  满足式子返回————true，否则，返回————false
     */
    public static boolean C_isNull(String right, Character no_terminal,HashMap<Character, ArrayList<String>> expressionMap){
            Character nextStr = getANext(right, no_terminal);
            ArrayList<String> arrayList = expressionMap.get(nextStr);
            if (arrayList.contains("ε")){
                return true;
            }
        return false;
    }

    /**
     * 判断 no_termunal 是不是文法最后一个
     * @param rights        // 文法
     * @param no_terminal   //待求Follow
     * @return      // 满足式子返回————true
     */
    public static boolean B_isEnd(String rights, Character no_terminal){
        if (rights.endsWith(no_terminal.toString())){
            return true;
        }
        return false;
    }
    /**
     * 寻找 no_terminal 的下一个字符串
     * @param right     // 文法
     * @param no_terminal   // 待求Follow
     * @return  字符
     */
    public static Character getANext(String right, Character no_terminal){
        String noStr = no_terminal.toString();
        if (right.contains(noStr)){
            int index = right.indexOf(noStr);
            String findStr;
            try{    // 寻找该非终结符后一位符号
                findStr = right.substring(index + 1, index + 2);
            }catch (Exception e){
                return null; 
            }
            return findStr.charAt(0);
        }
        return null;
    }
}