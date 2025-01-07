# EfficientSearchEngine
EfficientSearch Engine is a student-developed information retrieval system designed to effectively search and retrieve documents from a predefined corpus.
The project is structured in three phases:

1. **Inverted Index Creation:** Constructs an inverted index to enable quick lookup of documents containing specific keywords.
2. **Enhanced Query Processing:** Integrates Porter’s stemming algorithm, allowing users to perform searches based on word roots for broader matching. This feature can be toggled via a command-line flag.
3. **Flexible Output Options:** Provides output methods including text file output or both text file output and command-line display based on user preference. Additionally, the system calculates precision and recall metrics to evaluate search performance and supports persistent storage of the inverted index to optimize performance for subsequent searches.

EfficientSearch Engine offers a user-friendly and effective solution for automated document retrieval, making it a valuable tool for educational purposes and foundational search engine development.

## Key Features

- **Inverted Index:** Facilitates fast and efficient keyword-based searches.
- **Porter’s Stemming Algorithm:** Enhances search flexibility by matching word roots.
- **Flexible Output Options:** Choose between text file output or both text file output and command-line display.
- **Performance Evaluation:** Computes precision and recall to assess search accuracy.
- **Persistent Storage:** Saves the inverted index for quicker subsequent searches.

## Usage

The system operates via the command line and offers various flags to customize its behavior. Below are the primary functionalities and how to use them.

### 1. `--stem` Flag (for `Indexstem.txt`)

- **Description:**
  - Applies Porter’s Stemming Algorithm to normalize words in the corpus.
  - Reduces words to their root form by removing suffixes.
    - `running`, `runs`, and `runner` → `run`
    - `happier`, `happiness` → `happi`
    - `studying`, `studies` → `studi`
- **Impact on Index:**
  - The index uses these stemmed forms as keys, meaning multiple word variations point to the same stemmed root.
  - **Example:**
    ```
    Word: run
      Document: example.txt -> [10, 25, 50]
    ```

### 2. Without `--stem` Flag (for `Index.txt`)

- **Description:**
  - Words are indexed as they appear in the corpus without normalization.
  - Variations of a word are treated as separate entries in the index.
- **Impact on Index:**
  - **Example:**
    ```
    Word: running
      Document: example.txt -> [10, 50]
    Word: runner
      Document: example.txt -> [25]
    ```

### 3. Key Differences

| Aspect            | `Indexstem.txt` (with `--stem`)          | `Index.txt` (without `--stem`)             |
|-------------------|------------------------------------------|--------------------------------------------|
| **Key Words**     | Root forms (stemmed words)               | Exact forms (as in the corpus)             |
| **Word Variations** | Merged into a single entry             | Separate entries for each variation        |
| **Example Words** | run, happi, studi                        | running, happiness, studying               |
| **Use Case**      | Suitable for queries with word variations| Precise indexing without normalization     |

### 4. Impact on Search

- **With Stemming:**
  - A query for "running" will match all occurrences of "run", "running", "runner", etc.
- **Without Stemming:**
  - A query for "running" will only match exact occurrences of "running" and will not include "runner" or "run".

### 5. Persistence - Serialized

- **.ser Not Exist:**
  - **Command:**
    ```bash
    java InvertedIndex --stopword=Stoplist.txt --corpus=Corpus --output=Index.txt --stem
    ```
  - **Description:**
    - Builds the index from the `Corpus` directory.
    - Saves the index to `Index.txt` and the serialized file `InvertedIndex.ser`.
    - **Verification:**
      - `Index.txt` contains the correct index.
      - `InvertedIndex.ser` is created.

- **.ser Exists:**
  - **Command:**
    ```bash
    java InvertedIndex --stopword=Stoplist.txt --corpus=Corpus --output=IndexReloaded.txt --stem
    ```
  - **Description:**
    - Loads the index from `InvertedIndex.ser` without rebuilding it from the `Corpus` directory.
    - Saves the reloaded index to `IndexReloaded.txt`.
    - **Verification:**
      - Compare `Index.txt` and `IndexReloaded.txt` to ensure they are identical.

### 6. Additional Commands

- **Without Stemming:**
  ```bash
  java InvertedIndex --stopword=Stoplist.txt --corpus=Corpus --output=NonStemmedIndex.txt --nonstem-ser=CustomNonStemmed.ser
  WORD SEARCH
  java InvertedIndex --stopword=Stoplist.txt --SEARCH=word:flower --search-output=WordSearchFlowerOutput.txt
  ```

- **With Stemming:**
  ```bash
  java InvertedIndex --stopword=Stoplist.txt --corpus=Corpus --output=StemmedIndex.txt --stem --stem-ser=CustomStemmed.ser
  WORD SEARCH
  java InvertedIndex --stopword=Stoplist.txt --SEARCH=word:flower --search-output=WordSearchFlowerOutput.txt --stem
  ```

## Installation

1. **Prerequisites:**
   - Java Development Kit (JDK) installed on your system.
   - Command-line interface (e.g., Terminal, Command Prompt).

2. **Setup:**
   - Clone or download the project repository.
   - Navigate to the project directory.

3. **Compilation:**
   ```bash
   javac *.java
   ```

4. **Execution:**
   - Follow the usage instructions provided in the [Usage](#usage) section.

## Example

### Building the Index with Stemming

```bash
java InvertedIndex --stopword=Stoplist.txt --corpus=Corpus --output=Indexstem.txt --stem
```

### Building the Index without Stemming

```bash
java InvertedIndex --stopword=Stoplist.txt --corpus=Corpus --output=Index.txt
```

### Performing a Word Search with Stemming

```bash
java InvertedIndex --stopword=Stoplist.txt --SEARCH=word:flower --search-output=WordSearchFlowerOutput.txt --stem
```

### Performing a Word Search without Stemming

```bash
java InvertedIndex --stopword=Stoplist.txt --SEARCH=word:flower --search-output=WordSearchFlowerOutput.txt
```

## Evaluation

The system calculates **precision** and **recall** for each query to evaluate search performance:

- **Precision:** Measures the accuracy of the retrieved documents.
- **Recall:** Measures the completeness of the retrieved documents relative to the relevant documents in the corpus.

## Contributing

This project is intended for educational purposes. Contributions are welcome to enhance its features and functionalities.

## License

This project is licensed under the MIT License.