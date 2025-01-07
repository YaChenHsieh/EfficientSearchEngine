import java.io.Serializable;
import java.util.*;

public class Document implements Serializable {
    private String documentId;
    private Map<String, List<Integer>> wordPositions; // To store positions of each word

    public Document(String documentId) {
        this.documentId = documentId;
        this.wordPositions = new HashMap<>();
    }

    public String getDocumentId() {
        return documentId;
    }

    public void addPosition(String word, int position) {
        wordPositions.computeIfAbsent(word, k -> new ArrayList<>()).add(position);
    }

    public List<Integer> getPositions(String word) {
        // Debugging output to check if the requested word exists
        System.out.println("Retrieving positions for word '" + word + "' in document " + documentId);
        System.out.println("Available words in document: " + wordPositions.keySet());
        return wordPositions.getOrDefault(word, new ArrayList<>());
    }

    // Custom toString() method for debugging purposes
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Document ID: ").append(documentId).append("\n");
        sb.append("Word Positions:\n");
        for (Map.Entry<String, List<Integer>> entry : wordPositions.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
