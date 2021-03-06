package org.molgenis.vcf.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.meta.VcfMetaInfo;

//FIXME: duplicate of the version in the gavin algorith only branch of gavin plus
public class VepUtils {

  public static final String VEP_SEPERATOR = "\\|";
  public static final String VEP_INFO_NAME = "CSQ";
  public static String 	ALLELE	=	 "Allele";
  public static String 	GENE	=	 "Gene";


  public static Set<String> getVepValues(String key, VcfRecord record){
    Set<String> result = new HashSet<>();
    String[] vepResults = getVepValues(record);
    for(String singleVepResult : vepResults) {
      result.add(getValueForKey(key, record.getVcfMeta(), singleVepResult));
    }
    return result;
  }

  public static Set<String> getVepValues(String key, VcfRecord record, String allele){
    Set<String> result = new HashSet<>();
    String[] vepResults = getVepValues(record);
    for(String singleVepResult : vepResults) {
      String vepAllele = getValueForKey(ALLELE, record.getVcfMeta(), singleVepResult);
      if(allele.equals(vepAllele)) {
        result.add(getValueForKey(key, record.getVcfMeta(), singleVepResult));
      }
    }
    return result;
  }

  public static Set<String> getVepValues(String key, VcfRecord record, String allele, String gene){
    Set<String> result = new HashSet<>();
    String[] vepResults = getVepValues(record);
    for(String singleVepResult : vepResults) {
      String vepAllele = getValueForKey(ALLELE, record.getVcfMeta(), singleVepResult);
      String vepGene = getValueForKey(GENE, record.getVcfMeta(), singleVepResult);
      if(allele.equals(vepAllele) && gene.equals(vepGene)) {
        result.add(getValueForKey(key, record.getVcfMeta(), singleVepResult));
      }
      if(allele.equals(vepAllele)) {
        result.add(getValueForKey(key, record.getVcfMeta(), singleVepResult));
      }
    }
    return result;
  }

  public static String[] getVepValues(VcfRecord record) {
    VcfInfo vepInfoField = getVepInfoField(record);
    if(vepInfoField == null){
      return new String[0];
    }
    String multiVepResult = vepInfoField.getValRaw();
    return multiVepResult.split(",");
  }

  public static String getValueForKey(String key, VcfMeta vcfMeta,
      String singleVepResult) {
    int index = getIndex(key, vcfMeta);
    String[] vepValues = singleVepResult.split(VEP_SEPERATOR, -1);
    if(vepValues.length >= index) {
      return vepValues[index];
    }else{
      throw new RuntimeException("The index found in the headers for key "+key+" was not present in the info field.");
    }
  }

  private static VcfInfo getVepInfoField(VcfRecord record) {
    for(VcfInfo info : record.getInformation()){
      if(info.getKey().equals(VEP_INFO_NAME)){
        return info;
      }
    }
    return null;
  }

  private static int getIndex(String key, VcfMeta meta) {
    Iterator<VcfMetaInfo> infoMetaIterator = meta.getInfoMeta().iterator();
    while (infoMetaIterator.hasNext()) {
      VcfMetaInfo infoMeta = infoMetaIterator.next();
      if (infoMeta.getId().equals(VEP_INFO_NAME)) {
        String desc = infoMeta.getDescription().replace("Consequence annotations from Ensembl VEP. Format: ","");
        String[] header = desc.split(VEP_SEPERATOR);
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
