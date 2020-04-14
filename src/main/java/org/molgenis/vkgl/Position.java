package org.molgenis.vkgl;

public class Position {
  String chrom;
  int start;
  int stop;

  public Position(String chrom, int start, int stop) {
    this.chrom = chrom;
    this.start = start;
    this.stop = stop;
  }

  public String getChrom() {
    return chrom;
  }

  public int getStart() {
    return start;
  }

  public int getStop() {
    return stop;
  }

  @Override
  public String toString() {
    return chrom + ":"+ start + "-"+ stop;
  }
}
