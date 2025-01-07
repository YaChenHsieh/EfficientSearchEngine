import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * QueryHandler class handles the SEARCH functionality, allowing users to search for words or documents.
 */
public class QueryHandler {
    private InvertedIndex invertedIndex;

    /**
     * Constructor initializes the handler with a reference to the InvertedIndex.
     *
     * @param invertedIndex The InvertedIndex instance containing the inverted index data.
     */
    public QueryHandler(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    /**
     * Handles the search query and writes the results to the specified output file.
     *
     * @param queryType  The type of search: "word" or "doc".
     * @param queryTerm  The word or document to search for.
     * @param outputFile The file to write the search results to.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    public void handleSearch(String queryType, String queryTerm, String outputFile) throws IOException {
        StringBuilder outputBuilder = new StringBuilder();

        if (queryType.equals("word")) {
            Map<String, List<Integer>> searchResult = invertedIndex.searchWord(queryTerm);

            if (searchResult.isEmpty()) {
                outputBuilder.append("The word '").append(queryTerm).append("' does not appear in any document.\n");
            } else {
                int documentCount = searchResult.size();
                outputBuilder.append("The word '").append(queryTerm).append("' appears in ").append(documentCount).append(" document(s):\n");

                for (Map.Entry<String, List<Integer>> entry : searchResult.entrySet()) {
                    String document = entry.getKey();
                    int frequency = entry.getValue().size();
                    List<Integer> positions = entry.getValue();  // Get the positions

                    outputBuilder.append("  Document: ").append(document).append(" | Frequency: ").append(frequency).append("\n");
                    outputBuilder.append("    Positions: ").append(positions).append("\n");  // Include positions
                }
            }
        } else if (queryType.equals("doc")) {
            Map<String, List<Integer>> searchResult = invertedIndex.searchDocument(queryTerm);

            if (searchResult.isEmpty()) {
                outputBuilder.append("The document '").append(queryTerm).append("' does not contain any indexed words.\n");
            } else {
                int wordCount = searchResult.size();
                outputBuilder.append("The document '").append(queryTerm).append("' contains ").append(wordCount).append(" word(s):\n");

                for (Map.Entry<String, List<Integer>> entry : searchResult.entrySet()) {
                    String word = entry.getKey();
                    int frequency = entry.getValue().size();
                    List<Integer> positions = entry.getValue();  // Get the positions

                    outputBuilder.append("  Word: ").append(word).append(" | Frequency: ").append(frequency).append("\n");
                    outputBuilder.append("    Positions: ").append(positions).append("\n");  // Include positions
                }
            }
        } else {
            outputBuilder.append("Invalid SEARCH type. Use 'word' or 'doc'.\n");
        }

        // Write the search results to the output file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            bw.write(outputBuilder.toString());
        }

        System.out.println("Search results have been written to " + outputFile);
    }
}
