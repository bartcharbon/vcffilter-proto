package org.molgenis.filter;

import static java.util.Objects.requireNonNull;

import org.molgenis.vcf.VcfRecord;

public class FilterResult {
  FilterResultEnum result;
  VcfRecord record;

  public FilterResult(FilterResultEnum result, VcfRecord record) {
    this.result = requireNonNull(result);
    this.record = requireNonNull(record);
  }

  public FilterResultEnum getResult() {
    return result;
  }

  public VcfRecord getRecord() {
    return record;
  }

  public void setRecord(VcfRecord record) {
    this.record = record;
  }
}
