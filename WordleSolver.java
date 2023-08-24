import java.util.*;
import java.io.*;
import java.lang.Math;

    public class WordleSolver {

        static ArrayList<String> wordleAnswers;
        static ArrayList<String> wordleGuesses;
        static ArrayList<WordEntropy> initialEntropies;

        public static void main (String[] args) throws IOException {
            // read in valid wordle answers file
            Scanner s = new Scanner(new File("wordle_answers.txt")).useDelimiter(",");
            wordleAnswers = new ArrayList<>();
            while (s.hasNext()) {
                wordleAnswers.add(s.next().substring(1, 6));
            }

            // read in valid wordle guesses file
            s = new Scanner(new File("wordle_guesses.txt")).useDelimiter(",");
            wordleGuesses = new ArrayList<>();
            while (s.hasNext()) {
                wordleGuesses.add(s.next().substring(1, 6));
            }
            s.close();

            // pre-compute intial entropy of all guesses (reused for first guess of each word) for efficiency
            // (use initialEntropies defined above)
            // YOUR CODE HERE:
            initialEntropies = calculateEntropy(wordleGuesses, wordleAnswers);
            Collections.sort(initialEntropies, new EntropyRank());

            System.out.println("Best 10 first guesses");
            for (int i = 0; i < 10; i++) {
                System.out.println(initialEntropies.get(i).getWord());
            }
            System.out.println("Worst 10 first guesses");
            for (int i = initialEntropies.size() - 1; i > initialEntropies.size() - 11; i--) {
                System.out.println(initialEntropies.get(i).getWord());
            }
            // solve for each possible answer in wordle
            solveAll();
        }

        // @description: runs the solving algorithm for every possible answer in wordle
        //               then compiles and prints the results
        public static void solveAll() {
            // store results categorized by # guesses to solve for answer
            int[] results = new int[10];

            // Comment this out to watch your code solve each word

            int num = 1;
            int denom = wordleAnswers.size();


            // YOUR CODE HERE:
            // solve for each possible answer
            // Hint: start at first guess and case where all wordle answers are possible
            // Hint: guess until there is only 1 possible answer left, which must be the actual answer that we will guess right away
            // Hint: calculate entropy of all possible guesses and choose the guess with the highest expected information value
            // Hint: update the remaining possible answers based on the coloring that appears from the guess
            // Hint: if we found the answer with our guess (coloring is all green), stop
            // Hint: otherwise, repeat guessing until word is solved
            for (String answer : wordleAnswers) {
                int count = 1;
                String guess = initialEntropies.get(0).getWord();
                ArrayList<String> remainingAnswers = wordleAnswers;

                while (remainingAnswers.size() != (1)) {
                    ArrayList<WordEntropy> entropyList;
                    if (count == 1) {
                        entropyList = initialEntropies;
                    } else {
                        entropyList = calculateEntropy(wordleGuesses, remainingAnswers);
                        Collections.sort(entropyList, new EntropyRank());
                    }
                    guess = entropyList.get(0).getWord();

                    int[] coloring = computeColoring(guess, answer);
                    int index = computeIndex(coloring);
                    if (index == 242) {
                        break;
                    }
                    remainingAnswers = findMatches(index, guess, remainingAnswers);
                    count++;
                }


                // Comment this out to watch your code solve each word
                // REQUIRES String answer = word being guessed, int count = # guesses it took to solve

                System.out.println("Num " + num + " of " + denom + ": word is " + answer + ", solved in " + count + " guesses");
                num++;


                // store how many guesses it took to solve the word in result!
                // YOUR CODE HERE:
                results[count]++;
            }
            // Comment this out to see the statistics of solving all words

            int total = 0;
            for (int i = 0; i < 10; i++) {
                System.out.println("words solved in " + i + " guesses: " + results[i]);
                total += results[i] * i;
            }
            double meanGuesses = ((double) total) / ((double) denom);
            System.out.println("Average # of guesses to solve: " + meanGuesses);


        }


        // @params: (int) coloring index that describes the coloring output of the guess
        //          (String) guess, the guess word
        //          (ArrayList<String>) list of all answers that are still possible
        // @return: (ArrayList<String>) subset of all answers that still match the coloring output
        public static ArrayList<String> findMatches(int coloringIndex, String guess, ArrayList<String> answers) {
            ArrayList<String> remainingAnswers = new ArrayList<>();

            // YOUR CODE HERE:
            for (String answer : answers) {
                int[] coloring = computeColoring(guess, answer);
                int index = computeIndex(coloring);
                if (coloringIndex == index) {
                    remainingAnswers.add(answer);
                }
            }
            return remainingAnswers;
        }

        // @params: (String) guess word, (String) answer word
        // @return: (int[]) array of a 5 digit base-3 number that uniquely represents a coloring index
        //          gray = 0, yellow = 1, green = 2 and each digit corresponds to a letter
        //          format is {c_0, c_1, c_2, c_3, c_4} = "C R A N E", c_0 = C coloring, c_1 = R coloring, ...
        public static int[] computeColoring(String guess, String answer) {
            int[] coloring = new int[5];
            ArrayList<Character> correct = new ArrayList<>();
            HashMap<Character, Integer> count = new HashMap<>();

            // COPY PASTE TASK 1 CODE:
            for (int i = 0; i < answer.length(); i++) {
                correct.add(answer.charAt(i));
                if (count.containsKey(answer.charAt(i))) {
                    count.put(answer.charAt(i), count.get(answer.charAt(i)) + 1);
                } else {
                    count.put(answer.charAt(i), 1);
                }
            }
            for (int i = 0; i < answer.length(); i++) {
                char g = guess.charAt(i);
                if (g == correct.get(i)) {
                    coloring[i] = 2;
                    count.put(g, count.get(g) - 1);
                }
            }
            for (int i = 0; i < answer.length(); i++) {
                char g = guess.charAt(i);
                if (count.containsKey(g) && coloring[i] != 2 && count.get(g) > 0) {
                    coloring[i] = 1;
                    count.put(g, count.get(g) - 1);
                } else if (coloring[i] != 2){
                    coloring[i] = 0;
                }
            }
            return coloring;
        }

        // @params: (int[]) array representing a 5 digit base-3 coloring
        // @return: (int) the coloring index in base 10, to be used as array indice
        //          array {c_0, c_1, c_2, c_3, c_4} = c_4 * 3^4 + c_3 * 3^3 + c_2 * 3^2 + c_1 * 3^1 + c_0 * 3^0
        public static int computeIndex(int[] coloring) {
            // COPY PASTE TASK 2 CODE:
            int color = 0;
            for (int i = 0; i < coloring.length; i++) {
                color += (int) (coloring[i] * Math.pow(3, i));
            }

            return color;
        }

        // @params: (ArrayList<String>) guesses is the set of all possible wordle guesses
        //          (ArrayList<String>) answers is the subset of valid wordle answers in consideration
        // @return: (ArrayList<WordEntropy>) the list of guesses and their calculated entropies
        //              entropy is defined as expected information value, which is calculated from
        //              a summation of P(coloring) * -log_2(P(coloring))
        //              P(coloring) = # of answers that match the coloring / total # of possible answers
        //              information value is measured in bits, 1 bit of information halves the set of possible answers
        public static ArrayList<WordEntropy> calculateEntropy(ArrayList<String> guesses, ArrayList<String> answers) {
            ArrayList<WordEntropy> entropies = new ArrayList<>();

            for (String g : guesses) {
                int[] matches = new int[243];

                for (String a : answers) {
                    int[] color = computeColoring(g, a);
                    matches[computeIndex(color)] += 1;
                }
                double entropy = 0;
                for (int i = 0; i < matches.length; i++) {
                    double prob = ((double) matches[i] / (double) answers.size());
                    if (prob > 0) {
                        double info = -1 * Math.log(prob / Math.log(2));
                        entropy += info * prob;
                    }
                }
                entropies.add(new WordEntropy(g, entropy));
            }

            // COPY PASTE TASK 2 CODE:

            return entropies;
        }
    }

    // Word Entropy class used for storing word and entropy pairs
    class WordEntropy {

        String word;
        double entropy;

        public WordEntropy(String w, double e) {
            this.word = w;
            this.entropy = e;
        }

        public String getWord() {
            return this.word;
        }

        public double getEntropy() {
            return this.entropy;
        }
    }

    // custom comparator for sorting by entropy
    // sort by decreasing entropy
    // tiebreak lexicographically (alphabetically), must do this or else will error
    class EntropyRank implements Comparator<WordEntropy> {
        @Override
        public int compare(WordEntropy we1, WordEntropy we2) {
            // COPY PASTE TASK 2 CODE:
            if (we2.getEntropy() > we1.getEntropy()) {
                return 1;
            } else if (we1.getEntropy() > we2.getEntropy()) {
                return -1;
            } else {
                return we1.getWord().compareTo(we2.getWord());
            }
        }
    }
