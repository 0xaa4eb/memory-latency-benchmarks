package com.xaa4eb.perf.latency.cpu;

import com.xaa4eb.perf.BenchmarkRunner;
import com.chibik.perf.util.*;
import com.xaa4eb.perf.util.IndexedLatencyBenchmark;
import com.xaa4eb.perf.util.MemUtil;
import com.xaa4eb.perf.util.Padder;
import com.xaa4eb.perf.util.SingleShotBenchmark;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Test reading two memory addresses which are dependent, so there is a CPU stall
 */
@State(Scope.Benchmark)
@SingleShotBenchmark(batchSize = TwoDependentReadsLatency.BATCH_SIZE)
public class TwoDependentReadsLatency extends IndexedLatencyBenchmark {

    public static final int BATCH_SIZE = 1000000;

    private static class Link extends Padder {
        public final long value;

        public Link(long value) {
            this.value = value;
        }
    }

    private static class StateHolder extends Padder {
        Link link;

        StateHolder() {
            link = new Link(getHeadXored());
        }

        public Link getLink() {
            return link;
        }
    }

    private StateHolder[] arr;

    @Setup(Level.Trial)
    public void setUpTrial() {
        arr = MemUtil.allocateArray(BATCH_SIZE, StateHolder.class, StateHolder::new);
    }

    @Benchmark
    public void read(Blackhole blackhole) {
        Link link = arr[getIndex()].getLink();
        blackhole.consume(link.value);
        blackhole.consume(link);
    }

    public static void main(String[] args) {
        BenchmarkRunner.run(TwoDependentReadsLatency.class);
    }
}
