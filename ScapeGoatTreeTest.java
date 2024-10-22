package CommonUtils.test;

import CommonUtils.Interfaces.ScapeGoatTreeInterface.Node;
import CommonUtils.ScapeGoatIntKey;
import CommonUtils.ScapeGoatTree;
import CommonUtils.test.UsefulObjects.Blob;
import CommonUtils.test.UsefulObjects.ComplexObject;
import CommonUtils.test.UsefulObjects.NewInt;
import CommonUtils.test.factories.IntTestFactory;
import CommonUtils.test.factories.NullAndNotFoundTestFactory;
import CommonUtils.test.factories.SizeEmptyBasicAddRemoveBackTestFactory;
import Drones.test.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ethan Dickey
 * Tests the ScapeGoatTree class.  Extensively.  According to the interface specifications.
 */
public class ScapeGoatTreeTest {
    /**
     * Sanity check.  Failing this should fail all other test cases.
     */
    @Test
    @SuppressWarnings("unchecked")
    void sanityCheck(){
        ScapeGoatTree<String, Integer> tree = new ScapeGoatTree<>();
        List<Node<String, Integer>> nodes = List.of(new Node[]{
                new Node("0", 1, null, null, null),
                new Node("1", 2, null, null, null),
                new Node("2", 3, null, null, null),
                new Node("3", 4, null, null, null),
        });

        for(var i : nodes){
            tree.add(i.key, i.value);
        }

        TestUtils.compareArraysWithEqual(nodes, tree.inorder(tree.root()), "Sanity check");
    }

    /**
     * Tests add/remove/get/getRange/root for null and nonexistent key behavior.
     */
    @Nested
    class ErrorTests {
        /**
         * Contains all the error tests for the 4 constructors (mostly looking for uninitialized tree errors)
         *
         * test use of both constructors + add/remove/get/getrange/root, etc, for uninitialized errors via remove, etc
         */
        @Nested
        class ConstructorTestSuite {
            /**
             * Tests errors associated with the constructors
             */
            abstract class ConstructorTest<K extends Comparable<K>, V> {
                ScapeGoatTree<K, V> tree;

                /**
                 * Setup tree for testing
                 */
                @BeforeEach
                abstract void setup();

                /**
                 * Gets a default key value for testing
                 * @return default key value
                 */
                abstract K getDefaultKey();

                /**
                 * Gets a default value for testing
                 * @return default value
                 */
                abstract V getDefaultValue();

                /**
                 * Tests add in this context
                 */
                @Test
                void addTest(){
                    assertDoesNotThrow(() -> tree.add(getDefaultKey(), getDefaultValue()));
                    assertEquals(getDefaultValue(), tree.get(getDefaultKey()));
                }
                /**
                 * Tests remove in this context
                 */
                @Test
                void removeTest(){
                    assertDoesNotThrow(() -> tree.remove(getDefaultKey()));
                }
                /**
                 * Tests get in this context
                 */
                @Test
                void getTest(){
                    assertDoesNotThrow(() -> tree.get(getDefaultKey()));
                }
                /**
                 * Tests root() in this context
                 */
                @Test
                void rootTest(){
                    assertDoesNotThrow(() -> tree.root());
                }
            }

            /**
             * Tests constructor errors with the constructor with no parameters
             */
            @Nested
            class ConstructorNoParams extends ConstructorTest<String, Integer> {
                /**
                 * Setup tree for testing
                 */
                @BeforeEach
                @Override
                void setup() {
                    tree = new ScapeGoatTree<>();
                }

                /**
                 * Gets a default key value for testing
                 *
                 * @return default key value
                 */
                @Override
                String getDefaultKey() {
                    return "hi";
                }

                /**
                 * Gets a default value for testing
                 *
                 * @return default value
                 */
                @Override
                Integer getDefaultValue() {
                    return 0;
                }
            }

