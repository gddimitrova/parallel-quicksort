package network.programming.quicksort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Quicksort {
    private final List<Double> numbers;

    public Quicksort(List<Double> numbers) {
        this.numbers = new ArrayList<>(numbers);
    }

    public void quickSort(int begin, int end) {
        if (begin < end) {
            int partitionIndex = partition(begin, end);

            quickSort(begin, partitionIndex - 1);
            quickSort(partitionIndex + 1, end);
        }
    }

    private int partition(int begin, int end) {
        Double pivot = numbers.get(end);
        int i = begin - 1;

        for (int j = begin; j < end; j++) {
            if (numbers.get(j) <= pivot) {
                i++;

                Collections.swap(numbers, i, j);
            }
        }

        Collections.swap(numbers, i + 1, end);

        return i + 1;
    }

    public List<Double> getNumbers() {
        return numbers;
    }

}
