package BackendManagerTest;

import BackendManagers.EquipmentUpgradeManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Tests {@link EquipmentUpgradeManager}
 * @author Ethan Dickey
 */
public class EquipmentUpgradeManagerTest {
    final static String prefix = "./test/BackendManagerTest/equipmentUpgradeFiles/";
    final static File[] folderPaths = {new File(prefix + "sample"),
            new File(prefix + "manual"),
            new File(prefix + "generated")
    };
    final static String inputSuffix = "in", ansSuffix = "ans";
    // doesn't make sense for this manager test

    /**
     * All done in one instance of the manager
     */
    EquipmentUpgradeManager manager = new EquipmentUpgradeManager();


    /**
     * Provides a list of test files and their names for the parameterized test below.
     * @return List of valid test input files and their names
     */
    static Stream<Arguments> testFileProvider(){
        ArrayList<Arguments> args = new ArrayList<>();
        //for all folders provided
        for(final File path : folderPaths){
            //for each file in each folder
            for(final File entry : Objects.requireNonNull(path.listFiles())){
                String inputFile = entry.getPath();
                //if not an input file, skip
                if(! (inputFile.substring(inputFile.length() - inputSuffix.length()).equalsIgnoreCase(inputSuffix))){
                    continue;
                }
                args.add(arguments(Named.of(entry.getName(), entry)));
            }
        }

        return args.stream();
    }


    /**
     * Runs all input files
     */
    @DisplayName("File-based tests for Story 5")
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("testFileProvider")
    void runFiles(File file) {
        String inputFile = file.getPath();

        //guaranteed to have a valid input file
        String ansFile = inputFile.substring(0, inputFile.length() - inputSuffix.length()) + ansSuffix;

        //System.out.println("Starting test " + inputFile + ", getting student response, time=0");
        long startTime = System.currentTimeMillis();
        //run test
        List<Integer> ans = null;
        try {
            ans = manager.getTopoOrdering(inputFile);
        } catch(StackOverflowError e){
            fail("Student likely did not exit early in DFS when they found a cycle: StackOverflowError");
        } catch(Exception e){
            e.printStackTrace();
            fail("Error calling manager.getCleaningTimes(\"" + file.getName() + "\": " + e.getMessage());
        }
        //System.out.println("Student response ended at " + (System.currentTimeMillis() - startTime) + "ms");

        //calculate if answer is correct
        //read in input file edges
        try {
            BufferedReader bf = new BufferedReader(new FileReader(inputFile));

            //System.out.println("Reading edges at " + (System.currentTimeMillis() - startTime) + "ms");
            int V = Integer.parseInt(bf.readLine().split(" ")[1]);
            List<List<Integer>> edges = new ArrayList<>(V);
            for(int i=0; i<V; i++){
                edges.add(new ArrayList<>());
            }
            bf.lines().forEach((rawLine) -> {
                String[] line = rawLine.split(" ");
                edges.get(Integer.parseInt(line[0])).add(Integer.parseInt(line[1]));
            });
            //System.out.println("Finished edges at " + (System.currentTimeMillis() - startTime) + "ms, verifying answer");
            bf.close();

            //verify
            if(ans == null){
                fail("Returned null answer");
            } else if(ans.size() == 0){
                bf = new BufferedReader(new FileReader(ansFile));
                if(!bf.readLine().equalsIgnoreCase("cycle")){
                    fail("Detected cycle when there was none.");
                }
                bf.close();
            } else {
                verifyAns(edges, ans);
            }
            //System.out.println("Finished verifying answer and the entire test at " + (System.currentTimeMillis() - startTime) + "ms\n");
        } catch (FileNotFoundException e) {
            fail("GRADER ERROR:: INPUT FILE NOT FOUND:: \"" + file.getName() + "\"");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * simple tool to verify a topo sort with the edges.  Runs in O(V+E), fail() if wrong.
     * @param edges adjacency list
     * @param ans topo-sorted thing to verify
     */
    private void verifyAns(List<List<Integer>> edges, List<Integer> ans) {
        boolean[] seen = new boolean[edges.size()];
        for(var node : ans){
            seen[node] = true;
            for(var fren : edges.get(node)){
                if(seen[fren]){
                    fail("Wrong order, found backedge: edge " + node + "->" + fren + ", " + fren + " came first");
                }
            }
        }
    }
}
