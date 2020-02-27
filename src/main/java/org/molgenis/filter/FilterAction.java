package org.molgenis.filter;

import static java.util.Objects.requireNonNull;

public class FilterAction {

  private final FilterState state;
  private String nextStep;
  private String label;

  public FilterAction(FilterState state, String nextStep, String label) {
    this.state = requireNonNull(state);
    this.nextStep = nextStep;
    this.label = label;
  }

  public FilterState getState() {
    return state;
  }

  public String getNextStep() {
    return nextStep;
  }

  public String getLabel() {
    return label;
  }
}
