package org.molgenis.filter;

public class SampleField implements Field {
  private final FieldType fieldType;
  private final String name;

  public SampleField(FieldType fieldType, String name) {
    this.fieldType = fieldType;
    this.name = name;
  }

  @Override
  public FieldType getFieldType() {
    return fieldType;
  }

  @Override
  public String getName() {
    return name;
  }
}
