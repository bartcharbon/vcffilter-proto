package org.molgenis.inheritance;

import static java.util.Objects.requireNonNull;
import static org.molgenis.filter.FilterTool.GZIP_EXTENSION;
import static org.molgenis.inheritance.tree.BlackTree.getBlackTree;
import static org.molgenis.inheritance.tree.BlueTree.getBlueTree;
import static org.molgenis.inheritance.tree.GreenTree.getGreenTree;
import static org.molgenis.inheritance.tree.RedTree.getRedTree;
import static org.molgenis.inheritance.tree.YellowTree.getYellowTree;
import static org.molgenis.vcf.utils.VcfUtils.getVcfReader;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterRunner;
import org.molgenis.inheritance.pedigree.Affected;
import org.molgenis.inheritance.pedigree.Pedigreader;
import org.molgenis.inheritance.pedigree.Pedigree;
import org.molgenis.inheritance.pedigree.PedigreeUtils;
import org.molgenis.vcf.VcfReader;

public class InheritanceTree {

  private final FilterRunner filterRunner;
  private final boolean nonPenetrance;
  private final File inputFile;
  private final File pedigreeFile;
  private final String patientSampleId;

  public InheritanceTree(FilterRunner filterRunner, boolean nonPenetrance, File inputFile,
      File pedigreeFile, String patientSampleId) {
    this.filterRunner = requireNonNull(filterRunner);
    this.nonPenetrance = requireNonNull(nonPenetrance);
    this.inputFile = requireNonNull(inputFile);
    this.pedigreeFile = requireNonNull(pedigreeFile);
    this.patientSampleId = requireNonNull(patientSampleId);
  }

  public void match() throws Exception {
    String fullInputFileName = inputFile.getName();
    String extension = fullInputFileName.substring(fullInputFileName.indexOf("."));
    VcfReader reader = getVcfReader(inputFile, extension.endsWith(GZIP_EXTENSION));

    Pedigreader pedigreader = new Pedigreader();
    Map<String, Pedigree> pedigrees = pedigreader
        .run(pedigreeFile);
    Pedigree pedigree = pedigrees.get(patientSampleId);

    InheritanceFilters inheritanceFilters = new InheritanceFilters(reader, pedigree, "1",
        nonPenetrance);
    Map<String, Filter> filters = inheritanceFilters.getFilters();

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
        if (PedigreeUtils.getFather(pedigree).getAffected() == Affected.TRUE) {
          isAffectedParentPresent = true;
        }
        if (PedigreeUtils.getMother(pedigree).getAffected() == Affected.TRUE) {
          isAffectedParentPresent = true;
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


}
