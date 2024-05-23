/*
 * Copyright 2019, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.benchmarks.stats;

import io.opencensus.benchmarks.tags.TagsBenchmarksUtil;
import io.opencensus.stats.MeasureMap;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.Tagger;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/** Benchmarks for {@link io.opencensus.stats.StatsRecorder}. */
public class RecordBatchedBenchmark {
  @State(org.openjdk.jmh.annotations.Scope.Benchmark)
  public static class Data {
    @Param({"0", "1", "2", "3", "6", "8"})
    int numValues;

    @Param({"impl", "impl-lite"})
    String implementation;

    private StatsRecorder recorder;
    private Tagger tagger;
    private TagContext tags;

    @Setup
    public void setup() throws Exception {
      ViewManager manager = StatsBenchmarksUtil.getViewManager(implementation);
      recorder = StatsBenchmarksUtil.getStatsRecorder(implementation);
      tagger = TagsBenchmarksUtil.getTagger(implementation);
      tags = TagsBenchmarksUtil.createTagContext(tagger.emptyBuilder(), 1);
      for (int i = 0; i < numValues; i++) {
        manager.registerView(StatsBenchmarksUtil.DOUBLE_COUNT_VIEWS[i]);
        manager.registerView(StatsBenchmarksUtil.LONG_COUNT_VIEWS[i]);
        manager.registerView(StatsBenchmarksUtil.DOUBLE_SUM_VIEWS[i]);
        manager.registerView(StatsBenchmarksUtil.LONG_SUM_VIEWS[i]);
        manager.registerView(StatsBenchmarksUtil.DOUBLE_DISTRIBUTION_VIEWS[i]);
        manager.registerView(StatsBenchmarksUtil.LONG_DISTRIBUTION_VIEWS[i]);
        manager.registerView(StatsBenchmarksUtil.DOUBLE_LASTVALUE_VIEWS[i]);
        manager.registerView(StatsBenchmarksUtil.LONG_LASTVALUE_VIEWS[i]);
      }
    }
  }

  /** Record batched double count measures. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap recordBatchedDoubleCount(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(StatsBenchmarksUtil.DOUBLE_COUNT_MEASURES[i], (double) i);
    }
    map.record(data.tags);
    return map;
  }

  /** Record batched long count measures. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap recordBatchedLongCount(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(StatsBenchmarksUtil.LONG_COUNT_MEASURES[i], i);
    }
    map.record(data.tags);
    return map;
  }

  /** Record batched double sum measures. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap recordBatchedDoubleSum(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(StatsBenchmarksUtil.DOUBLE_SUM_MEASURES[i], (double) i);
    }
    map.record(data.tags);
    return map;
  }

  /** Record batched long sum measures. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap recordBatchedLongSum(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(StatsBenchmarksUtil.LONG_SUM_MEASURES[i], i);
    }
    map.record(data.tags);
    return map;
  }

  /** Record batched double distribution measures. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap recordBatchedDoubleDistribution(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(StatsBenchmarksUtil.DOUBLE_DISTRIBUTION_MEASURES[i], (double) i);
    }
    map.record(data.tags);
    return map;
  }

  /** Record batched ling distribution measures. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap recordBatchedLongDistribution(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(StatsBenchmarksUtil.DOUBLE_DISTRIBUTION_MEASURES[i], i);
    }
    map.record(data.tags);
    return map;
  }

  /** Record batched double last value measures. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap recordBatchedDoubleLastValue(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(StatsBenchmarksUtil.DOUBLE_LASTVALUE_MEASURES[i], (double) i);
    }
    map.record(data.tags);
    return map;
  }

  /** Record batched long last value measures. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap recordBatchedLongLastValue(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(StatsBenchmarksUtil.LONG_LASTVALUE_MEASURES[i], i);
    }
    map.record(data.tags);
    return map;
  }
}
