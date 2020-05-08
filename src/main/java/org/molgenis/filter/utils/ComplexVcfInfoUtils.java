package org.molgenis.filter.utils;

import static org.molgenis.filter.utils.VcfUtils.getInfoField;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.meta.VcfMetaInfo;

public class ComplexVcfInfoUtils {

  public static Set<String> getSubValues(String key, VcfRecord record, String name, String separator){
    Set<String> result = new HashSet<>();
    String[] complexResults = getSubValues(record, name);
    for(String singleVepResult : complexResults) {
      result.add(getValueForKey(key, record.getVcfMeta(), singleVepResult, name, separator));
    }
    return result;
  }

  public static String[] getSubValues(VcfRecord record, String name) {
    VcfInfo complexInfoField = getInfoField(record, name);
    if(complexInfoField == null){
      return new String[0];
    }
    String multiVepResult = complexInfoField.getValRaw();
    return multiVepResult.split(",");
  }

  public static String getValueForKey(String key, VcfMeta vcfMeta,
      String singleVepResult, String name, String separator) {
    int index = getIndex(key, vcfMeta, name, separator);
    String[] complexValues = singleVepResult.split(separator, -1);
    if(complexValues.length >= index) {
      return complexValues[index];
    }else{
      throw new RuntimeException("The index found in the headers for key "+key+" was not present in the info field.");
    }
  }

  private static int getIndex(String key, VcfMeta meta, String name, String separator) {
    Iterator<VcfMetaInfo> infoMetaIterator = meta.getInfoMeta().iterator();
    while (infoMetaIterator.hasNext()) {
      VcfMetaInfo infoMeta = infoMetaIterator.next();
      if (infoMeta.getId().equals(name)) {
        String desc = infoMeta.getDescription().replace("Consequence annotations from Ensembl VEP. Format: ","");
        String[] header = desc.split(separator);
        for (int i = 0; i < header.length; i++) {
          if (header[i].equals(key)) {
            return i;
          }
        }
      }
    }
    throw new RuntimeException("Key: [" + key + "] not found in VEP values");
  }
}