            /**
             * Tests the constructor with parameters
             *
             * I love being lazy, abstract classes are so much fun :)
             */
            @Nested
            class ConstructorWithParams extends ConstructorTest<String, Integer> {
                /**
                 * Setup tree for testing
                 */
                @BeforeEach
                @Override
                void setup() {
                    tree = new ScapeGoatTree<>("Hi", 0);
                }

                /**
                 * Gets a default key value for testing
                 *
                 * @return default key value
                 */
                @Override
                String getDefaultKey() {
                    return "null?";
                }

                /**
                 * Gets a default value for testing
                 *
                 * @return default value
                 */
                @Override
                Integer getDefaultValue() {
                    return 2000000000;
                }
            }

            /**
             * Tests the constructor with no params for the IntKey child class
             */
            @Nested
            class ConstructorIntKeyNoParams extends ConstructorTest<Integer, String>{
                /**
                 * Tests getRange in this context
                 */
                @Test
                void getRangeTest(){
                    ScapeGoatIntKey<String> tree1 = new ScapeGoatIntKey<>();
                    assertDoesNotThrow(() -> tree1.getRange(0, 1000000));
                }

                /**
                 * Setup tree for testing
                 */
                @BeforeEach
                @Override
                void setup() {
                    tree = new ScapeGoatIntKey<>();
                }

                /**
                 * Gets a default key value for testing
                 *
                 * @return default key value
                 */
                @Override
                Integer getDefaultKey() {
                    return 42;
                }

                /**
                 * Gets a default value for testing
                 *
                 * @return default value
                 */
                @Override
                String getDefaultValue() {
                    return "null!";
                }
            }


            /**
             * Tests the constructor with params for the IntKey child class
             */
            @Nested
            class ConstructorIntKeyWithParams extends ConstructorTest<Integer, String>{
                /**
                 * Tests getRange in this context
                 */
                @Test
                void getRangeTest(){
                    ScapeGoatIntKey<String> tree1 = new ScapeGoatIntKey<>(34, "hi");
                    assertDoesNotThrow(() -> tree1.getRange(0, 1000000));
                }

                /**
                 * Setup tree for testing
                 */
                @BeforeEach
                @Override
                void setup() {
                    tree = new ScapeGoatIntKey<>(256, "The first perfect square of a square of a square :)");
                }

                /**
                 * Gets a default key value for testing
                 *
                 * @return default key value
                 */
                @Override
                Integer getDefaultKey() {
                    return 43;
                }

                /**
                 * Gets a default value for testing
                 *
                 * @return default value
                 */
                @Override
                String getDefaultValue() {
                    return "Forty three is not the answer to everything! Why you do dis :/";
                }
            }
        }

        /**
         * Tests invalid get/getrange/remove (null and key not found should not throw), get should return null.
         * Also tests that adding null does not change the state of the container.
         */
        @Nested
        class NullAndNotFoundErrors extends NullAndNotFoundTestFactory {
            ScapeGoatIntKey<Integer> tree = null;
            private final Integer NUM_EL_TO_FILL = 5000000;

            /**
             * Tests that getRange returns an empty list for ranges that don't exist.
             * Parent class does not check this (nor should it).
             */
            @Test
            void testGetInvalidRange(){
                fillContainer();

                //hacky: creating ranges out of all pairs of negative or positive numbers
                // that shouldn't appear individually in the container
                List<Integer> neg = getKeysThatDoNotExistInFullContainer().stream()
                                                .sorted()
                                                .filter(i -> i < 0)
                                                .collect(Collectors.toList());
                List<Integer> pos = getKeysThatDoNotExistInFullContainer().stream()
                                                .sorted()
                                                .filter(i -> i > 0)
                                                .collect(Collectors.toList());
                //all neg pairs
                for(var i : neg){//sorted
                    for(var j : neg){
                        List<Integer> temp = tree.getRange(i, j);
                        assertNotNull(temp);
                        assertEquals(0, temp.size());
                    }
                }

                //all pos pairs
                for(var i : pos){//sorted
                    for(var j : pos){
                        List<Integer> temp = tree.getRange(i, j);
                        assertNotNull(temp);
                        assertEquals(0, temp.size());
                    }
                }

                verifyContainerDidNotChange();
            }

