package gitlet;

import java.util.ArrayList;
import java.io.File;

/** Executes commands of the gitlet system.
 *  @author Akshay Sreekumar and Lenny Dong
 */
public class Commander {

    /** Assorted utilities.
    *  @author Akshay Sreekumar and Lenny Dong
    */
    public Commander() {
        _init = new File(".gitlet");
    }

    /** Executes the proper command parsed from INPUTS. */
    public void run(ArrayList<String> inputs) {
        if (inputs.isEmpty()) {
            System.out.println("Please enter a command.");
            return;
        }
        String command = inputs.remove(0);
        _inputs = inputs;
        switch (command.toLowerCase()) {
        case "init":
            init();
            break;
        case "add":
            add();
            break;
        case "commit":
            commit();
            break;
        case "rm":
            rm();
            break;
        case "log":
            log();
            break;
        case "global-log":
            globallog();
            break;
        case "find":
            find();
            break;
        case "status":
            status();
            break;
        case "checkout":
            checkout();
            break;
        case "branch":
            branch();
            break;
        case "rm-branch":
            rmbranch();
            break;
        case "reset":
            reset();
            break;
        case "merge":
            merge();
            break;
        default:
            System.out.println("No command with that name exists.");
            break;
        }
    }

    /** Initializes the .gitlet directory. */
    private void init() {
        if (!_inputs.isEmpty()) {
            System.out.println("Incorrect operands.");
            return;
        }
        if (_init.exists() && _init.isDirectory()) {
            System.out.print("A gitlet version-control system already exists");
            System.out.println("in the current directory.");
            return;
        } else {
            _repo = new Repository();
            _repo.init();
        }
    }

    /** Adds a file of name FILENAME to the staging area. */
    private void add() {
        try {
            String filename = _inputs.remove(0);
            if (!_inputs.isEmpty()) {
                System.out.println("Incorrect operands.");
                return;
            }
            if (!Main.getDir().exists()) {
                System.out.println("Not in an initialized "
                        + "gitlet directory.");
                return;
            }
            _repo = Repository.deserialize();
            _repo.add(filename);
        } catch (IndexOutOfBoundsException inexcp) {
            System.out.println("Incorrect operands.");
        }
    }

    /** Commits all files in the staging area with message MSG. */
    private void commit() {
        try {
            String msg = _inputs.remove(0);
            if (!_inputs.isEmpty()) {
                System.out.println("Incorrect operands.");
                return;
            }
            if (!Main.getDir().exists()) {
                System.out.println("Not in an initialized "
                        + "gitlet directory.");
                return;
            }
            _repo = Repository.deserialize();
            _repo.commit(msg);
        } catch (IndexOutOfBoundsException inexcp) {
            System.out.println("Incorrect operands.");
        }
    }

    /** Untracks FILENAME if it is staged, and deletes it if
     *  it is in the current commit. */
    private void rm() {
        try {
            String filename = _inputs.remove(0);
            if (!_inputs.isEmpty()) {
                System.out.println("Incorrect operands.");
                return;
            }
            if (!Main.getDir().exists()) {
                System.out.println("Not in an initialized "
                        + "gitlet directory.");
                return;
            }
            _repo = Repository.deserialize();
            _repo.rm(filename);
        } catch (IndexOutOfBoundsException inexcp) {
            System.out.println("Incorrect operands.");
        }
    }

     /** Creates a new branch with name NAME. */
    private void branch() {
        try {
            String name = _inputs.remove(0);
            if (!_inputs.isEmpty()) {
                System.out.println("Incorrect operands.");
                return;
            }
            if (!Main.getDir().exists()) {
                System.out.println("Not in an initialized "
                        + "gitlet directory.");
                return;
            }
            _repo = Repository.deserialize();
            _repo.branch(name);
        } catch (IndexOutOfBoundsException inexcp) {
            System.out.println("Incorrect operands.");
        }
    }

    /** Removes a branch with name NAME. */
    private void rmbranch() {
        try {
            String name = _inputs.remove(0);
            if (!_inputs.isEmpty()) {
                System.out.println("Incorrect operands.");
                return;
            }
            if (!Main.getDir().exists()) {
                System.out.println("Not in an initialized "
                        + "gitlet directory.");
                return;
            }
            _repo = Repository.deserialize();
            _repo.rmbranch(name);
        } catch (IndexOutOfBoundsException inexcp) {
            System.out.println("Incorrect operands.");
        }
    }

