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

package io.opencensus.metrics;

import io.opencensus.internal.Utils;
import java.util.List;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Long Cumulative metric, to report instantaneous measurement of an int64 value. Cumulative values
 * can go up or stay the same, but can never go down. Cumulative values cannot be negative.
 *
 * <p>Example 1: Create a Cumulative with default labels.
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final MetricRegistry metricRegistry = Metrics.getMetricRegistry();
 *
 *   List<LabelKey> labelKeys = Arrays.asList(LabelKey.create("Name", "desc"));
 *
 *   LongCumulative cumulative = metricRegistry.addLongCumulative(
 *     "processed_jobs", "Processed jobs", "1", labelKeys);
 *
 *   // It is recommended to keep a reference of a point for manual operations.
 *   LongPoint defaultPoint = cumulative.getDefaultTimeSeries();
 *
 *   void doWork() {
 *      // Your code here.
 *      defaultPoint.add(10);
 *   }
 *
 * }
 * }</pre>
 *
 * <p>Example 2: You can also use labels(keys and values) to track different types of metric.
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final MetricRegistry metricRegistry = Metrics.getMetricRegistry();
 *
 *   List<LabelKey> labelKeys = Arrays.asList(LabelKey.create("Name", "desc"));
 *   List<LabelValue> labelValues = Arrays.asList(LabelValue.create("Inbound"));
 *
 *   LongCumulative cumulative = metricRegistry.addLongCumulative(
 *     "processed_jobs", "Processed jobs", "1", labelKeys);
 *
 *   // It is recommended to keep a reference of a point for manual operations.
 *   LongPoint inboundPoint = cumulative.getOrCreateTimeSeries(labelValues);
 *
 *   void doSomeWork() {
 *      // Your code here.
 *      inboundPoint.set(15);
 *   }
 *
 * }
 * }</pre>
 *
 * @since 0.21
 */
@ThreadSafe
public abstract class LongCumulative {

  /**
   * Creates a {@code TimeSeries} and returns a {@code LongPoint} if the specified {@code
   * labelValues} is not already associated with this cumulative, else returns an existing {@code
   * LongPoint}.
   *
   * <p>It is recommended to keep a reference to the LongPoint instead of always calling this method
   * for manual operations.
   *
   * @param labelValues the list of label values. The number of label values must be the same to
   *     that of the label keys passed to {@link MetricRegistry#addLongCumulative}.
   * @return a {@code LongPoint} the value of single cumulative.
   * @throws NullPointerException if {@code labelValues} is null OR any element of {@code
   *     labelValues} is null.
   * @throws IllegalArgumentException if number of {@code labelValues}s are not equal to the label
   *     keys passed to {@link MetricRegistry#addLongCumulative}.
   * @since 0.21
   */
  public abstract LongPoint getOrCreateTimeSeries(List<LabelValue> labelValues);

  /**
   * Returns a {@code LongPoint} for a cumulative with all labels not set, or default labels.
   *
   * @return a {@code LongPoint} for a cumulative with all labels not set, or default labels.
   * @since 0.21
   */
  public abstract LongPoint getDefaultTimeSeries();

  /**
   * Removes the {@code TimeSeries} from the cumulative metric, if it is present. i.e. references to
   * previous {@code LongPoint} objects are invalid (not part of the metric).
   *
   * @param labelValues the list of label values.
   * @throws NullPointerException if {@code labelValues} is null.
   * @since 0.21
   */
  public abstract void removeTimeSeries(List<LabelValue> labelValues);

  /**
   * Removes all {@code TimeSeries} from the cumulative metric. i.e. references to all previous
   * {@code LongPoint} objects are invalid (not part of the metric).
   *
   * @since 0.21
   */
  public abstract void clear();

  /**
   * Returns the no-op implementation of the {@code LongCumulative}.
   *
   * @return the no-op implementation of the {@code LongCumulative}.
   * @since 0.21
   */
  static LongCumulative newNoopLongCumulative(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    return NoopLongCumulative.create(name, description, unit, labelKeys);
  }

  /**
   * The value of a single point in the Cumulative.TimeSeries.
   *
   * @since 0.21
   */
  public abstract static class LongPoint {

    /**
     * Adds the given value to the current value. The values cannot be negative.
     *
     * @param delta the value to add
     * @since 0.21
     */
    public abstract void add(long delta);
  }

  /** No-op implementations of LongCumulative class. */
  private static final class NoopLongCumulative extends LongCumulative {
    private final int labelKeysSize;

    static NoopLongCumulative create(
        String name, String description, String unit, List<LabelKey> labelKeys) {
      return new NoopLongCumulative(name, description, unit, labelKeys);
    }

    /** Creates a new {@code NoopLongPoint}. */
    NoopLongCumulative(String name, String description, String unit, List<LabelKey> labelKeys) {
      labelKeysSize = labelKeys.size();
    }

    @Override
    public NoopLongPoint getOrCreateTimeSeries(List<LabelValue> labelValues) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelValues, "labelValues"), "labelValue");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      return NoopLongPoint.INSTANCE;
    }

    @Override
    public NoopLongPoint getDefaultTimeSeries() {
      return NoopLongPoint.INSTANCE;
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void clear() {}

    /** No-op implementations of LongPoint class. */
    private static final class NoopLongPoint extends LongPoint {
      private static final NoopLongPoint INSTANCE = new NoopLongPoint();

      private NoopLongPoint() {}

      @Override
      public void add(long delta) {}
    }
  }
}