            /**
             * Ensures that getRange can handle start > end (i.e. no throw and empty list).
             */
            @Test
            void testGetRangeSwappedRange(){
                fillContainer();
                assertDoesNotThrow(() -> tree.getRange(10, 5));
                assertNotNull(tree.getRange(10, 5));
                assertEquals(0, tree.getRange(10, 5).size());
                verifyContainerDidNotChange();
            }

            /**
             * Reinitializes the container to be empty.  Useful in NullInsert test.
             */
            @Override
            protected void makeNewEmptyContainer() { tree = new ScapeGoatIntKey<>(); }

            /**
             * Children must provide functionality to fill their container before each test, as well as a way to
             * verify that nothing changed.  Does not assume container is initialized.
             *
             * @implNote this function and {@link #verifyContainerDidNotChange()} must agree on what
             * is in the container.
             */
            @Override
            protected void fillContainer() {
                tree = new ScapeGoatIntKey<>();
                List<Integer> keys = new ArrayList<>();
                //generate keys
                for(int i=0; i<NUM_EL_TO_FILL; i++){
                    keys.add(i);
                }
                //mix them up for insertion
                Collections.shuffle(keys);
                //insert
                for(var i : keys){
                    tree.add(i, i+1);
                }
            }

            /**
             * Returns a key that does not exist in the full container, after running {@link #fillContainer()}
             *
             * @return key that does not exist in full container
             */
            @Override
            protected List<Integer> getKeysThatDoNotExistInFullContainer() {
                List<Integer> orig = new ArrayList<>(Arrays.asList(-1, 10000000, NUM_EL_TO_FILL, NUM_EL_TO_FILL+265,
                                    NUM_EL_TO_FILL+1024, NUM_EL_TO_FILL+2048,
                                    NUM_EL_TO_FILL+1000000, NUM_EL_TO_FILL+65535));
                int sizeThing = orig.size()-1;
                for(int i=1; i<sizeThing; i++){
                    orig.add(orig.get(i)*-1);
                }
                //just in case and because I'm too lazy to check when I change NUM_EL_TO_FILL
                return orig.stream().filter(i -> i < 0 || i >= NUM_EL_TO_FILL).collect(Collectors.toList());
            }

            /**
             * Children must provide functionality to fill their container before each test, as well as a way to
             * verify that nothing changed.
             *
             * @implNote {@link #fillContainer()} and this function must agree on what is in the container.
             */
            @Override
            protected void verifyContainerDidNotChange() {
                //size
                assertEquals(NUM_EL_TO_FILL, tree.size());

                //inorder
                var nodes = tree.inorder(tree.root());
                assertEquals(NUM_EL_TO_FILL, nodes.size());
                for(int i=0; i<nodes.size(); i++){
                    assertEquals(i, nodes.get(i).key);
                    assertEquals(i+1, nodes.get(i).value);
                }

                //get
                for(int i=0; i<NUM_EL_TO_FILL; i++){
                    assertEquals(i+1, tree.get(i));
                }

                //remove
                int numLeft = NUM_EL_TO_FILL;
                tree.remove(0); numLeft--;//min key in the table
                assertEquals(numLeft, tree.size());
                tree.remove(NUM_EL_TO_FILL-1); numLeft--;//max key in the table
                assertEquals(numLeft, tree.size());

                //awkward remove:: every 3rd/5th/7th element
                for(int i=1, j=0; i<NUM_EL_TO_FILL-1; i += 2*j+3, j = (j+1)%3){
                    tree.remove(i); numLeft--;
                    assertEquals(numLeft, tree.size());
                }

                //clear
                tree.clear();
                assertNull(tree.root());
                assertEquals(0, tree.size());
                for(int i=0; i<NUM_EL_TO_FILL; i++){
                    assertNull(tree.get(i));
                }
            }

