import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class GraphViz {
    private String runPath = "";
    private String dotPath = "";
    private String runOrder = "";
    private String dotCodeFile = "";
    private String resultGif = "";
    private StringBuilder graph = new StringBuilder();

    Runtime runtime = Runtime.getRuntime();

    public void run() {
        File file = new File(runPath);
        file.mkdirs();
        writeGraphToFile(graph.toString(), runPath);
        creatOrder();
        try {
            runtime.exec(runOrder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void creatOrder() {
        runOrder += dotPath + " ";
        runOrder += runPath;
        runOrder += "\\" + dotCodeFile + " ";
        runOrder += "-T gif ";
        runOrder += "-o ";
        runOrder += runPath;
        runOrder += "\\" + resultGif + ".gif";
        System.out.println(runOrder);
    }

    public void writeGraphToFile(String dotcode, String filename) {
        try {
            File file = new File(filename + "\\" + dotCodeFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(dotcode.getBytes());
            fos.close();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public GraphViz(String runPath, String dotPath, String resultGif, String dotCodeFile) {
        this.runPath = runPath;
        this.dotPath = dotPath;
        this.resultGif = resultGif;
        this.dotCodeFile = dotCodeFile;
    }

    public void addStart(String start){
        graph.append("\t" + "\"\"" + "->" + start + " [label=\"start\"];" + "\n");
    }

    // 添加一条边和标签
    public void addEdge(String start, String end, char symbol) {
        graph.append("\t" + start + " -> " + end + " [label=\"" + symbol + "\"];" + "\n");
    }

    // 添加终止节点————双层圆
    public void addDoubleCircle() {
        graph.append("\t" + "node [shape = doublecircle];" + "\n");
    }

    // 添加其他节点————单层圆
    public void addCircle(){
        graph.append("\t" + "node [shape = circle];" + "\n");
    }

    // 添加双层圆节点序号
    public void addDoubleNode(String state) {
        graph.append("\t" + state + ";" + "\n");
    }

    // 画图代码起始，设置图形从左到右
    public void start_graph() {
        graph.append("digraph G {\n\trankdir = LR;\n\t\"\" [shape=none];\n");
    }

    // 画图终止代码
    public void end_graph() {
        graph.append("}");
    }
}