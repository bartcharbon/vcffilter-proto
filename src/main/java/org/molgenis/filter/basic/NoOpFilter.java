package org.molgenis.filter.basic;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterResult;
import org.molgenis.filter.FilterResultEnum;
import org.molgenis.vcf.VcfRecord;

public class NoOpFilter implements Filter {

  private static final String NO_OP = "no-op";

  @Override
  public FilterResult filter(VcfRecord vcfRecord) {
    return new FilterResult(FilterResultEnum.TRUE, vcfRecord);
  }
  @Override
  public String getName() {
    return NO_OP;
  }
}