            /**
             * Factory methods for calling the appropriate add function on the container you are testing.
             *
             * @param key   key to add with value
             * @param value value to add with key
             * @throws Exception if something goes wrong
             */
            @Override
            protected void add(Integer key, Integer value) throws Exception { tree.add(key, value); }

            /**
             * Factory method for calling the appropriate get(key) function on the container you are testing.
             *
             * @param key key to retrieve the value for
             * @return value associated with the key
             * @throws Exception if something goes wrong
             */
            @Override
            protected Integer get(Integer key) throws Exception { return tree.get(key); }

            /**
             * Factory method for calling the appropriate parameterized remove function on the container you are testing.
             *
             * @param key key to remove from container
             * @throws Exception if something goes wrong
             */
            @Override
            protected void remove(Integer key) throws Exception { tree.remove(key); }

            /**
             * Factory methods for calling the appropriate size() function on the container you are testing.
             */
            @Override
            protected long getSize() { return tree.size(); }
        }

        /**
         * Verifies that calling root() on an empty tree returns null;
         */
        @Test
        void testNullRoot(){
            ScapeGoatTree<String, Integer> tree = new ScapeGoatTree<>();
            assertNull(tree.root());
        }
    }

    /**
     * Verifies that it can hold integers properly
     */
    @Nested
    class AddGetTests extends IntTestFactory {
        ScapeGoatTree<Integer, Integer> tree = new ScapeGoatTree<>();

        /**
         * Factory method for calling the appropriate function you want to test for signed ints validity
         *
         * @param num signed int to test
         * @return the result of a getField on the respective object
         * @throws Exception if something goes wrong
         */
        @Override
        protected int setGetField(int num) throws Exception {
            tree.add(num, num);
            return tree.get(num);
        }
    }

    /**
     * Does basic add/remove with large amount of elements exhaustive testing
     * Also test memory efficient use (vs hashtable, but this is mute because add is so slow).
     */
    @Nested
    class ExtensiveUsageTests extends SizeEmptyBasicAddRemoveBackTestFactory {
        ScapeGoatTree<Long, Long> tree = new ScapeGoatTree<>();
        /**
         * Factory methods for calling the appropriate add function on the container you are testing.  Requires persistence.
         *
         * @param o object to add
         * @throws Exception if something goes wrong
         */
        @Override
        protected void add(long o) throws Exception { tree.add(o, o); }

        /**
         * Factory methods for calling the appropriate parameterized remove function on the container you are
         * testing.  Requires persistence.
         * If you don't have one, leave it blank and override getTestParamterizedRemove to return false.
         *
         * @param o object to add
         * @throws Exception if something goes wrong
         */
        @Override
        protected void removeParameterized(long o) throws Exception { tree.remove(o); }

        /**
         * Factory methods for calling the appropriate remove back function on the container you are
         * testing.  Requires persistence.
         * If you don't have one, leave it blank and override getTestRemoveBack to return false.
         *
         * @throws Exception if something goes wrong
         */
        @Override
        protected void removeBack() throws Exception { /* nothing here on purpose */}
        /**
         * Gives the subclass the option of whether to run remove back tests (if the type
         * being tested can't perform that operation, for example)
         *
         * @return whether to run remove back tests
         */
        @Override
        protected boolean getTestRemoveBack() { return false; }

        /**
         * Factory methods for calling the appropriate size() function on the container you are testing.  Requires persistence.
         */
        @Override
        protected long getSize() { return tree.size(); }

        /**
         * Factory methods for calling the appropriate isEmpty back function on the container you are testing.  Requires persistence.
         */
        @Override
        protected boolean isEmpty() { return tree.size() == 0; }

