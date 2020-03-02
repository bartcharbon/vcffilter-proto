package org.molgenis.filter;

import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.utils.VcfConstants.ALT;
import static org.molgenis.vcf.utils.VcfConstants.CHROM;
import static org.molgenis.vcf.utils.VcfConstants.FILTER;
import static org.molgenis.vcf.utils.VcfConstants.FORMAT;
import static org.molgenis.vcf.utils.VcfConstants.ID;
import static org.molgenis.vcf.utils.VcfConstants.INFO;
import static org.molgenis.vcf.utils.VcfConstants.POS;
import static org.molgenis.vcf.utils.VcfConstants.QUAL;
import static org.molgenis.vcf.utils.VcfConstants.REF;
import static org.molgenis.vcf.utils.VcfConstants.SAMPLE;
import static org.molgenis.vcf.utils.VcfUtils.getInfoFieldValue;
import static org.molgenis.vcf.utils.VcfUtils.updateInfoField;
import static org.molgenis.vcf.utils.VepUtils.VEP_INFO_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import joptsimple.internal.Strings;
import org.apache.commons.lang3.ArrayUtils;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfSample;
import org.molgenis.vcf.utils.VepUtils;

public class SimpleFilter implements Filter {

  private static final String VEP = "VEP";
  static final String BRACET = "(";
  private final String field;
  private final SimpleOperator operator;
  private final String filterValue;

  public SimpleFilter(String field, SimpleOperator operator, String value) {
    this.field = requireNonNull(field);
    this.operator = requireNonNull(operator);
    this.filterValue = requireNonNull(value);
  }

  @Override
  public FilterResult filter(VcfRecord vcfRecord) {
    if (field.startsWith(VEP + "(")) {
      return filterVepValues(vcfRecord);
    } else {
      Object value = getValue(vcfRecord, field);
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
  }

  private FilterResult filterVepValues(VcfRecord vcfRecord) {
    List<String> filteredVepValues = new ArrayList<>();
    String fieldName = field.substring(4, field.length() - 1);
    String[] vepValues = VepUtils.getVepValues(vcfRecord);
    for (String vepValue : vepValues) {
      if (!vepValue.isEmpty()) {
        String value = VepUtils.getValueForKey(fieldName, vcfRecord.getVcfMeta(), vepValue);
        if (!value.isEmpty() && filterSingleValue(value)) {
          filteredVepValues.add(vepValue);
        }
      }
    }
    if (filteredVepValues.isEmpty()) {
      return new FilterResult(false, vcfRecord);
    }
    return new FilterResult(true, updateInfoField(vcfRecord, VEP_INFO_NAME,
        Strings.join(filteredVepValues, ",")));
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

  private Object getValue(VcfRecord record, String field) {
    Object value;
    switch (field) {
      case CHROM:
        value = record.getChromosome();
        break;
      case REF:
        value = record.getReferenceAllele().getAlleleAsString();
        break;
      case QUAL:
        value = record.getQuality();
        break;
      case FILTER:
        value = record.getFilterStatus();
        break;
      case ID:
        value = record.getIdentifiers();
        break;
      case POS:
        //FIXME: should not be toStringed
        value = Integer.toString(record.getPosition());
        break;
      case ALT:
      case FORMAT:
        throw new IllegalArgumentException("Field [" + field + "] is currently unsupported");
      default:
       if (field.startsWith(INFO + BRACET)) {
          //FIXME: always assumes String for info field
          String info = field.substring(5, field.length() - 1);
          value = getInfoFieldValue(record, info);
        } else {
          throw new IllegalArgumentException("Field [" + field + "] is unsupported");
        }
    }
    return value;
  }

  @Override
  public String toString() {
    return "SimpleFilter{" +
        "field='" + field + '\'' +
        ", operator=" + operator +
        ", filterValue='" + filterValue + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SimpleFilter that = (SimpleFilter) o;
    return field.equals(that.field) &&
        operator == that.operator &&
        filterValue.equals(that.filterValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(field, operator, filterValue);
  }
}
