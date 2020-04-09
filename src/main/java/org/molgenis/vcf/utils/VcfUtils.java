package org.molgenis.vcf.utils;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.molgenis.vcf.utils.VcfConstants.ALT;
import static org.molgenis.vcf.utils.VcfConstants.CHROM;
import static org.molgenis.vcf.utils.VcfConstants.FILTER;
import static org.molgenis.vcf.utils.VcfConstants.FORMAT;
import static org.molgenis.vcf.utils.VcfConstants.ID;
import static org.molgenis.vcf.utils.VcfConstants.POS;
import static org.molgenis.vcf.utils.VcfConstants.QUAL;
import static org.molgenis.vcf.utils.VcfConstants.REF;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import net.sf.samtools.util.BlockCompressedInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.genotype.Allele;
import org.molgenis.inheritance.pedigree.Pedigree;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfSample;
import org.molgenis.vcf.VcfWriter;
import org.molgenis.vcf.VcfWriterFactory;
import org.molgenis.vcf.VcfWriterFactory.Format;
import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.meta.VcfMetaInfo;

public class VcfUtils {

  private static final String MISSING_VALUE = ".";
  private static final String KEY_ID = "ID";
  private static final String KEY_NUMBER = "Number";
  private static final String KEY_TYPE = "Type";
  private static final String KEY_DESCRIPTION = "Description";

  public static void addInfoFieldMetadata(VcfMeta vcfMeta, String id, String number, String type,
      String description) {
    Map<String, String> properties = new LinkedHashMap<>();

    properties.put(KEY_ID, id);
    properties.put(KEY_NUMBER, number);
    properties.put(KEY_TYPE, type);
    properties.put(KEY_DESCRIPTION, description);
    vcfMeta.addInfoMeta(new VcfMetaInfo(properties));
  }

  public static VcfRecord addInfoFieldData(VcfRecord record, VcfMeta vcfMeta, String key,
      String value) {
    List<String> tokens = VcfUtils.getExistingVcfTokens(record);
    tokens.add(VcfUtils.createInfoToken(record, key, value, false));
    return new VcfRecord(vcfMeta, tokens.toArray(new String[0]));
  }

  public static List<String> getExistingVcfTokens(VcfRecord record) {
    List<String> tokens = new ArrayList<>();
    tokens.add(record.getChromosome());
    tokens.add(String.valueOf(record.getPosition()));
    List<String> identifiers = record.getIdentifiers();
    tokens.add(!identifiers.isEmpty() ? identifiers.stream().collect(joining(";")) : MISSING_VALUE);
    tokens.add(record.getReferenceAllele().toString());
    tokens.add(StringUtils.join(record.getAlternateAlleles(), ","));
    tokens.add(record.getQuality() != null ? record.getQuality() : ".");
    tokens.add(record.getFilterStatus() != null ? record.getFilterStatus() : ".");
    return tokens;
  }

  private static String createInfoToken(VcfRecord record, String key, String value,
      boolean isReplace) {
    Iterable<VcfInfo> vcfInformations = record.getInformation();

    boolean hasInformation = vcfInformations.iterator().hasNext();

    StringBuilder stringBuilder = new StringBuilder();

    if (hasInformation) {
      stringBuilder.append(StreamSupport.stream(vcfInformations.spliterator(), false)
          .filter(vcfInfo -> !vcfInfo.getKey().equals(key) || isReplace == false)
          .map(VcfUtils::createInfoTokenPart)
          .collect(joining(";")));
    }
    if (stringBuilder.length() > 0) {
      stringBuilder.append(';');
    }
    stringBuilder.append(createInfoTokenPart(key, value));
    if (StringUtils.isNotEmpty(value)) {
      stringBuilder.append(';');
    }
    return stringBuilder.toString();
  }

  private static String removeInfoToken(VcfRecord record, String key) {
    Iterable<VcfInfo> vcfInformations = record.getInformation();

    boolean hasInformation = vcfInformations.iterator().hasNext();

    StringBuilder stringBuilder = new StringBuilder();

    if (hasInformation) {
      stringBuilder.append(StreamSupport.stream(vcfInformations.spliterator(), false)
          .filter(vcfInfo -> !vcfInfo.getKey().equals(key))
          .map(VcfUtils::createInfoTokenPart)
          .collect(joining(";")));
    }
    return stringBuilder.toString();
  }

