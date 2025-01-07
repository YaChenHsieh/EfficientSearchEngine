public class PorterStemmer {
    private StringBuilder word;

    public PorterStemmer(String word) {
        this.word = new StringBuilder(word != null ? word.toLowerCase() : "");
    }

    // Check if a character is a consonant
    private boolean isConsonant(int i) {
        if (i < 0 || i >= word.length()) return false;
        char ch = word.charAt(i);
        return (ch != 'a' && ch != 'e' && ch != 'i' && ch != 'o' && ch != 'u')
                && !(ch == 'y' && i > 0 && !isConsonant(i - 1));
    }

    // Calculate the measure (m) of the word
    private int measure() {
        int count = 0;
        boolean inVowelSeq = false;
        for (int i = 0; i < word.length(); i++) {
            if (!isConsonant(i)) {
                inVowelSeq = true;
            } else if (inVowelSeq) {
                count++;
                inVowelSeq = false;
            }
        }
        return count;
    }

    // Step 1a: Deal with plurals and "s" endings
    private void step1a() {
        if (word.length() == 0) return;
        if (word.toString().endsWith("sses")) {
            word.replace(word.length() - 4, word.length(), "ss");
        } else if (word.toString().endsWith("ies")) {
            word.replace(word.length() - 3, word.length(), "i");
        } else if (word.length() > 2 && word.charAt(word.length() - 1) == 's') {
            word.deleteCharAt(word.length() - 1);
        }
    }

    // Step 1b: Deal with -ed and -ing endings
    private void step1b() {
        if (word.length() == 0) return;
        if (word.toString().endsWith("eed")) {
            if (measure() > 0) {
                word.replace(word.length() - 3, word.length(), "ee");
            }
        } else if ((word.toString().endsWith("ed") || word.toString().endsWith("ing")) && containsVowel()) {
            word.delete(word.length() - (word.toString().endsWith("ed") ? 2 : 3), word.length());
            if (word.toString().endsWith("at") || word.toString().endsWith("bl") || word.toString().endsWith("iz")) {
                word.append("e");
            } else if (isDoubleConsonant(word.length() - 1)) {
                word.deleteCharAt(word.length() - 1);
            } else if (measure() == 1 && cvc(word.length() - 1)) {
                word.append("e");
            }
        }
    }

    // Step 1c: Replace ending 'y' with 'i' if there is a vowel before it
    private void step1c() {
        if (word.length() > 0 && word.charAt(word.length() - 1) == 'y' && containsVowelBeforeLast()) {
            word.setCharAt(word.length() - 1, 'i');
        }
    }

    // Step 2: Handle certain suffixes like -ational, -tional
    private void step2() {
        if (word.length() == 0) return;
        if (word.toString().endsWith("ational") && measure() > 0) {
            word.replace(word.length() - 7, word.length(), "ate");
        } else if (word.toString().endsWith("tional") && measure() > 0) {
            word.replace(word.length() - 6, word.length(), "tion");
        }
    }

    // Step 3: Further suffix reduction (e.g., -icate -> -ic)
    private void step3() {
        if (word.length() == 0) return;
        if (word.toString().endsWith("icate") && measure() > 0) {
            word.replace(word.length() - 5, word.length(), "ic");
        } else if (word.toString().endsWith("ative") && measure() > 0) {
            word.delete(word.length() - 5, word.length());
        }
    }

    // Step 4: Remove certain suffixes like -ance, -ence
    private void step4() {
        if (word.length() == 0) return;
        if (word.toString().endsWith("ance") && measure() > 1) {
            word.replace(word.length() - 4, word.length(), "");
        } else if (word.toString().endsWith("ence") && measure() > 1) {
            word.replace(word.length() - 4, word.length(), "");
        }
    }

    // Step 5a: Remove 'e' if m > 1, or if m = 1 and not CVC
    private void step5a() {
        if (word.length() > 0 && word.charAt(word.length() - 1) == 'e') {
            int m = measure();
            if (m > 1 || (m == 1 && !cvc(word.length() - 2))) {
                word.deleteCharAt(word.length() - 1);
            }
        }
    }

    // Step 5b: Remove double 'l' if measure > 1
    private void step5b() {
        if (word.length() > 1 && isDoubleConsonant(word.length() - 1) && measure() > 1 && word.charAt(word.length() - 1) == 'l') {
            word.deleteCharAt(word.length() - 1);
        }
    }

    // Helper methods
    private boolean containsVowel() {
        for (int i = 0; i < word.length(); i++) {
            if (!isConsonant(i)) return true;
        }
        return false;
    }

    private boolean containsVowelBeforeLast() {
        for (int i = 0; i < word.length() - 1; i++) {
            if (!isConsonant(i)) return true;
        }
        return false;
    }

    private boolean isDoubleConsonant(int i) {
        return i > 0 && word.charAt(i) == word.charAt(i - 1) && isConsonant(i);
    }

    private boolean cvc(int i) {
        return i >= 2 && isConsonant(i) && !isConsonant(i - 1) && isConsonant(i - 2)
                && word.charAt(i) != 'w' && word.charAt(i) != 'x' && word.charAt(i) != 'y';
    }

    // Complete Stemming Process
    public String stem() {
        if (word.length() == 0) return ""; // Handle empty input
        step1a();
        step1b();
        step1c();
        step2();
        step3();
        step4();
        step5a();
        step5b();
        return word.toString();
    }

    // Main method to test the PorterStemmer
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java PorterStemmer <Enter_the_word>");
            return;
        }

        String wordToStem = args[0];
        PorterStemmer stemmer = new PorterStemmer(wordToStem);
        System.out.println("Original word: " + wordToStem);
        System.out.println("Stemmed word: " + stemmer.stem());
    }
}
