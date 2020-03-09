package org.molgenis.filter.yaml;

public class Node {
  String name;
  String filter;
  NextStep pass;
  NextStep fail;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public NextStep getPass() {
    return pass;
  }

  public void setPass(NextStep pass) {
    this.pass = pass;
  }

  public NextStep getFail() {
    return fail;
  }

  public void setFail(NextStep fail) {
    this.fail = fail;
  }

  @Override
  public String toString() {
    return "Node{" +
        "name='" + name + '\'' +
        ", filter='" + filter + '\'' +
        ", pass=" + pass +
        ", fail=" + fail +
        '}';
  }
}
