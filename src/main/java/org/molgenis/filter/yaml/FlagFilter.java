package org.molgenis.filter.yaml;

public class FlagFilter {
  String field;
  String operator;

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  @Override
  public String toString() {
    return "SimpleFilter{" +
        "field='" + field + '\'' +
        ", operator='" + operator + '\'' +
        '}';
  }
}
