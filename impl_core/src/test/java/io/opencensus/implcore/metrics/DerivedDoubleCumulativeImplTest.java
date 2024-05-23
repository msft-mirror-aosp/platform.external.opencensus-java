/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.implcore.metrics;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.common.ToDoubleFunction;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.TimeSeries;
import io.opencensus.metrics.export.Value;
import io.opencensus.testing.common.TestClock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DerivedDoubleCumulativeImpl}. */
@RunWith(JUnit4.class)
public class DerivedDoubleCumulativeImplTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String METRIC_NAME = "name";
  private static final String METRIC_DESCRIPTION = "description";
  private static final String METRIC_UNIT = "1";
  private static final List<LabelKey> LABEL_KEY =
      Collections.singletonList(LabelKey.create("key", "key description"));
  private static final List<LabelValue> LABEL_VALUES =
      Collections.singletonList(LabelValue.create("value"));
  private static final List<LabelValue> LABEL_VALUES_1 =
      Collections.singletonList(LabelValue.create("value1"));
  private static final Timestamp START_TIME = Timestamp.create(60, 0);
  private static final Duration ONE_MINUTE = Duration.create(60, 0);
  private static final Map<LabelKey, LabelValue> EMPTY_CONSTANT_LABELS =
      Collections.<LabelKey, LabelValue>emptyMap();

  private final TestClock testClock = TestClock.create();
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.CUMULATIVE_DOUBLE, LABEL_KEY);

  private final DerivedDoubleCumulativeImpl derivedDoubleCumulative =
      new DerivedDoubleCumulativeImpl(
          METRIC_NAME,
          METRIC_DESCRIPTION,
          METRIC_UNIT,
          LABEL_KEY,
          EMPTY_CONSTANT_LABELS,
          START_TIME);

  // helper class
  public static class QueueManager {
    public double size() {
      return 2.5;
    }
  }

  private static final ToDoubleFunction<Object> doubleFunction =
      new ToDoubleFunction<Object>() {
        @Override
        public double applyAsDouble(Object value) {
          return 5.5;
        }
      };
  private static final ToDoubleFunction<Object> negativeDoubleFunction =
      new ToDoubleFunction<Object>() {
        @Override
        public double applyAsDouble(Object value) {
          return -200.5;
        }
      };
  private static final ToDoubleFunction<QueueManager> queueManagerFunction =
      new ToDoubleFunction<QueueManager>() {
        @Override
        public double applyAsDouble(QueueManager queue) {
          return queue.size();
        }
      };

  @Before
  public void setUp() {
    testClock.setTime(START_TIME);
  }

  @Test
  public void createTimeSeries_WithNullLabelValues() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    derivedDoubleCumulative.createTimeSeries(null, null, doubleFunction);
  }

  @Test
  public void createTimeSeries_WithNullElement() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues = Arrays.asList(LabelValue.create("value1"), null);
    DerivedDoubleCumulativeImpl derivedDoubleCumulative =
        new DerivedDoubleCumulativeImpl(
            METRIC_NAME,
            METRIC_DESCRIPTION,
            METRIC_UNIT,
            labelKeys,
            EMPTY_CONSTANT_LABELS,
            START_TIME);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValue");
    derivedDoubleCumulative.createTimeSeries(labelValues, null, doubleFunction);
  }

  @Test
  public void createTimeSeries_WithInvalidLabelSize() {
    List<LabelValue> labelValues =
        Arrays.asList(LabelValue.create("value1"), LabelValue.create("value2"));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Label Keys and Label Values don't have same size.");
    derivedDoubleCumulative.createTimeSeries(labelValues, null, doubleFunction);
  }

  @Test
  public void createTimeSeries_WithNullFunction() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("function");
    derivedDoubleCumulative.createTimeSeries(LABEL_VALUES, null, null);
  }

  @Test
  public void createTimeSeries_WithObjFunction() {
    derivedDoubleCumulative.createTimeSeries(
        LABEL_VALUES, new QueueManager(), queueManagerFunction);
    testClock.advanceTime(ONE_MINUTE);
    Timestamp endTime = testClock.now();
    Metric metric = derivedDoubleCumulative.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric)
        .isEqualTo(
            Metric.createWithOneTimeSeries(
                METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.doubleValue(2.5), endTime), START_TIME)));
  }

  @Test
  public void createTimeSeries_WithSameLabel() {
    derivedDoubleCumulative.createTimeSeries(
        LABEL_VALUES, new QueueManager(), queueManagerFunction);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("A different time series with the same labels already exists.");
    derivedDoubleCumulative.createTimeSeries(LABEL_VALUES, null, queueManagerFunction);
  }

  @Test
  public void addTimeSeries_WithNullObj() {
    derivedDoubleCumulative.createTimeSeries(LABEL_VALUES, null, doubleFunction);
    testClock.advanceTime(ONE_MINUTE);
    Timestamp endTime = testClock.now();
    Metric metric = derivedDoubleCumulative.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric)
        .isEqualTo(
            Metric.createWithOneTimeSeries(
                METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.doubleValue(5.5), endTime), START_TIME)));
  }

  @Test
  public void addTimeSeries_IgnoreNegativeValue() {
    derivedDoubleCumulative.createTimeSeries(LABEL_VALUES, null, negativeDoubleFunction);
    testClock.advanceTime(ONE_MINUTE);
    Timestamp endTime = testClock.now();
    Metric metric = derivedDoubleCumulative.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric)
        .isEqualTo(
            Metric.createWithOneTimeSeries(
                METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.doubleValue(0), endTime), START_TIME)));
  }

  @Test
  public void withConstantLabels() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues =
        Arrays.asList(LabelValue.create("value1"), LabelValue.create("value2"));
    LabelKey constantKey = LabelKey.create("constant_key", "desc");
    LabelValue constantValue = LabelValue.create("constant_value");
    Map<LabelKey, LabelValue> constantLabels =
        Collections.<LabelKey, LabelValue>singletonMap(constantKey, constantValue);
    DerivedDoubleCumulativeImpl derivedDoubleCumulative2 =
        new DerivedDoubleCumulativeImpl(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys, constantLabels, START_TIME);

    derivedDoubleCumulative2.createTimeSeries(
        labelValues, new QueueManager(), queueManagerFunction);

    testClock.advanceTime(ONE_MINUTE);
    Timestamp endTime = testClock.now();
    List<LabelKey> allKeys = new ArrayList<>(labelKeys);
    allKeys.add(constantKey);
    MetricDescriptor expectedDescriptor =
        MetricDescriptor.create(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.CUMULATIVE_DOUBLE, allKeys);

    List<LabelValue> allValues = new ArrayList<>(labelValues);
    allValues.add(constantValue);
    TimeSeries expectedTimeSeries =
        TimeSeries.createWithOnePoint(
            allValues, Point.create(Value.doubleValue(2.5), endTime), START_TIME);

    Metric metric = derivedDoubleCumulative2.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(expectedDescriptor);
    assertThat(metric.getTimeSeriesList()).containsExactly(expectedTimeSeries);

    derivedDoubleCumulative2.removeTimeSeries(labelValues);
    Metric metric2 = derivedDoubleCumulative2.getMetric(testClock);
    assertThat(metric2).isNull();
  }

  @Test
  public void removeTimeSeries() {
    derivedDoubleCumulative.createTimeSeries(LABEL_VALUES, null, doubleFunction);
    Metric metric = derivedDoubleCumulative.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(1);
    derivedDoubleCumulative.removeTimeSeries(LABEL_VALUES);
    assertThat(derivedDoubleCumulative.getMetric(testClock)).isNull();
  }

  @Test
  public void removeTimeSeries_WithNullLabelValues() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    derivedDoubleCumulative.removeTimeSeries(null);
  }

  @Test
  public void multipleMetrics_GetMetric() {
    derivedDoubleCumulative.createTimeSeries(LABEL_VALUES, null, doubleFunction);
    derivedDoubleCumulative.createTimeSeries(
        LABEL_VALUES_1, new QueueManager(), queueManagerFunction);
    List<TimeSeries> expectedTimeSeriesList = new ArrayList<TimeSeries>();
    testClock.advanceTime(ONE_MINUTE);
    Timestamp endTime = testClock.now();
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            LABEL_VALUES, Point.create(Value.doubleValue(5.5), endTime), START_TIME));
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            LABEL_VALUES_1, Point.create(Value.doubleValue(2.5), endTime), START_TIME));
    Metric metric = derivedDoubleCumulative.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(2);
    assertThat(metric.getTimeSeriesList()).containsExactlyElementsIn(expectedTimeSeriesList);
    assertThat(metric.getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(1);
    assertThat(metric.getTimeSeriesList().get(0).getLabelValues().get(0))
        .isEqualTo(LabelValue.create("value"));
    assertThat(metric.getTimeSeriesList().get(1).getLabelValues().size()).isEqualTo(1);
    assertThat(metric.getTimeSeriesList().get(1).getLabelValues().get(0))
        .isEqualTo(LabelValue.create("value1"));
  }

  @Test
  public void clear() {
    derivedDoubleCumulative.createTimeSeries(LABEL_VALUES, null, doubleFunction);
    derivedDoubleCumulative.createTimeSeries(
        LABEL_VALUES_1, new QueueManager(), queueManagerFunction);
    Metric metric = derivedDoubleCumulative.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(2);
    derivedDoubleCumulative.clear();
    assertThat(derivedDoubleCumulative.getMetric(testClock)).isNull();
  }

  @Test
  public void empty_GetMetrics() {
    assertThat(derivedDoubleCumulative.getMetric(testClock)).isNull();
  }
}