  private static String createInfoTokenPart(VcfInfo vcfInfo) {
    return createInfoTokenPart(vcfInfo.getKey(), vcfInfo.getValRaw());
  }

  private static String createInfoTokenPart(String key, String value) {
    return key + '=' + value;
  }

  public static VcfRecord addInfoField(VcfRecord vcfRecord, String key, String value){
    return addInfoField(vcfRecord, key, value, ".","");
  }

  public static VcfRecord addInfoField(VcfRecord vcfRecord, String key, String value, String number, String description){
    return addOrUpdateInfoField(vcfRecord, key, value, description, number, false);
  }

  public static VcfRecord updateInfoField(VcfRecord vcfRecord, String key, String value){
    VcfMetaInfo info = vcfRecord.getVcfMeta().getInfoMeta(key);
    return addOrUpdateInfoField(vcfRecord, key, value,info.getDescription(), info.getNumber(),true);
  }

  public static String getInfoFieldValue(VcfRecord record, String key) {
    for(VcfInfo info : record.getInformation()){
      if(info.getKey().equals(key)){
        return info.getValRaw();
      }
    }
    return null;
  }

  private static VcfRecord addOrUpdateInfoField(VcfRecord vcfRecord, String key, String value, String description, String number,
      boolean isReplace){

    VcfMeta vcfMeta = vcfRecord.getVcfMeta();
    addInfoFieldMetadata(vcfMeta,key,number,"String",description);

    List<String> tokens = new ArrayList<>();
    tokens.add(vcfRecord.getChromosome());
    tokens.add(String.valueOf(vcfRecord.getPosition()));

    List<String> identifiers = vcfRecord.getIdentifiers();
    tokens.add(!identifiers.isEmpty() ? identifiers.stream().collect(joining(";")) : MISSING_VALUE);

    tokens.add(vcfRecord.getReferenceAllele().toString());
    List<String> altTokens = vcfRecord.getAlternateAlleles().stream().map(allele -> allele.getAlleleAsString()).collect(toList());
    if (altTokens.size() == 0) {
      tokens.add(MISSING_VALUE);
    } else {
      tokens.add(altTokens.stream().collect(joining(",")));
    }

    tokens.add(vcfRecord.getQuality()==null?MISSING_VALUE:vcfRecord.getQuality());
    tokens.add(vcfRecord.getFilterStatus()==null?MISSING_VALUE:vcfRecord.getFilterStatus());
    tokens.add(createInfoToken(vcfRecord, key, value, isReplace));

    Iterable<VcfSample> vcfSamples = vcfRecord.getSamples();
    if (vcfSamples.iterator().hasNext()) {
      tokens.add(createFormatToken(vcfRecord));
      tokens.addAll(getSampleTokens(vcfRecord));
    }
    return new VcfRecord(vcfMeta, tokens.toArray(new String[0]));
  }

  public static VcfRecord removeInfoField(VcfRecord vcfRecord, String key){

    VcfMeta vcfMeta = vcfRecord.getVcfMeta();
    List<String> tokens = new ArrayList<>();
    tokens.add(vcfRecord.getChromosome());
    tokens.add(String.valueOf(vcfRecord.getPosition()));

    List<String> identifiers = vcfRecord.getIdentifiers();
    tokens.add(!identifiers.isEmpty() ? identifiers.stream().collect(joining(";")) : MISSING_VALUE);

    tokens.add(vcfRecord.getReferenceAllele().toString());
    List<String> altTokens = vcfRecord.getAlternateAlleles().stream().map(allele -> allele.getAlleleAsString()).collect(toList());
    if (altTokens.size() == 0) {
      tokens.add(MISSING_VALUE);
    } else {
      tokens.add(altTokens.stream().collect(joining(",")));
    }

    tokens.add(vcfRecord.getQuality()==null?MISSING_VALUE:vcfRecord.getQuality());
    tokens.add(vcfRecord.getFilterStatus()==null?MISSING_VALUE:vcfRecord.getFilterStatus());
    tokens.add(removeInfoToken(vcfRecord, key));

    Iterable<VcfSample> vcfSamples = vcfRecord.getSamples();
    if (vcfSamples.iterator().hasNext()) {
      tokens.add(createFormatToken(vcfRecord));
      tokens.addAll(getSampleTokens(vcfRecord));
    }
    return new VcfRecord(vcfMeta, tokens.toArray(new String[0]));
  }

