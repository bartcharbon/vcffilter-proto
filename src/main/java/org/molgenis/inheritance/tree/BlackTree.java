package org.molgenis.inheritance.tree;

import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_AD_AR_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_AD_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_AR_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_UNKNOWN_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_XL_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.PATIENT_HMZ_ALT;
import static org.molgenis.inheritance.InheritanceFilters.TWICE_IN_A_GENE_FILTER;
import static org.molgenis.inheritance.tree.IfTree.IF1;
import static org.molgenis.inheritance.tree.IfTree.addIfTree;

import java.util.LinkedHashMap;
import java.util.Map;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterAction;
import org.molgenis.filter.FilterState;
import org.molgenis.filter.FilterStep;

public class BlackTree {
  private static final String BLACK_1 = "AD[black1]";
  private static final String BLACK_2 = "AR[black2]";
  private static final String BLACK_3 = "Unknown[black3]";
  private static final String BLACK_4 = "AD/AR[black4]";
  private static final String BLACK_5 = "XL[black5]";
  private static final String BLACK_6 = "TwiceInGene[black6]";
  private static final String BLACK_7 = "HMZ[black7]";

  public static Map<String, FilterStep> getBlackTree(Map<String, Filter> filters) {
    Map<String, FilterStep> result = new LinkedHashMap<>();
    result
        .put(BLACK_1, new FilterStep(BLACK_1, filters.get(INHERITANCE_AD_FILTER),
            new FilterAction(FilterState.KEEP, null, null),
            new FilterAction(FilterState.NEXT, BLACK_2, null)));
    result.put(BLACK_2, new FilterStep(BLACK_2, filters.get(INHERITANCE_AR_FILTER),
        new FilterAction(FilterState.NEXT, BLACK_6, null),
        new FilterAction(FilterState.NEXT, BLACK_3, null)));
    result.put(BLACK_3, new FilterStep(BLACK_3, filters.get(INHERITANCE_UNKNOWN_FILTER),
        new FilterAction(FilterState.NEXT, BLACK_6, null),
        new FilterAction(FilterState.NEXT, BLACK_4, null)));
    result.put(BLACK_4, new FilterStep(BLACK_4, filters.get(INHERITANCE_AD_AR_FILTER),
        new FilterAction(FilterState.NEXT, BLACK_6, null),
        new FilterAction(FilterState.NEXT,  BLACK_5, null)));
    result.put(BLACK_5, new FilterStep(BLACK_5, filters.get(INHERITANCE_XL_FILTER),
        new FilterAction(FilterState.KEEP,  null,null),
        new FilterAction(FilterState.NEXT,  IF1, null)));
    result.put(BLACK_6, new FilterStep(BLACK_6, filters.get(TWICE_IN_A_GENE_FILTER),
        new FilterAction(FilterState.KEEP, null, null),
        new FilterAction(FilterState.NEXT, BLACK_7, null)));
    result.put(BLACK_7, new FilterStep(BLACK_7, filters.get(PATIENT_HMZ_ALT),
        new FilterAction(FilterState.KEEP, null, null),
        new FilterAction(FilterState.NEXT, IF1, null)));
    addIfTree(filters, result);

    return result;
  }
}
