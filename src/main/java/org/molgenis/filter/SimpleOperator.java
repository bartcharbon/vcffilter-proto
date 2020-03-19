package org.molgenis.filter;

import static java.util.Objects.requireNonNull;

public enum SimpleOperator implements Operator{
  EQ("=="),LESS("<"),LESS_OR_EQUAL("<="),GREATER(">"),GREATER_OR_EQUAL(">="),NOT_EQ("!="),IN("in"),CONTAINS("contains"),CONTAINS_WORD("containsWord"),NOT_CONTAINS("notContains"),NOT_CONTAINS_WORD("notContainsWord"),CONTAINS_ANY("containsAny"),CONTAINS_ALL("containsAll"),CONTAINS_NONE("containsNone");
  private final String symbol;

  SimpleOperator(String symbol) {
    this.symbol = requireNonNull(symbol);
  }

  @Override
  public String getSymbol() {
    return symbol;
  }
}
