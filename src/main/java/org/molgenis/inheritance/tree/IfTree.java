package org.molgenis.inheritance.tree;

import static org.molgenis.inheritance.InheritanceFilters.MVL;
import static org.molgenis.inheritance.InheritanceFilters.SPLICE;
import static org.molgenis.inheritance.InheritanceFilters.TRUNCATING;

import java.util.Map;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterAction;
import org.molgenis.filter.FilterState;
import org.molgenis.filter.FilterStep;

public class IfTree {
  public static final String IF1 = "if1";
  public static final String IF2 = "if2";
  public static final String IF3 = "if3";

  static void addIfTree(Map<String, Filter> filters, Map<String, FilterStep> result) {
    result.put(IF1, new FilterStep(IF1, filters.get(TRUNCATING),
        new FilterAction(FilterState.KEEP, null, "IF1:Truncating"),
        new FilterAction(FilterState.NEXT, IF2, null)));
    result.put(IF2, new FilterStep(IF2, filters.get(MVL),
        new FilterAction(FilterState.KEEP, null, "IF1:MVLorVKGL"),
        new FilterAction(FilterState.NEXT, IF3, null)));
    result.put(IF3, new FilterStep(IF3, filters.get(SPLICE),
        new FilterAction(FilterState.KEEP, null, "IF1:SpliceVar"),
        new FilterAction(FilterState.REMOVE, null, null)));
  }
}
