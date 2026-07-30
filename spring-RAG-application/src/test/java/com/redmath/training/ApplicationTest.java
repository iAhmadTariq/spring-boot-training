package com.redmath.training;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;

class ApplicationTest {

  @Test
  void initSetsUtcAndUsDefaults() {
    TimeZone originalTimeZone = TimeZone.getDefault();
    Locale originalLocale = Locale.getDefault();

    try {
      TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));
      Locale.setDefault(Locale.CANADA_FRENCH);

      Application.init();

      assertThat(TimeZone.getDefault().getID()).isEqualTo("UTC");
      assertThat(Locale.getDefault()).isEqualTo(Locale.US);
    } finally {
      TimeZone.setDefault(originalTimeZone);
      Locale.setDefault(originalLocale);
    }
  }
}
