package org.molgenis.filter;
import org.molgenis.vcf.VcfRecord;

public class NoOpFilter implements Filter {
  @Override
  public FilterResult filter(VcfRecord vcfRecord) {
    return new FilterResult(true, vcfRecord);
  }
}
