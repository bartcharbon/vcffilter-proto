package org.molgenis.filter;

public class BasicField implements Field {
  private final FieldType fieldType;
  private final String name;

  public BasicField(FieldType fieldType, String name) {
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
