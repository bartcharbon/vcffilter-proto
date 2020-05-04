package org.molgenis.filter.vep;

import static java.util.Objects.requireNonNull;

import java.io.File;
import org.molgenis.filter.FileResource;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterResult;
import org.molgenis.filter.FilterResultEnum;
import org.molgenis.filter.SimpleOperator;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.utils.VepUtils;

public class VepFileFilter implements Filter {
  private final String name;
  private final String field;
  private final SimpleOperator operator;
  private String columnName;
  private FileResource fileResource;

  public VepFileFilter(String name, String field, SimpleOperator operator, String path, String column) {
    this.name = name;
    this.field = requireNonNull(field);
    this.operator = requireNonNull(operator);
    this.columnName = column;
    loadFile(path);
  }

  private void loadFile(String path) {
    File file = new File(path);
    fileResource = new FileResource(file);
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
          //FIXMEL contains not? conctains word?
          else if (fileResource.contains(columnName, value)) {
            return new FilterResult(FilterResultEnum.TRUE, vcfRecord);
          }
          else{
            return new FilterResult(FilterResultEnum.FALSE, vcfRecord);
          }
        }
    return new FilterResult(FilterResultEnum.MISSING, vcfRecord);
  }

  @Override
  public String getName() {
    return name;
  }
}
