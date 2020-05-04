package org.molgenis.filter.info;

import static java.util.Objects.requireNonNull;
import static org.molgenis.filter.FilterUtils.SEPARATOR;
import static org.molgenis.filter.FilterUtils.contains;
import static org.molgenis.filter.FilterUtils.containsAll;
import static org.molgenis.filter.FilterUtils.containsAny;
import static org.molgenis.filter.FilterUtils.containsNone;
import static org.molgenis.filter.FilterUtils.containsWord;
import static org.molgenis.vcf.utils.VcfUtils.getInfoFieldValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import joptsimple.internal.Strings;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterResult;
import org.molgenis.filter.FilterResultEnum;
import org.molgenis.filter.SimpleOperator;
import org.molgenis.vcf.VcfRecord;

public class InfoFilter implements Filter {
  private final String field;
  private final SimpleOperator operator;
  private final String filterValue;
  private final String name;

  public InfoFilter(String name, String field, SimpleOperator operator, String value) {
    this.name = requireNonNull(name);
    this.field = requireNonNull(field);
    this.operator = requireNonNull(operator);
    this.filterValue = requireNonNull(value);
  }

  @Override
  public FilterResult filter(VcfRecord vcfRecord) {
      Object value = getValue(vcfRecord, field);
      if (value == null) {
        return new FilterResult(FilterResultEnum.MISSING, vcfRecord);
      }
      if (value instanceof String) {
        if (filterSingleValue(value.toString())) {
          return new FilterResult(FilterResultEnum.TRUE, vcfRecord);
        }
        return new FilterResult(FilterResultEnum.FALSE, vcfRecord);
      } else if (value instanceof Collection) {
        if (filterCollection((Collection<String>) value)) {
          return new FilterResult(FilterResultEnum.TRUE, vcfRecord);
        }
        return new FilterResult(FilterResultEnum.FALSE, vcfRecord);
      } else {
        throw new IllegalStateException();
    }
  }

  private boolean filterCollection(Collection<String> value) {
    boolean subresult = false;
    for (String subValue : value) {
      if (!Strings.isNullOrEmpty(subValue) && filterSingleValue(subValue)) {
        subresult = true;
      }
    }
    return subresult;
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
        List<String> filtervalues = Arrays.asList(filterValue.split(SEPARATOR));
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

  private Object getValue(VcfRecord record, String field) {
    return getInfoFieldValue(record, field);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InfoFilter that = (InfoFilter) o;
    return Objects.equals(field, that.field) &&
        operator == that.operator &&
        Objects.equals(filterValue, that.filterValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(field, operator, filterValue);
  }

  @Override
  public String toString() {
    return "InfoFilter{" +
        "field='" + field + '\'' +
        ", operator=" + operator +
        ", filterValue='" + filterValue + '\'' +
        '}';
  }

  @Override
  public String getName() {
    return name;
  }
}
