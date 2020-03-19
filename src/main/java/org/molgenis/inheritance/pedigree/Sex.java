package org.molgenis.inheritance.pedigree;

public enum Sex {
  MALE,FEMALE,UNKNOWN;

  public static Sex get(String value){
    if(value.equals("1")){
      return Sex.MALE;
    }
    if(value.equals("2")){
      return Sex.FEMALE;
    }
    return Sex.UNKNOWN;
  }
}