    /** Calls one of the three checkout functions. */
    private void checkout() {
        boolean executed = false;
        try {
            String first = _inputs.remove(0);
            if (first.equals("--")) {
                String filename = _inputs.remove(0);
                checkout1(filename);
                executed = true;
            } else if (_inputs.isEmpty()) {
                checkout3(first);
                executed = true;
            } else {
                String second = _inputs.remove(0);
                if (second.equals("--")) {
                    String filename = _inputs.remove(0);
                    checkout2(first, filename);
                    executed = true;
                }
            }
        } catch (IndexOutOfBoundsException inexcp) {
            System.out.println("Incorrect operands.");
        }

        if (!executed) {
            System.out.println("Incorrect operands.");
        }
    }
    /** Restores file FILENAME from the latest commit. */
    private void checkout1(String filename) {
        if (!_inputs.isEmpty()) {
            System.out.println("Incorrect operands.");
            return;
        }
        if (!Main.getDir().exists()) {
            System.out.println("Not in an initialized "
                    + "gitlet directory.");
            return;
        }
        _repo = Repository.deserialize();
        _repo.checkout1(filename);
    }
    /** Restores file FILENAME from the latest commit ID. */
    private void checkout2(String id, String filename) {
        if (!_inputs.isEmpty()) {
            System.out.println("Incorrect operands.");
            return;
        }
        if (!Main.getDir().exists()) {
            System.out.println("Not in an initialized "
                    + "gitlet directory.");
            return;
        }
        _repo = Repository.deserialize();
        _repo.checkout2(id, filename);
    }

    /** Checks out BRANCHNAME. */
    private void checkout3(String branchname) {
        if (!_inputs.isEmpty()) {
            System.out.println("Incorrect operands.");
            return;
        }
        if (!Main.getDir().exists()) {
            System.out.println("Not in an initialized "
                    + "gitlet directory.");
            return;
        }
        _repo = Repository.deserialize();
        _repo.checkout3(branchname);
    }

    /** Resets to commit with id COMMITID. */
    private void reset() {
        try {
            String commitid = _inputs.remove(0);
            if (!_inputs.isEmpty()) {
                System.out.println("Incorrect operands.");
                return;
            }
            if (!Main.getDir().exists()) {
                System.out.println("Not in an initialized "
                        + "gitlet directory.");
                return;
            }
            _repo = Repository.deserialize();
            _repo.reset(commitid);
        } catch (IndexOutOfBoundsException inexcp) {
            System.out.println("Incorrect operands.");
        }
    }

    /** Displays the commit log. */
    private void log() {
        if (!Main.getDir().exists()) {
            System.out.println("Not in an initialized "
                    + "gitlet directory.");
            return;
        }
        _repo = Repository.deserialize();
        _repo.log();
    }

    /** Displays the history of all commits made. */
    private void globallog() {
        if (!Main.getDir().exists()) {
            System.out.println("Not in an initialized "
                    + "gitlet directory.");
            return;
        }
        _repo = Repository.deserialize();
        _repo.globallog();
    }

    /** Finds a commit with message MSG. */
    private void find() {
        try {
            String msg = _inputs.remove(0);
            if (!_inputs.isEmpty()) {
                System.out.println("Incorrect operands.");
                return;
            }
            if (!Main.getDir().exists()) {
                System.out.println("Not in an initialized "
                        + "gitlet directory.");
                return;
            }
            _repo = Repository.deserialize();
            _repo.find(msg);
        } catch (IndexOutOfBoundsException inexcp) {
            System.out.println("Incorrect operands.");
        }
    }

    /** Displays status of gitlet program. */
    private void status() {
        if (!Main.getDir().exists()) {
            System.out.println("Not in an initialized "
                    + "gitlet directory.");
            return;
        }
        _repo = Repository.deserialize();
        _repo.status();
    }

    /** Merges the current branch to BRANCHNAME. */
    private void merge() {
        try {
            String branchname = _inputs.remove(0);
            if (!Main.getDir().exists()) {
                System.out.println("Not in an initialized "
                        + "gitlet directory.");
                return;
            }
            _repo = Repository.deserialize();
            _repo.merge(branchname);
        } catch (IndexOutOfBoundsException inexcp) {
            System.out.println("Incorrect operands.");
        }
    }

    /** Location of the initial .gitlet directory. */
    private File _init;
    /** The current repository. */
    private Repository _repo;
    /** Inputs. */
    private ArrayList<String> _inputs;
}

