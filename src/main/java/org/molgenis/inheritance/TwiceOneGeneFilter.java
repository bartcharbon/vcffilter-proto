package org.molgenis.inheritance;

import static org.molgenis.vcf.utils.VcfUtils.getRecordIdentifierString;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterResult;
import org.molgenis.inheritance.pedigree.Pedigree;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.utils.VepUtils;

public class TwiceOneGeneFilter implements Filter {
  private final Iterable<VcfRecord> records;
  private final Pedigree pedigree;

  public TwiceOneGeneFilter(Iterable<VcfRecord> records, Pedigree pedigree) {
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
      return new FilterResult(StreamSupport.stream(records.spliterator(), false).filter(record -> inGene(gene, record)).collect(
          Collectors.toList()).size() >= 2, vcfRecord);
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