  private static List<String> getSampleTokens(VcfRecord vcfRecord) {
    int firstSample = VcfMeta.COL_FORMAT_IDX + 1;
    return Arrays.asList(Arrays.copyOfRange(vcfRecord.getTokens(), firstSample, firstSample + vcfRecord.getNrSamples()));
  }

  private static String createFormatToken(VcfRecord vcfEntity) {
    String[] formatTokens = vcfEntity.getFormat();
    return stream(formatTokens).collect(joining(":"));
  }

  public static String getRecordIdentifierString(VcfRecord record) {
    return record.getChromosome() + "_" + record.getPosition() + "_" + record.getReferenceAllele()
        + "_" + record.getAlternateAlleles();
  }

  public static void writeRecord(VcfRecord record, VcfWriter vcfWriter) {
    try {
      vcfWriter.write(record);
    } catch (IOException | NullPointerException e) {
      throw new RuntimeException(e);
    }
  }

  public static VcfWriter getVcfWriter(File outputVCFFile, VcfMeta vcfMeta,
      Map<String, String> additionalHeaders, boolean isCompressed) throws IOException {
    VcfWriterFactory vcfWriterFactory = new VcfWriterFactory();
    additionalHeaders.entrySet().forEach(header -> vcfMeta.add(header.getKey(), header.getValue()));
    Format format = isCompressed?Format.GZIP:Format.UNCOMPRESSED;
    return vcfWriterFactory.create(outputVCFFile, vcfMeta, format);
  }

  public static VcfReader getVcfReader(File inputVcfFile, boolean isCompressed) throws FileNotFoundException {
    if(isCompressed) {
      return new VcfReader(
          new BlockCompressedInputStream(new FileInputStream(inputVcfFile)));
    }
    return new VcfReader(
        new InputStreamReader(new FileInputStream(inputVcfFile)));
  }

  static VcfInfo getInfoField(VcfRecord record, String field) {
    for (VcfInfo info : record.getInformation()) {
      if (info.getKey().equals(field)) {
        return info;
      }
    }
    throw new RuntimeException("Info field not found in vcf");
  }

  public static Object getVcfValue(VcfRecord record, String field) {
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
        value = record.getAlternateAlleles().stream().map(Allele::getAlleleAsString);
        break;
      case FORMAT:
        value = record.getFormat();
        break;
      default:
        throw new IllegalArgumentException("Field [" + field + "] is unsupported");
    }
    return value;
  }

  public static Object getSampleValue(VcfRecord record, String field, String sampleId) {
    Object value;
    Integer sampleIndex;
    sampleIndex = getSampleIndex(record.getVcfMeta(), sampleId);
    value = getSampleValue(record, field, sampleIndex);
    return value;
  }

  private static Integer getSampleIndex(VcfMeta vcfMeta, String sampleId) {
    if (sampleId == null) {
      return null;
    }
    int i = 0;
    Iterator<String> names = vcfMeta.getSampleNames().iterator();
    while (names.hasNext()) {
      String name = names.next();
      if (name.equals(sampleId)) {
        return i;
      }
      i++;
    }
    return null;
  }

  private static Object getSampleValue(VcfRecord record, String sampleFieldName, Integer index) {
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
    } else if(sampleFieldIndex != -1){
      value = new ArrayList<String>();
      record.getSamples()
          .forEach(sample -> ((List<String>) value).add(sample.getData(sampleFieldIndex)));
    }else{
      value = null;
    }
    return value;
  }

  public static boolean hasVariant(String gt, String alleleIdx) {
    return !gt.contains(alleleIdx);
  }

  public static boolean deNovo(VcfRecord vcfRecord, Pedigree pedigree, String alleleIdx) {
      String fatherGt = VcfUtils.getSampleValue(vcfRecord, "GT", pedigree.getFather().get().getPatientId()).toString();
      String motherGt = VcfUtils.getSampleValue(vcfRecord, "GT", pedigree.getMother().get().getPatientId()).toString();
      return hasVariant(fatherGt, alleleIdx) && hasVariant(motherGt, alleleIdx);
    }

  public static boolean isHmz(VcfRecord vcfRecord, String sampleId, String alleleIdx) {
    String gt = VcfUtils.getSampleValue(vcfRecord, "GT", sampleId).toString();
    return gt.equals(alleleIdx+"|"+alleleIdx)||gt.equals(alleleIdx+"/"+alleleIdx);
  }
}
