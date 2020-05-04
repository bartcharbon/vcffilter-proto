package org.molgenis.filter.yaml;

public class VepInfoStep {
  String name;
  VepInfoFilter filter;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public VepInfoFilter getFilter() {
    return filter;
  }

  public void setFilter(VepInfoFilter filter) {
    this.filter = filter;
  }

  @Override
  public String toString() {
    return "VepInfoStep{" +
        "name='" + name + '\'' +
        ", filter=" + filter +
        '}';
  }
}
