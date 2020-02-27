package org.molgenis.filter;

import static java.util.Objects.requireNonNull;

public enum SimpleOperator implements Operator{
  EQ("=="),LESS("<"),LESS_OR_EQUAL("<="),GREATER(">"),GREATER_OR_EQUAL(">="),NOT_EQ("!="),IN("in"),CONTAINS("contains");

  private final String symbol;

  SimpleOperator(String symbol) {
    this.symbol = requireNonNull(symbol);
  }

  @Override
  public String getSymbol() {
    return symbol;
  }
}
