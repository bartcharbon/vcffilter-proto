package org.molgenis.filter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import org.molgenis.vcf.VcfRecord;

class CustomFilterTest {

  @org.junit.jupiter.api.Test
  void filter() {
    VcfRecord record = mock(VcfRecord.class);
    when(record.getChromosome()).thenReturn("X");
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("testPythonFilter.py").getFile());
    CustomFilter filter = new CustomFilter("test", "#CHROM", file, null);
    assertTrue(filter.filter(record).result == FilterResultEnum.TRUE);
  }
}