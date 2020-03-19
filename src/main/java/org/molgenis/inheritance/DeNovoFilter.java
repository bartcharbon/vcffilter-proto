package org.molgenis.inheritance;

import static org.molgenis.vcf.utils.VcfUtils.deNovo;

import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterResult;
import org.molgenis.inheritance.pedigree.Pedigree;
import org.molgenis.vcf.VcfRecord;

public class DeNovoFilter implements Filter {
  private final Pedigree pedigree;
  private final String alleleIdx;

  public DeNovoFilter(Pedigree pedigree, String alleleIdx) {
    this.pedigree = pedigree;
    this.alleleIdx = alleleIdx;
  }

  @Override
  public FilterResult filter(VcfRecord vcfRecord) {
    return new FilterResult(deNovo(vcfRecord, pedigree, alleleIdx), vcfRecord);
  }
}
