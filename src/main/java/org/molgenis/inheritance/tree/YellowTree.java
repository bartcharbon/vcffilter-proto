package org.molgenis.inheritance.tree;

import static org.molgenis.inheritance.InheritanceFilters.DE_NOVO_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.FATHER_AFFECTED;
import static org.molgenis.inheritance.InheritanceFilters.FATHER_HAS_VARIANT;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_AD_AR_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_AD_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_AR_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_UNKNOWN_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_XL_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.NON_PENETRANCE;
import static org.molgenis.inheritance.InheritanceFilters.PATIENT_HMZ_ALT;
import static org.molgenis.inheritance.InheritanceFilters.TWICE_IN_A_GENE_FILTER;
import static org.molgenis.inheritance.tree.IfTree.IF1;
import static org.molgenis.inheritance.tree.IfTree.addIfTree;

import java.util.HashMap;
import java.util.Map;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterAction;
import org.molgenis.filter.FilterState;
import org.molgenis.filter.FilterStep;

public class YellowTree {
  private static final String YELLOW1 = "yellow1";
  private static final String YELLOW2 = "yellow2";
  private static final String YELLOW3 = "yellow3";
  private static final String YELLOW4 = "yellow4";
  private static final String YELLOW5 = "yellow5";
  private static final String YELLOW6 = "yellow6";
  private static final String YELLOW7 = "yellow7";
  private static final String YELLOW8 = "yellow8";
  private static final String YELLOW9 = "yellow9";
  private static final String YELLOW10 = "yellow10";
  private static final String YELLOW11 = "yellow11";
  private static final String YELLOW13 = "yellow13";
  private static final String YELLOW14 = "yellow14";
  private static final String YELLOW15 = "yellow15";
  private static final String YELLOW16 = "yellow16";
  public static Map<String, FilterStep> getYellowTree(Map<String, Filter> filters) {
    Map<String, FilterStep> result = new HashMap<>();
    result
        .put(YELLOW1, new FilterStep(YELLOW1, filters.get(INHERITANCE_AD_FILTER),
            new FilterAction(FilterState.NEXT, YELLOW6, null),
            new FilterAction(FilterState.NEXT, YELLOW2, null)));
    result
        .put(YELLOW2, new FilterStep(YELLOW2, filters.get(INHERITANCE_AR_FILTER),
            new FilterAction(FilterState.NEXT, YELLOW7, null),
            new FilterAction(FilterState.NEXT, YELLOW3, null)));
    result
        .put(YELLOW3, new FilterStep(YELLOW3, filters.get(INHERITANCE_UNKNOWN_FILTER),
            new FilterAction(FilterState.NEXT, YELLOW7, null),
            new FilterAction(FilterState.NEXT, YELLOW4, null)));
    result
        .put(YELLOW4, new FilterStep(YELLOW4, filters.get(INHERITANCE_XL_FILTER),
            new FilterAction(FilterState.NEXT, YELLOW5, null),
            new FilterAction(FilterState.NEXT, YELLOW8, null)));
    result
        .put(YELLOW5, new FilterStep(YELLOW5, filters.get(INHERITANCE_AD_AR_FILTER),
            new FilterAction(FilterState.NEXT, YELLOW9, null),
            new FilterAction(FilterState.NEXT, IF1, null)));
    result
        .put(YELLOW6, new FilterStep(YELLOW6, filters.get(NON_PENETRANCE),
            new FilterAction(FilterState.KEEP, null, null),
            new FilterAction(FilterState.NEXT, YELLOW10, null)));
    result
        .put(YELLOW7, new FilterStep(YELLOW7, filters.get(PATIENT_HMZ_ALT),
            new FilterAction(FilterState.KEEP, null, null),
            new FilterAction(FilterState.NEXT, YELLOW11, null)));
    result
        .put(YELLOW8, new FilterStep(YELLOW8, filters.get(FATHER_AFFECTED),
            new FilterAction(FilterState.NEXT, YELLOW13, null),
            new FilterAction(FilterState.KEEP, null, null)));
    result
        .put(YELLOW9, new FilterStep(YELLOW9, filters.get(NON_PENETRANCE),
            new FilterAction(FilterState.KEEP, null, null),
            new FilterAction(FilterState.NEXT, YELLOW14, null)));
    result
        .put(YELLOW10, new FilterStep(YELLOW10, filters.get(DE_NOVO_FILTER),
            new FilterAction(FilterState.KEEP, null, null),
            new FilterAction(FilterState.NEXT, IF1, null)));
    result
        .put(YELLOW11, new FilterStep(YELLOW11, filters.get(TWICE_IN_A_GENE_FILTER),
            new FilterAction(FilterState.NEXT, YELLOW13, null),
            new FilterAction(FilterState.NEXT, IF1, null)));
    result
        .put(YELLOW13, new FilterStep(YELLOW13, filters.get(FATHER_HAS_VARIANT),
            new FilterAction(FilterState.NEXT, IF1, null),
            new FilterAction(FilterState.KEEP, null,null)));
    result
        .put(YELLOW14, new FilterStep(YELLOW14, filters.get(DE_NOVO_FILTER),
            new FilterAction(FilterState.KEEP, null, null),
            new FilterAction(FilterState.NEXT, YELLOW15, null)));
    result
        .put(YELLOW15, new FilterStep(YELLOW15, filters.get(PATIENT_HMZ_ALT),
            new FilterAction(FilterState.KEEP, null, null),
            new FilterAction(FilterState.NEXT, YELLOW16, null)));
    result
        .put(YELLOW16, new FilterStep(YELLOW16, filters.get(TWICE_IN_A_GENE_FILTER),
            new FilterAction(FilterState.KEEP, null, null),
            new FilterAction(FilterState.NEXT, IF1, null)));
    addIfTree(filters,result);
    return result;
  }
}
