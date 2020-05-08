package org.molgenis.filter;

import static java.util.Objects.requireNonNull;

import java.util.Map;

public class FilterConfig {
  private final boolean isLogicFiltering;
  private final Map<String, FilterStep> filters;

  public FilterConfig(boolean isLogicFiltering,
      Map<String, FilterStep> filters) {
    this.isLogicFiltering = requireNonNull(isLogicFiltering);
    this.filters = requireNonNull(filters);
  }

  public boolean isLogicFiltering() {
    return isLogicFiltering;
  }

  public Map<String, FilterStep> getFilters() {
    return filters;
  }
}
