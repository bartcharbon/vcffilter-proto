package org.molgenis.filter;

import static java.util.Objects.requireNonNull;

public class FilterStep {
  private final String key;
  private final FilterAction trueAction;
  private final FilterAction falseAction;
  private final FilterAction missingAction;
  private final Filter filter;

  public FilterStep(String key,
      Filter filter, FilterAction trueAction, FilterAction falseAction,
      FilterAction missingAction) {
    this.key = requireNonNull(key);
    this.trueAction = requireNonNull(trueAction);
    this.falseAction = requireNonNull(falseAction);
    this.filter = requireNonNull(filter);
    this.missingAction = requireNonNull(missingAction);
  }

  public FilterStep(String key,
      Filter filter, FilterAction trueAction, FilterAction falseAction) {
    this.key = requireNonNull(key);
    this.trueAction = requireNonNull(trueAction);
    this.falseAction = requireNonNull(falseAction);
    this.filter = requireNonNull(filter);
    this.missingAction = requireNonNull(falseAction);
  }

  public String getKey() {
    return key;
  }

  public FilterAction getAction(FilterResultEnum result) {
    if(result == FilterResultEnum.TRUE){
      return trueAction;
    }else if(result == FilterResultEnum.FALSE) {
      return falseAction;
    }else if(result == FilterResultEnum.MISSING){
      return missingAction;
    }
    throw new RuntimeException("Unknown result enum value");
  }

  public Filter getFilter() {
    return filter;
  }
}
