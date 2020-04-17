package org.molgenis.filter.yaml;

import java.util.List;
import org.molgenis.filter.VepFilter;

public class Steps {
  private List<SimpleStep> simple;
  private List<SimpleStep> sample;
  private List<SimpleStep> info;
  private List<FlagStep> infoFlag;
  private List<VepStep> vep;
  private List<ComplexStep> complex;
  private List<SimpleStep> custom;

  public List<SimpleStep> getSimple() {
    return simple;
  }

  public void setSimple(List<SimpleStep> simple) {
    this.simple = simple;
  }

  public List<SimpleStep> getCustom() {
    return custom;
  }

  public void setCustom(List<SimpleStep> custom) {
    this.custom = custom;
  }

  public List<SimpleStep> getSample() {
    return sample;
  }

  public void setSample(List<SimpleStep> sample) {
    this.sample = sample;
  }

  public List<SimpleStep> getInfo() {
    return info;
  }

  public void setInfo(List<SimpleStep> info) {
    this.info = info;
  }

  public List<FlagStep> getInfoFlag() {
    return infoFlag;
  }

  public void setInfoFlag(List<FlagStep> info) {
    this.infoFlag = info;
  }

  public List<VepStep> getVep() {
    return vep;
  }

  public void setVep(List<VepStep> vep) {
    this.vep = vep;
  }

  public List<ComplexStep> getComplex() {
    return complex;
  }

  public void setComplex(List<ComplexStep> complex) {
    this.complex = complex;
  }

  @Override
  public String toString() {
    return "Steps{" +
        "simple=" + simple +
        ", sample=" + sample +
        ", info=" + info +
        ", vep=" + vep +
        ", complex=" + complex +
        '}';
  }
}
