package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

/** A representation of a repository.
 * @author Lenny Dong, Akshay Sreekumar
 */
public class Repository implements Serializable {

    /** Initiates a Repository instance. */
    public Repository() {
    }

    /** initializes .gitlet directory. */
    public void init() {
        Main.getDir().mkdir();
        Commit initial = new Commit();
        Stage stage = new Stage();
        _currBranch = new Branch("master", initial.getID());
        _branches.put(_currBranch.getName(), _currBranch);
        _head = initial.getID();
        stage.serialize();
        initial.serialize();
        serialize();
    }

    /** Add FILENAME to the staging area. */
    public void add(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        } else {
            Stage stage = Stage.deserialize();
            Blob blobfile = new Blob(file);
            Commit currCommit = getHead();
            if (stage.containsRemoved(filename)
                    || !currCommit.getBlobsHash()
                    .contains(blobfile.getID())) {
                stage.add(blobfile);
            }
        }
    }

    /** Creates a new branch with name NAME. */
    public void branch(String name) {
        Repository repo = Repository.deserialize();
        if (repo.getBranches().containsKey(name)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        Branch newbranch = new Branch(name, repo.getHeadPointer());
        repo.addBranch(newbranch);
        repo.serialize();
    }

     /** Removes a branch with name NAME. */
    public void rmbranch(String name) {
        Repository repo = Repository.deserialize();
        if (!repo.getBranches().containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        Branch toremove = repo.getBranches().get(name);
        if (toremove.getName() == repo.getCurrentBranch().getName()) {
            System.out.println("Cannot remove the current branch you are on.");
            return;
        }
        repo.getBranches().remove(name);
        repo.serialize();
    }

    /** Restores file FILENAME from the latest commit. */
    public void checkout1(String filename) {
        Repository repo = Repository.deserialize();
        String headcode = repo.getHeadPointer();
        Commit head = Commit.deserialize(headcode);
        String blobcode = head.blobMap(filename);
        if (blobcode == null) {
            System.out.println("File does not exist"
                    + "in the commit.");
            return;
        }
        Blob blob = Blob.deserialize(blobcode);
        Utils.writeContents(new File(filename), blob.getContents());
    }

    /** Restores file FILENAME from commit with has ID. */
    public void checkout2(String id, String filename) {
        Repository repo = Repository.deserialize();
        HashMap<String, String> abbrevmap =
            (HashMap<String, String>)
            Utils.deserialize(Main.getDir(), "abbrevmap");
        if (id.length() == 8) {
            String commitcode = abbrevmap.get(id);
            if (commitcode == null) {
                System.out.println("No commit with that id exists.");
                return;
            }
            Commit commit = Commit.deserialize(commitcode);
            String blobcode = commit.blobMap(filename);
            if (blobcode == null) {
                System.out.println("File does not exist in the commit.");
                return;
            }
            Blob blob = Blob.deserialize(blobcode);
            Utils.writeContents(new File(filename), blob.getContents());
        } else {
            Commit commit = Commit.deserialize(id);
            if (commit == null) {
                System.out.println("No commit with that id exists.");
                return;
            }
            String blobcode = commit.blobMap(filename);
            if (blobcode == null) {
                System.out.println("File does not exist in the commit.");
                return;
            }
            Blob blob = Blob.deserialize(blobcode);
            Utils.writeContents(new File(filename), blob.getContents());
        }
        repo.serialize();
        Utils.serialize(Main.getDir(), "abbrevmap", abbrevmap);
    }

    /** Checks out to BRANCHNAME and restores those head commits. */
    public void checkout3(String branchname) {
        File folder = Main.getDir().getAbsoluteFile();
        Branch branch = _branches.get(branchname);
        Stage stage = Stage.deserialize();
        if (branch == null) {
            System.out.println("No such branch exists.");
            return;
        }
        if (branch == _currBranch) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Commit newcommit = branch.getCommit();
        rewrite(newcommit, branch);
    }

    /** Commits a commit with the given MSG. */
    public void commit(String msg) {
        Stage stage = Stage.deserialize();
        if (msg.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (stage.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Commit currentCommit = getHead();
        HashMap<String, Blob> blobMap = currentCommit.getBlobMap();
        HashMap<String, String> idMap = currentCommit.getIDMap();
        Collection<Blob> stagedBlobs = stage.getBlobs();
        for (Blob b: stagedBlobs) {
            if (idMap.containsValue(b)) {
                continue;
            }
            if (blobMap.containsKey(b.getName())) {
                blobMap.remove(b.getName());
            }
            blobMap.put(b.getName(), b);
            b.serialize();
        }
        Collection<Blob> removedBlobs = stage.getRemovedBlobs();
        for (Blob b: removedBlobs) {
            blobMap.remove(b.getName());
        }
        Commit commit = new Commit(msg,
                new ArrayList<Blob>(blobMap.values()), _head);
        _head = commit.getID();
        _currBranch.update(_head);
        stage.clear();
        serialize();
        commit.serialize();
    }

    /** Unstages FILENAME if it is staged, and deletes it if
     *  it is in the current commit. */
    public void rm(String fileName) {
        Commit headCommit = getHead();
        Stage stage = Stage.deserialize();
        if (stage == null) {
            stage = new Stage();
        }
        if (tracked(fileName) || stage.contains(fileName)) {
            if (headCommit.getBlobsName().contains(fileName)) {
                Utils.restrictedDelete(fileName);
                stage.remove(headCommit.getBlob(fileName));
            }
            stage.unstage(fileName);
        } else {
            System.out.println("No reason to remove the file.");
            return;
        }
    }

    /** Merges the current branch with BRANCHNAME. */
    public void merge(String branchname) {
        _conflicted = false;
        if (!_branches.containsKey(branchname)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        Stage stage = Stage.deserialize();
        if (!stage.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        Branch givenBran = _branches.get(branchname);
        Commit splitPt = getSplitPt(_currBranch, givenBran);
        ArrayList<String> filesToAdd = new ArrayList<String>();
        ArrayList<String> filesToRemove = new ArrayList<String>();
        File parentFolder = new File(".").getAbsoluteFile();
        for (File file: parentFolder.listFiles()) {
            String fileName = file.getName();
            boolean overwritten = !(tracked(fileName))
                    && givenBran.getCommit().getBlobsName().contains(fileName)
                    && !Arrays.equals(givenBran.getCommit()
                    .getBlob(fileName).getContents(), Utils.readContents(
                            new File(parentFolder, fileName)));
            boolean deleted = !(tracked(fileName))
                    && !givenBran.getCommit().getBlobsName().contains(fileName)
                    && splitPt.getBlobsName().contains(fileName);
            if (overwritten || deleted) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it or add it first.");
                return;
            }
        }
        if (givenBran == _currBranch) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        if (splitPt.getID().equals(givenBran.getCommit().getID())) {
            System.out.println("Given branch is an ancestor of"
                    + "the current branch.");
            return;
        }
        if (splitPt.getID().equals(_head)) {
            _head = givenBran.getCommitID();
            _currBranch.update(givenBran.getCommitID());
            System.out.println("Current branch fast-forwarded..");
            rewrite(getHead(), _currBranch);
            return;
        } else {
            filesToAdd = toAdd(givenBran, _currBranch, splitPt);
            filesToRemove = toDel(givenBran, _currBranch, splitPt);
        }
        addAll(filesToAdd, givenBran);
        delAll(filesToRemove, givenBran);
        if (!_conflicted) {
            commit("Merged " + _currBranch.getName() + " with " + branchname
                    + ".");
        }
        serialize();
    }

    /** Goes through all the files listed in FILESTOADD and adds all
     *  of them from GIVENBRAN. */
    public void addAll(ArrayList<String> filesToAdd,
            Branch givenBran) {
        for (String blobName: filesToAdd) {
            File file = new File(blobName);
            Utils.writeContents(file, givenBran.getCommit()
                    .getBlob(blobName).getContents());
            add(blobName);
        }
    }

    /** Goes through all the files listed in FILESTOREMOVE and deletes
     *  all of them from GIVENBRAN. */
    public void delAll(ArrayList<String> filesToRemove,
            Branch givenBran) {
        for (String blobName: filesToRemove) {
            rm(blobName);
        }
    }

    /** Goes through the files deleted in GIVENBRAN since the SPLITPT,
     *  and returns the files to delete in CURRBRAN, while handling
     *  conflict issues. */
    public ArrayList<String> toDel(Branch givenBran, Branch currBran,
            Commit splitPt) {
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> delGiven =
                deletedFiles(splitPt, givenBran.getCommit());
        ArrayList<String> delCurr =
                deletedFiles(splitPt, getHead());
        ArrayList<ArrayList<String>> tempCurr =
                modedAddedFiles(splitPt, getHead());
        for (String blobName: delGiven) {
            if (delCurr.contains(blobName)) {
                continue;
            }
            if (tempCurr.get(0).contains(blobName)) {
                conflictFile(blobName, givenBran);
                continue;
            }
            names.add(blobName);
        }
        return names;
    }

    /** Goes through the files added in GIVENBRAN since the SPLITPT,
     *  and returns the files to add in CURRBRAN, while handling
     *  conflict issues. */
    public ArrayList<String> toAdd(Branch givenBran, Branch currBran,
            Commit splitPt) {
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<ArrayList<String>> tempGiven =
                modedAddedFiles(splitPt, givenBran.getCommit());
        ArrayList<String> delCurr =
                deletedFiles(splitPt, getHead());
        ArrayList<String> modGiven = tempGiven.remove(0);
        ArrayList<String> addGiven = tempGiven.remove(0);
        ArrayList<ArrayList<String>> tempCurr =
                modedAddedFiles(splitPt, getHead());
        for (String blobName: modGiven) {
            if (tempCurr.get(0).contains(blobName)) {
                Blob curr = getHead().getBlob(blobName);
                Blob given = givenBran.getCommit().getBlob(blobName);
                String currContent = curr.getStringContents();
                String givenContent = given.getStringContents();
                if (currContent.equals(givenContent)) {
                    continue;
                } else {
                    conflictFile(blobName, givenBran);
                    continue;
                }
            } else if (delCurr.contains(blobName)) {
                conflictFile(blobName, givenBran);
                continue;
            } else {
                names.add(blobName);
            }
        }
        for (String blobName: addGiven) {
            if (tempCurr.get(1).contains(blobName)) {
                Blob curr = getHead().getBlob(blobName);
                Blob given = givenBran.getCommit().getBlob(blobName);
                if (!curr.getID().equals(given.getID())) {
                    conflictFile(blobName, givenBran);
                    continue;
                }
            } else {
                names.add(blobName);
            }
        }
        return names;
    }

    /** Writes the content of NAME in case of a merge conflict
     *  between the current branch and the given BRANCH. */
    public void conflictFile(String name, Branch branch) {
        if (!_conflicted) {
            _conflicted = !_conflicted;
            System.out.println("Encountered a merge conflict.");
        }
        File file = new File(name);
        Commit head = getHead();
        String expected = "<<<<<<< HEAD\n";
        if (!file.exists()) {
            Utils.writeContents(file, new byte[]{});
        }
        if (head.getBlobsName().contains(name)) {
            expected += (head.getBlob(name)
                    .getStringContents());
        }
        expected += ("=======\n");
        if (branch.getCommit().getBlobsName().contains(name)) {
            expected += (branch.getCommit()
                    .getBlob(name).getStringContents());
        }
        expected += (">>>>>>>\n");
        Utils.writeContents(file, expected.getBytes());
    }

    /** Returns a List containing names of all the files that have been
     *  changed or added since SPLITPT up until COMMIT. The first
     *  List contains the names of the files that have been changed,
     *  and the second list contains names of the added files*/
    public ArrayList<ArrayList<String>> modedAddedFiles(
            Commit splitPt, Commit commit) {
        ArrayList<ArrayList<String>> names = new ArrayList<ArrayList<String>>();
        ArrayList<String> moded = new ArrayList<String>();
        ArrayList<String> added = new ArrayList<String>();
        for (String blobName: commit.getBlobsName()) {
            if (!splitPt.getBlobsName().contains(blobName)) {
                added.add(blobName);
            } else if (!splitPt.getBlob(blobName).getID()
                    .equals(commit.getBlob(blobName).getID())) {
                moded.add(blobName);
            }
        }
        names.add(moded);
        names.add(added);
        return names;
    }

    /** Returns a Lit containing names of all the files that have been
     *  deleted since SPLITPT up until COMMIT. */
    public ArrayList<String> deletedFiles(Commit splitPt, Commit commit) {
        ArrayList<String> names = new ArrayList<String>();
        for (String blobName : splitPt.getBlobsName()) {
            if (!commit.getBlobsName().contains(blobName)) {
                names.add(blobName);
            }
        }
        return names;
    }

    /** Rewrites the working directory according to NEWCOMMIT,
     *  and BRANCH. If rewrite is successful, returns true, false
     *  otherwise. */
    public boolean rewrite(Commit newcommit, Branch branch) {
        Commit headcommit = getHead();
        Stage stage = Stage.deserialize();
        for (String blobcode : newcommit.getBlobsHash()) {
            Blob blob = Blob.deserialize(blobcode);
            String filename = blob.getName();
            File asfile = new File(filename);
            if (asfile.exists() && !tracked(filename)) {
                System.out.println("There is an untracked file in the way; "
                    +
                    "delete it or add it first.");
                return false;
            }
        }
        for (String blobcode : headcommit.getBlobsHash()) {
            Blob blob = Blob.deserialize(blobcode);
            String filename = blob.getName();
            if (!newcommit.getBlobsHash().contains(filename)) {
                Utils.restrictedDelete(filename);
            }
        }
        for (String blobcode : newcommit.getBlobsHash()) {
            Blob blob = Blob.deserialize(blobcode);
            String filename = blob.getName();
            Utils.writeContents(new File(filename), blob.getContents());
        }
        _currBranch = branch;
        _head = branch.getCommit().getID();
        stage.clear();
        stage.serialize();
        serialize();
        return true;
    }


    /** Resets to commit with id ID. */
    public void reset(String id) {
        Commit newcommit;
        HashMap<String, String> abbrevmap =
            (HashMap<String, String>)
            Utils.deserialize(Main.getDir(), "abbrevmap");
        if (id.length() == 8) {
            String commitcode = abbrevmap.get(id);
            newcommit = Commit.deserialize(commitcode);
        } else {
            newcommit = Commit.deserialize(id);
        }
        if (newcommit == null) {
            System.out.println("No commit with that id exists.");
            return;
        }

        if (rewrite(newcommit, _currBranch)) {
            _currBranch.update(newcommit.getID());
            _head = _currBranch.getCommitID();
            this.serialize();
        } else {
            return;
        }
        Utils.serialize(Main.getDir(), "abbrevmap", abbrevmap);
    }

    /** Displays the commit log from the head. */
    public void log() {
        Commit current;
        current = getHead();
        while (current.getID() != null) {
            System.out.println(current);
            if (current.getParentID() == null) {
                return;
            }
            current = current.getParent();
        }
    }

    /** Displays the total commit history so far. */
    public void globallog() {
        if (Main.getDir().exists()) {
            for (String id : Main.getDir().list()) {
                File checkfile = new File(id);
                if (id.length() == hashlength
                    && !(id.substring(0, 4).equals("blob"))
                    && !checkfile.isDirectory()) {
                    Commit toshow = Commit.deserialize(id);
                    System.out.println(toshow);
                }
            }
        }
    }

    /** Displays commits with the message MSG. */
    public void find(String msg) {
        boolean found = false;
        if (Main.getDir().exists()) {
            for (String id : Main.getDir().list()) {
                File checkfile = new File(id);
                if (id.length() == hashlength
                    && !(id.substring(0, 4).equals("blob"))
                    && !checkfile.isDirectory()) {
                    Commit toshow = Commit.deserialize(id);
                    if (toshow.getMsg().equals(msg)) {
                        System.out.println(toshow.getID());
                        found = true;
                    }
                }
            }
            if (!found) {
                System.out.println("Found no commit with that message.");
            }
        }

    }

    /** Displays status of gitlet program. */
    public void status() {
        displayBranches();
        displayStaged();
        displayRemoved();
        displayModified();
        displayUntracked();
    }
    /** Displays current branches in the program. */
    public void displayBranches() {
        System.out.println("=== Branches ===");
        for (String branchname : _branches.keySet()) {
            if (_currBranch.getName().equals(branchname)) {
                System.out.println("*" + branchname);
            } else {
                System.out.println(branchname);
            }
        }
        System.out.println();
    }
    /** Displays staged files. */
    public void displayStaged() {
        Stage stage = Stage.deserialize();
        System.out.println("=== Staged Files ===");
        for (String filename : stage.getFileNames()) {
            File checkfile = new File(filename);
            if (checkfile.exists()) {
                Blob checkblob = new Blob(checkfile);
                if (checkblob.getID().equals(
                        stage.blobMapStage(filename).getID())) {
                    System.out.println(filename);
                }
            }
        }
        System.out.println();
    }

    /** Displays removed files. */
    public void displayRemoved() {
        Stage stage = Stage.deserialize();
        System.out.println("=== Removed Files ===");
        for (String filename : stage.getRemovedFileNames()) {
            System.out.println(filename);
        }
        System.out.println();
    }

    /** Displays modifications not staged for commit. */
    public void displayModified() {
        File working = Main.getDir().getAbsoluteFile().getParentFile();
        ArrayList<String> toprint = new ArrayList<String>();
        Stage stage = Stage.deserialize();
        Commit headcommit = Commit.deserialize(_head);
        for (File file : working.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            Blob fileblob = new Blob(file);
            String filename = fileblob.getName();
            if (headcommit.getBlobsName().contains(filename)
                &&
                !stage.contains(filename)
                &&
                !fileblob.getID().equals(headcommit.blobMap(filename))) {
                toprint.add(filename + " (modified)");
            }

            if (stage.contains(filename)
                &&
                !fileblob.getID().equals(
                        stage.blobMapStage(filename).getID())) {
                toprint.add(filename + " (modified)");
            }
        }
        for (String filename : headcommit.getBlobsName()) {
            File checkfile = new File(filename);
            Blob fileblob = Blob.deserialize(headcommit.blobMap(filename));
            if ((!stage.contains(filename)) && (!checkfile.exists())
                &&
                !stage.containsRemoved(filename)) {
                toprint.add(filename + " (deleted)");
            }
        }

        for (String filename : stage.getFileNames()) {
            File checkfile = new File(filename);
            if (!checkfile.exists()) {
                toprint.add(filename + "(deleted)");
            }
        }

        Collections.sort(toprint);
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String file : toprint) {
            System.out.println(file);
        }
        System.out.println();
    }

    /** Displays the untracked files. */
    public void displayUntracked() {
        System.out.println("=== Untracked Files ===");
        Stage stage = Stage.deserialize();
        File working = Main.getDir().getAbsoluteFile().getParentFile();
        for (String filename : working.list()) {
            File asfile = new File(filename);
            if (!tracked(filename) && !asfile.isDirectory()
                    && !stage.contains(filename)
                    && !stage.containsRemoved(filename)) {
                System.out.println(filename);
            }
        }
        System.out.println();
    }

    /** Returns whether BLOBNAME is tracked in the current commit
     *  or staged. */
    public boolean tracked(String blobName) {
        Commit headCommit = getHead();
        Stage stage = Stage.deserialize();
        return (headCommit.getBlobsName().contains(blobName))
                && !stage.containsRemoved(blobName);
    }

    /** Returns the split point between BRANA and BRANB. */
    public Commit getSplitPt(Branch branA, Branch branB) {
        Commit commitA = branA.getCommit();
        Commit commitB = branB.getCommit();
        Commit temp;
        if (commitA.getAge() < commitB.getAge()) {
            temp = commitA;
            commitA = commitB;
            commitB = temp;
        }
        while (commitA.getAge() > commitB.getAge()) {
            commitA = commitA.getParent();
        }
        while (!commitA.getID().equals(commitB.getID())) {
            commitA = commitA.getParent();
            commitB = commitB.getParent();
        }
        return commitA;
    }

    /** Returns the head commit. */
    public Commit getHead() {
        return (Commit) Commit.deserialize(_head);
    }

    /** Adds the branch BRANCH to the hashmap of the repo. */
    private void addBranch(Branch branch) {
        _branches.put(branch.getName(), branch);
    }

    /** Returns the hashmap of branches contained by the repo. */
    public TreeMap<String, Branch> getBranches() {
        return this._branches;
    }

    /** Returns the head pointer. */
    public String getHeadPointer() {
        return this._head;
    }

    /** Sets HEAD (for testing). */
    public void setHead(String head) {
        this._head = head;
    }

    /** Returns the current branch you are on. */
    public Branch getCurrentBranch() {
        return this._currBranch;
    }

    /** Returns the name of the current branch you are on. */
    public String getCurrentBranchName() {
        return this._currBranch.getName();
    }

    /** Returns the branch corresponding to BRANCHNAME. */
    public Branch getBranch(String branchname) {
        return _branches.get(branchname);
    }

    /** Deserializes the Repository object in /.gitlet and returns it. */
    static Repository deserialize() {
        return (Repository) Utils.deserialize(Main.getDir(), "repo");
    }

    /** Serializes this repository object. */
    private void serialize() {
        Utils.serialize(Main.getDir(), "repo", this);
    }

    /** A Treemap containing all the branches. */
    private TreeMap<String, Branch> _branches =
        new TreeMap<String, Branch>();
    /** The current HEAD commit. */
    private String _head;
    /** Current branch. */
    private Branch _currBranch;
    /** Whether there were conflicted filse. */
    private boolean _conflicted = false;
    /** Length of hashcode. */
    private final int hashlength = 40;
}
