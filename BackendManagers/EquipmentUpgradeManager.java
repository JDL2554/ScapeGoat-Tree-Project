package BackendManagers;

import BackendManagers.Interfaces.EquipmentUpgradeManagerInterface;
import CommonUtils.ScapeGoatTree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides input into the construction and display of equipment upgrade chains.  Will likely
 *   be a standalone application used when designing displays and content (i.e. integrated
 *   into the content creator toolbox).
 *
 * <bold>251 students: you may use any of the data structures you have previously created, but may not use
 *   any Java util library except List/ArrayList and Stack.</bold>
 */
public class EquipmentUpgradeManager implements EquipmentUpgradeManagerInterface {
    /**
     * Gets a valid topological ordering per the specifications.
     *
     * @param filename file to read input from
     * @return a valid topological ordering, or an empty list to indicate there is not one (not <code>null</code>).
     */
    @Override
    public List<Integer> getTopoOrdering(String filename) {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(filename));
            List<Integer> topographicalOrder = new ArrayList<>();
            String[] firstLine = bf.readLine().split(" ");
            int edges = Integer.parseInt(firstLine[0]); // Number of edges
            int vertexes = Integer.parseInt(firstLine[1]); // Number of vertices

            ScapeGoatTree<Integer, List<Integer>> vertexTree = new ScapeGoatTree<>();

            ScapeGoatTree<Integer, Integer> degree = new ScapeGoatTree<>();

            for (int i = 0; i < vertexes; i++) {
                degree.add(i, 0);
                vertexTree.add(i, new ArrayList<>());
            }

            for (int i = 0; i < edges; i++) {
                String[] edgeLine = bf.readLine().split(" ");
                int A = Integer.parseInt(edgeLine[0]); // From vertex A
                int B = Integer.parseInt(edgeLine[1]); // To vertex B

                vertexTree.get(A).add(B);

                int inDegree = degree.get(B);
                degree.remove(B);
                degree.add(B, inDegree + 1);
            }

            bf.close();

            ArrayList<Integer> noDegreeNodes = new ArrayList<>();

            for (int i = 0; i < vertexes; i++) {
                if (degree.get(i) == 0) {
                    noDegreeNodes.add(i);
                }
            }

            for (int i = 0; i < noDegreeNodes.size(); i++) {
                int key = noDegreeNodes.get(i);

                topographicalOrder.add(key);

                List<Integer> adjacentNodes = vertexTree.get(key);

                if (adjacentNodes != null) {
                    for (int adjacentNode : adjacentNodes) {
                        int adjacentDegree = degree.get(adjacentNode) - 1;
                        degree.remove(adjacentNode);
                        degree.add(adjacentNode, adjacentDegree);

                        if (adjacentDegree == 0) {
                            noDegreeNodes.add(adjacentNode);
                        }
                    }
                }
            }

            if (topographicalOrder.size() != vertexes) {
                return new ArrayList<>();
            }
            else {
                return topographicalOrder;
            }

        } catch (IOException e) {
            //This should never happen... uh oh o.o
            System.err.println("ATTENTION TAs: Couldn't find test file: \"" + filename + "\":: " + e.getMessage());
            System.exit(1);
        }
        return new ArrayList<>();
    }
}
