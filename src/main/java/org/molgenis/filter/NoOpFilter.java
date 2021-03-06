package org.molgenis.filter;
import org.molgenis.vcf.VcfRecord;

public class NoOpFilter implements Filter {
  @Override
  public FilterResult filter(VcfRecord vcfRecord) {
    return new FilterResult(FilterResultEnum.TRUE, vcfRecord);
  }
  @Override
  public String getName() {
    return "no-op";
  }
}
