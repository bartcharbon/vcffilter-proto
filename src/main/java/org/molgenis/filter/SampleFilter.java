package org.molgenis.filter;

import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.utils.VcfUtils.getSampleValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import joptsimple.internal.Strings;
import org.molgenis.vcf.VcfRecord;

public class SampleFilter implements Filter {

  private final String field;
  private final SimpleOperator operator;
  private final String filterValue;
  private final String sampleId;

  public SampleFilter(String field, SimpleOperator operator, String value, String sampleId) {
    this.field = requireNonNull(field);
    this.operator = requireNonNull(operator);
    this.filterValue = requireNonNull(value);
    this.sampleId = sampleId;
  }

  @Override
  public FilterResult filter(VcfRecord vcfRecord) {
    Object value = getSampleValue(vcfRecord, field, sampleId);
    if (value == null) {
      return new FilterResult(false, vcfRecord);
    }
    if (value instanceof String) {
      if (filterSingleValue(value.toString())) {
        return new FilterResult(true, vcfRecord);
      }
      return new FilterResult(false, vcfRecord);
    } else if (value instanceof Collection) {
      if (filterCollection((Collection<String>) value)) {
        return new FilterResult(true, vcfRecord);
      }
      return new FilterResult(false, vcfRecord);
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
        result = value.contains(filterValue);
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
      default:
        throw new IllegalArgumentException(
            "Invalid filter operator, expecting one of [==,>=,<=,>,<,!=]");
    }
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SampleFilter that = (SampleFilter) o;
    return Objects.equals(field, that.field) &&
        operator == that.operator &&
        Objects.equals(filterValue, that.filterValue) &&
        Objects.equals(sampleId, that.sampleId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(field, operator, filterValue, sampleId);
  }

  @Override
  public String toString() {
    return "SampleFilter{" +
        "sampleField='" + field + '\'' +
        ", operator=" + operator +
        ", filterValue='" + filterValue + '\'' +
        ", sampleId='" + sampleId + '\'' +
        '}';
  }
}