        /**
         * Gives the subclass the option to override the maximum container size based on what is being tested
         * Default 800mil (don't want test to run too long)
         *
         * @return Max size of container to test
         */
        @Override
        protected long maxElementsInContainer() {
            return 10000000L;//10m
        }
    }

    /**
     * Tests the efficiency of the getRange function in ScapeGoatIntKey.  Goal is to have a big tree with
     *   lots of small (~10) range queries -- this will ensure they aren't searching the whole tree each time.
     */
    @Nested
    class GetRangeEfficiencyTest {
        private final int NUM_EL_TO_INSERT = 1000000;//1m -- multiplied by 2
        //locally range queries ran in about 800ms, so providing 2s
        private final int MAX_TIME_FOR_RANGE_QUERIES = 2000;//2000ms based on local testing, OK TO CHANGE!
        /**
         * VALUE_DELTA is the difference between the key and the value
         * RANGE_TO_PROBE is the size of range to request at a time (keeping it small),
         *   important when generating valid ranges
         */
        private final int VALUE_DELTA = 20, RANGE_TO_PROBE = 10;

        /**
         * Runs the efficiency test
         *
         * @apiNote After extensive testing, I have come to realize that our add() is only a little more
         *   inefficient than TreeMap, which the Java Library's version of a RB tree.  It is the main
         *   cause of lag in this function, but is not included in the time allocation for the range queries,
         *   as that is not what we are trying to test here.
         */
        @Test
        void getRangeEfficiencyTest() {
            ScapeGoatIntKey<Integer> tree = new ScapeGoatIntKey<>();

            //generate keys
            List<Integer> keys = new ArrayList<>();
            for (int i = -1 * NUM_EL_TO_INSERT; i < NUM_EL_TO_INSERT; i++) {
                keys.add(i);
            }
            //shuffle keys
            Collections.shuffle(keys);
            //insert keys
            for (var i : keys) {
                tree.add(i, i + VALUE_DELTA);
            }

            //generate ranges to test
            List<Integer> rangeStarts = getRangeNums();

            //test small ranges of 10 numbers for each range start value
            //first verify that the range is correct, then run time trial without validation to speed things up
            for(var i : rangeStarts){
                List<Integer> temp = tree.getRange(i, i+RANGE_TO_PROBE-1), trueAns = new ArrayList<>(RANGE_TO_PROBE);
                for(int j=0; j<RANGE_TO_PROBE; j++){
                    trueAns.add(i + j + VALUE_DELTA);
                }
                TestUtils.compareArraysWithEqual(trueAns, temp, "GetRange efficiency test");
            }


            long start = System.currentTimeMillis();
            for(var i : rangeStarts){
                tree.getRange(i, i+RANGE_TO_PROBE-1);
            }
            long finish = System.currentTimeMillis();

            //fail if it took too long
            if(MAX_TIME_FOR_RANGE_QUERIES < finish-start){
                fail("Ran too long, likely failing inefficient implementation. " +
                        "Max: "+ MAX_TIME_FOR_RANGE_QUERIES + ", actual: " + (finish-start));
            }
        }

