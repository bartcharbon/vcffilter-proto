package org.molgenis.inheritance;

import static org.molgenis.inheritance.pedigree.PedigreeUtils.getSingleParentPedigree;
import static org.molgenis.vcf.utils.VcfUtils.getRecordIdentifierString;
import static org.molgenis.vcf.utils.VcfUtils.getSampleValue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterResult;
import org.molgenis.filter.FilterResultEnum;
import org.molgenis.filter.FilterUtils;
import org.molgenis.inheritance.pedigree.Affected;
import org.molgenis.inheritance.pedigree.Pedigree;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.utils.VcfUtils;
import org.molgenis.vcf.utils.VepUtils;

public class TwiceOneGeneFilter implements Filter {

  private static final String ALLELE_IDX = "1";
  private final Iterable<VcfRecord> records;
  private final Pedigree pedigree;

  public TwiceOneGeneFilter(Iterable<VcfRecord> records, Pedigree pedigree) {
    this.records = records;
    this.pedigree = pedigree;
  }
  @Override
  public FilterResult filter(VcfRecord vcfRecord) {
    Set<String> genes = VepUtils.getVepValues("SYMBOL", vcfRecord);
    List<String> filteredGenes = genes.stream().filter(gene -> !gene.isEmpty()).distinct()
        .collect(Collectors.toList());
    if (!filteredGenes.isEmpty()) {
      if (filteredGenes.size() > 1) {
        System.err.println(
            "More than one gene found for record: " + getRecordIdentifierString(vcfRecord)
                + "using the first: " + filteredGenes.get(0));
      }
      List<VcfRecord> variantsInGene = StreamSupport.stream(records.spliterator(), false)
          .filter(record -> inGene(filteredGenes.get(0), record)).collect(
              Collectors.toList());
      if(variantsInGene.size() > 1) {
        return new FilterResult(FilterUtils
            .toFilterResultEnum(parentNotHasVariant(vcfRecord, variantsInGene, pedigree)), vcfRecord);
      }
    }else{
      System.err.println("No gene found for variant: " + getRecordIdentifierString(vcfRecord));
    }
    return new FilterResult(FilterResultEnum.FALSE, vcfRecord);
  }

  @Override
  public String getName() {
    return "TwiceInOneGene";
  }

  private boolean parentNotHasVariant(VcfRecord vcfRecord, List<VcfRecord> variantsInGene,
      Pedigree pedigree) {
    boolean result = true;
    if(pedigree.getFather().isPresent() || pedigree.getMother().isPresent()) {
      Pedigree parent = getSingleParentPedigree(pedigree);
      boolean parentHasVariant = VcfUtils
          .hasVariant(getSampleValue(vcfRecord, "GT", parent.getPatientId()).toString(),
              ALLELE_IDX);
      //parent has variant under test
      //for variants excluding under test
      for (VcfRecord record : variantsInGene) {
        if (!record.equals(vcfRecord)) {
          boolean parentHasOtherVariant = VcfUtils
              .hasVariant(getSampleValue(record, "GT", parent.getPatientId()).toString(),
                  ALLELE_IDX);
          if (parent.getAffected() == Affected.TRUE) {
            //Parent does not have both of the variants
            if (!parentHasVariant || !parentHasOtherVariant) {
              return false;
            }
          } else {
            //Parent does not have either one of the variants
            if (!parentHasVariant && !parentHasOtherVariant) {
              return false;
            }
          }
        }
      }
    }
    return result;
  }

  private boolean inGene(String targetGene, VcfRecord record) {
    Set<String> genes = VepUtils.getVepValues("SYMBOL", record);
    for(String gene : genes){
      if(gene.equals(targetGene)){
        return true;
      }
    }
    return false;
  }

}
