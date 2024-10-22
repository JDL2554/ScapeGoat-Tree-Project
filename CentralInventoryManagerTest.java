package BackendManagerTest;

import BackendManagers.CentralInventoryManager;
import BackendManagers.Interfaces.CentralInventoryManagerInterface.*;
import Drones.test.TestUtils;
import Items.ItemHeaderInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.api.Timeout;
/**
 * Tests {@link CentralInventoryManager}.
 * @author Ethan Dickey
 */
//@Timeout(value = 2400000, unit = TimeUnit.MILLISECONDS)
public class CentralInventoryManagerTest {
    final static String prefix = "./test/BackendManagerTest/centralInventoryFiles/";
    final static File[] folderPaths = {new File(prefix + "sample"),
            new File(prefix + "manual"),
            new File(prefix + "generated")
    };
    final static String inputSuffix = "in", ansSuffix = "out";

    /**
     * All done in one instance of the manager
     */
    CentralInventoryManager manager = new CentralInventoryManager();


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
        List<InventoryResponse> ans = null;
        try {
            ans = manager.getRequestResponses(inputFile);
        } catch(Exception e){
            e.printStackTrace();
            fail("Error calling manager.getRequestResponses(\"" + file.getName() + "\": " + e.getMessage());
        }
        //System.out.println("Student response ended at " + (System.currentTimeMillis() - startTime) + "ms");

        //compare to answer
        //read in answer file
        BufferedReader bf = null;
        try {
            bf = new BufferedReader(new FileReader(ansFile));
        } catch (FileNotFoundException e) {
            fail("GRADER ERROR:: ANSWER FILE NOT FOUND:: \"" + file.getName() + "\"");
        }

        //System.out.println("Reading answer file at " + (System.currentTimeMillis() - startTime) + "ms");
        List<InventoryResponse> trueAns = new ArrayList<>();
        bf.lines().forEach((rawLine) -> {
            boolean success = rawLine.charAt(0) == '1';
            if(rawLine.length() > 1 && rawLine.charAt(2) == '['){//query response
                //difference between a request gone wrong and a query gone wrong?  empty list
                trueAns.add(new InventoryResponse(success, null, getQueryResponses(success, rawLine)));
            } else {
                String[] line = rawLine.split(" ");
                trueAns.add(new InventoryResponse(success,
                                                  line.length > 1 ? new ItemCount(Integer.parseInt(line[1]),
                                                                          Long.parseLong(line[2])) : null,
                                                  null));
            }
        });
        //System.out.println("Finished reading answer file at " + (System.currentTimeMillis() - startTime) + "ms");

        //compare
        TestUtils.compareArraysWithEqual(trueAns, ans, "Test case: " + inputFile);
        //System.out.println("Finished comparing arrays and the entire test at " + (System.currentTimeMillis() - startTime) + "ms\n");
    }

    /**
     * Retrieves the list of items from a query correct answer line, in the right format
     * @param success first bit of the line, because laziness
     * @param rawLine line to parse
     * @return list of parsed items
     */
    private List<ItemHeaderInfo> getQueryResponses(boolean success, String rawLine){
        List<ItemHeaderInfo> list = new ArrayList<>();

        if(success){
            String[] line = rawLine.split(" \\[");//java regex is weird
            for(int i=1; i<line.length; i++){//skip the first one, it's just the success bit
                String[] itemParts = line[i].split(", ");
                //written out explicitly for future readers
                int id = Integer.parseInt(itemParts[0]),
                    picID = Integer.parseInt(itemParts[3].substring(0, itemParts[3].length()-1));//cut off trailing bracket
                long count = Long.parseLong(itemParts[1]);
                String name = itemParts[2].substring(1, itemParts[2].length()-1);//strip the ""
                list.add(new ItemHeaderInfo(id, count, name, picID));
            }
        }

        return list;
    }
}
