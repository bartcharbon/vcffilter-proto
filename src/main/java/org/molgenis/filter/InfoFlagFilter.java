package org.molgenis.filter;

import static java.util.Objects.requireNonNull;
import static org.molgenis.filter.FlagOperator.PRESENT;

import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;

public class InfoFlagFilter implements Filter {
  private final String field;
  private final Operator operator;

  public InfoFlagFilter(String field, Operator operator) {
    this.field = requireNonNull(field);
    this.operator = requireNonNull(operator);
  }

  @Override
  public FilterResult filter(VcfRecord vcfRecord) {
    boolean filterResult = false;
    Iterable<VcfInfo> infoFields = vcfRecord.getInformation();
    for(VcfInfo info : infoFields){
      if(info.getKey().equals(field)){
        filterResult = true;
      }
    }
    return new FilterResult(filterResult == operator.equals(PRESENT), vcfRecord);
  }
}
