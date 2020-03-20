package org.molgenis.inheritance;

import static org.molgenis.filter.FilterTool.GZIP_EXTENSION;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_AD_AR_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_AD_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_AR_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_UNKNOWN_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.INHERITANCE_XL_FILTER;
import static org.molgenis.inheritance.InheritanceFilters.PATIENT_HMZ_ALT;
import static org.molgenis.inheritance.InheritanceFilters.TWICE_IN_A_GENE_FILTER;
import static org.molgenis.vcf.utils.VcfUtils.getVcfReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterAction;
import org.molgenis.filter.FilterRunner;
import org.molgenis.filter.FilterState;
import org.molgenis.filter.FilterStep;
import org.molgenis.filter.FilterTool;
import org.molgenis.inheritance.pedigree.Affected;
import org.molgenis.inheritance.pedigree.Pedigreader;
import org.molgenis.inheritance.pedigree.Pedigree;
import org.molgenis.inheritance.pedigree.PedigreeUtils;
import org.molgenis.vcf.VcfReader;

public class InheritanceMatcher {

  private static final String BLACK_1 = "black1";
  private static final String BLACK_2 = "black2";
  private static final String BLACK_3 = "black3";
  private static final String BLACK_4 = "black4";
  private static final String BLACK_5 = "black5";
  private static final String BLACK_6 = "black6";
  private static final String BLACK_7 = "black7";
  private static final String BLACK_8 = "black8";
  private boolean nonPenetrance;
  private File inputFile;
  private File pedigreeFile;
  private String patientSampleId;

  public void match() throws Exception {
    String fullInputFileName = inputFile.getName();
    String extension = fullInputFileName.substring(fullInputFileName.indexOf("."));
    VcfReader reader = getVcfReader(inputFile, extension.endsWith(GZIP_EXTENSION));

    Pedigreader pedigreader = new Pedigreader();
    Map<String, Pedigree> pedigrees = pedigreader
        .run(inputFile);
    Pedigree pedigree = pedigrees.get(patientSampleId);

    InheritanceFilters inheritanceFilters = new InheritanceFilters(reader, pedigree, "1",
        nonPenetrance);
    Map<String, Filter> filters = inheritanceFilters.getFilters();

    FilterRunner filterRunner = new FilterRunner(inputFile, extension, null, null, null, null, null,
        null);

    switch (PedigreeUtils.getNumberOfPresentParents(pedigree)) {
      case 0:
        filterRunner.runFilters(getBlackTree(filters));
        break;
      case 1:
        Pedigree parent = PedigreeUtils.getSingleParentPedigree(pedigree);
        if (parent.getAffected() == Affected.TRUE) {
          filterRunner.runFilters(getGreenTree(filters));
        } else {
          filterRunner.runFilters(getYellowTree(filters));
        }
        break;
      case 2:
        boolean isAffectedParentPresent = false;
        for (Entry<String, Pedigree> entry : pedigrees.entrySet()) {
          if (entry.getValue().getAffected() == Affected.TRUE) {
            isAffectedParentPresent = true;
          }
        }
        if (isAffectedParentPresent) {
          filterRunner.runFilters(getRedTree(filters));
        } else {
          filterRunner.runFilters(getBlueTree(filters));
        }
        break;
      default:
        throw new RuntimeException("More than 2 parents...");
    }
  }

  private Map<String, FilterStep> getBlueTree(Map<String, Filter> filters) {
    Map<String, FilterStep> result = new HashMap<>();
    return result;
  }

  private Map<String, FilterStep> getRedTree(Map<String, Filter> filters) {
    Map<String, FilterStep> result = new HashMap<>();
    return result;
  }

  private Map<String, FilterStep> getYellowTree(Map<String, Filter> filters) {
    Map<String, FilterStep> result = new HashMap<>();
    return result;
  }

  private Map<String, FilterStep> getGreenTree(Map<String, Filter> filters) {
    Map<String, FilterStep> result = new HashMap<>();
    return result;
  }

  private Map<String, FilterStep> getBlackTree(Map<String, Filter> filters) {
    Map<String, FilterStep> result = new HashMap<>();
    result
        .put(BLACK_1, new FilterStep(BLACK_1, filters.get(INHERITANCE_AD_FILTER),
            new FilterAction(FilterState.NEXT, "", null),
            new FilterAction(FilterState.NEXT, "", null)));
    result.put(BLACK_2, new FilterStep(BLACK_2, filters.get(INHERITANCE_AR_FILTER),
        new FilterAction(FilterState.NEXT, "", null),
        new FilterAction(FilterState.NEXT, "", null)));
    result.put(BLACK_3, new FilterStep(BLACK_3, filters.get(INHERITANCE_UNKNOWN_FILTER),
        new FilterAction(FilterState.NEXT, "", null),
        new FilterAction(FilterState.NEXT, "", null)));
    result.put(BLACK_4, new FilterStep(BLACK_4, filters.get(INHERITANCE_AD_AR_FILTER),
        new FilterAction(FilterState.NEXT, "", null),
        new FilterAction(FilterState.NEXT, "", null)));
    result.put(BLACK_5, new FilterStep(BLACK_5, filters.get(INHERITANCE_XL_FILTER),
        new FilterAction(FilterState.NEXT, "", null),
        new FilterAction(FilterState.NEXT, "", null)));
    result.put(BLACK_6, new FilterStep(BLACK_6, filters.get(TWICE_IN_A_GENE_FILTER),
        new FilterAction(FilterState.NEXT, "", null),
        new FilterAction(FilterState.NEXT, "", null)));
    result.put(BLACK_7, new FilterStep(BLACK_7, filters.get(PATIENT_HMZ_ALT),
        new FilterAction(FilterState.NEXT, "", null),
        new FilterAction(FilterState.NEXT, "", null)));
    result.put(BLACK_8, new FilterStep(BLACK_8, filters.get(),
        new FilterAction(FilterState.NEXT, "", null),
        new FilterAction(FilterState.NEXT, "", null)));

    return result;
  }
}