        /**
         * Generates a range of random numbers distributed across the possible values -- filters out bad ones out of range
         * @return list of valid ints to generate ranges from
         */
        private List<Integer> getRangeNums(){
            //starting numbers
            List<Integer> orig = new ArrayList<>(Arrays.asList(-1, 10000000, NUM_EL_TO_INSERT-1, NUM_EL_TO_INSERT-265,
                    NUM_EL_TO_INSERT-1024, NUM_EL_TO_INSERT-2048, NUM_EL_TO_INSERT-1000000, NUM_EL_TO_INSERT-65535,
                    -610536, -179237549, -175257713, -155437404, 264888111, 225332182, -160344848, -80025619,
                    -760976, -810283, -150704692, 2295, 130, 277, 492, 68325 //random number generator
            ));//24

            //mix and match to make more numbers
            for(int i=0, sizeThing = orig.size(); i < sizeThing; i++){
                orig.add(orig.get(i)*-1);
            }//48
            for(int i=0, sizeThing = orig.size(); i < sizeThing; i++){
                orig.add(orig.get(i) / 2);
            }//96
            for(int i=0, sizeThing = orig.size(); i < sizeThing; i++){
                orig.add(orig.get(i) / 3);
            }//192
            for(int i=0, sizeThing = orig.size(); i < sizeThing; i++){
                orig.add(orig.get(i)*2 / 3);
            }//384
            for(int i=0, sizeThing = orig.size(); i < sizeThing; i++){
                orig.add(orig.get(i)*6 / 7);
            }//768
            for(int i=0, sizeThing = orig.size(); i < sizeThing; i++){
                orig.add((int) (orig.get(i)*12L / 13L));
            }//1536
            for(int i=0, sizeThing = orig.size(); i < sizeThing; i++){
                orig.add((int) (orig.get(i)*13L / 29L));
            }//3072
            for(int i=0, sizeThing = orig.size(); i < sizeThing; i++){
                orig.add((int) (orig.get(i)*28L / 29L));
            }//6144
            for(int i=0, sizeThing = orig.size(); i < sizeThing; i++){
                orig.add((int) (orig.get(i)*17L / 29L));
            }//12288
            for(int i=0, sizeThing = orig.size(); i < sizeThing; i++){
                orig.add((int) (orig.get(i)*23L / 29L));
            }//24576
            for(int i=0, sizeThing = orig.size(); i < sizeThing; i++){
                orig.add((int) (orig.get(i)*23L / 53L));
            }//49152
            for(int i=0, sizeThing = orig.size(); i < sizeThing; i++){
                orig.add((int) (orig.get(i)*36L / 53L));
            }//98304
            for(int i=0, sizeThing = orig.size(); i < sizeThing; i++){
                orig.add((int) (orig.get(i)*7L / 53L));
            }//196608
            for(int i=0, sizeThing = orig.size(); i < sizeThing; i++){
                orig.add((int) (orig.get(i)*13L / 53L));
            }//393216

            //one-sided range
            orig.add(NUM_EL_TO_INSERT*-1);

            //just in case and because I'm too lazy to check when I change NUM_EL_TO_INSERT
            return orig.stream()
                       .filter(i -> i >= -1*NUM_EL_TO_INSERT && i < NUM_EL_TO_INSERT - RANGE_TO_PROBE)
                       .collect(Collectors.toList());
        }
    }


    //complex sorting tests designed to trick them if they use the minus sign or any other form of comparison
    //  besides compareTo (check after every add what the root() is, then after each removal of root)
    /**
     * Tests use of comparators with multiple fields to compare on
     */
    @Nested
    class ComplexObjectTests{
        /**
         * Tests that the tree produces a series of things in the order of orderRootRemoval
         * @param tree tree to test
         * @param orderRootRemoval order of root objects to compare to
         */
        void sequenceEqual(ScapeGoatTree<ComplexObject, Integer> tree, List<ComplexObject> orderRootRemoval){
            for(var i : orderRootRemoval){
                assertEquals(0, tree.root().key.compareTo(i));
                tree.remove(i);
            }
        }

        /**
         * Gets the key out of the node
         * @param list list of nodes
         * @return list of keys
         */
        List<ComplexObject> extractKeysFromNodes(List<Node<ComplexObject, Integer>> list){
            return list.stream()
                       .map(node -> node.key)
                       .collect(Collectors.toList());
        }

