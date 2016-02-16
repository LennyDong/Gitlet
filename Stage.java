package gitlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.io.File;
import java.io.Serializable;
/** Represents the staging area.
 *  @author Akshay Sreekumar and Lenny Dong
 */
public class Stage implements Serializable {
    /** The directory containing all the files to be added. */
    private static final File ADDITION = new File(Main.getDir(), "addition");
    /** The directory containing all the files to be removed. */
    private static final File REMOVAL = new File(Main.getDir(), "removal");

    /** Creates a new stage to hold blobs. */
    public Stage() {
        init();
        _stage = new TreeMap<String, String>();
        _removed = new TreeMap<String, String>();
    }

    /** Initializes the addition and removal folder. */
    static void init() {
        if (!ADDITION.exists()) {
            ADDITION.mkdir();
        }
        if (!REMOVAL.exists()) {
            REMOVAL.mkdir();
        }
    }

    /** Adds BLOB to the stage. */
    public void add(Blob blob) {
        if (_removed.containsKey(blob.getName())) {
            _removed.remove(blob.getName());
            new File(REMOVAL, blob.getID()).delete();
            Utils.writeContents(new File(blob.getName()), blob.getContents());
            serialize();
            return;
        }
        _stage.put(blob.getName(), blob.getID());
        blob.serialize(ADDITION);
        serialize();
    }

    /** Unstages blob mapped to BLOBNAME for addition. */
    public void unstage(String blobName) {
        new File(ADDITION, blobName).delete();
        _stage.remove(blobName);
        serialize();
    }

    /** Clears all blobs from the stage. */
    public void clear() {
        _stage.clear();
        _removed.clear();
        for (String s: ADDITION.list()) {
            new File(ADDITION, s).delete();
        }
        for (String s: REMOVAL.list()) {
            new File(REMOVAL, s).delete();
        }
        serialize();
    }

    /** Returns the all blobs to be added. */
    public Collection<Blob> getBlobs() {
        ArrayList<Blob> blobs = new ArrayList<Blob>();
        for (String id: ADDITION.list()) {
            blobs.add(Blob.deserialize(id, ADDITION));
        }
        return blobs;
    }

    /** Returns if BLOBNAME is part of the stage. */
    public boolean contains(String blobName) {
        return _stage.keySet().contains(blobName);
    }

    /** Return all blobs to be removed from the current commit. */
    public Collection<Blob> getRemovedBlobs() {
        ArrayList<Blob> blobs = new ArrayList<Blob>();
        for (String id: REMOVAL.list()) {
            blobs.add(Blob.deserialize(id, REMOVAL));
        }
        return blobs;
    }

    /** Return if BLOBNAME is one of the removed blobs. */
    public boolean containsRemoved(String blobName) {
        return _removed.containsKey(blobName);
    }

    /** Returns the blob mapped to the FILENAME. */
    public Blob blobMapStage(String filename) {
        String id = _stage.get(filename);
        return Blob.deserialize(id, ADDITION);
    }

    /** Returns the filenames of blobs to be added. */
    public Collection<String> getFileNames() {
        return _stage.keySet();
    }

    /** Returns the filenames of blobs that were removed. */
    public Collection<String> getRemovedFileNames() {
        return _removed.keySet();
    }

    /** Adds BLOB to _removed. */
    public void remove(Blob blob) {
        _removed.put(blob.getName(), blob.getID());
        blob.serialize(REMOVAL);
        serialize();
    }

    /** Returns if the stage is empty. */
    public boolean isEmpty() {
        return _stage.size() == 0 && _removed.size() == 0;
    }

    /** Serializes this Stage object. */
    public void serialize() {
        Utils.serialize(Main.getDir(), "stage", this);
    }

    /** Deserializes the Stage object in /.gitlet and returns it. */
    public static Stage deserialize() {
        return (Stage) Utils.deserialize(Main.getDir(), "stage");
    }

    /** Underlying structure of the stage. */
    private TreeMap<String, String> _stage;
    /** Blob to be removed from the current commit. */
    private TreeMap<String, String> _removed;

}
