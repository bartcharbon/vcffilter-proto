package org.molgenis.filter;

import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.utils.VcfUtils.getInfoFieldValue;
import static org.molgenis.vcf.utils.VcfUtils.getSampleValue;
import static org.molgenis.vcf.utils.VcfUtils.getVcfValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.molgenis.vcf.VcfRecord;

public class CustomFilter implements Filter {

  private final String fields;
  private File file;

  public CustomFilter(String field, File file) {
    this.fields = requireNonNull(field);
    this.file = requireNonNull(file);
  }

  @Override
  public FilterResult filter(VcfRecord vcfRecord) {
    Map<String, String> values = parseFields(vcfRecord);
    StringBuilder valueString = new StringBuilder();
    boolean isEmpty = true;
    for (Entry entry : values.entrySet()) {
      if (!isEmpty) {
        valueString.append(",");
      }
      isEmpty = false;
      valueString.append(entry.getKey()).append(":").append(entry.getValue());
    }
    try {
      Process proc = Runtime.getRuntime()
          .exec("python " + file.getAbsolutePath() + " " + valueString);
      BufferedReader stdInput = new BufferedReader(new
          InputStreamReader(proc.getInputStream()));
      String result = stdInput.readLine();
      return new FilterResult("True".equals(result), vcfRecord);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Map<String, String> parseFields(VcfRecord vcfRecord) {
    Map<String, String> result = new HashMap<>();
    for (String field : fields.split(",")) {
      if (field.startsWith("INFO(")) {
        getInfoFieldValue(vcfRecord, field.substring(5));
      } else if (field.startsWith("SAMPLE(")) {
        String[] split = field.split(",");
        getSampleValue(vcfRecord, split[0].substring(7), split[1]);
      } else {
        result.put(field, getVcfValue(vcfRecord, field).toString());
      }
    }
    return result;
  }
}
