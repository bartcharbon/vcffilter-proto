package org.molgenis.inheritance;

import static org.molgenis.inheritance.pedigree.PedigreeUtils.getFather;
import static org.molgenis.inheritance.pedigree.PedigreeUtils.getMother;
import static org.molgenis.vcf.utils.VcfUtils.isHmz;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.molgenis.filter.ComplexFilter;
import org.molgenis.filter.ComplexOperator;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterResult;
import org.molgenis.filter.InfoFilter;
import org.molgenis.filter.SimpleOperator;
import org.molgenis.filter.VepFilter;
import org.molgenis.inheritance.pedigree.Affected;
import org.molgenis.inheritance.pedigree.Pedigree;
import org.molgenis.inheritance.pedigree.PedigreeUtils;
import org.molgenis.inheritance.pedigree.Sex;
import org.molgenis.vcf.VcfRecord;

public class InheritanceFilters {

  public static final String MVL_P_FILTER = "mvlPFilter";
  public static final String MVL_LP_FILTER = "mvlLpFilter";
  public static final String MVL_V_FILTER = "mvlVFilter";
  public static final String VKGL_LP_FILTER = "vkglLPFilter";
  public static final String VKGL_V_FILTER = "vkglVFilter";
  public static final String INHERITANCE_AD_FILTER = "inheritanceAdFilter";
  public static final String INHERITANCE_AR_FILTER = "inheritanceArFilter";
  public static final String INHERITANCE_AD_AR_FILTER = "inheritanceAdArFilter";
  public static final String INHERITANCE_XL_FILTER = "inheritanceXlFilter";
  public static final String INHERITANCE_UNKNOWN_FILTER = "inheritanceUnknownFilter";
  public static final String TWICE_IN_A_GENE_FILTER = "twiceInAGeneFilter";
  public static final String COMPOUND_FILTER = "compoundFilter";
  public static final String DE_NOVO_FILTER = "deNovoFilter";
  public static final String TRUNCATING = "truncating";
  public static final String PATIENT_IS_MALE = "patientIsMale";
  public static final String FATHER_AFFECTED = "fatherAffected";
  public static final String FATHER_HAS_VARIANT = "fatherHasVariant";
  public static final String PATIENT_HMZ_ALT = "patientHmzAlt";
  public static final String UNAFFECTED_PARENT_HMZ_ALT = "unaffectedParentHmzAlt";
  public static final String PARENT_PRESENT_HMZ_ALT = "parentPresentHmzAlt";
  public static final String NON_PENETRANCE = "nonPenetrance";
  public static final String AFFECTED_PARENT_HAS_VARIANT = "affectedParentHasVariant";
  public static final String MVL = "mvl";
  public static final String SPLICE = "splice";
  private final Pedigree pedigree;
  private final boolean nonPenetrance;
  Map<String, Filter> filters = new HashMap<>();

