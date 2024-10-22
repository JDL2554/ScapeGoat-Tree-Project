package CommonUtils;

import CommonUtils.Interfaces.ScapeGoatTreeInterface;

import java.awt.*;
import java.util.List;

/**
 * Extends/implements methods from our {@link ScapeGoatTreeInterface} and adds two constructors.
 *
 * <bold>251 students: You are allowed to use java.util.* in your implementation (e.g. ArrayList), if you want. However,
 *   you must implement your own ScapeGoat tree.</bold>
 *
 * @apiNote This class uses either .equals() or .compareTo() to check if two keys are equal. Any parameterized
 *   use of this class must ensure these functions are implemented in the key type for correct behavior.
 *
 * @param <K> data type for the keys the tree will store.  Must be comparable.
 * @param <V> data type for the values the tree will be storing
 */
public class ScapeGoatTree<K extends Comparable<K>, V> extends ScapeGoatTreeInterface<K, V> {
    //root
    protected Node<K, V> root;
    //total number of nodes
    protected int nodeCount = 0;
    /**
     * max node count required to use rebuild in remove
     *
     * @implNote maxNodeCount enables scapegoat trees to be well-balanced over time. They store
     *   this additional value with the tree data structure.  maxNodeCount simply represents
     *   the highest achieved NodeCount.  maxNodeCount is set to nodeCount whenever the entire tree
     *   is rebalanced (i.e. rebuilt is called on root), and after adding a node it is
     *   set to max(maxNodeCount, nodeCount).
     */
    protected int maxNodeCount = 0;

    //alpha parameter defined in abstract parent class ("ALPHA_THRESHOLD")

    /**
     * Constructs an empty scapegoat tree
     */
    public ScapeGoatTree(){ root = null; }

    /**
     * Constructs a scapegoat tree with a root.
     * @param rootKey root key
     * @param rootData root data to store
     */
    public ScapeGoatTree(K rootKey, V rootData) {
        root = new Node<K, V>(rootKey, rootData, null, null, null);
        nodeCount++;
        maxNodeCount = nodeCount;
    }


    /**
     * This might be helpful for your debugging
     */
    public void printTreeInorder(){
        List<Node<K, V>> nodes = inorder(root);
        for (Node<K, V> node : nodes) {
            System.out.println(node.toString());
        }
    }
    /**
     * This might be helpful for your debugging
     */
    public void printTreePreorder(){
        List<Node<K, V>> nodes = preorder(root);
        for (Node<K, V> node : nodes) {
            System.out.println(node.toString());
        }
    }

    /**
     * Retrieves the root of the scapegoat tree, or <code>null</code> if none exists.
     *
     * @return the root of the scapegoat tree, or <code>null</code> if none exists
     */
    @Override
    public Node<K, V> root() {
        if (this.size() == 0) {
            return null;
        }
        return root;
    }

    /**
     * Finds the first scapegoat node and returns it.  The first scapegoat node is the first node at or above
     * the passed in node which does not satisfy the alpha-weight-balanced property.  This function is meant
     * to be used after you insert a node in the tree.  Since this function is only supposed to be called
     * when an insertion breaks the alpha-weight-balanced property, we know that we must find at least one
     * node that isn't alpha-weight-balanced.
     * <p>
     * Hint: when you add a new node, the possible nodes that do not meet the alpha-weight-balanced property
     * are on the path from the inserted node to the root.
     *
     * @param node newly inserted node to start searching at
     * @return the first scapegoat node
     * @implNote this function is not individually tested, it is for your convenience in implementation.
     */
    @Override
    protected Node<K, V> scapeGoatNode(Node<K, V> node) {
        Node<K, V> current = node.parent;
        while (current != null) {
            int leftSize = sizeOfSubtree(current.left);
            int rightSize = sizeOfSubtree(current.right);
            if (Math.max(leftSize, rightSize) > ALPHA_THRESHOLD * sizeOfSubtree(current)) {
                return current;
            }
            current = current.parent;
        }
        return null;
    }

    /**
     * Rebuilds the subtree rooted at this node to be a perfectly balanced BST.
     * <p>
     * One approach is to get the subtree elements in some sorted order (for you to think about). Then,
     * we can build a perfectly balanced BST.  The main idea is to rebuild the subtree rooted at this
     * node into a perfectly balanced BST and then return the new root.  You could (but are not required
     * to) use a recursive function.  The middle of a list is defined as floor(size()/2).
     *
     * @param node root of subtree to rebuild
     * @return the new root of the balanced subtree
     * @implNote this function is not individually tested, it is for your convenience in implementation.
     */
    @Override
    protected Node<K, V> rebuild(Node<K, V> node) {
        List<Node<K, V>> inorder = inorder(node);
        node = buildSubtree(inorder);
        return node;
    }


