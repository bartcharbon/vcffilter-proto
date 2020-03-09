package org.molgenis.filter.yaml;

public class ComplexFilter {
  String fields;
  String operator;

  public String getFields() {
    return fields;
  }

  public void setFields(String fields) {
    this.fields = fields;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  @Override
  public String toString() {
    return "ComplexFilter{" +
        "fields='" + fields + '\'' +
        ", operator='" + operator + '\'' +
        '}';
  }
}
