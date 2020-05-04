package org.molgenis.filter;

import static java.util.Objects.requireNonNull;

public enum SimpleOperator implements Operator{
  EQ,LESS,LESS_OR_EQUAL,GREATER,GREATER_OR_EQUAL,NOT_EQ,IN,CONTAINS,CONTAINS_WORD,NOT_CONTAINS,NOT_CONTAINS_WORD,CONTAINS_ANY,CONTAINS_ALL,CONTAINS_NONE,PRESENT,NOT_PRESENT

}
