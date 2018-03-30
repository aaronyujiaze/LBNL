
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Aaron Jia Ze Yu on 29/3/2018.
 */
public class Main {

    /**
     * @desciption
     * Parse .txt files with the format
     * ______________________________
     * | # filename size            | -The comments after '#' are ignored.
     * | tom.dat 1024               | -The name is parsed as strings.
     * | jerry.dat 16553            | -The size after the name separated by at least a white space character
     * | tweety.out 12345           |  is parsed as integers.
     * | elmerfudd.txt 987654321    | -The two types of .txt files that can be parsed are 'files' and 'nodes'
     * |____________________________|  which share the same two column format.
     *
     * @param dstbr, path, type
     * @throws IOException
     */

    public static void parseFile(Distributer dstbr, String path, String type) throws IOException {

        // Regex strings to specify string pattern to capture information required from text files
        final Pattern COMMENT = Pattern.compile("\\s*#.*"),
                DATA = Pattern.compile("\\s*(\\S+)\\s+" + "([0-9]+)\\s*");

        // Initialization
        File file = new File(path);
        Scanner sc = new Scanner(file);
        Matcher m;

        // Line by line scanning
        while (sc.hasNextLine()) {
            String line = sc.nextLine();

            // If '#' is seen, skip this line
            if ((m = COMMENT.matcher(line)).matches()) {
                continue;

            // If the correct data format is seen, parse it accordingly to the file type specified
            } else if ((m = DATA.matcher(line)).matches()) {

                if (type.equals("files")) {
                    dstbr.addFile(m.group(1), Integer.parseInt(m.group(2)));

                } else if (type.equals("nodes")) {
                    dstbr.addNode(m.group(1), Integer.parseInt(m.group(2)));

                } else {
                    throw new Error("Unrecognizable file type.");

                }

            // Otherwise, throw error for incorrect data format
            } else {
                throw new IOException("Invalid file format");

            }
        }
    }

    /**
     * @description
     * -Execute the program by first reading in the command line arguments which include the directory to
     *  input -f files.txt (required), -n nodes.txt (required) and -o output.txt files (optional) from command line
     *  interface.
     * -parseFile() is used to parse files.txt and nodes.txt into data structures of File and Node
     *  objects respectively in the Distributer class and distribute the objects.
     * -print the matchings returned by Distributer on either the console or into the output text file.
     * @param args
     * @throws IOException
     */

    public static void main(String[] args) throws IOException {

        // Boolean flag to check if output should be printed to a text file and keep track of the filename
        boolean printToOutputfile = false;
        String outputfileName = null;

        // Check if length of args array is greater than 0
        if (args.length > 0) {

            // Initialize a Distribute class object
            Distributer dstbr = new Distributer();

            // Read the command line arguments. If '-f', '-n' or '-o' flags are read, take the argument
            // immediately after them as filename.
            for (int i = 0; i < args.length; i++) {

                String val = args[i];

                if (val.equals("-f")) {
                    parseFile(dstbr, args[i + 1], "files");
                    i++;

                } else if (val.equals("-n")) {
                    parseFile(dstbr, args[i + 1], "nodes");
                    i++;

                } else if (val.equals("-o")) {
                    printToOutputfile = true;
                    outputfileName = args[i + 1];
                    i++;

                // If '-h' flag is read, throw an error that displays all the usage information
                } else if (val.equals("-h")) {
                    throw new Error(
                            "\n-f filename: Input file for files (required).  e.g. -f files.txt\n"
                                    + "-n filename: Input file for nodes (required).  e.g. -n nodes.txt\n"
                                    + "-o filename: Output file (optional). Results printed on console if not given."
                                    + "e.g. -o result.txt\n"
                                    + "-h : Help. Print usage information to standard error and stop."
                    );

                // Otherwise, throw an error for invalid arguments
                } else {
                    throw new IOException("Invalid arguments");

                }
            }

            // Initialize a hashmap for matchings returned by Distributer
            HashMap<String, String> matchings = dstbr.distribute();

            // If matchings output is specified to be printed to a textfile
            if (printToOutputfile) {
                PrintWriter out = new PrintWriter(new FileWriter(outputfileName));

                for (Map.Entry entry : matchings.entrySet()) {
                    out.println(entry.getKey() + " " + entry.getValue());
                }

                out.close();

            // If unspecified, print matchings to console
            } else {
                for (Map.Entry entry : matchings.entrySet()) {
                    System.out.println(entry.getKey() + " " + entry.getValue());
                }

            }

        // If no argument given, throw error message containing usage information
        } else{
                throw new Error("No command line arguments found. Please enter valid arguments:\n"
                        + "-f filename: Input file for files (required).  e.g. -f files.txt\n"
                        + "-n filename: Input file for nodes (required).  e.g. -n nodes.txt\n"
                        + "-o filename: Output file (optional). Results printed on console if not given."
                        + "e.g. -o result.txt\n"
                        + "-h : Help. Print usage information to standard error and stop.");
        }
    }
}
