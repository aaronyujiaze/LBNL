import java.util.*;

/**
 * Created by Aaron Jia Ze Yu on 29/3/2018.
 * Distributer class that contains a resizable arraylist of File objects and a PriorityQueue of Node objects.
 * It also keeps track of the number of nodes and the total amount of file size to calculate the average file size
 * for each Node as a greedy heuristic to distribute the files across the nodes as evenly as possible.
 */
public class Distributer {

    public ArrayList<File> filesize;
    public PriorityQueue<Node> nodespace;
    public int totalfileSize;
    public int numNodes;

    // Constructor
    public Distributer() {
        filesize = new ArrayList<File>();
        nodespace = new PriorityQueue<Node>();
        totalfileSize = 0;
        numNodes = 0;
    }

    // File object that keeps track of the name and size of each file and imposes a natural ordering based on its size
    public class File implements Comparable<File>{

        String name;
        int size;

        // Constructor
        public File(String name, int size) {
            this.name = name;
            this.size = size;
        }

        // Imposing natural ordering
        public int compareTo(File other) {
            return size - other.size;
        }
    }

    // Node object that keeps track of the name, space and the size of files contained in each node
    // and imposes a natural ordering based on its space (for the first space) and the size of
    // files contained (for the subsequent passes)
    public class Node implements Comparable<Node>{

        String name;
        int space;
        int contains;
        boolean sortByContent; // boolean flag to indicate whether Nodes should be sorted by the size of files contained

        // Constructor
        public Node(String name, int space) {
            this.name = name;
            this.space = space;
            this.contains = 0;
            this.sortByContent = false;
        }

        // Imposing natural ordering
        public int compareTo(Node other) {

            // After first pass (no node is empty anymore), natural ordering is based on the size of files contained
            if (sortByContent) {
                return contains - other.contains;
            }

            // For first pass, sort Node based on its space in descending order
            return - (space - other.space);

        }

        // Insert File object into this Node. Return TRUE if successful, FALSE otherwise.
        public boolean insert(File file) {

            // Check if File fits into this Node
            if (this.contains + file.size > this.space) {
                return false;
            }

            // Increment the filesize contained in Node and indicate that this Node is not empty anymore
            this.contains += file.size;
            sortByContent = true;
            return true;

        }
    }

    // Add files parsed from Main as File obejcts into the arraylist filesize
    public void addFile(String filename, Integer size) {
        filesize.add(new File(filename, size));
        totalfileSize += size;
    }

    // Add nodes parsed from Main as Node obejcts into the priorityqueue nodespace
    public void addNode(String nodename, Integer space) {
        nodespace.add(new Node(nodename, space));
        numNodes++;
    }

    // Helper function to calculate the distance of File size from the mean size for greedy heuristic
    public double distToMean(File a) {
        return Math.abs(a.size - totalfileSize / numNodes);
    }

