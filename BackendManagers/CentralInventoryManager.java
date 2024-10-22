package BackendManagers;

import BackendManagers.Interfaces.CentralInventoryManagerInterface;
import CommonUtils.ScapeGoatIntKey;
import Items.ItemHeaderInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages everything regarding the central server's inventory in our backend. Will be integrated with
 *   our other manager processes and threads. Future work likely required when we switch to a true database,
 *   so please keep code clean (good documentation is assumed for all work, but the highest cleanliness standards
 *   are held to those pieces of code we will have to rewrite in the future).
 *
 * <bold>251 students: you may not use any Java util library except
 * List/ArrayList.</bold>
 */
public class CentralInventoryManager implements CentralInventoryManagerInterface {
    /**
     * Gets the request responses per the specifications.
     *
     * @param filename file to read input from
     * @return the list of appropriate responses, in the order of the requests, as per the specifications
     */
    @Override
    public List<InventoryResponse> getRequestResponses(String filename) {
        List<InventoryResponse> responses = new ArrayList<>();
        try {
            BufferedReader bf = new BufferedReader(new FileReader(filename));
            String[] firstLine = bf.readLine().split(" ");
            int nitems = Integer.parseInt(firstLine[0]);
            int nreq = Integer.parseInt(firstLine[1]);

            ScapeGoatIntKey<ItemHeaderInfo> inventoryTree = new ScapeGoatIntKey<>();

            for (int i = 0; i < nitems; i++) {
                String[] itemLine = bf.readLine().split(",");
                String findingName = itemLine[2].trim().replaceAll("\"", "");
                ItemHeaderInfo currentItem = new ItemHeaderInfo(Integer.parseInt(itemLine[0].replaceAll(" ", "")),
                        Long.parseLong(itemLine[1].replaceAll(" ", "")), findingName,
                        Integer.parseInt(itemLine[3].replaceAll(" ", "")));
                inventoryTree.add(Integer.parseInt(itemLine[0].replaceAll(" ", "")), currentItem);
            }

            for (int j = 0; j < nreq; j++) {
                String rLine = bf.readLine();
                String[] lineBreakdown = rLine.split(" ");
                if (lineBreakdown[0].replaceAll(",", "").equals("A")) {
                    String[] requestLine = rLine.split(",");
                    ItemHeaderInfo currentItem = inventoryTree.get(Integer.parseInt(requestLine[1].replaceAll(" ", "")));
                    if (currentItem != null) {
                        currentItem.count += Integer.parseInt(requestLine[2].replaceAll(" ", ""));
                        responses.add(new InventoryResponse(true, new ItemCount(currentItem.itemID, currentItem.count),
                                null));
                    }
                    else {
                        ItemHeaderInfo newItem = new ItemHeaderInfo(Integer.parseInt(requestLine[1].trim()),
                                Integer.parseInt(requestLine[2].trim()),
                                requestLine[3].replaceAll("\"", "").trim(), Integer.parseInt(requestLine[4].trim()));
                        inventoryTree.add(Integer.parseInt(requestLine[1].trim()), newItem);
                        responses.add(new InventoryResponse(true, new ItemCount(newItem.itemID,
                                newItem.count),
                                null));
                    }
                } else {
                    String[] requestLine = rLine.split(" ");
                    if (requestLine[0].equals("R")) {
                        ItemHeaderInfo currentItem = inventoryTree.get(Integer.parseInt(requestLine[1]));
                        if (currentItem == null) {
                            responses.add(new InventoryResponse(false, null, null));
                        } else if (currentItem.count < Long.parseLong(requestLine[2])){
                            responses.add(new InventoryResponse(false, new ItemCount(currentItem.itemID,
                                    currentItem.count),
                                    null));
                        } else {
                            currentItem.count -= Integer.parseInt(requestLine[2]);
                            responses.add(new InventoryResponse(true, new ItemCount(currentItem.itemID, currentItem.count),
                                    null));
                        }
                    } else if (requestLine[0].equals("Q")) {
                        if (requestLine.length == 3) {
                            List<ItemHeaderInfo> itemsRange = inventoryTree.getRange(Integer.parseInt(requestLine[1]),
                                    Integer.parseInt(requestLine[2]));
                            if (itemsRange.size() == 0) {
                                responses.add(new InventoryResponse(false, null, itemsRange));
                            } else {
                                responses.add(new InventoryResponse(true, null, itemsRange));
                            }
                        } else {
                            ItemHeaderInfo currentItem = inventoryTree.get(Integer.parseInt(requestLine[1]));
                            List<ItemHeaderInfo> itemsRange = inventoryTree.getRange(Integer.parseInt(requestLine[1]),
                                    Integer.parseInt(requestLine[1]));
                            if (currentItem == null) {
                                responses.add(new InventoryResponse(false, null, itemsRange));
                            } else {
                                responses.add(new InventoryResponse(true, null, itemsRange));
                            }
                        }
                    }
                }
            }

            return responses;

        } catch (IOException e) {
            //This should never happen... uh oh o.o
            System.err.println("ATTENTION TAs: Couldn't find test file: \"" + filename + "\":: " + e.getMessage());
            System.exit(1);
        }
        return responses;
    }
}
