import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * PrintIndexHandler class handles printing detailed information about a specific word or document
 * from the inverted index to an output file.
 */
public class PrintIndexHandler {
    private InvertedIndex invertedIndex;

    /**
     * Constructor initializes the handler with a reference to the InvertedIndex.
     *
     * @param invertedIndex The InvertedIndex instance containing the inverted index data.
     */
    public PrintIndexHandler(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    /**
     * Prints detailed information about a specific word to the specified output file.
     *
     * @param word        The word to print information for.
     * @param outputFile  The file to write the information to.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    public void printWordInfo(String word, String outputFile) throws IOException {
        word = word.toLowerCase();
        StringBuilder outputBuilder = new StringBuilder();

        Map<String, List<Integer>> searchResult = invertedIndex.searchWord(word);

        if (searchResult.isEmpty()) {
            outputBuilder.append("The word '").append(word).append("' does not appear in any document.\n");
        } else {
            int documentCount = searchResult.size();
            outputBuilder.append("The word '").append(word).append("' appears in ").append(documentCount).append(" document(s):\n");

            for (Map.Entry<String, List<Integer>> entry : searchResult.entrySet()) {
                String document = entry.getKey();
                int frequency = entry.getValue().size();
                List<Integer> positions = entry.getValue();

                outputBuilder.append("  Document: ").append(document).append("\n");
                outputBuilder.append("    Frequency: ").append(frequency).append("\n");
                outputBuilder.append("    Positions: ").append(positions).append("\n");
            }
        }

        // Write the information to the output file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            bw.write(outputBuilder.toString());
        }

        System.out.println("Word information has been written to " + outputFile);
    }

    /**
     * Prints detailed information about a specific document to the specified output file.
     *
     * @param document    The document to print information for.
     * @param outputFile  The file to write the information to.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    public void printDocumentInfo(String document, String outputFile) throws IOException {
        document = document.toLowerCase();
        StringBuilder outputBuilder = new StringBuilder();

        Map<String, List<Integer>> searchResult = invertedIndex.searchDocument(document);

        if (searchResult.isEmpty()) {
            outputBuilder.append("The document '").append(document).append("' does not contain any indexed words.\n");
        } else {
            int wordCount = searchResult.size();
            outputBuilder.append("The document '").append(document).append("' contains ").append(wordCount).append(" word(s):\n");

            for (Map.Entry<String, List<Integer>> entry : searchResult.entrySet()) {
                String word = entry.getKey();
                int frequency = entry.getValue().size();
                List<Integer> positions = entry.getValue();

                outputBuilder.append("  Word: ").append(word).append("\n");
                outputBuilder.append("    Frequency: ").append(frequency).append("\n");
                outputBuilder.append("    Positions: ").append(positions).append("\n");
            }
        }

        // Write the information to the output file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            bw.write(outputBuilder.toString());
        }

        System.out.println("Document information has been written to " + outputFile);
    }
}
