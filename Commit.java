package gitlet;

import java.util.List;
import java.util.Set;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;

/** Represents a commit object.
 *  @author Akshay Sreekumar and Lenny Dong
 */
public class Commit implements Serializable {
    /** Format for timestamp. **/
    private static final SimpleDateFormat FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** Creates an intial commit with just a message. */
    Commit() {
        _msg = "initial commit";
        _calendar = Calendar.getInstance();
        _now = _calendar.getTime();
        _time = new Timestamp(_now.getTime());
        _id = Utils.sha1(_msg);
        _abbrev = _id.substring(start, end);
        _abbrevs.put(_abbrev, _id);
        Utils.serialize(Main.getDir(), "abbrevmap", _abbrevs);
        _age = 0;
    }
    /** Creates a commit with message MSG
    blob hash codes BLOBS, and hash code of the
    parent PARENT. */
    Commit(String msg, List<Blob> blobs, String parent) {
        _calendar = Calendar.getInstance();
        _now = _calendar.getTime();
        _msg = msg;
        _time = new Timestamp(_now.getTime());
        for (Blob b: blobs) {
            _blobz.put(b.getName(), b.getID());
        }
        _parent  = parent;
        Commit parentCommit = Commit.deserialize(parent);
        _age = parentCommit.getAge() + 1;
        String blobFileNames = "";
        for (String name: _blobz.keySet()) {
            blobFileNames.concat(name);
        }

        String blobID = "";
        for (String name: _blobz.values()) {
            blobID.concat(name);
        }
        _id = Utils.sha1(_msg, _time.toString(),
            blobFileNames, blobID, _parent);
        _abbrev = _id.substring(start, end);
        HashMap<String, String> abbrevmap =
            (HashMap<String, String>)
            Utils.deserialize(Main.getDir(), "abbrevmap");
        abbrevmap.put(_abbrev, _id);
        Utils.serialize(Main.getDir(), "abbrevmap", abbrevmap);
    }

    /** Returns a Collection containing the hashcodes
     *  of the blobs in this commit. */
    public Collection<String> getBlobsHash() {
        return _blobz.values();
    }

    /** Returns a Set containing the filenames of the blobs in this commit. */
    public Set<String> getBlobsName() {
        return _blobz.keySet();
    }

    /** Returns the hash code of the commit. */
    public String getID() {
        return _id;
    }

    /** Returns the blob with BLOBNAME as name. */
    public Blob getBlob(String blobName) {
        String hash = _blobz.get(blobName);
        return Blob.deserialize(hash);
    }

    /** Returns a hashmap containing the blobs mapped by their filename. */
    public HashMap<String, Blob> getBlobMap() {
        HashMap<String, Blob> blobs = new HashMap<String, Blob>();
        Blob temp;
        for (String blobName: _blobz.keySet()) {
            temp = Blob.deserialize(_blobz.get(blobName));
            blobs.put(blobName, temp);
        }
        return blobs;
    }

    /** Returns a hashmap containing the hash codes of the blobs mapped
     *  by their filenames. */
    public HashMap<String, String> getIDMap() {
        return _blobz;
    }

    /** Returns the id of a blob from its internal FILENAME. */
    public String blobMap(String filename) {
        return _blobz.get(filename);
    }

     /** Returns the abbreviation of the hash code. */
    public String getAbbrev() {
        return this._abbrev;
    }
    /** Returns the mapping of abbrevs to full codes. */
    public static HashMap<String, String> getAbbrevMap() {
        return _abbrevs;
    }
    /** Returns the ID of the parent commit. */
    public String getParentID() {
        return _parent;
    }
    /** Returns the actual parent commit. */
    public Commit getParent() {
        return Commit.deserialize(_parent);
    }
    /** Returns the message of this commit. */
    public String getMsg() {
        return _msg;
    }
    /** Returns the age of the commit. */
    public int getAge() {
        return _age;
    }
    /** Serializes this Commit object. */
    public void serialize() {
        Utils.serialize(Main.getDir(), this._id, this);
    }
    /** Deserializes the Commit with the given ID, and returns it. */
    public static Commit deserialize(String id) {
        return (Commit) Utils.deserialize(Main.getDir(), id);
    }

    /** Returns the log entry of the commit. */
    public String toString() {
        String result = "===" + "\n" + "Commit "
            + this._id + "\n" + FORMAT.format(this._time)
            + "\n" + this._msg
            + "\n";
        return result;
    }

    /** Hash code of the parent commit. */
    private String _parent;
    /** Map linking filenames to hash codes of all the blobs the commit has. */
    private HashMap<String, String> _blobz = new HashMap<String, String>();
    /** Message of the commit. */
    private String _msg;
    /** Time commit was created. */
    private Timestamp _time;
    /** Hash code of the commit. */
    private String _id;
    /** Helper to determine timestamp. */
    private Calendar _calendar;
    /** Helper to determine timestamp. */
    private Date _now;
    /** Abbreviated hash code of the commit. */
    private String _abbrev;
    /** Number of commits since the inital commit. */
    private int _age;
    /** A hashmap from abbrevs to full codes. */
    private static HashMap<String, String> _abbrevs =
        new HashMap<String, String>();
    /** Start of abbreviation. */
    private final int start = 0;
    /** End of abbreviation. */
    private final int end = 8;
}