    /**  Algorithm to distribute files to nodes:
     *  1. Sort the files in ascending order
     *  2. Insert the largest and smallest file into the same node at a time. For non-empty nodes, the node containing
     *      the smallest files is selected, else the node with the largest space is selected. For the cases where the
     *      insertion of one from the pair of largest and smallest files is unsuccessful, remove it and choose from the
     *      next pair of largest and smallest files with the greedy heuristic that it has size further from the mean
     *      to balance out the distribution.
     *  3. Repeat until all files have been inserted to nodes.
     *  4. Return the matchings as entries in a hashmap and send it to Main to be printed.
     * @return a hashmap containing the matchings.
     */
    public HashMap<String, String> distribute() {

        // Initialization
        HashMap<String, String> ret = new HashMap<String, String>(); // matchings to be returned
        PriorityQueue<Node> afterFirst = new PriorityQueue<Node>(); // priorityqueue for non-empty Nodes after first pass
        Node currNode; // current node being processed

        // Sort the arraylist filesize based on the natural ordering defined in File class
        filesize.sort(new Comparator<File>() {
            public int compare(File o1, File o2) {
                return o1.compareTo(o2);
            }
        });

        // Process the filesize arraylist until it is empty
        while (!filesize.isEmpty()) {

            // If the Node is empty, get the Node with the largest space.
            // Otherwise, get the Node that contains the smallest file size.
            if (!nodespace.isEmpty()) {
                currNode = nodespace.poll();
            } else {
                currNode = afterFirst.poll();
            }

            // If only 1 file is left, insert it into the current Node unless it exceeds the space
            if (filesize.size() == 1) {
                File firstFile = filesize.remove(0);
                boolean successFirst = currNode.insert(firstFile);
                if (successFirst) {
                    afterFirst.add(currNode);
                    ret.put(firstFile.name, currNode.name);
                } else {
                    ret.put(firstFile.name, "NULL");
                }
                return ret;
            }

            // Otherwise, process 2 files at a time
            // (insert the largest and smallest file into the same node at the same time)
            File firstFile = filesize.remove(0);
            File lastFile = filesize.remove(filesize.size() - 1);
            boolean successFirst = currNode.insert(firstFile);
            boolean successLast = currNode.insert(lastFile);

            // If both are successfully added, record the matchings
            if (successFirst && successLast) {
                ret.put(firstFile.name, currNode.name);
                ret.put(lastFile.name, currNode.name);

            // If the insertion of last file is unsuccessful, remove it
            } else if (successFirst && !successLast) {
                ret.put(firstFile.name, currNode.name);
                ret.put(lastFile.name, "NULL");

                // choose the file that is further from the mean among
                // the next pair of files at the ends of the arraylist
                if (distToMean(filesize.get(0)) > distToMean(filesize.get(filesize.size() - 1))) {
                    firstFile = filesize.remove(0);

                    // repeat if insertion to Node is unsuccessful again
                    while (!successFirst) {
                        ret.put(firstFile.name, "NULL");
                        successFirst = currNode.insert(firstFile);
                    }
                    ret.put(firstFile.name, currNode.name);

                } else {

                    // repeat if insertion to Node is unsuccessful again
                    while (!successLast) {
                        ret.put(lastFile.name, "NULL");
                        lastFile = filesize.remove(filesize.size() - 1);
                        successLast = currNode.insert(lastFile);
                    }
                    ret.put(lastFile.name, currNode.name);

                }

                // If the insertion of first file is unsuccessful, remove it
            } else if (!successFirst && successLast) {
                ret.put(lastFile.name, currNode.name);
                ret.put(firstFile.name, "NULL");

                // choose the file that is further from the mean among
                // the next pair of files at the ends of the arraylist
                if (distToMean(filesize.get(0)) > distToMean(filesize.get(filesize.size() - 1))) {

                    // repeat if insertion to Node is unsuccessful again
                    while (!successFirst) {
                        ret.put(firstFile.name, "NULL");
                        firstFile = filesize.remove(0);
                        successFirst = currNode.insert(firstFile);
                    }
                    ret.put(firstFile.name, currNode.name);
                } else {
                    lastFile = filesize.remove(filesize.size() - 1);

                    // repeat if insertion to Node is unsuccessful again
                    while (!successLast) {
                        ret.put(lastFile.name, "NULL");
                        successLast = currNode.insert(lastFile);
                    }
                    ret.put(lastFile.name, currNode.name);
                }


            // If both insertion are unsuccessful, remove them and move on to the next pair.
            } else {
                while (!successFirst) {
                    ret.put(firstFile.name, "NULL");
                    firstFile = filesize.remove(0);
                    successFirst = currNode.insert(firstFile);
                }
                ret.put(firstFile.name, currNode.name);
                while (!successLast) {
                    ret.put(lastFile.name, "NULL");
                    lastFile = filesize.remove(filesize.size() - 1);
                    successLast = currNode.insert(lastFile);
                }
                ret.put(lastFile.name, currNode.name);
            }

            afterFirst.add(currNode);

        }
        return ret;
    }
}
