package CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows us to perform range queries over the data in the tree by assuming the key is an int.
 * Extends the functionality of {@link ScapeGoatTree} by adding the possibility of range queries.
 *
 * <bold>251 students: you must implement this class as well. This is meant to make your solution to
 *   story 5 easier, as well as any other use cases.</bold>
 *
 * @param <V> data type for the values the tree will be storing
 */
public class ScapeGoatIntKey<V> extends ScapeGoatTree<Integer, V> {
    /**
     * Constructs an empty scapegoat tree
     */
    public ScapeGoatIntKey() {
        super();
    }

    /**
     * Constructs a scapegoat tree with a root.
     *
     * @param rootKey  root key
     * @param rootData root data to store
     */
    public ScapeGoatIntKey(Integer rootKey, V rootData) {
        super(rootKey, rootData);
    }

    /**
     * Returns the data associated with the given range of keys, inclusive ( [start, end] ).  The data is sorted
     * by key.
     * <p>
     * It may be helpful to use a recursive function that acts similar to inorder().
     *
     * @param start starting key to retrieve
     * @param end   ending key to retrieve
     * @return a sorted list of values associated with the range of keys, or an empty list if no item exists in that range
     */
    public List<V> getRange(int start, int end) {
        List<V> result = new ArrayList<>();
        recursiveRange(root, start, end, result);
        return result;
    }

    private void recursiveRange(Node<Integer, V> node, int start, int end, List<V> result) {
        if (node == null) {
            return;
        }
        if (node.key > start) {
            recursiveRange(node.left, start, end, result);
        }
        if (node.key >= start && node.key <= end) {
            result.add(node.value);
        }
        if (node.key < end) {
            recursiveRange(node.right, start, end, result);
        }
    }
}