    protected Node<K, V> buildSubtree(List<Node<K, V>> ordered) { // A is a sorted array of keys
        int k = ordered.size();
        if (k == 0){
            return null;
        }
        else {
            int median = k/2;
            Node<K, V> oldNode = ordered.get(median);
            Node<K, V> newRoot = new Node<>(oldNode.key, oldNode.value, null, null, null);


            List<Node<K, V>> orderedLeft = ordered.subList(0, median);
            newRoot.left = buildSubtree(orderedLeft);
            if (newRoot.left != null) {
                newRoot.left.parent = newRoot;
            }


            List<Node<K, V>> orderedRight = ordered.subList(median + 1, k);
            newRoot.right = buildSubtree(orderedRight);
            if (newRoot.right != null) {
                newRoot.right.parent = newRoot;
            }
            return newRoot;
        }
    }


    /**
     * Adds an element to the scapegoat tree. Passing key=null will not change the state of the tree.
     * Some guidance is provided below:
     * <p>
     * 1. Find the insertion point. Ensure you know the depth you are inserting at, as it is useful later.
     * 2. If that data already exists in the tree, skip inserting that data.
     * 3. Insert the new data
     * 4. Check if the tree is still alpha-weight-balanced. By the theory on the wiki page, we know we can
     * check this by making sure the tree is still alpha-height-balanced.
     * 5. If not, rebalance. You will need to find the scapegoat node.
     * 6. The entire subtree rooted at the scapegoat node will need to be rebuilt using <code>rebuild()</code> above
     * 7. Connect new subtree back to main tree correctly
     * <p>
     * The above steps are based on the wikipedia article provided in the handout and at the top of this file.
     *
     * @param key   key to insert
     * @param value value to associate with key
     */
    @Override
    public void add(K key, V value) {
        Node<K, V> current = root;
        int depth = 0;
        Node<K, V> newNode = new Node<>(key, value, null, null, null);
        if (key == null) {
            return;
        }
        if (root == null) {
            root = new Node<>(key, value, null, null, null);
            nodeCount++;
            return;
        }
        while (current != null) {
            if (key.compareTo(current.key) < 0) {
                if (current.left == null) {
                    newNode.parent = current;
                    current.left = newNode;
                    depth++;
                    nodeCount++;
                    break;
                }
                current = current.left;
                depth++;
            }
            else if (key.compareTo(current.key) > 0) {
                if (current.right == null) {
                    newNode.parent = current;
                    current.right = newNode;
                    depth++;
                    nodeCount++;
                    break;
                }
                current = current.right;
                depth++;
            }
            else {
                return;
            }
        }
        int subtreeHeight = depth;
        double cmpSize = Math.log(this.size()) / Math.log(1.0 / ALPHA_THRESHOLD);
        if (subtreeHeight > cmpSize) {
            Node<K, V> scapeGoatNode = scapeGoatNode(newNode);
            Node<K, V> postGoatNode = rebuild(scapeGoatNode);
            if (scapeGoatNode.parent == null) {
                root = postGoatNode;
                root.parent = null;
            } else if ((scapeGoatNode.parent.left != null) && (scapeGoatNode.parent.left.equals(scapeGoatNode))) {
                scapeGoatNode.parent.left = postGoatNode;
                postGoatNode.parent = scapeGoatNode.parent;
            } else {
                scapeGoatNode.parent.right = postGoatNode;
                postGoatNode.parent = scapeGoatNode.parent;
            }
            scapeGoatNode = null;
            maxNodeCount = Math.max(nodeCount, maxNodeCount);
        }
    }