  public InheritanceFilters(Iterable<VcfRecord> records, Pedigree pedigree, String alleleIdx,
      boolean nonPenetrance) {
    this.pedigree = pedigree;
    this.nonPenetrance = nonPenetrance;
    filters.put(MVL_P_FILTER, new InfoFilter("UMCG_MVL", SimpleOperator.EQ, "P"));
    filters.put(MVL_LP_FILTER, new InfoFilter("UMCG_MVL", SimpleOperator.EQ, "LP"));
    filters.put(MVL_V_FILTER, new InfoFilter("UMCG_MVL", SimpleOperator.EQ, "VUS"));
    filters.put(VKGL_LP_FILTER, new InfoFilter("VKGL_CL", SimpleOperator.EQ, "LP"));
    filters.put(VKGL_V_FILTER, new InfoFilter("VKGL_CL", SimpleOperator.EQ, "VUS"));
    filters.put(
        INHERITANCE_AD_FILTER, new InfoFilter("FILTER_LABELS", SimpleOperator.CONTAINS_WORD, "AD"));
    filters.put(
        INHERITANCE_AR_FILTER, new InfoFilter("FILTER_LABELS", SimpleOperator.CONTAINS_WORD, "AR"));
    filters.put(INHERITANCE_AD_AR_FILTER,
        new InfoFilter("FILTER_LABELS", SimpleOperator.CONTAINS_WORD, "AD/AR"));
    //FIXME autosomal R and D
    filters.put(
        INHERITANCE_XL_FILTER, new InfoFilter("FILTER_LABELS", SimpleOperator.CONTAINS_WORD, "XL"));
    filters.put(INHERITANCE_UNKNOWN_FILTER,
        new InfoFilter("FILTER_LABELS", SimpleOperator.CONTAINS_NONE, "AD,AR,XL,AD/AR"));
    filters.put(TWICE_IN_A_GENE_FILTER, new TwiceOneGeneFilter(records, pedigree));
    filters.put(COMPOUND_FILTER, new CompoundFilter(records, pedigree));
    filters.put(DE_NOVO_FILTER, new DeNovoFilter(pedigree, alleleIdx));
    filters.put(TRUNCATING, new VepFilter("HGVSp", SimpleOperator.CONTAINS, "*"));
    filters.put(PATIENT_IS_MALE, getPatientIsMaleFilter());
    filters.put(FATHER_AFFECTED, getFatherAffectedFilter());
    filters.put(FATHER_HAS_VARIANT, getFatherHasVariantFilter());
    filters.put(PATIENT_HMZ_ALT, getPatientHmzAltFilter());
    filters.put(UNAFFECTED_PARENT_HMZ_ALT, getUnaffectedParentHmzAltFilter());
    filters.put(PARENT_PRESENT_HMZ_ALT, getHmzAltParentPresentFilter(pedigree));
    filters.put(NON_PENETRANCE, getNonPenetranceFilter());
    filters.put(MVL, new ComplexFilter(Arrays
        .asList(filters.get(MVL_LP_FILTER), filters.get(MVL_P_FILTER), filters.get(MVL_V_FILTER),
            filters.get(VKGL_LP_FILTER), filters.get(VKGL_V_FILTER)),
        ComplexOperator.OR));
    filters.put(SPLICE, new VepFilter("Consequence", SimpleOperator.CONTAINS_ANY,
        "splice_acceptor_variant,splice_donor_variant"));
    filters.put(AFFECTED_PARENT_HAS_VARIANT, getAffectedParentVariantFilter());
  }

  private Filter getAffectedParentVariantFilter() {
    return vcfRecord -> {
      Pedigree affectedParent = PedigreeUtils.getAffectedParent(pedigree);
      return new FilterResult(!isHmz(vcfRecord, affectedParent.getPatientId(), "0"), vcfRecord);
    };
  }

  private Filter getUnaffectedParentHmzAltFilter() {
    return vcfRecord -> new FilterResult(
        isHmz(vcfRecord, PedigreeUtils.getUnaffectedParent(pedigree).getPatientId(), "1"),
        vcfRecord);
  }

  private Filter getPatientHmzAltFilter() {
    return vcfRecord -> new FilterResult(isHmz(vcfRecord, pedigree.getPatientId(), "1"), vcfRecord);
  }

  private Filter getFatherHasVariantFilter() {
    return vcfRecord -> {
      Pedigree father = getFather(pedigree);
      return new FilterResult(!isHmz(vcfRecord, father.getPatientId(), "0"), vcfRecord);
    };
  }

  private Filter getFatherAffectedFilter() {
    return vcfRecord -> new FilterResult(
        getFather(pedigree).getAffected() == Affected.TRUE, vcfRecord);
  }

  private Filter getPatientIsMaleFilter() {
    return vcfRecord -> new FilterResult(pedigree.getSex() == Sex.MALE, vcfRecord);
  }


  private Filter getNonPenetranceFilter() {
    return vcfRecord -> new FilterResult(nonPenetrance, vcfRecord);
  }

  private Filter getHmzAltParentPresentFilter(Pedigree pedigree) {
    return vcfRecord -> {
      Pedigree father = getFather(pedigree);
      Pedigree mother = getMother(pedigree);
      return new FilterResult(isHmz(vcfRecord, father.getPatientId(), "1") || isHmz(vcfRecord, mother.getPatientId(), "1"), vcfRecord);
    };
  }

  public Map<String, Filter> getFilters() {
    return filters;
  }
}
