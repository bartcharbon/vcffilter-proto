package org.molgenis.filter.yaml;

public class SimpleStep {
  String name;
  SimpleFilter filter;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public SimpleFilter getFilter() {
    return filter;
  }

  public void setFilter(SimpleFilter filter) {
    this.filter = filter;
  }

  @Override
  public String toString() {
    return "SimpleStep{" +
        "name='" + name + '\'' +
        ", filter=" + filter +
        '}';
  }
}