    /**
     * Removes an element from the tree. Does not change the tree if key does not exist in it.
     * Some guidance is provided below:
     * <p>
     * 1. Find the deletion point (if it exists).
     * 2. Deletion is done in the way you would delete a node from a regular BST.  The policy for this should be
     * the same as the one in the professor's slides (there are multiple correct policies, follow the one on
     * the slides).  The one difference is that we will use the successor node instead of the predecessor node
     * (see {@link #succNode})
     * 3. Slight modifications for scapegoat based on your implementation may be required.
     * 4. After deletion, if nodeCount <= alphaweight * MaxNodeCount, then we rebuild the entire tree around the
     * "root" again (i.e. call rebuild and ensure the new root follows the properties of the root).
     *
     * @param key key to remove
     */
    @Override
    public void remove(K key) {
        Node<K, V> deleteNode = findNode(key);
        if (deleteNode == null) {
            return;
        }
        if (deleteNode.left == null && deleteNode.right == null) {
            if (deleteNode.parent == null) {
                root = null;
            }
            else {
                if ((deleteNode.parent.left != null)  && (deleteNode.parent.left.equals(deleteNode))) {
                    deleteNode.parent.left = null;
                } else {
                    deleteNode.parent.right = null;
                }
            }
        }
        else if (deleteNode.left != null && deleteNode.right == null) {
            if (deleteNode.parent == null) {
                // Node is root
                root = deleteNode.left;
                root.parent = null;
            } else {
                if ((deleteNode.parent.left != null) && (deleteNode.parent.left.equals(deleteNode))) {
                    deleteNode.parent.left = deleteNode.left;
                } else {
                    deleteNode.parent.right = deleteNode.left;
                }
                deleteNode.left.parent = deleteNode.parent;
            }
        }
        else if (deleteNode.right != null && deleteNode.left == null) {
            if (deleteNode.parent == null) {
                // Node is root
                root = deleteNode.right;
                root.parent = null;
            } else {
                if ((deleteNode.parent.left != null) && (deleteNode.parent.left.equals(deleteNode))) {
                    deleteNode.parent.left = deleteNode.right;
                } else {
                    deleteNode.parent.right = deleteNode.right;
                }
                deleteNode.right.parent = deleteNode.parent;
            }
        }
        else {
            Node<K, V> successor = succNode(deleteNode);
            deleteNode.key = successor.key;
            deleteNode.value = successor.value;


            if (successor.parent.left.equals(successor)) {
                successor.parent.left = successor.right;
            } else {
                successor.parent.right = successor.right;
            }
            if (successor.right != null) {
                successor.right.parent = successor.parent;
            }
        }
        nodeCount--;


        if (nodeCount <= ALPHA_THRESHOLD * maxNodeCount) {
            root = rebuild(root);
            maxNodeCount = nodeCount;
        }
    }


    /**
     * Returns the node associated with the given key.
     *
     * (be careful will null...)
     *
     * @param key key to search for
     * @return node associated with key, or <code>null</code> if item does not exist
     * @apiNote This function is protected because we don't want outside classes to have access to
     * the internal structure of our tree, which is possible through Node's interface.  Thus, we
     * have an internal function to find a Node and an external function which just returns the
     * value, preventing external classes from modifying the tree.
     */
    @Override
    protected Node<K, V> findNode(K key) {
        if (key == null) {
            return null;
        }
        Node<K, V> current = root;
        while (current != null) {
            int cmp = key.compareTo(current.key);
            if (cmp == 0) {
                return current;
            } else if (cmp < 0) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return null;
    }

    /**
     * Empties the tree, resetting all pertinent variables.
     */
    @Override
    public void clear() {
        root = null;
        nodeCount = 0;
        maxNodeCount = 0;
    }

    /**
     * Returns the number of nodes this tree contains.
     * @return number of nodes in the tree
     */
    public int size(){ return this.nodeCount; }


    /**
     * DO NOT MODIFY NOR IMPLEMENT THIS FUNCTION
     *
     * @param g graphics object to draw on
     */
    @Override
    public void draw(Graphics g) {
        //DO NOT MODIFY NOR IMPLEMENT THIS FUNCTION
        if(g != null) g.getColor();
        //todo GRAPHICS DEVELOPER:: draw the hash table how we discussed
        //251 STUDENTS:: YOU ARE NOT THE GRAPHICS DEVELOPER!
    }

    /**
     * DO NOT MODIFY NOR IMPLEMENT THIS FUNCTION
     *
     * @param g graphics object to draw on
     */
    @Override
    public void visualize(Graphics g) {
        //DO NOT MODIFY NOR IMPLEMENT THIS FUNCTION
        if(g != null) g.getColor();
        //todo GRAPHICS DEVELOPER:: visualization is to be time-based -- how we discussed
        //251 STUDENTS:: YOU ARE NOT THE GRAPHICS DEVELOPER!
    }
}
