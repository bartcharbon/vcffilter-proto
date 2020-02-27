package org.molgenis.filter;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.molgenis.vcf.VcfRecord;

public class ComplexFilter implements Filter{
  private final List<Filter> filters;
  private final ComplexOperator operator;

  public ComplexFilter(List<Filter> filters, ComplexOperator operator) {
    this.filters = requireNonNull(filters);
    this.operator = requireNonNull(operator);
  }

  @Override
  public FilterResult filter(VcfRecord vcfRecord) {
    if(operator == ComplexOperator.AND){
      for(Filter filter : filters){
        if(!filter.filter(vcfRecord).getPass()){
          return new FilterResult(false, vcfRecord);
        }
      }
      return new FilterResult(true, vcfRecord);
    }else{
      for(Filter filter : filters){
        if(filter.filter(vcfRecord).getPass()){
          return new FilterResult(true, vcfRecord);
        }
      }
      return new FilterResult(false, vcfRecord);
    }
  }
}
