package org.molgenis.filter;

import org.molgenis.vcf.meta.VcfMetaInfo;
import org.molgenis.vcf.meta.VcfMetaInfo.Type;

public class InfoField implements Field {
  private final FieldType fieldType;
  private final String name;
  private final VcfMetaInfo.Type type;

  public InfoField(FieldType fieldType, String name, Type type) {
    this.fieldType = fieldType;
    this.name = name;
    this.type = type;
  }

  @Override
  public FieldType getFieldType() {
    return fieldType;
  }

  @Override
  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }
}
