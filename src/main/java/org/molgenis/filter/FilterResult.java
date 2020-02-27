package org.molgenis.filter;

import static java.util.Objects.requireNonNull;

import org.molgenis.vcf.VcfRecord;

public class FilterResult {
  boolean isPass;
  VcfRecord record;

  public FilterResult(boolean isPass, VcfRecord record) {
    this.isPass = requireNonNull(isPass);
    this.record = requireNonNull(record);
  }

  public boolean getPass() {
    return isPass;
  }

  public VcfRecord getRecord() {
    return record;
  }

  public void setRecord(VcfRecord record) {
    this.record = record;
  }
}
