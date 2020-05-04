package org.molgenis.filter.vep;

import static java.util.Objects.requireNonNull;
import static org.molgenis.filter.FilterUtils.SEPARATOR;
import static org.molgenis.filter.FilterUtils.contains;
import static org.molgenis.filter.FilterUtils.containsAll;
import static org.molgenis.filter.FilterUtils.containsAny;
import static org.molgenis.filter.FilterUtils.containsNone;
import static org.molgenis.filter.FilterUtils.containsWord;

import java.util.Arrays;
import java.util.List;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterResult;
import org.molgenis.filter.FilterResultEnum;
import org.molgenis.filter.SimpleOperator;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.utils.VepUtils;

public class VepFilter implements Filter {
  private final String name;
  private final String field;
  private final SimpleOperator operator;
  private String filterValue;

  public VepFilter(String name, String field, SimpleOperator operator, String value) {
    this.name = name;
    this.field = requireNonNull(field);
    this.operator = requireNonNull(operator);
    this.filterValue = requireNonNull(value);
  }

  @Override
  public FilterResult filter(VcfRecord vcfRecord) {
    String[] vepValues = VepUtils.getVepValues(vcfRecord);
    // boolean to indicate if any Vep hit contained a value for the filter field
        if (vepValues.length > 0 && !vepValues[0].isEmpty()) {
          String value = VepUtils.getValueForKey(field, vcfRecord.getVcfMeta(), vepValues[0]);
          if(value.isEmpty()){
            return new FilterResult(FilterResultEnum.MISSING, vcfRecord);
          }
          else if (filterSingleValue(value)) {
            return new FilterResult(FilterResultEnum.TRUE, vcfRecord);
          }
          else{
            return new FilterResult(FilterResultEnum.FALSE, vcfRecord);
          }
        }
    return new FilterResult(FilterResultEnum.MISSING, vcfRecord);
  }

  private boolean filterSingleValue(String value) {
    boolean result;
    switch (operator) {
      case EQ:
        //FIXME: currently assumes String datatype
        result = value.equals(filterValue);
        break;
      case CONTAINS:
        result = contains(value, filterValue);
        break;
      case CONTAINS_WORD:
        result = containsWord(value, filterValue);
        break;
      case CONTAINS_ANY:
        result = containsAny(value.split(SEPARATOR), filterValue.split(SEPARATOR));
        break;
      case CONTAINS_ALL:
        result = containsAll(value.split(SEPARATOR), filterValue.split(SEPARATOR));
        break;
      case CONTAINS_NONE:
        result = containsNone(value.split(SEPARATOR), filterValue.split(SEPARATOR));
        break;
      case NOT_CONTAINS:
        result = !contains(value, filterValue);
        break;
      case NOT_CONTAINS_WORD:
        result = !containsWord(value,filterValue);
        break;
      case GREATER_OR_EQUAL:
        result = Double.valueOf(value) >= Double.valueOf(filterValue);
        break;
      case LESS_OR_EQUAL:
        result = Double.valueOf(value) <= Double.valueOf(filterValue);
        break;
      case GREATER:
        result = Double.valueOf(value) > Double.valueOf(filterValue);
        break;
      case LESS:
        result = Double.valueOf(value) < Double.valueOf(filterValue);
        break;
      case NOT_EQ:
        //FIXME: currently assumes String datatype
        result = !value.equals(filterValue);
        break;
      case IN:
        List<String> filtervalues = Arrays.asList(filterValue.split(","));
        result = filtervalues.contains(value);
        break;
      case PRESENT:
        result = !value.isEmpty();
        break;
      case NOT_PRESENT:
        result = value.isEmpty();
        break;
      default:
        throw new IllegalArgumentException(
            "Invalid filter operator, expecting one of [==,>=,<=,>,<,!=]");
    }
    return result;
  }

  @Override
  public String getName() {
    return name;
  }
}
