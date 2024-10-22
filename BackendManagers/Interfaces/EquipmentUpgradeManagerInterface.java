package BackendManagers.Interfaces;

import java.util.List;

/**
 * Interface for the equipment upgrade manager.  The implementing class should follow the specifications
 *   listed in the project description ("Story 4").
 *
 * <bold>251 students: you may use any of the data structures you have previously created, but may not use
 *   any Java.util library except List/ArrayList.</bold>
 */
public interface EquipmentUpgradeManagerInterface {
    /**
     * Gets a valid topological ordering per the specifications.
     * @param filename file to read input from
     * @return a valid topological ordering, or an empty list to indicate there is not one (not <code>null</code>).
     */
    List<Integer> getTopoOrdering(String filename);
}
