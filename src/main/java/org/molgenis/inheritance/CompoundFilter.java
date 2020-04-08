package org.molgenis.inheritance;

import static org.molgenis.vcf.utils.VcfUtils.deNovo;
import static org.molgenis.vcf.utils.VcfUtils.getRecordIdentifierString;
import static org.molgenis.vcf.utils.VcfUtils.getSampleValue;
import static org.molgenis.vcf.utils.VcfUtils.hasVariant;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterResult;
import org.molgenis.filter.FilterResultEnum;
import org.molgenis.filter.FilterUtils;
import org.molgenis.inheritance.pedigree.Pedigree;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.utils.VepUtils;

public class CompoundFilter implements Filter {

  private final Iterable<VcfRecord> records;
  private final Pedigree pedigree;

  public CompoundFilter(Iterable<VcfRecord> records, Pedigree pedigree) {
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
      List<VcfRecord> sameGeneRecords = StreamSupport.stream(records.spliterator(), false)
          .filter(record -> inGene(filteredGenes.get(0), record)).collect(
              Collectors.toList());

      //any variant in gene deNovo
      boolean deNovo = !StreamSupport.stream(records.spliterator(), false)
          .filter(record -> deNovo(record, pedigree, "1")).collect(
              Collectors.toList()).isEmpty();

      if (sameGeneRecords.size() > 1) {
        boolean fatherHasVariant = false;
        boolean motherHasVariant = false;
        for (VcfRecord record : sameGeneRecords) {
          //FIXME multi allelic vcf not supported, picking "1" as the genotype of the patient
          if (hasVariant(
              getSampleValue(record, "GT", pedigree.getFather().get().getPatientId()).toString(),
              "1")) {
            fatherHasVariant = true;
          }
          if (hasVariant(
              getSampleValue(record, "GT", pedigree.getFather().get().getPatientId()).toString(),
              "1")) {
            motherHasVariant = true;
          }

          if (fatherHasVariant && motherHasVariant) {
            return new FilterResult(FilterResultEnum.TRUE, vcfRecord);
          } else if (deNovo) {
            return new FilterResult(
                FilterUtils.toFilterResultEnum(fatherHasVariant || motherHasVariant), vcfRecord);
          } else {
            return new FilterResult(FilterResultEnum.FALSE, vcfRecord);
          }
        }
      }
      return new FilterResult(FilterResultEnum.FALSE, vcfRecord);
    } else {
      System.err.println("No gene found for variant: " + getRecordIdentifierString(vcfRecord));
    }
    return new FilterResult(FilterResultEnum.FALSE, vcfRecord);
  }


  private boolean inGene(String targetGene, VcfRecord record) {
    Set<String> genes = VepUtils.getVepValues("SYMBOL", record);
    for (String gene : genes) {
      if (gene.equals(targetGene)) {
        return true;
      }
    }
    return false;
  }
}