        /**
         * Basic use with complex object
         */
        @Test
        void basicUseTest(){
            ScapeGoatTree<ComplexObject, Integer> tree = new ScapeGoatTree<>();
            ComplexObject a0 = new ComplexObject("abcd", 0),
                          a10 = new ComplexObject("abcd", 1),
                          a01 = new ComplexObject("abcd", 0),
                          b0 = new ComplexObject("dcba", 0),
                          b1 = new ComplexObject("dcba", 1),
                          c = new ComplexObject();
            tree.add(b1, 0); tree.add(b0, 1);
            tree.add(c, 2);
            tree.add(a10, 3); tree.add(a01, 4); tree.add(a0, 5);
            assertEquals(5, tree.size());

            List<ComplexObject> orderInOrder = new ArrayList<>(Arrays.asList(a01, a10, b0, b1, c)),
                                orderPreOrder = new ArrayList<>(Arrays.asList(b1, a10, a01, b0, c)),
                                orderRootRemoval = new ArrayList<>(Arrays.asList(b1, c, a10, b0, a01));
            TestUtils.compareArraysWithEqual(orderInOrder, extractKeysFromNodes(tree.inorder(tree.root())), "Complex object test: in order");
            TestUtils.compareArraysWithEqual(orderPreOrder, extractKeysFromNodes(tree.preorder(tree.root())), "Complex object test: pre order");

            sequenceEqual(tree, orderRootRemoval);
        }
    }

    /**
     * Tests that the tree uses equivalence based on equals or compareTo function and not object equivalence.
     * Tests with blob comparisons and reverse int comparisons for get() and remove().
     * Note that it does not make sense to test getRange here because of the range
     *   assumption (inherent math property of numbers being ordered and all...)
     */
    @Nested
    class UsingComparableForSearching {
        final String FAIL_MSG = "If someone fails this test, they are not using compareTo correctly.";
        /**
         * Ensures that their comparisons are all done with compareTo.
         */
        @Test
        void basicEquivalenceTest(){
            Blob b = new Blob("hi0", "asdf", 0, 1),
                 c = new Blob("hi0", ";lkj", 0, 10000);
            assertEquals(b, c, "Something has gone very wrong in the junit, not student's fault");

            ScapeGoatTree<Blob, Integer> tree = new ScapeGoatTree<>();
            tree.add(b, 0);
            assertNotNull(tree.get(c), FAIL_MSG);
            assertEquals(1, tree.size(), FAIL_MSG);
            tree.remove(c);
            assertEquals(0, tree.size(), FAIL_MSG);
        }

        /**
         * Gets the key out of the node
         * @param list list of nodes
         * @return list of keys
         */
        List<NewInt> extractKeysFromNodes(List<Node<NewInt, Integer>> list){
            return list.stream()
                    .map(node -> node.key)
                    .collect(Collectors.toList());
        }

        /**
         * Tests basic use with a reverse comparator
         */
        @Test
        void reverseOrder(){
            ScapeGoatTree<NewInt, Integer> tree = new ScapeGoatTree<>();
            //insert
            final int MAX = 10000;//10k
            for(int i=0; i<MAX; i++){
                tree.add(new NewInt(i), i);
            }
            //retrieve
            List<NewInt> list = extractKeysFromNodes(tree.inorder(tree.root()));
            if(list.size() == 0){
                fail("Failed to add elements to tree");
            }
            //compare
            for(int i=MAX-1, j=0; i>=0; i--, j++){
                assertEquals((new NewInt(i)).get(), list.get(j).get(), FAIL_MSG);
                tree.remove(new NewInt(i));
                assertEquals(i, tree.size());//my abuse of i here is so funky to read through
            }
        }
    }


    /**
     * Because I want code coverage to be 100%. Yes, it's unnecessary. Yes, I'm doing it anyways.
     * Better to have someone this meticulous than not thorough enough.
     */
    @Test
    void drawVisualizeDummyTestForCodeCoverage(){
        ScapeGoatTree<Integer, Integer> tree = new ScapeGoatTree<>();
        tree.printTreeInorder();
        tree.printTreePreorder();
        tree.toString();

        JPanel panel = new JPanel();
        tree.draw(panel.getGraphics());
        tree.visualize(panel.getGraphics());
    }
}
