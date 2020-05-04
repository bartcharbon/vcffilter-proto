package org.molgenis.filter;

import static java.util.Objects.requireNonNull;

import java.util.Map;

public class FilterConfig {
  private final boolean isLogicFiltering;
  private final Map<String, FilterStep> filters;
  private final boolean isLogRoute;

  public FilterConfig(boolean isLogicFiltering,
      Map<String, FilterStep> filters, boolean isLogRoute) {
    this.isLogicFiltering = requireNonNull(isLogicFiltering);
    this.filters = requireNonNull(filters);
    this.isLogRoute = requireNonNull(isLogRoute);
  }

  public boolean isLogicFiltering() {
    return isLogicFiltering;
  }

  public Map<String, FilterStep> getFilters() {
    return filters;
  }

  public boolean isLogRoute() {
    return isLogRoute;
  }
}
