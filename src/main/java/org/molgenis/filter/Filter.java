package org.molgenis.filter;

import org.molgenis.vcf.VcfRecord;

public interface Filter {
  FilterResult filter(VcfRecord vcfRecord);
  String getName();
}
