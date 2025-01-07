import java.io.*;
import java.util.*;

/**
 * InvertedIndex class builds and manages the inverted index for a corpus of documents.
 * It supports optional stemming based on a command-line flag.
 */
public class InvertedIndex implements Serializable {
    private static final long serialVersionUID = 1L; // Serialization version for compatibility
    private HashMap<String, HashMap<String, List<Integer>>> invertedIndex;
    private transient StopwordHandler stopwordHandler; //"transient" because it's not serializable
    private boolean enableStemming; // Flag for stemming

    public InvertedIndex(String stopwordFile, boolean enableStemming) throws IOException {
        this.invertedIndex = new HashMap<>();
        this.stopwordHandler = new StopwordHandler();
        this.enableStemming = enableStemming;
        this.stopwordHandler.loadStopwords(stopwordFile);
    }

    // **: Save to .ser file**
    public void saveToSerFile(String serFilePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serFilePath))) {
            oos.writeObject(this); 
            System.out.println("Inverted index has been saved to " + serFilePath);
        } catch (IOException e) {
            System.err.println("Failed to save inverted index: " + e.getMessage());
        }
    }

    // **: Load from .ser file**
    public static InvertedIndex loadFromSerFile(String serFilePath, String stopwordFile) throws IOException, ClassNotFoundException {
        File file = new File(serFilePath);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                InvertedIndex index = (InvertedIndex) ois.readObject();
                index.stopwordHandler = new StopwordHandler();
                index.stopwordHandler.loadStopwords(stopwordFile);
                System.out.println("Inverted index has been loaded from " + serFilePath);
                return index;
            }
        } else {
            System.out.println(serFilePath + " not found. Creating a new index.");
            return null;
        }
    }

    public void buildIndex(String corpusDirPath) throws IOException {
        File corpusDir = new File(corpusDirPath);
        if (!corpusDir.exists() || !corpusDir.isDirectory()) {
            throw new IllegalArgumentException("The provided corpus directory path is invalid: " + corpusDirPath);
        }

        File[] files = corpusDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt") || name.toLowerCase().endsWith(".html"));
        if (files == null || files.length == 0) {
            System.out.println("No text or HTML files found in the directory: " + corpusDirPath);
            return;
        }

        for (File file : files) {
            if (file.getName().toLowerCase().endsWith(".txt")) {
                indexDocument(file);
            } else if (file.getName().toLowerCase().endsWith(".html")) {
                indexHtmlDocument(file);
            }
        }
    }

    private void indexDocument(File file) throws IOException {
        String documentName = file.getName().toLowerCase();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int wordPosition = 0;

            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\\W+");

                for (String token : tokens) {
                    if (token.isEmpty()) continue;

                    wordPosition++;
                    String word = token.toLowerCase();

                    if (stopwordHandler.isStopword(word)) continue;

                    if (enableStemming) {
                        PorterStemmer stemmer = new PorterStemmer(word);
                        word = stemmer.stem();
                    }

                    invertedIndex
                        .computeIfAbsent(word, k -> new HashMap<>())
                        .computeIfAbsent(documentName, k -> new ArrayList<>())
                        .add(wordPosition);
                }
            }
        }
        System.out.println("Indexed document: " + documentName);
    }

    private void indexHtmlDocument(File file) throws IOException {
        String documentName = file.getName().toLowerCase();
        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append(" ");
            }
        }

        String text = content.toString().replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        String[] tokens = text.split("\\W+");
        int wordPosition = 0;

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            wordPosition++;
            String word = token.toLowerCase();

            if (stopwordHandler.isStopword(word)) continue;

            if (enableStemming) {
                PorterStemmer stemmer = new PorterStemmer(word);
                word = stemmer.stem();
            }

            invertedIndex
                .computeIfAbsent(word, k -> new HashMap<>())
                .computeIfAbsent(documentName, k -> new ArrayList<>())
                .add(wordPosition);
        }
        System.out.println("Indexed HTML document: " + documentName);
    }

    public Map<String, List<Integer>> searchWord(String word) {
        word = word.toLowerCase();
        if (enableStemming) {
            PorterStemmer stemmer = new PorterStemmer(word);
            word = stemmer.stem();
        }
        // force to use HashMap
        return (HashMap<String, List<Integer>>) invertedIndex.getOrDefault(word, new HashMap<>());
    }

    public Map<String, List<Integer>> searchDocument(String document) {
        document = document.toLowerCase();
        Map<String, List<Integer>> result = new HashMap<>();

        for (Map.Entry<String, HashMap<String, List<Integer>>> wordEntry : invertedIndex.entrySet()) {
            String word = wordEntry.getKey();
            HashMap<String, List<Integer>> docMap = wordEntry.getValue();

            if (docMap.containsKey(document)) {
                result.put(word, docMap.get(document));
            }
        }
        return result;
    }

    public void saveIndex(String outputFilePath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {
            for (Map.Entry<String, HashMap<String, List<Integer>>> entry : invertedIndex.entrySet()) {
                String word = entry.getKey();
                bw.write("Word: " + word);
                bw.newLine();
                for (Map.Entry<String, List<Integer>> docEntry : entry.getValue().entrySet()) {
                    bw.write("  Document: " + docEntry.getKey() + " -> " + docEntry.getValue());
                    bw.newLine();
                }
            }
        }
        System.out.println("Inverted index has been saved to " + outputFilePath);
    }

    public Set<String> search(String query) {
        String[] terms = query.split("\\s+"); // Split query into terms
        Set<String> resultSet = null;

        for (String term : terms) {
            String processedTerm = term.toLowerCase();
            if (enableStemming) {
                PorterStemmer stemmer = new PorterStemmer(processedTerm);
                processedTerm = stemmer.stem();
            }

            HashMap<String, List<Integer>> postings = invertedIndex.get(processedTerm);
            if (postings == null) {
                return new HashSet<>(); // If any term has no postings, result is empty
            }

            Set<String> documents = postings.keySet();
            if (resultSet == null) {
                resultSet = new HashSet<>(documents);
            } else {
                resultSet.retainAll(documents); // Intersect with previous results
            }
        }
        return resultSet == null ? new HashSet<>() : resultSet;
    }

    // Save search results to a file
    public void saveSearchResults(Set<String> results, String outputFilePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            for (String document : results) {
                writer.write(document);
                writer.newLine();
            }
        }
        System.out.println("Search results have been saved to " + outputFilePath);
    }


    // Method to search for snippets around a term
    public Map<String, Map<Integer, List<String>>> searchSnippets(String term, int snipRange, String corpusDirPath) throws IOException {
        String processedTerm = term.toLowerCase();
        if (enableStemming) {
            PorterStemmer stemmer = new PorterStemmer(processedTerm);
            processedTerm = stemmer.stem();
        }

        Map<String, Map<Integer, List<String>>> snippetResults = new LinkedHashMap<>();
        HashMap<String, List<Integer>> postings = invertedIndex.get(processedTerm);
        if (postings == null) {
            return snippetResults; // Return empty map if term not found
        }

        for (Map.Entry<String, List<Integer>> entry : postings.entrySet()) {
            String documentName = entry.getKey();
            List<Integer> positions = entry.getValue();
            File documentFile = new File(corpusDirPath, documentName);

            if (!documentFile.exists()) {
                System.err.println("File not found: " + documentName);
                continue;
            }

            Map<Integer, List<String>> snippets = extractSnippetsFromFile(documentFile, positions, snipRange);
            snippetResults.put(documentName, snippets);
        }
        return snippetResults;
    }


    private Map<Integer, List<String>> extractSnippetsFromFile(File file, List<Integer> positions, int snipRange) throws IOException {
        Map<Integer, List<String>> snippetMap = new LinkedHashMap<>();

        if (!file.exists()) {
            System.err.println("File not found during snippet extraction: " + file.getName());
            return snippetMap;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<String> words = new ArrayList<>();
            String line;

            // Tokenize entire file
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\W+");
                Collections.addAll(words, tokens);
            }

            // Create snippets for each position
            for (int position : positions) {
                int start = Math.max(0, position - snipRange - 1);
                int end = Math.min(words.size() - 1, position + snipRange - 1);
                List<String> snippetParts = new ArrayList<>();

                for (int i = start; i <= end; i++) {
                    snippetParts.add(String.format("[Index %d: %s]", i + 1, words.get(i)));
                }

                snippetMap.put(position, snippetParts);
            }
        }
        return snippetMap;
    }
    
    public void saveSnippets(Map<String, Map<Integer, List<String>>> snippets, String outputFilePath, String query) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, true))) { // 使用追加模式
            writer.write("Query: " + query);
            writer.newLine();

            for (Map.Entry<String, Map<Integer, List<String>>> docEntry : snippets.entrySet()) {
                writer.write("Document: " + docEntry.getKey());
                writer.newLine();

                for (Map.Entry<Integer, List<String>> snippetEntry : docEntry.getValue().entrySet()) {
                    writer.write("  Position " + snippetEntry.getKey() + ": ");
                    writer.write(String.join(" ", snippetEntry.getValue()));
                    writer.newLine();
                }
            }
        }
        System.out.println("Snippets have been saved to " + outputFilePath);
    }

    public List<String> readQueriesFromFile(String queryFilePath) throws IOException {
        List<String> queries = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(queryFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    queries.add(line.trim());
                }
            }
        }
        return queries;
    }

    // Save search results in a grouped format with query-specific context
    public void saveFormattedSearchResults(Map<String, Set<String>> queryResults, String outputFilePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            for (Map.Entry<String, Set<String>> entry : queryResults.entrySet()) {
                String query = entry.getKey();
                Set<String> results = entry.getValue();

                writer.write("Query: " + query);
                writer.newLine();

                if (results.isEmpty()) {
                    writer.write("Results: No matching documents.");
                } else {
                    writer.write("Results: " + String.join(", ", results));
                }
                writer.newLine();
                writer.newLine(); // Add extra line for readability
            }
        }
        System.out.println("Formatted search results have been saved to " + outputFilePath);
    }

    public static void main(String[] args) {
        // Define command-line argument variables
        String stopwordFile = null;         // Path to stopword file
        String corpusDirectory = null;     // Path to the directory containing documents
        String queryFilePath = null;       // Path to the query file
        String singleQuery = null;         // Single query from the command line
        String searchOutputFile = null;    // File to save search results
        String snippetOutputFile = null;   // File to save snippets
        String indexOutputFile = null;     // File to save the inverted index
        int snippetRange = 0;              // Range for generating snippets
        boolean enableStemming = false;    // Flag to enable stemming

        // Define paths for serialized index files
        String serFilePathStemmed = "InvertedIndexStemmed.ser";
        String serFilePathNonStemmed = "InvertedIndexNonStemmed.ser";

        // Parse command-line arguments
        for (String arg : args) {
            if (arg.startsWith("--stopword=")) stopwordFile = arg.substring(arg.indexOf('=') + 1);
            else if (arg.startsWith("--corpus=")) corpusDirectory = arg.substring(arg.indexOf('=') + 1);
            else if (arg.startsWith("--query-file=")) queryFilePath = arg.substring(arg.indexOf('=') + 1);
            else if (arg.startsWith("--query=")) singleQuery = arg.substring(arg.indexOf('=') + 1);
            else if (arg.startsWith("--search-output=")) searchOutputFile = arg.substring(arg.indexOf('=') + 1);
            else if (arg.startsWith("--snip=")) snippetRange = Integer.parseInt(arg.substring(arg.indexOf('=') + 1));
            else if (arg.startsWith("--snip-output=")) snippetOutputFile = arg.substring(arg.indexOf('=') + 1);
            else if (arg.startsWith("--output=")) indexOutputFile = arg.substring(arg.indexOf('=') + 1);
            else if (arg.equals("--stem") || arg.equals("-st")) enableStemming = true;
        }

        // Ensure required parameters are provided
        if (stopwordFile == null) {
            System.err.println("Usage: java InvertedIndex --stopword=<file> [--corpus=<dir>] [--query-file=<file>] [--query=<query>] [--search-output=<file>] [--snip=<number>] [--snip-output=<file>] [--output=<file>] [--stem]");
            System.exit(1);
        }

        try {
            // Determine the serialized file path
            String serFilePath = enableStemming ? serFilePathStemmed : serFilePathNonStemmed;
            System.out.println("Stemming enabled: " + enableStemming);
            System.out.println("Using serialized file: " + serFilePath);

            // Load or build the inverted index
            InvertedIndex index = InvertedIndex.loadFromSerFile(serFilePath, stopwordFile);
            if (index == null) {
                System.out.println(serFilePath + " not found. Creating a new index.");
                index = new InvertedIndex(stopwordFile, enableStemming);
                if (corpusDirectory != null) {
                    index.buildIndex(corpusDirectory);
                }
                index.saveToSerFile(serFilePath);
            } else {
                System.out.println("Inverted index has been loaded successfully.");
            }

            // Load queries
            List<String> queries = new ArrayList<>();
            if (queryFilePath != null) {
                queries = index.readQueriesFromFile(queryFilePath); // Load queries from file
            } else if (singleQuery != null) {
                queries.add(singleQuery); // Add single query
            }

            // Process queries and save results
            if (searchOutputFile != null) {
                Map<String, Set<String>> queryResults = new LinkedHashMap<>();

                for (String query : queries) {
                    System.out.println("Processing query: " + query);
                    Set<String> searchResults = index.search(query); // Get search results
                    queryResults.put(query, searchResults);
                }

                // Save search results
                index.saveFormattedSearchResults(queryResults, searchOutputFile);
            }

            // Handle snippet generation
            if (snippetRange > 0 && snippetOutputFile != null) {
                for (String query : queries) {
                    System.out.println("Extracting snippets for query: " + query);
                    Map<String, Map<Integer, List<String>>> snippets = index.searchSnippets(query, snippetRange, corpusDirectory);
                    index.saveSnippets(snippets, snippetOutputFile, query);
                }
            }

            // Save the index to a file if specified
            if (indexOutputFile != null) {
                System.out.println("Saving inverted index to file: " + indexOutputFile);
                index.saveIndex(indexOutputFile);
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }//main


} // class
