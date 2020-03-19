package org.molgenis.filter.yaml;

public class FlagStep {
  String name;
  FlagFilter filter;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public FlagFilter getFilter() {
    return filter;
  }

  public void setFilter(FlagFilter filter) {
    this.filter = filter;
  }

  @Override
  public String toString() {
    return "FlagFilter{" +
        "name='" + name + '\'' +
        ", filter=" + filter +
        '}';
  }
}
