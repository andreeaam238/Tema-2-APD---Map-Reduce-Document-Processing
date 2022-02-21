import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

// The Reduce Task.
public class ReduceRunnable implements Runnable {
    private final DocumentMap documentInfo;
    private final Map<String, MapReduceResult> mapReduceResults;
    private final String documentName;

    public ReduceRunnable(DocumentMap documentInfo, Map<String, MapReduceResult> mapReduceResults,
                          String documentName) {
        this.documentInfo = documentInfo;
        this.mapReduceResults = mapReduceResults;
        this.documentName = documentName;
    }

    // Function for joining two or more Maps.
    public static Map<Integer, Integer> joinMaps(CopyOnWriteArrayList<Map<Integer, Integer>> maps) {
        // Join the keys.
        Set<Integer> keys = new HashSet<>();
        for (Map<Integer, Integer> map : maps) {
            keys.addAll(map.keySet());
        }

        Map<Integer, Integer> joinedMap = new HashMap<>();
        for (Integer key : keys) {
            // Join the values for the current key in each map by summing them up.
            Integer value = 0;
            for (Map<Integer, Integer> map : maps) {
                if (map.containsKey(key)) {
                    value += map.get(key);
                }
            }

            // Insert the (key, value) pair in the merged map.
            joinedMap.put(key, value);
        }

        return joinedMap;
    }

    // Function for computing the first n + 1 numbers from the Fibonacci Sequence.
    private static int[] fibonacci(int n) {
        int[] fibonacciSequence = new int[n + 1];

        // Base cases.
        fibonacciSequence[0] = 0;
        fibonacciSequence[1] = 1;

        // Compute te next term in the sequence using the formula: v[i] = v[i - 1] + v[i - 2].
        for (int i = 2; i <= n; i++) {
            fibonacciSequence[i] = fibonacciSequence[i - 1] + fibonacciSequence[i - 2];
        }

        return fibonacciSequence;
    }

    @Override
    public void run() {
        // Join the word maps of each fragment generated by the Map Tasks in the previous phase of the Map Reduce
        // Algorithm.
        Map<Integer, Integer> map = joinMaps(documentInfo.getFragmentsWordLengths());

        // Join all the maximum length words from each fragment in the document.
        ArrayList<String> fragmentsMaxLengthWords = new ArrayList<>();
        for (List<String> l : documentInfo.getMaxLengthWords()) {
            fragmentsMaxLengthWords.addAll(l);
        }

        int maxLength = 0;
        List<String> maxLengthWords = new ArrayList<>();

        // Find the maximum length of a word and the number of words of this length in the whole document.
        for (String word : fragmentsMaxLengthWords) {
            if (word.length() > maxLength) {
                maxLength = word.length();
                maxLengthWords.clear();
                maxLengthWords.add(word);
            } else if (word.length() == maxLength) {
                maxLengthWords.add(word);
            }
        }

        // Compute the first maxLength + 1 term in the Fibonacci Sequence.
        int[] fibonacciSequence = fibonacci(maxLength + 1);

        double rank = 0, noWords = 0;

        // Compute the number of words in the whole document and the document's rank using the formula from the
        // homework's requirements.
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            noWords += entry.getValue();
            rank += fibonacciSequence[entry.getKey() + 1] * entry.getValue();
        }
        rank /= noWords;

        // Add the Reduce Phase results of the current Task to the map of results of the Map Reduce Algorithm.
        mapReduceResults.put(documentName, new MapReduceResult(rank, maxLength, maxLengthWords.size()));
    }
}