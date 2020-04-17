package org.molgenis.filter.yaml;

import java.util.Objects;

public class Node {
  String name;
  String filter;
  NextStep pass;
  NextStep fail;
  NextStep missing;

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

  public NextStep getMissing() {
    return missing;
  }
  public void setMissing(NextStep fail) {
    this.missing = missing;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Node node = (Node) o;
    return Objects.equals(name, node.name) &&
        Objects.equals(filter, node.filter) &&
        Objects.equals(pass, node.pass) &&
        Objects.equals(fail, node.fail) &&
        Objects.equals(missing, node.missing);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, filter, pass, fail, missing);
  }

  @Override
  public String toString() {
    return "Node{" +
        "name='" + name + '\'' +
        ", filter='" + filter + '\'' +
        ", pass=" + pass +
        ", fail=" + fail +
        ", missing=" + missing +
        '}';
  }
}
