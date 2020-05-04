package org.molgenis.filter.info;

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
import java.util.regex.Pattern;
import joptsimple.internal.Strings;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterResult;
import org.molgenis.filter.FilterResultEnum;
import org.molgenis.filter.SimpleOperator;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.utils.VepUtils;

public class InfoVepFilter implements Filter {

  private final String name;
  private final String infoField;
  private final int infoIndex;
  private final String vepField;
  private final int vepIndex;
  private final String seperator;
  private final SimpleOperator operator;
  private final String filterValue;

  public InfoVepFilter(String name, String infoField, int infoIndex, String vepField, int vepIndex,
      String seperator, SimpleOperator operator, String value) {

    this.name = name;
    this.infoField = infoField;
    this.infoIndex = infoIndex;
    this.vepField = vepField;
    this.vepIndex = vepIndex;
    this.seperator = seperator;
    this.operator = operator;
    this.filterValue = value;
  }

  @Override
  public FilterResult filter(VcfRecord vcfRecord) {
    Object value = getValue(vcfRecord);
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

  private Object getValue(VcfRecord record) {
    String value = getInfoFieldValue(record, infoField);
    String vepValue = VepUtils.getValueForKey(vepField, record.getVcfMeta(), getInfoFieldValue(record,"CSQ"));
    String[] values = value.split(",");
    for(String singleInfoValue : values){
      String[] split = singleInfoValue.split(Pattern.quote(seperator));
      if(split[vepIndex].equals(vepValue)){
        return split[infoIndex];
      }
    }
    return null;
  }

  @Override
  public String getName() {
    return name;
  }
}
