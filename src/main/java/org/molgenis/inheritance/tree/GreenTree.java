package org.molgenis.inheritance.tree;

import static org.molgenis.inheritance.InheritanceFilters.FATHER_AFFECTED;
import static org.molgenis.inheritance.InheritanceFilters.FATHER_HAS_VARIANT;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_AD_AR_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_AD_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_AR_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_UNKNOWN_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_XL_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.PATIENT_HMZ_ALT;
import static org.molgenis.inheritance.InheritanceFilters.PATIENT_IS_MALE;
import static org.molgenis.inheritance.InheritanceFilters.TWICE_IN_A_GENE_FILTER;
import static org.molgenis.inheritance.tree.IfTree.IF1;
import static org.molgenis.inheritance.tree.IfTree.addIfTree;

import java.util.LinkedHashMap;
import java.util.Map;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterAction;
import org.molgenis.filter.FilterState;
import org.molgenis.filter.FilterStep;

public class GreenTree {
  private static final String GREEN1 = "AD[green1]";
  private static final String GREEN2 = "AR[green2]";
  private static final String GREEN3 = "Unknown[green3]";
  private static final String GREEN4 = "AD/AR[green4]";
  private static final String GREEN5 = "XL[green5]";
  private static final String GREEN6 = "Hmz[green6]";
  private static final String GREEN7 = "Male[green7]";
  private static final String GREEN8 = "TwiceInGene[green8]";
  private static final String GREEN9 = "FatherAffected[green9]";
  private static final String GREEN10 = "FatherHasVariant[green10]";
  public static Map<String, FilterStep> getGreenTree(Map<String, Filter> filters) {
    Map<String, FilterStep> result = new LinkedHashMap<>();
    result
        .put(GREEN1, new FilterStep(GREEN1, filters.get(INHERITANCE_AD_FILTER),
            new FilterAction(FilterState.KEEP, null, null),
            new FilterAction(FilterState.NEXT, GREEN2, null)));
    result
        .put(GREEN2, new FilterStep(GREEN2, filters.get(INHERITANCE_AR_FILTER),
            new FilterAction(FilterState.NEXT, GREEN6, null),
            new FilterAction(FilterState.NEXT, GREEN3, null)));
    result
        .put(GREEN3, new FilterStep(GREEN3, filters.get(INHERITANCE_UNKNOWN_FILTER),
            new FilterAction(FilterState.NEXT, GREEN6, null),
            new FilterAction(FilterState.NEXT,GREEN4 , null)));
    result
        .put(GREEN4, new FilterStep(GREEN4, filters.get(INHERITANCE_AD_AR_FILTER),
            new FilterAction(FilterState.NEXT, GREEN6, null),
            new FilterAction(FilterState.NEXT, GREEN5, null)));
    result
        .put(GREEN5, new FilterStep(GREEN5, filters.get(INHERITANCE_XL_FILTER),
            new FilterAction(FilterState.NEXT, GREEN7, null),
            new FilterAction(FilterState.NEXT, IF1, null)));
    result
        .put(GREEN6, new FilterStep(GREEN6, filters.get(PATIENT_HMZ_ALT),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, GREEN8, null)));
    result
        .put(GREEN7, new FilterStep(GREEN7, filters.get(PATIENT_IS_MALE),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, GREEN9, null)));
    result
        .put(GREEN8, new FilterStep(GREEN8, filters.get(TWICE_IN_A_GENE_FILTER),
            new FilterAction(FilterState.NEXT, IF1 , null),
            new FilterAction(FilterState.KEEP, null, null)));
    result
        .put(GREEN9, new FilterStep(GREEN9, filters.get(FATHER_AFFECTED),
            new FilterAction(FilterState.NEXT, GREEN10 , null),
            new FilterAction(FilterState.KEEP, null, null)));
    result
        .put(GREEN10, new FilterStep(GREEN10, filters.get(FATHER_HAS_VARIANT),
            new FilterAction(FilterState.NEXT, IF1 , null),
            new FilterAction(FilterState.KEEP, null, null)));
    addIfTree(filters,result);
    return result;
  }
}
