package gitlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ucb.junit.textui;
import org.junit.Test;
import static org.junit.Assert.*;


/** The suite of all JUnit tests for the gitlet package.
 *  @author
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** Tests the Main.getDir() function. */
    @Test
    public void initTest() {
        assertFalse(Main.getDir().exists() && Main.getDir().isDirectory());
        init();
        assertTrue(Main.getDir().exists() && Main.getDir().isDirectory());

        Commit initial = new Commit();
        List<String> files = Utils.plainFilenamesIn(Main.getDir());
        assertTrue(files.contains(initial.getID()));
        assertTrue(files.contains("repo"));

        delete();
    }

    /** Tests the add function. */
    @Test
    public void addTest() {
        Utils.writeContents(new File("bic.txt"), new byte[]{});
        Utils.writeContents(new File("boiz.txt"), new byte[]{});
        Utils.writeContents(new File("klub.txt"), new byte[]{});

        Utils.deleteDirectory(Main.getDir());
        init();
        Stage stage = Stage.deserialize();

        add("bic.txt");
        stage = Stage.deserialize();
        assertTrue(stage.contains("bic.txt"));

        add("boiz.txt");
        stage = Stage.deserialize();
        assertTrue(stage.contains("bic.txt"));
        assertTrue(stage.contains("boiz.txt"));

        add("klub.txt");
        stage = Stage.deserialize();
        assertTrue(stage.contains("bic.txt"));
        assertTrue(stage.contains("boiz.txt"));
        assertTrue(stage.contains("klub.txt"));

        delete();
    }

    /** Tests the commit function with added files. */
    @Test
    public void commitAddTest() {
        Utils.deleteDirectory(Main.getDir());
        init();
        Utils.writeContents(new File("bic.txt"), new byte[]{});
        Utils.writeContents(new File("boiz.txt"), new byte[]{});
        Utils.writeContents(new File("klub.txt"), new byte[]{});

        add("bic.txt");
        commit("added bic.txt");
        Repository repo = Repository.deserialize();
        assertFalse(null == repo);
        Blob bic = new Blob(new File("bic.txt"));
        assertFalse(Utils.deserialize(Main.getDir(), bic.getID()) == null);
        Commit commit1 = repo.getHead();
        assertFalse(commit1 == null);
        assertTrue(commit1.getBlobsName().contains("bic.txt"));

        add("boiz.txt");
        commit("added boiz.txt");
        repo = Repository.deserialize();
        assertFalse(null == repo);
        Blob boiz = new Blob(new File("boiz.txt"));
        assertFalse(Utils.deserialize(Main.getDir(), boiz.getID()) == null);
        Commit commit2 = repo.getHead();
        assertFalse(commit2 == null);
        assertTrue(commit2.getBlobsName().contains("bic.txt"));
        assertTrue(commit2.getBlobsName().contains("boiz.txt"));

        add("klub.txt");
        commit("added klub.txt");
        repo = Repository.deserialize();
        assertFalse(null == repo);
        Blob klub = new Blob(new File("klub.txt"));
        assertFalse(Utils.deserialize(Main.getDir(), klub.getID()) == null);
        Commit commit3 = repo.getHead();
        assertFalse(commit3 == null);
        assertTrue(commit3.getBlobsName().contains("bic.txt"));
        assertTrue(commit3.getBlobsName().contains("boiz.txt"));
        assertTrue(commit3.getBlobsName().contains("klub.txt"));

        delete();
    }

    /** Tests the commit function with changed files. */
    @Test
    public void commitChangeTest() {
        setUp();
        Utils.writeContents(new File("bic.txt"), "bic".getBytes());
        Utils.writeContents(new File("boiz.txt"), "boiz".getBytes());
        Utils.writeContents(new File("klub.txt"), "klub".getBytes());

        add("bic.txt");
        commit("changed bic.txt");
        Repository repo = Repository.deserialize();
        assertFalse(null == repo);
        Blob bic = new Blob(new File("bic.txt"));
        assertFalse(Utils.deserialize(Main.getDir(), bic.getID()) == null);
        Commit commit1 = repo.getHead();
        assertFalse(commit1 == null);
        assertTrue(commit1.getBlobsName().contains("bic.txt"));

        add("boiz.txt");
        commit("changed boiz.txt");
        repo = Repository.deserialize();
        assertFalse(null == repo);
        Blob boiz = new Blob(new File("boiz.txt"));
        assertFalse(Utils.deserialize(Main.getDir(), boiz.getID()) == null);
        Commit commit2 = repo.getHead();
        assertFalse(commit2 == null);
        assertTrue(commit2.getBlobsName().contains("bic.txt"));
        assertTrue(commit2.getBlobsName().contains("boiz.txt"));

        add("klub.txt");
        commit("changed klub.txt");
        repo = Repository.deserialize();
        assertFalse(null == repo);
        Blob klub = new Blob(new File("klub.txt"));
        assertFalse(Utils.deserialize(Main.getDir(), klub.getID()) == null);
        Commit commit3 = repo.getHead();
        assertFalse(commit3 == null);
        assertTrue(commit3.getBlobsName().contains("bic.txt"));
        assertTrue(commit3.getBlobsName().contains("boiz.txt"));
        assertTrue(commit3.getBlobsName().contains("klub.txt"));

        delete();
    }

    /** Tests the commit function with removed files. */
    @Test
    public void commitRemoveTest() {
        setUp();
        Repository repo;

        rm("bic.txt");
        commit("Removed bic.txt");
        repo = Repository.deserialize();
        Commit commit1 = repo.getHead();
        assertFalse(commit1.getBlobsName().contains("bic.txt"));

        File boiz = new File("boiz.txt");
        Utils.writeContents(boiz, "modified boiz.txt".getBytes());
        rm("boiz.txt");
        commit("Removed boiz.txt");
        repo = Repository.deserialize();
        Commit commit2 = repo.getHead();
        assertFalse(commit2.getBlobsName().contains("boiz.txt"));

        File temp = new File("temp.txt");
        Utils.writeContents(temp, "modifiec temp.txt".getBytes());
        add("temp.txt");
        rm("temp.txt");
        commit("dummy commit");
        repo = Repository.deserialize();
        Commit commit3 = repo.getHead();
        assertEquals(commit2.getID(), commit3.getID());
        assertFalse(commit3.getBlobsName().contains("temp.txt"));

        delete();
    }

    /** Tests if the rm function is working properly. */
    @Test
    public void rmTest() {
        setUp();
        Repository repo;
        Stage stage;

        File bic = new File("bic.txt");
        Utils.writeContents(bic, "modifying bic.txt".getBytes());
        rm("bic.txt");
        untrackedDeleted("bic.txt");

        File boiz = new File("boiz.txt");
        Utils.writeContents(boiz, "modifying boiz.txt".getBytes());
        add("boiz.txt");
        rm("boiz.txt");
        untrackedDeleted("boiz.txt");

        File klub = new File("klub.txt");
        Utils.writeContents(klub, "modifying klub.txt".getBytes());
        add("klub.txt");
        commit("Deleted bic & boiz and changed klub");
        rm("klub.txt");
        untrackedDeleted("klub.txt");

        File temp = new File("temp.txt");
        Utils.writeContents(temp, "Created temp.txt".getBytes());
        add("temp.txt");
        repo = Repository.deserialize();
        stage = Stage.deserialize();
        assertTrue(stage.contains("temp.txt"));
        assertFalse(stage.containsRemoved("temp.txt"));
        assertFalse(repo.tracked("temp.txt"));
        assertTrue(temp.exists());
        rm("temp.txt");
        repo = Repository.deserialize();
        stage = Stage.deserialize();
        assertFalse(stage.contains("temp.txt"));
        assertFalse(stage.containsRemoved("temp.txt"));
        assertTrue(temp.exists());
        assertFalse(repo.tracked("temp.txt"));

        delete();
    }

    /** Tests the branch function. */
    @Test
    public void branchTest() {
        Utils.deleteDirectory(Main.getDir());
        init();
        Repository repo = Repository.deserialize();

        assertFalse(repo.getBranches().containsKey("testbranch1"));
        assertTrue(repo.getBranches().containsKey("master"));
        branch("testbranch1");
        repo = Repository.deserialize();
        assertTrue(repo.getBranches().containsKey("master"));
        assertTrue(repo.getBranches().containsKey("testbranch1"));
        Branch one = repo.getBranches().get("testbranch1");
        assertEquals(one.getCommitID(), repo.getHeadPointer());

        assertFalse(repo.getBranches().containsKey("testbranch2"));
        branch("testbranch2");
        repo = Repository.deserialize();
        assertTrue(repo.getBranches().containsKey("master"));
        assertTrue(repo.getBranches().containsKey("testbranch1"));
        assertTrue(repo.getBranches().containsKey("testbranch2"));
        Branch two = repo.getBranches().get("testbranch2");
        assertEquals(two.getCommitID(), repo.getHeadPointer());
        assertEquals(two.getCommitID(), one.getCommitID());

        rmbranch("testbranch2");
        repo = Repository.deserialize();
        assertEquals(null, repo.getBranches().get("testbranch2"));
        assertFalse(repo.getBranches().containsKey("testbranch2"));
        branch("testbranch2");
        repo = Repository.deserialize();
        two = repo.getBranches().get("testbranch2");
        assertEquals(two.getCommitID(), repo.getHeadPointer());
        assertEquals(two.getCommitID(), one.getCommitID());
        assertTrue(repo.getBranches().containsKey("testbranch2"));
        assertTrue(repo.getBranches().get("testbranch2") instanceof Branch);

        delete();
    }

    /** Tests the rm-branch function. */
    @Test
    public void rmbranchTest() {
        Utils.deleteDirectory(Main.getDir());
        init();
        Repository repo = Repository.deserialize();

        branch("testbranch1");
        repo = Repository.deserialize();
        assertTrue(repo.getBranches().containsKey("testbranch1"));
        Branch one = repo.getBranches().get("testbranch1");
        assertEquals(one.getCommitID(), repo.getHeadPointer());
        rmbranch("testbranch1");
        repo = Repository.deserialize();
        assertEquals(false, repo.getBranches().containsKey("testbranch1"));
        assertEquals(null, repo.getBranches().get("testbranch1"));

        Branch toremove = repo.getBranches().get("master");
        assertEquals(toremove.getName(), repo.getCurrentBranch().getName());

        delete();
    }

    /** Tests the checkout of a single file. */
    @Test
    public void fileCheckoutTest() {
        Utils.deleteDirectory(Main.getDir());
        init();

        File test = new File("checkout1.txt");
        createFile("checkout1.txt", "one line");
        add("checkout1.txt");
        commit("updatetest");

        createFile("checkout1source.txt", "hai boi");
        byte[] sourcecontents =
        Utils.readContents(new File("checkout1source.txt"));
        Utils.writeContents(test, sourcecontents);

        checkout1("checkout1.txt");


        byte[] onecontents =
        Utils.readContents(test);
        assertEquals(new String(onecontents), "one line");

        assertTrue(test.exists());
        test.delete();
        assertFalse(test.exists());
        checkout1("checkout1.txt");
        assertTrue(test.exists());
        assertEquals(new String(onecontents), "one line");

        checkout1("checkout1source.txt");
        delete();
    }

    /** Tests the checkout of a commit. */
    @Test
    public void commitCheckoutTest() {
        Utils.deleteDirectory(Main.getDir());
        init();
        Repository repo = Repository.deserialize();
        createFile("checkout1.txt", "lyk duh sumbaodyyyyyyy");
        File test = new File("checkout1.txt");
        try {
            test.createNewFile();
        } catch (IOException exp) {
            System.out.println("IO Error.");
        }
        byte[] sourcecontents = "lyk duh sumbaodyyyyyyy".getBytes();
        Utils.writeContents(test, sourcecontents);
        add("checkout1.txt");
        commit("updatetest");
        createFile("checkout2.txt", "");
        add("checkout2.txt");
        commit("updatetest2");
        repo = Repository.deserialize();
        test.delete();
        assertFalse(test.exists());
        checkout2(repo.getHead().getParentID(), "checkout1.txt");
        assertTrue(test.exists());
        try {
            Scanner out = new Scanner(test);
            assertEquals("lyk duh sumbaodyyyyyyy", out.nextLine());
            out.close();
        } catch (FileNotFoundException excp) {
            System.out.println("Cannot find file.");
        }

        repo = Repository.deserialize();
        test.delete();
        assertFalse(test.exists());
        checkout2(repo.getHead().getParent().getAbbrev(), "checkout1.txt");
        assertTrue(test.exists());
        try {
            Scanner out = new Scanner(test);
            assertEquals("lyk duh sumbaodyyyyyyy", out.nextLine());
            out.close();
        } catch (FileNotFoundException excp) {
            System.out.println("Cannot find file.");
        }
        checkout2("nonsense", "checkout1.txt");
        delete();
    }

    /** Tests the checkout of a branch. */
    @Test
    public void branchCheckoutTest() {
        Utils.deleteDirectory(Main.getDir());
        init();
        Repository repo = Repository.deserialize();

        createFile("same.txt", "samesies");
        createFile("boi.txt", "boiiiii");
        add("same.txt");
        add("boi.txt");
        commit("for master branch");
        repo = Repository.deserialize();
        assertEquals(repo.getHeadPointer(),
            repo.getCurrentBranch().getCommit().getID());

        branch("branch1");
        checkout3("branch1");
        createFile("movecommit.txt", "moved down one");
        add("movecommit.txt");
        rm("same.txt");
        commit("to move down one");
        repo = Repository.deserialize();
        assertEquals(repo.getCurrentBranchName(), "branch1");
        createFile("same.txt", "untracked");
        checkout3("master");
        repo = Repository.deserialize();
        assertEquals(repo.getCurrentBranchName(), "branch1");
        createFile("tracked.txt", "tracked");
        add("same.txt");
        add("tracked.txt");
        commit("for branch 1");
        repo = Repository.deserialize();
        assertEquals(readFile("same.txt"), "untracked");
        checkout3("master");
        repo = Repository.deserialize();
        checkout3("master");
        repo = Repository.deserialize();
        assertEquals(readFile("same.txt"), "samesies");

        delete();
    }

    /** Tests resetting to a certain commit. */
    @Test
    public void resetTest() {
        Utils.deleteDirectory(Main.getDir());
        init();
        Repository repo = Repository.deserialize();

        createFile("same.txt", "samesies");
        createFile("boi.txt", "boiiiii");
        add("same.txt");
        add("boi.txt");
        commit("for master branch");
        repo = Repository.deserialize();
        String firstid = repo.getHeadPointer();
        String firstidabbrev = repo.getHead().getAbbrev();
        assertEquals(repo.getHeadPointer(),
            repo.getCurrentBranch().getCommit().getID());

        branch("branch1");
        checkout3("branch1");
        createFile("movecommit.txt", "moved down one");
        add("movecommit.txt");
        rm("same.txt");
        commit("to move down one");
        repo = Repository.deserialize();
        String secondid = repo.getHeadPointer();
        assertEquals(repo.getCurrentBranchName(), "branch1");
        createFile("same.txt", "untracked");
        reset(firstid);
        reset(firstidabbrev);
        repo = Repository.deserialize();
        assertEquals(secondid, repo.getCurrentBranch().getCommitID());

        add("same.txt");
        commit("same added");
        repo = Repository.deserialize();
        String thirdidabbrev = repo.getHead().getAbbrev();
        reset(firstid);
        repo = Repository.deserialize();
        assertEquals(firstid, repo.getHeadPointer());
        reset(thirdidabbrev);
        repo = Repository.deserialize();
        assertEquals(thirdidabbrev, repo.getHead().getAbbrev());

        delete();
    }

    /** Tests the log command. */
    @Test
    public void logTest() {
        Utils.deleteDirectory(Main.getDir());
        init();
        Repository repo = Repository.deserialize();

        createFile("commit1.txt", "commit1");
        add("commit1.txt");
        commit("commit1");

        branch("branch1");
        checkout3("branch1");

        createFile("b1commit2.txt", "b1commit2");
        add("b1commit2.txt");
        commit("b1commit2");

        createFile("b1commit3.txt", "b1commit3");
        add("b1commit3.txt");
        commit("b1commit3");

        checkout3("master");

        createFile("mcommit4.txt", "mcommit4");
        add("mcommit4.txt");
        commit("mcommit4");

        createFile("mcommit5.txt", "mcommit5");
        add("mcommit5.txt");
        commit("mcommit5");

        log();
        checkout3("branch1");
        log();

        delete();
    }

    /** Tests the global-log command. */
    @Test
    public void globallogTest() {
        Utils.deleteDirectory(Main.getDir());
        init();
        Repository repo = Repository.deserialize();

        createFile("commit1.txt", "commit1");
        add("commit1.txt");
        commit("commit1");

        branch("branch1");
        checkout3("branch1");

        createFile("b1commit2.txt", "b1commit2");
        add("b1commit2.txt");
        commit("b1commit2");

        createFile("b1commit3.txt", "b1commit3");
        add("b1commit3.txt");
        commit("b1commit3");

        checkout3("master");

        createFile("mcommit4.txt", "mcommit4");
        add("mcommit4.txt");
        commit("mcommit4");

        createFile("mcommit5.txt", "mcommit5");
        add("mcommit5.txt");
        commit("mcommit5");

        globallog();

        delete();
    }


    /** Tests the find command. */
    @Test
    public void findTest() {
        Utils.deleteDirectory(Main.getDir());
        init();
        Repository repo = Repository.deserialize();

        createFile("commit1.txt", "commit1");
        add("commit1.txt");
        commit("hide");
        repo = Repository.deserialize();
        String commit1id = repo.getHeadPointer();

        branch("branch1");
        checkout3("branch1");

        createFile("b1commit2.txt", "b1commit2");
        add("b1commit2.txt");
        commit("and");

        createFile("b1commit3.txt", "b1commit3");
        add("b1commit3.txt");
        commit("seek");

        checkout3("master");

        createFile("mcommit4.txt", "mcommit4");
        add("mcommit4.txt");
        commit("hide");
        repo = Repository.deserialize();
        String mcommit4id = repo.getHeadPointer();

        createFile("mcommit5.txt", "mcommit5");
        add("mcommit5.txt");
        commit("and");

        find("hide");
        System.out.println(commit1id);
        System.out.println(mcommit4id);

        delete();
    }

    /** Tests the stauts command. */
    @Test
    public void statusTest() {
        Utils.deleteDirectory(Main.getDir());
        Stage stage = Stage.deserialize();
        init();
        String modification = "modifying dis";
        byte[] mod = modification.getBytes();
        File modify1 = new File("modify1.txt");
        File modify2 = new File("modify2.txt");
        File staged1 = new File("staged1.txt");

        createFile("modify1.txt", "modify1");
        add("modify1.txt");
        createFile("delete1.txt", "delete1");
        add("delete1.txt");
        createFile("remove1.txt", "remove1");
        add("remove1.txt");
        commit("modify and removal");

        branch("branch1");
        branch("branch2");

        Utils.restrictedDelete("delete1.txt");
        Utils.writeContents(modify1, mod);

        rm("remove1.txt");

        createFile("modify2.txt", "modify2");
        add("modify2.txt");
        createFile("delete2.txt", "delete2");
        add("delete2.txt");
        createFile("staged1.txt", "staged1");
        add("staged1.txt");

        Utils.restrictedDelete("delete2.txt");
        Utils.writeContents(modify2, mod);
        Repository repo = Repository.deserialize();

        checkout3("branch1");

        status();
        delete();
    }

    /** Tests the merge function when files are modified. */
    @Test
    public void mergeModTest() {
        init();
        branch("branch1");
        checkout3("branch1");
        File bic = new File("bic.txt");
        Utils.writeContents(bic, new byte[]{});
        add("bic.txt");
        commit("commit1");
        checkout3("master");
        merge("branch1");
        Repository repo = Repository.deserialize();
        assertEquals(repo.getBranch("branch1").getCommit().getID(),
                repo.getBranch("master").getCommit().getID());
        assertTrue(bic.exists());

        branch("branch2");
        checkout3("branch2");
        File boiz = new File("boiz.txt");
        Utils.writeContents(boiz, new byte[]{});
        add("boiz.txt");
        commit("commit2");
        merge("master");
        repo = Repository.deserialize();
        assertFalse(repo.getBranch("master").getCommit()
                == repo.getBranch("branch2").getCommit());
        assertTrue(boiz.exists());

        checkout3("master");
        merge("branch2");

        branch("branch3");
        checkout3("branch3");
        Utils.writeContents(bic, "bic changed".getBytes());
        add("bic.txt");
        commit("changed bic.txt");
        checkout3("master");
        Utils.writeContents(boiz, "boiz changed".getBytes());
        add("boiz.txt");
        commit("changed boiz.txt");
        merge("branch3");

        repo = Repository.deserialize();
        assertEquals(repo.getHead().getBlob("bic.txt")
                .getStringContents(), "bic changed");
        assertEquals(repo.getHead().getBlob("boiz.txt")
                .getStringContents(), "boiz changed");
        assertTrue(bic.exists());
        assertTrue(boiz.exists());
        assertEquals(readFile("bic.txt"), "bic changed");
        assertEquals(readFile("boiz.txt"), "boiz changed");

        delete();
    }

    /** Tests the merge function when files are added. */
    @Test
    public void mergeAddTest() {
        init();
        branch("branch1");
        checkout3("branch1");
        File boiz = new File("boiz.txt");
        Utils.writeContents(boiz, new byte[]{});
        add("boiz.txt");
        commit("added boiz.txt");
        checkout3("master");
        File bic = new File("bic.txt");
        Utils.writeContents(bic, new byte[]{});
        add("bic.txt");
        commit("added bic.txt");
        merge("branch1");

        Repository repo = Repository.deserialize();
        assertTrue(repo.getHead().getBlobMap().containsKey("bic.txt"));
        assertTrue(repo.getHead().getBlobMap().containsKey("boiz.txt"));

        assertTrue(bic.exists());
        assertTrue(boiz.exists());

        delete();
    }

    /** Tests the merge function when files are removed. */
    @Test
    public void mergeRemoveTest() {
        init();
        File bic = new File("bic.txt");
        File boiz = new File("boiz.txt");
        Utils.writeContents(bic, new byte[]{});
        Utils.writeContents(boiz, new byte[]{});
        add("bic.txt");
        add("boiz.txt");
        commit("added bic & boiz");
        branch("branch1");
        checkout3("branch1");
        rm("bic.txt");
        commit("Removed bic.txt");
        checkout3("master");
        rm("boiz.txt");
        commit("Removed boiz.txt");
        merge("branch1");

        Repository repo = Repository.deserialize();
        assertFalse(repo.getHead().getBlobMap().containsKey("bic.txt"));
        assertFalse(repo.getHead().getBlobMap().containsKey("boiz.txt"));

        assertFalse(bic.exists());
        assertFalse(boiz.exists());

        delete();
    }

    /** Tests the merge function when there is a modification conflict. */
    @Test
    public void mergeModificationConflictTest() {
        init();
        File bic = new File("bic.txt");
        Utils.writeContents(bic, new byte[]{});
        add("bic.txt");
        commit("added bic.txt");

        branch("branch1");
        checkout3("branch1");
        Utils.writeContents(bic, "Changing bic in branch1".getBytes());
        File bicBic = new File("bicBic.txt");
        Utils.writeContents(bicBic, new byte[]{});
        add("bic.txt");
        add("bicBic.txt");
        commit("changed bic.txt in branch1");

        checkout3("master");
        Utils.writeContents(bic, "Changing bic in master".getBytes());
        add("bic.txt");
        commit("changed bic.txt in master");

        merge("branch1");

        String expected1 = conflictMsg("bic.txt", "branch1");
        assertTrue(bicBic.exists());
        assertEquals(expected1, readFile(bic.getName()));

        Utils.writeContents(bic, "Changing bic in branch1".getBytes());
        add("bic.txt");
        commit("changed bic.txt to ensure merge");
        merge("branch1");

        assertTrue(bicBic.exists());

        delete();
    }

    /** Tests the merge function when there is a deletion conflict. */
    @Test
    public void mergeAbsentConflictTest() {
        init();
        File bic = new File("bic.txt");
        Utils.writeContents(bic, new byte[]{});
        add("bic.txt");
        commit("added bic.txt");
        branch("branch1");
        checkout3("branch1");
        rm("bic.txt");
        File bicBic = new File("bicBic.txt");
        Utils.writeContents(bicBic, new byte[]{});
        add("bicBic.txt");
        commit("Removed bic.txt");
        checkout3("master");
        Utils.writeContents(bic, "changed bic.txt".getBytes());
        add("bic.txt");
        commit("changed bic.txt");

        merge("branch1");

        String expected1 = conflictMsg("bic.txt", "branch1");
        assertTrue(bicBic.exists());
        assertEquals(expected1, readFile(bic.getName()));

        rm("bic.txt");
        commit("removed bic.txt");
        merge("branch1");

        assertTrue(bicBic.exists());

        File boiz = new File("boiz.txt");
        Utils.writeContents(boiz, new byte[]{});
        add("boiz.txt");
        commit("added boiz.txt");
        branch("branch2");
        checkout3("branch2");
        Utils.writeContents(boiz, "changed boiz.txt".getBytes());
        File boizBoiz = new File("boizBoiz.txt");
        Utils.writeContents(boizBoiz, new byte[]{});
        add("boiz.txt");
        add("boizBoiz.txt");
        commit("changed boiz.txt");
        checkout3("master");
        Utils.restrictedDelete(boiz);
        rm("boiz.txt");
        commit("Removed boiz.txt");
        merge("branch2");

        String expected2 = conflictMsg("boiz.txt", "branch2");
        assertTrue(boizBoiz.exists());
        assertEquals(expected2, readFile(boiz.getName()));

        Utils.writeContents(boiz, "changed boiz.txt".getBytes());
        add("boiz.txt");
        commit("added boiz.txt");
        merge("branch2");
        assertTrue(boizBoiz.exists());

        delete();
    }

    /** Tests the merge function when there is an addition conflict. */
    @Test
    public void mergeAddConflictTest() {
        init();
        branch("branch1");
        checkout3("branch1");
        File bic = new File("bic.txt");
        Utils.writeContents(bic, "adding bic.txt in branch1".getBytes());
        File boiz = new File("boiz.txt");
        Utils.writeContents(boiz, new byte[]{});
        add("bic.txt");
        add("boiz.txt");
        commit("added bic.txt");
        checkout3("master");
        Utils.writeContents(bic, "adding bic.txt in master".getBytes());
        add("bic.txt");
        commit("added bic.txt");

        merge("branch1");

        String expected = conflictMsg("bic.txt", "branch1");
        assertEquals(expected, readFile(bic.getName()));
        assertTrue(boiz.exists());

        Utils.writeContents(bic, "adding bic.txt in branch1".getBytes());
        add("bic.txt");
        commit("changed bic,txt to match branch1");
        merge("branch1");
        assertTrue(boiz.exists());

        delete();
    }

    /** Tests the merge function for failure cases. */
    @Test
    public void mergeFailureTest() {
        init();
        branch("branch1");
        checkout3("branch1");
        File bic = new File("bic.txt");
        Utils.writeContents(bic, "added bic in branch1".getBytes());
        add("bic.txt");
        commit("added bic.txt");
        checkout3("master");
        Utils.writeContents(bic, "added bic in master".getBytes());
        merge("branch1");
        assertFalse("added bic in branch1".equals(readFile(bic.getName())));

        add("bic.txt");
        merge("branch1");
        assertFalse("added bic in branch1".equals(readFile(bic.getName())));

        checkout3("branch1");
        rm("bic.txt");
        commit("removed bic.txt");
        checkout3("master");
        Utils.writeContents(bic, "added bic in master".getBytes());
        merge("branch1");
        assertTrue(bic.exists());

        delete();
    }

    /** Sets up the directory with a commit of three files called
     *  bic.txt, boiz.txt and klub.txt. */
    public void setUp() {
        Utils.deleteDirectory(Main.getDir());
        init();
        Utils.writeContents(new File("bic.txt"), new byte[]{});
        Utils.writeContents(new File("boiz.txt"), new byte[]{});
        Utils.writeContents(new File("klub.txt"), new byte[]{});

        add("bic.txt");
        commit("added bic.txt");

        add("boiz.txt");
        commit("added boiz.txt");

        add("klub.txt");
        commit("added klub.txt");

        Repository repo = Repository.deserialize();
        assertTrue(repo.tracked("bic.txt"));
        assertTrue(repo.tracked("boiz.txt"));
        assertTrue(repo.tracked("klub.txt"));
    }

    /** Deletes the three .txt files used for testing. */
    public void delete() {
        Stage.deserialize().clear();
        File directory = new File(".").getAbsoluteFile();
        for (File file : directory.listFiles()) {
            if (file.getName().contains(".txt")
                    && !file.getName().equals(".txt")) {
                Utils.restrictedDelete(file);
            }
        }
        Utils.deleteDirectory(Main.getDir());
    }

    /** Performs the add function on file named FILENAME. */
    public void add(String filename) {
        _inputs.add("add");
        _inputs.add(filename);
        _command.run(_inputs);
    }

    /** Performs the rm function on file named FILENAME. */
    public void rm(String filename) {
        _inputs.add("rm");
        _inputs.add(filename);
        _command.run(_inputs);
    }

    /** Performs the commit funtion with the given MSG. */
    public void commit(String msg) {
        _inputs.add("commit");
        _inputs.add(msg);
        _command.run(_inputs);
    }

    /** Performs the init function. */
    public void init() {
        _inputs.add("init");
        _command.run(_inputs);
    }

    /** Creates a new branch BRANCH. */
    public void branch(String branchname) {
        _inputs.add("branch");
        _inputs.add(branchname);
        _command.run(_inputs);
    }

    /** Deletes a branch BRANCH. */
    public void rmbranch(String branchname) {
        _inputs.add("rm-branch");
        _inputs.add(branchname);
        _command.run(_inputs);
    }

    /** Performs the file checkout on FILE. */
    public void checkout1(String filename) {
        _inputs.add("checkout");
        _inputs.add("--");
        _inputs.add(filename);
        _command.run(_inputs);
    }

    /** Performs the file checkout of FILE from a commit COMMIT. */
    public void checkout2(String commitname, String filename) {
        _inputs.add("checkout");
        _inputs.add(commitname);
        _inputs.add("--");
        _inputs.add(filename);
        _command.run(_inputs);
    }

    /** Performs the checkout of a branch BRANCHNAME. */
    public void checkout3(String branchname) {
        _inputs.add("checkout");
        _inputs.add(branchname);
        _command.run(_inputs);
    }

    /** Performs the merge to branch BRANCHNAME. */
    public void merge(String branchname) {
        _inputs.add("merge");
        _inputs.add(branchname);
        _command.run(_inputs);
    }

    /** Performs the reset to a commit with id ID. */
    public void reset(String id) {
        _inputs.add("reset");
        _inputs.add(id);
        _command.run(_inputs);
    }

    /** Performs the printing of the log history. */
    public void log() {
        _inputs.add("log");
        _command.run(_inputs);
    }

    /** Performs the printing of the complete commit history. */
    public void globallog() {
        _inputs.add("global-log");
        _command.run(_inputs);
    }

    /** Performs the finding of commits with msg MSG. */
    public void find(String msg) {
        _inputs.add("find");
        _inputs.add(msg);
        _command.run(_inputs);
    }

    /** Performs the status command. */
    public void status() {
        _inputs.add("status");
        _command.run(_inputs);
    }

    /** Creates a file as FILENAME with contents CONTENTS. */
    public void createFile(String filename, String contents) {
        String stuff = contents;
        byte[] bytestuff = stuff.getBytes();
        Utils.writeContents(new File(filename), bytestuff);
    }

    /** Returns the contents of file FILENAME as a string. */
    public String readFile(String filename) {
        byte[] contents = Utils.readContents(new File(filename));
        return new String(contents);
    }

    /** Returns a String representing what should be in the
     *  conflicted file FILENAME in the case of a merge conflict between
     *  current branch and GIVENBRANCH. */
    public String conflictMsg(String fileName, String givenBranch) {
        Repository repo = Repository.deserialize();
        String expected = "<<<<<<< HEAD\n";
        if (!(new File(fileName).exists())) {
            Utils.writeContents(new File(fileName), new byte[]{});
        }
        if (repo.getHead().getBlobsName().contains(fileName)) {
            expected += (repo.getHead()
                    .getBlob(fileName).getStringContents());
        }
        expected += ("=======\n");
        if (repo.getBranch(givenBranch)
                .getCommit().getBlobsName().contains(fileName)) {
            expected += (repo.getBranch(givenBranch).getCommit()
                    .getBlob(fileName).getStringContents());
        }
        expected += (">>>>>>>\n");
        return expected;
    }

    /** Asserts that FILENAME is untracked & deleted*/
    public void untrackedDeleted(String filename) {
        File file = new File(filename);
        Repository repo = Repository.deserialize();
        Stage stage = Stage.deserialize();
        assertFalse(repo.tracked(filename));
        assertTrue(stage.containsRemoved(filename));
        assertFalse(stage.contains(filename));
        assertFalse(file.exists());
    }

    /** ArrayList containing user _input */
    ArrayList<String> _inputs = new ArrayList<String>();

    /** The _commander receiving input. */
    Commander _command = new Commander();
}


