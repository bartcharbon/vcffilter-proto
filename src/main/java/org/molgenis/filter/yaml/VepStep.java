package org.molgenis.filter.yaml;

public class VepStep {
  String name;
  VepFilter filter;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public VepFilter getFilter() {
    return filter;
  }

  public void setFilter(VepFilter filter) {
    this.filter = filter;
  }

  @Override
  public String toString() {
    return "VepStep{" +
        "name='" + name + '\'' +
        ", filter=" + filter +
        '}';
  }
}
