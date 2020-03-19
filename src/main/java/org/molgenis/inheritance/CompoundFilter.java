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
import org.molgenis.inheritance.pedigree.Pedigree;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.utils.VcfUtils;
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
    if(genes.size() != 0) {
      String gene = genes.iterator().next();
      if (genes.size() > 1) {
        System.err.println(
            "More than one gene found for record: " + getRecordIdentifierString(vcfRecord)
                + "using the first: " + gene);
      }
      List<VcfRecord> sameGeneRecords = StreamSupport.stream(records.spliterator(), false).filter(record -> inGene(gene, record)).collect(
              Collectors.toList());

      //any variant in gene deNovo
      boolean deNovo = StreamSupport.stream(records.spliterator(), false).filter(record -> deNovo(record, pedigree, "1")).collect(
          Collectors.toList()).size() > 0;

      if(sameGeneRecords.size() > 1){
        boolean fatherHasVariant = false;
        boolean motherHasVariant = false;
        for(VcfRecord record : sameGeneRecords)
        {
          //FIXME multi allelic vcf not supported, picking "1" as the genotype of the patient
          if (hasVariant(getSampleValue(record, "GT", pedigree.getFather().get().getPatientId()).toString(), "1")) {
            fatherHasVariant = true;
          }
          if (hasVariant(getSampleValue(record, "GT", pedigree.getFather().get().getPatientId()).toString(), "1")) {
            motherHasVariant = true;
        }

        if (fatherHasVariant && motherHasVariant)
          return new FilterResult(true, vcfRecord);
        else if (deNovo)
          return new FilterResult(fatherHasVariant || motherHasVariant, vcfRecord);
        else
          return new FilterResult(false, vcfRecord);
      }
    }
      return new FilterResult(false, vcfRecord);
    }else{
      System.err.println("No gene found for variant: " + getRecordIdentifierString(vcfRecord));
    }
    return new FilterResult(false, vcfRecord);
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