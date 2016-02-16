package gitlet;
import java.io.File;
import java.util.ArrayList;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Lenny Dong and Akshay Sreekumar
 */
public class Main {

    /** The /.gitlet directory. */
    private static final File INIT = new File(".gitlet");

    /** Returns the /.gitlet directory. */
    public static File getDir() {
        return INIT;
    }


    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        ArrayList<String> inputs = new ArrayList<String>();
        for (String s : args) {
            inputs.add(s);
        }
        Commander chief = new Commander();
        chief.run(inputs);
    }

}
