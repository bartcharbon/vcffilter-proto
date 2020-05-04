package org.molgenis.filter.complex;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterResult;
import org.molgenis.filter.FilterResultEnum;
import org.molgenis.vcf.VcfRecord;

public class ComplexFilter implements Filter {
  private final List<Filter> filters;
  private final ComplexOperator operator;
  private final String name;

  public ComplexFilter(String name, List<Filter> filters, ComplexOperator operator) {
    this.name = requireNonNull(name);
    this.filters = requireNonNull(filters);
    this.operator = requireNonNull(operator);
  }

  @Override
  public FilterResult filter(VcfRecord vcfRecord) {
    if(operator == ComplexOperator.AND){
      for(Filter filter : filters){
        if(filter.filter(vcfRecord).getResult()== FilterResultEnum.FALSE){
          return new FilterResult(FilterResultEnum.FALSE, vcfRecord);
        }
      }
      return new FilterResult(FilterResultEnum.TRUE, vcfRecord);
    }else{
      for(Filter filter : filters){
        if(filter.filter(vcfRecord).getResult() == FilterResultEnum.TRUE){
          return new FilterResult(FilterResultEnum.TRUE, vcfRecord);
        }
      }
      return new FilterResult(FilterResultEnum.FALSE, vcfRecord);
    }
  }

  @Override
  public String getName() {
    return name;
  }
}
