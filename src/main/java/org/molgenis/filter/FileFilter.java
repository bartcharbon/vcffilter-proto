package org.molgenis.filter;

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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import joptsimple.internal.Strings;
import org.apache.commons.lang3.ArrayUtils;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfSample;
import org.molgenis.vcf.utils.VepUtils;

//FIXME: loads of duplication with SimpleFilter class
//TODO: multi column matching -> but hwo does that work with VEP value filtering -> if one of the vcf columns is VEP than filterVepValues?
public class FileFilter implements Filter {

  private FileResource fileResource;
  private String vcfField;
  private String columnName;

  private static final String BRACET = "(";
  private static final String VEP = "VEP";

  public FileFilter(String vcfField, String columnName, String path) {

    this.vcfField = vcfField;
    this.columnName = columnName;
    loadFile(path);
  }

  private void loadFile(String path) {
    File file = new File(path);
    fileResource = new FileResource(file);
  }

  @Override
  public FilterResult filter(VcfRecord vcfRecord) {
    if (vcfField.startsWith(VEP + "(")) {
      return filterVepValues(vcfRecord);
    } else {
      Object value = getValue(vcfRecord, vcfField);
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
    String fieldName = vcfField.substring(4, vcfField.length() - 1);
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
    return fileResource.contains(columnName, value);
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
        if (field.startsWith(SAMPLE + BRACET)) {
          value = getSampleValue(record, field);
        } else if (field.startsWith(INFO + BRACET)) {
          //FIXME: always assumes String for info field
          String info = field.substring(5, field.length() - 1);
          value = getInfoFieldValue(record, info);
        } else {
          throw new IllegalArgumentException("Field [" + field + "] is unsupported");
        }
    }
    return value;
  }

  private Object getSampleValue(VcfRecord record, String field) {
    Object value;
    String pattern = SAMPLE + "\\(([a-zA-Z]*)(\\,(\\d*))*\\)";
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(field);
    Integer sampleIndex;
    String sampleFieldName;
    if (m.matches()) {
      sampleFieldName = m.group(1);
      sampleIndex = m.group(3) != null ? Integer.valueOf(m.group(3)) : null;
      value = getSampleValue(record, sampleFieldName, sampleIndex);
    } else {
      throw new IllegalArgumentException(
          "Sample field is not correctly formatted, valid examples: 'SAMPLE(GT)','SAMPLE(GT,0)'");
    }
    return value;
  }

  private Object getSampleValue(VcfRecord record, String sampleFieldName, Integer index) {
    String[] format = record.getFormat();
    int sampleFieldIndex = ArrayUtils.indexOf(format, sampleFieldName);
    Object value;
    if (index != null) {
      VcfSample sample = com.google.common.collect.Iterators
          .get(record.getSamples().iterator(), index, null);
      if (sample != null) {
        value = sample.getData(sampleFieldIndex);
      } else {
        throw new IllegalStateException("Specified sample index does not exist.");
      }
    } else {
      value = new ArrayList<String>();
      record.getSamples()
          .forEach(sample -> ((List<String>) value).add(sample.getData(sampleFieldIndex)));
    }
    return value;
  }
}
