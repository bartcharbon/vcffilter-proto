package org.molgenis.inheritance.tree;

import static org.molgenis.inheritance.InheritanceFilters.AFFECTED_PARENT_HAS_VARIANT;
import static org.molgenis.inheritance.InheritanceFilters.COMPOUND_FILTER;
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
import static org.molgenis.inheritance.InheritanceFilters.UNAFFECTED_PARENT_HMZ_ALT;
import static org.molgenis.inheritance.tree.IfTree.IF1;
import static org.molgenis.inheritance.tree.IfTree.addIfTree;

import java.util.LinkedHashMap;
import java.util.Map;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterAction;
import org.molgenis.filter.FilterState;
import org.molgenis.filter.FilterStep;

public class RedTree {
  private static final String RED_1 = "AD[red1]";
  private static final String RED_2 = "AR[red2]";
  private static final String RED_3 = "Unknown[red3]";
  private static final String RED_4 = "XL[red4]";
  private static final String RED_5 = "AD/AR[red5]";
  private static final String RED_6 = "DeNovo[red6]";
  private static final String RED_7 = "Hmz[red7]";
  private static final String RED_8 = "UnaffectedParentHmz[red8]";
  private static final String RED_9 = "FatherAffected[red9]";
  private static final String RED_10 = "DeNovo[red10]";
  private static final String RED_11 = "NonPenetrance[red11]";
  private static final String RED_12 = "Compound[red12]";
  private static final String RED_13 = "FatherHasVariant[red13]";
  private static final String RED_14 = "NonPenetrance[red14]";
  private static final String RED_15 = "AffectedParentHasVar[red15]";
  private static final String RED_16 = "AffectedParentHasVar[red16]";
  private static final String RED_17 = "Hmz[red17]";
  private static final String RED_18 = "UnaffectedParentHmz[red18]";
  private static final String RED_19 = "Compound[red19]";
  public static Map<String, FilterStep> getRedTree(Map<String, Filter> filters) {
    Map<String, FilterStep> result = new LinkedHashMap<>();
    result
        .put(RED_1, new FilterStep(RED_1, filters.get(INHERITANCE_AD_FILTER),
            new FilterAction(FilterState.NEXT, RED_6, null),
            new FilterAction(FilterState.NEXT, RED_2, null)));
    result
        .put(RED_2, new FilterStep(RED_2, filters.get(INHERITANCE_AR_FILTER),
            new FilterAction(FilterState.NEXT, RED_7, null),
            new FilterAction(FilterState.NEXT, RED_3, null)));
    result
        .put(RED_3, new FilterStep(RED_3, filters.get(INHERITANCE_UNKNOWN_FILTER),
            new FilterAction(FilterState.NEXT, RED_7, null),
            new FilterAction(FilterState.NEXT, RED_4, null)));
    result
        .put(RED_4, new FilterStep(RED_4, filters.get(INHERITANCE_XL_FILTER),
            new FilterAction(FilterState.NEXT, RED_9, null),
            new FilterAction(FilterState.NEXT, RED_5, null)));
    result
        .put(RED_5, new FilterStep(RED_5, filters.get(INHERITANCE_AD_AR_FILTER),
            new FilterAction(FilterState.NEXT, RED_10, null),
            new FilterAction(FilterState.NEXT, IF1 , null)));
    result
        .put(RED_6, new FilterStep(RED_6, filters.get(DE_NOVO_FILTER),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, RED_11, null)));
    result
        .put(RED_7, new FilterStep(RED_7, filters.get(PATIENT_HMZ_ALT),
            new FilterAction(FilterState.NEXT, RED_8, null),
            new FilterAction(FilterState.NEXT, RED_12, null)));
    result
        .put(RED_8, new FilterStep(RED_8, filters.get(UNAFFECTED_PARENT_HMZ_ALT),//FIXME
            new FilterAction(FilterState.NEXT, IF1 , null),
            new FilterAction(FilterState.KEEP, null , null)));
    result
        .put(RED_9, new FilterStep(RED_9, filters.get(FATHER_AFFECTED),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, RED_13, null)));
    result
        .put(RED_10, new FilterStep(RED_10, filters.get(DE_NOVO_FILTER),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, RED_14, null)));
    result
        .put(RED_11, new FilterStep(RED_11, filters.get(NON_PENETRANCE),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, RED_16, null)));
    result
        .put(RED_12, new FilterStep(RED_12, filters.get(COMPOUND_FILTER),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, IF1 , null)));
    result
        .put(RED_13, new FilterStep(RED_13, filters.get(FATHER_HAS_VARIANT),
            new FilterAction(FilterState.NEXT, IF1 , null),
            new FilterAction(FilterState.KEEP, null , null)));
    result
        .put(RED_14, new FilterStep(RED_14, filters.get(NON_PENETRANCE),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, RED_15, null)));
    result
        .put(RED_15, new FilterStep(RED_15, filters.get(AFFECTED_PARENT_HAS_VARIANT),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, RED_17, null)));
    result
        .put(RED_16, new FilterStep(RED_16, filters.get(AFFECTED_PARENT_HAS_VARIANT),
            new FilterAction(FilterState.KEEP, null, null),
            new FilterAction(FilterState.NEXT, IF1 , null)));
    result
        .put(RED_17, new FilterStep(RED_17, filters.get(PATIENT_HMZ_ALT),
            new FilterAction(FilterState.NEXT, RED_18, null),
            new FilterAction(FilterState.NEXT, RED_19, null)));
    result
        .put(RED_18, new FilterStep(RED_18, filters.get(UNAFFECTED_PARENT_HMZ_ALT),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, IF1 , null)));
    result
        .put(RED_19, new FilterStep(RED_19, filters.get(COMPOUND_FILTER),
            new FilterAction(FilterState.KEEP, null, null),
            new FilterAction(FilterState.NEXT, IF1 , null)));
    addIfTree(filters, result);
    return result;
  }
}
