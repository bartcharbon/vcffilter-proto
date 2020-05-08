package org.molgenis.filter.vep;

import static java.util.Objects.requireNonNull;

import java.io.File;
import org.molgenis.filter.FileResource;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterResult;
import org.molgenis.filter.FilterResultEnum;
import org.molgenis.filter.utils.ComplexVcfInfoUtils;
import org.molgenis.vcf.VcfRecord;

public class VepFileFilter implements Filter {
  private final String name;
  private final String field;
  private String columnName;
  private FileResource fileResource;

  public VepFileFilter(String name, String field, String path, String column) {
    this.name = name;
    this.field = requireNonNull(field);
    this.columnName = column;
    loadFile(path);
  }

  private void loadFile(String path) {
    File file = new File(path);
    fileResource = new FileResource(file);
  }

  @Override
  public FilterResult filter(VcfRecord vcfRecord) {
    String[] vepValues = ComplexVcfInfoUtils.getSubValues(vcfRecord, "CSQ");
    // boolean to indicate if any Vep hit contained a value for the filter field
        if (vepValues.length > 0 && !vepValues[0].isEmpty()) {
          String value = ComplexVcfInfoUtils.getValueForKey(field, vcfRecord.getVcfMeta(), vepValues[0],"CSQ","\\|");
          if(value.isEmpty()){
            return new FilterResult(FilterResultEnum.MISSING, vcfRecord);
          }
          //FIXME contains not? contains word?
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
