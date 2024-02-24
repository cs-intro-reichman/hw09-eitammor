import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
        String window = "";
        char chr;
        In in = new In(fileName);
        // Reads just enough characters to form the first window
        for (int i = 0; i < windowLength; i++) {
            window += in.readChar();
        }

        // Processes the entire text, one character at a time
        while (!in.isEmpty()) {
            chr = in.readChar();
            List probs = CharDataMap.get(window);
            if (probs == null) {
                probs = new List();
                CharDataMap.put(window, probs);
            }
            probs.update(chr);
            String newWindow = window + chr;
            window = newWindow.substring(1);
        }
        for (List probs : CharDataMap.values())
            calculateProbabilities(probs);

	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
        int sumListNumber = 0;

        ListIterator listIterator = probs.listIterator(0);
        while (listIterator.hasNext()) {
            CharData charData = listIterator.next();
            sumListNumber += charData.count;
        }

        listIterator = probs.listIterator(0);
        double counter = 0;
        while (listIterator.hasNext()) {
            CharData charData = listIterator.next();
            charData.p = ((double) charData.count) / sumListNumber;
            counter += charData.p;
            charData.cp = counter;
        }
        

	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
        double rand = this.randomGenerator.nextDouble();
        ListIterator listIterator = probs.listIterator(0);
        while (listIterator.hasNext()) {
            CharData charData = listIterator.next();
            if (charData.cp > rand) {
                return charData.chr;
            }
        }
        return probs.get(probs.getSize()).chr;
        
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
        if (initialText.length() < this.windowLength || initialText.length() >= textLength) {
            return initialText;
        }
        String newTxt = initialText;
        String window = initialText.substring(initialText.length() - windowLength);
        while (newTxt.length() - windowLength < textLength) {
            if (CharDataMap.containsKey(window)) {
                char chr = getRandomChar(CharDataMap.get(window));
                newTxt += chr;
                window = window.substring(1) + chr;
            } else {
                return newTxt;
            }
        }
        return newTxt;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
		// Your code goes here
    }
}
