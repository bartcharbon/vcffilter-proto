package org.molgenis.filter;

import java.util.Optional;
import org.molgenis.vcf.meta.VcfMetaInfo;
import org.molgenis.vcf.meta.VcfMetaInfo.Type;

public class MultiValueField implements Field {
  private final FieldType fieldType;
  private final String name;
  private final String separator;
  private final Optional<String> subFieldName;
  private final Optional<Integer> index;
  private final VcfMetaInfo.Type type;

  public MultiValueField(FieldType fieldType, String name, String separator,
      Optional<String> subFieldName, Optional<Integer> index,
      Type type) {
    this.fieldType = fieldType;
    this.name = name;
    this.separator = separator;
    this.subFieldName = subFieldName;
    this.index = index;
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

  public String getSeparator() {
    return separator;
  }

  public Optional<String> getSubFieldName() {
    return subFieldName;
  }

  public Optional<Integer> getIndex() {
    return index;
  }

  public Type getType() {
    return type;
  }
}
