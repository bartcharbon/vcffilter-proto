package org.molgenis.filter.yaml;

public class NextStep {
  String next;
  String label;

  public String getNext() {
    return next;
  }

  public void setNext(String next) {
    this.next = next;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return "NextStep{" +
        "next='" + next + '\'' +
        ", label='" + label + '\'' +
        '}';
  }
}
