import java.util.ArrayList;

public class OutReach {

    /**
     * 将输入的文法变成拓展文法
     * @param oldGrammar  输入的文法
     * @return  新的拓展文法
     */
    public ArrayList<String> outReachGrammar(ArrayList<String> oldGrammar){
        ArrayList<String> newGrammar = new ArrayList<>();
        Character start = oldGrammar.get(0).charAt(0);
        String firstGrammar = "@->" + start.toString();     // 为了方便，新的起始符用 @ 表示
        newGrammar.add(firstGrammar);
        for (String grammar : oldGrammar){
            newGrammar.add(grammar);
        }
        return newGrammar;
    }
}