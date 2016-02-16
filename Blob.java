package gitlet;

import java.io.File;
import java.io.Serializable;

/** Represents a blob.
 *  @author Akshay Sreekumar and Lenny Dong
 */
public class Blob implements Serializable {

    /** Creates a new blob with the contents of FILE. */
    Blob(File file) {
        _contents = Utils.readContents(file);
        _testname = file.getName();
        _id = "blob" + Utils.sha1(_contents, _testname);
    }
    /** Returns the name of the file contained in the blob. */
    public String getName() {
        return _testname;
    }

    /** Returns the hashcode of the blob. */
    public String getID() {
        return _id;
    }

    /**Returns the contents of the blob. */
    public byte[] getContents() {
        return this._contents;
    }

    /** Returns the contents of the blob in a string. */
    public String getStringContents() {
        return new String(_contents);
    }

    /** Serializes the current Blob. */
    public void serialize() {
        Utils.serialize(Main.getDir(), _id, this);
    }

    /** Serializes the current Blob into DIR. */
    public void serialize(File dir) {
        Utils.serialize(dir, _id, this);
    }

    /** Deserializes the Blob with ID as its hashcode and returns
     *  the Blob. */
    public static Blob deserialize(String id) {
        return (Blob) Utils.deserialize(Main.getDir(), id);
    }

    /** Deserializes the Blob with ID as its hashcode and returns
     *  the Blob thats contained in DIR. */
    public static Blob deserialize(String id, File dir) {
        return (Blob) Utils.deserialize(dir, id);
    }

    /** Actual contents of the file. */
    private byte[] _contents;
    /** Hash code of the blob. */
    private String _id;
    /** Name of the file for testing purposes. */
    private String _testname;
}
