package org.molgenis.filter;

import static java.util.Objects.requireNonNull;

public class FilterStep {
  private final String key;
  private final FilterAction trueAction;
  private final FilterAction falseAction;
  private final Filter filter;

  public FilterStep(String key,
      Filter filter, FilterAction trueAction, FilterAction falseAction) {
    this.key = requireNonNull(key);
    this.trueAction = requireNonNull(trueAction);
    this.falseAction = requireNonNull(falseAction);
    this.filter = requireNonNull(filter);
  }

  public String getKey() {
    return key;
  }

  public FilterAction getAction(boolean bool) {
    if(bool){
      return trueAction;
    }
    return falseAction;
  }

  public Filter getFilter() {
    return filter;
  }
}
