package org.molgenis.filter.info;

import static java.util.Objects.requireNonNull;

import java.io.File;
import org.molgenis.filter.FileResource;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterResult;
import org.molgenis.filter.FilterResultEnum;
import org.molgenis.filter.SimpleOperator;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.utils.VcfUtils;

public class InfoFileFilter implements Filter {
  private final String name;
  private final String field;
  private final SimpleOperator operator;
  private String columnName;
  private FileResource fileResource;

  public InfoFileFilter(String name, String field, SimpleOperator operator, String path, String column) {
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
    String value = VcfUtils.getInfoFieldValue(vcfRecord, field);
       if(value.isEmpty()){
          return new FilterResult(FilterResultEnum.MISSING, vcfRecord);
        }
        //FIXME: contains not? conctains word?
        else if (fileResource.contains(columnName, value)) {
          return new FilterResult(FilterResultEnum.TRUE, vcfRecord);
        }
        else{
          return new FilterResult(FilterResultEnum.FALSE, vcfRecord);
      }
  }

  @Override
  public String getName() {
    return name;
  }
}
