import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;

/**
 * StopwordHandler class manages stopwords by loading from an input file,
 * storing them in a LinkedHashSet to preserve order, and writing them to an output file.
 */
public class StopwordHandler {
    private LinkedHashSet<String> stopwords;

    /**
     * Constructor initializes the LinkedHashSet.
     */
    public StopwordHandler() {
        stopwords = new LinkedHashSet<>();
    }

    /**
     * Loads stopwords from a given file into the LinkedHashSet.
     *
     * @param filename The path to the input stopword file.
     * @throws IOException If an I/O error occurs.
     */
    public void loadStopwords(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Trim the line to remove leading/trailing whitespace and convert to lowercase
                String word = line.trim().toLowerCase();
                if (!word.isEmpty()) {
                    if (!stopwords.add(word)) {
                        System.out.println("Duplicate stopword detected and ignored: " + word);
                    }
                }
            }
        }
    }

    /**
     * Writes the stopwords from the LinkedHashSet to an output file.
     *
     * @param outputFilename The path to the output stopword file.
     * @throws IOException If an I/O error occurs.
     */
    public void writeStopwords(String outputFilename) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilename))) {
            for (String word : stopwords) {
                bw.write(word);
                bw.newLine();
            }
        }

        System.out.println("Stopwords have been written to " + outputFilename);
    }

    /**
     * Adds a single stopword to the LinkedHashSet.
     *
     * @param word The stopword to add.
     */
    public void addStopword(String word) {
        if (word != null && !word.trim().isEmpty()) {
            if (!stopwords.add(word.trim().toLowerCase())) {
                System.out.println("Stopword already exists: " + word.trim().toLowerCase());
            }
        }
    }

    /**
     * Removes a stopword from the LinkedHashSet.
     *
     * @param word The stopword to remove.
     * @return True if the word was removed, false otherwise.
     */
    public boolean removeStopword(String word) {
        return stopwords.remove(word.trim().toLowerCase());
    }

    /**
     * Checks if a word is a stopword.
     *
     * @param word The word to check.
     * @return True if the word is a stopword, false otherwise.
     */
    public boolean isStopword(String word) {
        return stopwords.contains(word.trim().toLowerCase());
    }

    /**
     * Returns the number of stopwords stored.
     *
     * @return The size of the LinkedHashSet.
     */
    public int getStopwordCount() {
        return stopwords.size();
    }

    /**
     * Main method to load and write stopwords using command-line flags.
     *
     * @param args Command-line arguments: --input=<inputFile> --output=<outputFile>
     */
    public static void main(String[] args) {
        String inputFile = null;
        String outputFile = null;

        // Parsing of command-line arguments manually
        for (String arg : args) {
            if (arg.startsWith("--input=") || arg.startsWith("-i=")) {
                inputFile = arg.substring(arg.indexOf('=') + 1);
            } else if (arg.startsWith("--output=") || arg.startsWith("-o=")) {
                outputFile = arg.substring(arg.indexOf('=') + 1);
            }
        }

        // Validate that both input and output files are provided
        if (inputFile == null || outputFile == null) {
            System.err.println("Usage: java StopwordHandler --input=<inputFile> --output=<outputFile>");
            System.err.println("Eg: java StopwordHandler --input=Stoplist.txt --output=OutputStoplist.txt");
            System.exit(1);
        }

        StopwordHandler handler = new StopwordHandler();

        try {
            // Load stopwords from the input file
            handler.loadStopwords(inputFile);
            System.out.println("Loaded " + handler.getStopwordCount() + " stopwords from " + inputFile);

            // Write the stopwords to the output file
            handler.writeStopwords(outputFile);
        } catch (IOException e) {
            System.err.println("An error occurred while processing stopwords: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }
}
