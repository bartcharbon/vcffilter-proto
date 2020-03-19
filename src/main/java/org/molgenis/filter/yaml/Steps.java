package org.molgenis.filter.yaml;

import java.util.List;

public class Steps {
  List<SimpleStep> simple;
  List<SimpleStep> sample;
  List<SimpleStep> info;
  List<FlagStep> infoFlag;
  List<SimpleStep> vep;
  List<ComplexStep> complex;

  public List<SimpleStep> getSimple() {
    return simple;
  }

  public void setSimple(List<SimpleStep> simple) {
    this.simple = simple;
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

  public List<SimpleStep> getVep() {
    return vep;
  }

  public void setVep(List<SimpleStep> vep) {
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
