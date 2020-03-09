package org.molgenis.filter.yaml;
public class FilterSpec {
  String name;
  String date;
  Steps steps;
  Tree tree;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public Steps getSteps() {
    return steps;
  }

  public void setSteps(Steps steps) {
    this.steps = steps;
  }

  public Tree getTree() {
    return tree;
  }

  public void setTree(Tree tree) {
    this.tree = tree;
  }

  @Override
  public String toString() {
    return "FilterSpec{" +
        "name='" + name + '\'' +
        ", date='" + date + '\'' +
        ", steps=" + steps +
        ", tree=" + tree +
        '}';
  }
}
