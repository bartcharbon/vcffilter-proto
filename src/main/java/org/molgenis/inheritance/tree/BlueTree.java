package org.molgenis.inheritance.tree;

import static org.molgenis.inheritance.InheritanceFilters.COMPOUND_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.DE_NOVO_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.FATHER_HAS_VARIANT;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_AD_AR_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_AD_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_AR_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_UNKNOWN_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_XL_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.NON_PENETRANCE;
import static org.molgenis.inheritance.InheritanceFilters.PARENT_PRESENT_HMZ_ALT;
import static org.molgenis.inheritance.InheritanceFilters.PATIENT_HMZ_ALT;
import static org.molgenis.inheritance.InheritanceFilters.PATIENT_IS_MALE;
import static org.molgenis.inheritance.tree.IfTree.IF1;
import static org.molgenis.inheritance.tree.IfTree.addIfTree;

import java.util.LinkedHashMap;
import java.util.Map;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterAction;
import org.molgenis.filter.FilterState;
import org.molgenis.filter.FilterStep;

public class BlueTree {
  private static final String BLUE_1 = "AD[blue1]";
  private static final String BLUE_2 = "AR[blue2]";
  private static final String BLUE_3 = "Unknown[blue3]";
  private static final String BLUE_4 = "XL[blue4]";
  private static final String BLUE_5 = "AD/AR[blue5]";
  private static final String BLUE_6 = "DeNovo[blue6]";
  private static final String BLUE_7 = "DeNovo[blue7]";
  private static final String BLUE_8 = "Male[blue8]";
  private static final String BLUE_9 = "DeNovo[blue9]";
  private static final String BLUE_10 = "DeNovo[blue10]";
  private static final String BLUE_11 = "NonPenetrance[blue11]";
  private static final String BLUE_12 = "Hmz[blue12]";
  private static final String BLUE_13 = "FatherHasVariant[blue13]";
  private static final String BLUE_14 = "NonPenetrance[blue14]";
  private static final String BLUE_15 = "ParentHmz[blue15]";
  private static final String BLUE_16 = "Hmz[blue16]";
  private static final String BLUE_17 = "ParentHmz[blue17]";
  private static final String BLUE_18 = "Compound[blue18]";
  private static final String BLUE_19 = "Compound[blue19]";
  public static Map<String, FilterStep> getBlueTree(Map<String, Filter> filters) {
    Map<String, FilterStep> result = new LinkedHashMap<>();
    result
        .put(BLUE_1, new FilterStep(BLUE_1, filters.get(INHERITANCE_AD_FILTER),
            new FilterAction(FilterState.NEXT, BLUE_6, null),
            new FilterAction(FilterState.NEXT, BLUE_2, null)));
    result
        .put(BLUE_2, new FilterStep(BLUE_2, filters.get(INHERITANCE_AR_FILTER),
            new FilterAction(FilterState.NEXT, BLUE_7, null),
            new FilterAction(FilterState.NEXT, BLUE_3, null)));
    result
        .put(BLUE_3, new FilterStep(BLUE_3, filters.get(INHERITANCE_UNKNOWN_FILTER),
            new FilterAction(FilterState.NEXT, BLUE_7, null),
            new FilterAction(FilterState.NEXT, BLUE_4, null)));
    result
        .put(BLUE_4, new FilterStep(BLUE_4, filters.get(INHERITANCE_XL_FILTER),
            new FilterAction(FilterState.NEXT, BLUE_8, null),
            new FilterAction(FilterState.NEXT, BLUE_5, null)));
    result
        .put(BLUE_5, new FilterStep(BLUE_5, filters.get(INHERITANCE_AD_AR_FILTER),
            new FilterAction(FilterState.NEXT, BLUE_10, null),
            new FilterAction(FilterState.NEXT, IF1 , null)));
    result
        .put(BLUE_6, new FilterStep(BLUE_6, filters.get(DE_NOVO_FILTER),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, BLUE_11, null)));
    result
        .put(BLUE_7, new FilterStep(BLUE_7, filters.get(DE_NOVO_FILTER),
            new FilterAction(FilterState.KEEP, null , "de novo"),
            new FilterAction(FilterState.NEXT, BLUE_12, null)));
    result
        .put(BLUE_8, new FilterStep(BLUE_8, filters.get(PATIENT_IS_MALE),
            new FilterAction(FilterState.NEXT, BLUE_13, null),
            new FilterAction(FilterState.NEXT, BLUE_9, null)));
    result
        .put(BLUE_9, new FilterStep(BLUE_9, filters.get(DE_NOVO_FILTER),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, IF1 , null)));
    result
        .put(BLUE_10, new FilterStep(BLUE_10, filters.get(DE_NOVO_FILTER),
            new FilterAction(FilterState.KEEP, null , "de novo"),
            new FilterAction(FilterState.NEXT, BLUE_14, null)));
    result
        .put(BLUE_11, new FilterStep(BLUE_11, filters.get(NON_PENETRANCE),
            new FilterAction(FilterState.KEEP, null, null),
            new FilterAction(FilterState.NEXT, IF1, null)));
    result
        .put(BLUE_12, new FilterStep(BLUE_12, filters.get(PATIENT_HMZ_ALT),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, BLUE_15, null)));
    result
        .put(BLUE_13, new FilterStep(BLUE_13, filters.get(FATHER_HAS_VARIANT),
            new FilterAction(FilterState.NEXT, IF1 , null),
            new FilterAction(FilterState.KEEP, null , null)));
    result
        .put(BLUE_14, new FilterStep(BLUE_14, filters.get(NON_PENETRANCE),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, BLUE_16, null)));
    result
        .put(BLUE_15, new FilterStep(BLUE_15, filters.get(PARENT_PRESENT_HMZ_ALT),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, BLUE_18, null)));
    result
        .put(BLUE_16, new FilterStep(BLUE_16, filters.get(PATIENT_HMZ_ALT),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, BLUE_17, null)));
    result
        .put(BLUE_17, new FilterStep(BLUE_17, filters.get(PATIENT_HMZ_ALT),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, BLUE_19, null)));
    result
        .put(BLUE_18, new FilterStep(BLUE_18, filters.get(COMPOUND_FILTER),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, IF1 , null)));
    result
        .put(BLUE_19, new FilterStep(BLUE_19, filters.get(COMPOUND_FILTER),
            new FilterAction(FilterState.KEEP, null , null),
            new FilterAction(FilterState.NEXT, IF1 , null)));
    addIfTree(filters, result);
    return result;
  }
}
