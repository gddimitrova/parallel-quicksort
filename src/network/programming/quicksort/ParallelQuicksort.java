package network.programming.quicksort;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ParallelQuicksort extends Thread {

    private static final int MAX_THREADS = 32;

    private final AtomicReferenceArray<Double> numbers;
    private final int start;
    private final int end;

    private final ExecutorService executor;

    public ParallelQuicksort(List<Double> numbers, int start, int end) {
        this.numbers = new AtomicReferenceArray<>(numbers.size());

        for (int i = 0; i < numbers.size(); i++) {
            this.numbers.set(i, numbers.get(i));
        }

        this.start = start;
        this.end = end;
        this.executor = Executors.newFixedThreadPool(MAX_THREADS);
    }

    public ParallelQuicksort(AtomicReferenceArray<Double> numbers, int start, int end) {
        this.numbers = numbers;

        this.start = start;
        this.end = end;
        this.executor = Executors.newFixedThreadPool(MAX_THREADS);
    }

    @Override
    public void run() {
        sort(start, end);
    }

    private void sort(int begin, int end) {
        if (begin < end) {
            int partitionIndex = partition(begin, end);

            ParallelQuicksort left = new ParallelQuicksort(this.numbers, begin, partitionIndex - 1);
            ParallelQuicksort right = new ParallelQuicksort(this.numbers, partitionIndex + 1, end);

            executor.execute(left);
            executor.execute(right);

            executor.shutdown();

            try {
                if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

    private int partition(int begin, int end) {
        Double pivot = numbers.get(end);
        int i = begin - 1;

        for (int j = begin; j < end; j++) {
            if (numbers.get(j) <= pivot) {
                i++;

                double t1 = numbers.get(i);
                double t2 = numbers.get(j);
                numbers.set(i, t2);
                numbers.set(j, t1);
            }
        }

        double t1 = numbers.get(i + 1);
        double t2 = numbers.get(end);
        numbers.set(i + 1, t2);
        numbers.set(end, t1);

        return i + 1;
    }

    public List<Double> getNumbers() {
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < this.numbers.length(); i++) {
            result.add(this.numbers.get(i));
        }
        return result;
    }
}
