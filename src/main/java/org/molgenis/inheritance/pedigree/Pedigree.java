package org.molgenis.inheritance.pedigree;


import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

public class Pedigree {
  private String family;
  private String patientId;
  private Pedigree motherId;
  private Pedigree fatherId;
  private Sex sex;
  private Affected affected;

  public Pedigree(String family, String patientId, Pedigree motherId,
      Pedigree fatherId, Sex sex, Affected affected) {
    this.family = requireNonNull(family);
    this.patientId = requireNonNull(patientId);
    this.motherId = motherId;
    this.fatherId = fatherId;
    this.sex = requireNonNull(sex);
    this.affected = requireNonNull(affected);
  }

  public String getPatientId() {
    return patientId;
  }

  public Optional<Pedigree> getMother() {
    return motherId == null?Optional.empty():Optional.of(motherId);
  }

  public Optional<Pedigree> getFather() {
    return fatherId == null?Optional.empty():Optional.of(fatherId);
  }

  public Sex getSex() {
    return sex;
  }

  public Affected getAffected() {
    return affected;
  }

  public String getFamily() {
    return family;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Pedigree pedigree = (Pedigree) o;
    return Objects.equals(family, pedigree.family) &&
        Objects.equals(patientId, pedigree.patientId) &&
        Objects.equals(motherId, pedigree.motherId) &&
        Objects.equals(fatherId, pedigree.fatherId) &&
        sex == pedigree.sex &&
        affected == pedigree.affected;
  }

  @Override
  public int hashCode() {
    return Objects.hash(family, patientId, motherId, fatherId, sex, affected);
  }

  @Override
  public String toString() {
    return "Pedigree{" +
        "family='" + family + '\'' +
        ", patientId='" + patientId + '\'' +
        ", motherId=" + motherId +
        ", fatherId=" + fatherId +
        ", sex=" + sex +
        ", affected=" + affected +
        '}';
  }
}
