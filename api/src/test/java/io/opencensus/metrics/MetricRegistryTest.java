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

package io.opencensus.metrics;

import static com.google.common.truth.Truth.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MetricRegistry}. */
@RunWith(JUnit4.class)
public class MetricRegistryTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "test_name";
  private static final String NAME_2 = "test_name2";
  private static final String NAME_3 = "test_name3";
  private static final String NAME_4 = "test_name4";
  private static final String DESCRIPTION = "test_description";
  private static final String UNIT = "1";
  private static final LabelKey LABEL_KEY = LabelKey.create("test_key", "test key description");
  private static final List<LabelKey> LABEL_KEYS = Collections.singletonList(LABEL_KEY);
  private static final LabelValue LABEL_VALUE = LabelValue.create("test_value");
  private static final LabelValue LABEL_VALUE_2 = LabelValue.create("test_value_2");
  private static final List<LabelValue> LABEL_VALUES = Collections.singletonList(LABEL_VALUE);
  private static final Map<LabelKey, LabelValue> CONSTANT_LABELS =
      Collections.singletonMap(
          LabelKey.create("test_key_1", "test key description"), LABEL_VALUE_2);
  private static final MetricOptions METRIC_OPTIONS =
      MetricOptions.builder()
          .setDescription(DESCRIPTION)
          .setUnit(UNIT)
          .setLabelKeys(LABEL_KEYS)
          .setConstantLabels(CONSTANT_LABELS)
          .build();
  private final MetricRegistry metricRegistry =
      MetricsComponent.newNoopMetricsComponent().getMetricRegistry();

  @Test
  public void noopAddLongGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addLongGauge(null, METRIC_OPTIONS);
  }

  @Test
  public void noopAddDoubleGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addDoubleGauge(null, METRIC_OPTIONS);
  }

  @Test
  public void noopAddDerivedLongGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addDerivedLongGauge(null, METRIC_OPTIONS);
  }

  @Test
  public void noopAddDerivedDoubleGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addDerivedDoubleGauge(null, METRIC_OPTIONS);
  }

  @Test
  public void noopAddLongCumulative_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addLongCumulative(null, METRIC_OPTIONS);
  }

  @Test
  public void noopAddDoubleCumulative_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addDoubleCumulative(null, METRIC_OPTIONS);
  }

  @Test
  public void noopAddDerivedLongCumulative_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addDerivedLongCumulative(null, METRIC_OPTIONS);
  }

  @Test
  public void noopAddDerivedDoubleCumulative_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addDerivedDoubleCumulative(null, METRIC_OPTIONS);
  }

  @Test
  public void noopSameAs() {
    LongGauge longGauge = metricRegistry.addLongGauge(NAME, METRIC_OPTIONS);
    assertThat(longGauge.getDefaultTimeSeries()).isSameInstanceAs(longGauge.getDefaultTimeSeries());
    assertThat(longGauge.getDefaultTimeSeries())
        .isSameInstanceAs(longGauge.getOrCreateTimeSeries(LABEL_VALUES));

    DoubleGauge doubleGauge = metricRegistry.addDoubleGauge(NAME_2, METRIC_OPTIONS);
    assertThat(doubleGauge.getDefaultTimeSeries())
        .isSameInstanceAs(doubleGauge.getDefaultTimeSeries());
    assertThat(doubleGauge.getDefaultTimeSeries())
        .isSameInstanceAs(doubleGauge.getOrCreateTimeSeries(LABEL_VALUES));

    LongCumulative longCumulative = metricRegistry.addLongCumulative(NAME, METRIC_OPTIONS);
    assertThat(longCumulative.getDefaultTimeSeries())
        .isSameInstanceAs(longCumulative.getDefaultTimeSeries());
    assertThat(longCumulative.getDefaultTimeSeries())
        .isSameInstanceAs(longCumulative.getOrCreateTimeSeries(LABEL_VALUES));

    DoubleCumulative doubleCumulative = metricRegistry.addDoubleCumulative(NAME_2, METRIC_OPTIONS);
    assertThat(doubleCumulative.getDefaultTimeSeries())
        .isSameInstanceAs(doubleCumulative.getDefaultTimeSeries());
    assertThat(doubleCumulative.getDefaultTimeSeries())
        .isSameInstanceAs(doubleCumulative.getOrCreateTimeSeries(LABEL_VALUES));
  }

  @Test
  public void noopInstanceOf() {
    assertThat(metricRegistry.addLongGauge(NAME, METRIC_OPTIONS))
        .isInstanceOf(LongGauge.newNoopLongGauge(NAME, DESCRIPTION, UNIT, LABEL_KEYS).getClass());
    assertThat(metricRegistry.addDoubleGauge(NAME_2, METRIC_OPTIONS))
        .isInstanceOf(
            DoubleGauge.newNoopDoubleGauge(NAME_2, DESCRIPTION, UNIT, LABEL_KEYS).getClass());
    assertThat(metricRegistry.addDerivedLongGauge(NAME_3, METRIC_OPTIONS))
        .isInstanceOf(
            DerivedLongGauge.newNoopDerivedLongGauge(NAME_3, DESCRIPTION, UNIT, LABEL_KEYS)
                .getClass());
    assertThat(metricRegistry.addDerivedDoubleGauge(NAME_4, METRIC_OPTIONS))
        .isInstanceOf(
            DerivedDoubleGauge.newNoopDerivedDoubleGauge(NAME_4, DESCRIPTION, UNIT, LABEL_KEYS)
                .getClass());

    assertThat(metricRegistry.addLongCumulative(NAME, METRIC_OPTIONS))
        .isInstanceOf(
            LongCumulative.newNoopLongCumulative(NAME, DESCRIPTION, UNIT, LABEL_KEYS).getClass());
    assertThat(metricRegistry.addDoubleCumulative(NAME_2, METRIC_OPTIONS))
        .isInstanceOf(
            DoubleCumulative.newNoopDoubleCumulative(NAME_2, DESCRIPTION, UNIT, LABEL_KEYS)
                .getClass());
    assertThat(metricRegistry.addDerivedLongCumulative(NAME_3, METRIC_OPTIONS))
        .isInstanceOf(
            DerivedLongCumulative.newNoopDerivedLongCumulative(
                    NAME_3, DESCRIPTION, UNIT, LABEL_KEYS)
                .getClass());
    assertThat(metricRegistry.addDerivedDoubleCumulative(NAME_4, METRIC_OPTIONS))
        .isInstanceOf(
            DerivedDoubleCumulative.newNoopDerivedDoubleCumulative(
                    NAME_4, DESCRIPTION, UNIT, LABEL_KEYS)
                .getClass());
  }
}
