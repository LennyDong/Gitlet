package gitlet;

import java.io.Serializable;

/** A representation of Branches.
 * @author Lenny Dong, Akshay Sreekumar
 */
public class Branch implements Serializable {

    /** Initializes the Branch named NAME pointing to commit
     *  COMMIT. */
    public Branch(String name, String commit) {
        _name = name;
        _commit = commit;
    }

    /** Updates the current commit of the branch to COMMIT. */
    public void update(String commit) {
        _commit = commit;
    }

    /** Returns the name of the branch. */
    public String getName() {
        return this._name;
    }

    /** Returns the head commit ID of this branch. */
    public String getCommitID() {
        return this._commit;
    }

    /** Returns the head commit of this branch. */
    public Commit getCommit() {
        return Commit.deserialize(getCommitID());
    }

    /** Sets the name of the branch to NAME. */
    public void setName(String name) {
        this._name = name;
    }

    /** Name of the branch. */
    private String _name;
    /** The current commit in this branch. */
    private String _commit;
}
