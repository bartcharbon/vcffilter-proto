package org.molgenis.filter.yaml;

public class ComplexStep {
  String name;
  ComplexFilter filter;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ComplexFilter getFilter() {
    return filter;
  }

  public void setFilter(ComplexFilter filter) {
    this.filter = filter;
  }

  @Override
  public String toString() {
    return "ComplexStep{" +
        "name='" + name + '\'' +
        ", filter=" + filter +
        '}';
  }
}
