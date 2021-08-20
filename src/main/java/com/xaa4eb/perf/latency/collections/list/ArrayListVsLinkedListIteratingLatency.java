package com.xaa4eb.perf.latency.collections.list;

import com.xaa4eb.perf.BenchmarkRunner;
import com.xaa4eb.perf.util.IndexedLatencyBenchmark;
import com.xaa4eb.perf.util.MemUtil;
import com.xaa4eb.perf.util.Padder;
import com.xaa4eb.perf.util.SingleShotBenchmark;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@State(Scope.Benchmark)
@SingleShotBenchmark(batchSize = ArrayListVsLinkedListIteratingLatency.BATCH_SIZE)
public class ArrayListVsLinkedListIteratingLatency extends IndexedLatencyBenchmark {

    public static final int BATCH_SIZE = 100000;

    private static class StateHolder extends Padder {
        final List<Integer> list;

        private StateHolder(List<Integer> list) {
            this.list = list;
        }

        public int sum() {
            int sum = 0;
            for (int i : list) {
                sum = (sum + i ^ sum);
            }
            return sum;
        }
    }

    @Param({"ArrayList", "LinkedList"})
    private String listImpl;

    private StateHolder[] arr;

    private List<Integer> initList(List<Integer> list) {
        for (int i = 0; i < 32; i++) {
            list.add(ThreadLocalRandom.current().nextInt());
        }
        return list;
    }

    @Setup(Level.Trial)
    public void setUpTrial() {

        if (listImpl.equals("ArrayList")) {
            arr = MemUtil.allocateArray(BATCH_SIZE, StateHolder.class, () -> new StateHolder(initList(new IntArrayList())));
        } else {
            arr = MemUtil.allocateArray(BATCH_SIZE, StateHolder.class, () -> new StateHolder(initList(new LinkedList<>())));
        }
    }

    @Benchmark
    public void iterate(Blackhole blackhole) {
        blackhole.consume(arr[getIndex()].sum());
    }

    public static void main(String[] args) {
        BenchmarkRunner.run(ArrayListVsLinkedListIteratingLatency.class);
    }
}
