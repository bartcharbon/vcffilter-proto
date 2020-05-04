package org.molgenis.filter.yaml;

public class VepInfoFilter {
  String vepField;
  String infoField;
  String vepIndex;
  String infoIndex;
  String operator;
  String value;
  String seperator;

  public String getVepField() {
    return vepField;
  }

  public void setVepField(String vepField) {
    this.vepField = vepField;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public String getInfoField() {
    return infoField;
  }

  public void setInfoField(String infoField) {
    this.infoField = infoField;
  }

  public String getVepIndex() {
    return vepIndex;
  }

  public void setVepIndex(String vepIndex) {
    this.vepIndex = vepIndex;
  }

  public String getInfoIndex() {
    return infoIndex;
  }

  public void setInfoIndex(String infoIndex) {
    this.infoIndex = infoIndex;
  }

  public String getSeperator() {
    return seperator;
  }

  public void setSeperator(String seperator) {
    this.seperator = seperator;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
