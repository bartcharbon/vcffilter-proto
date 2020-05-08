package org.molgenis.filter;

import java.io.File;
import java.util.Optional;

public class ArchivedFile {
  private boolean isActive;
  private final Optional<String> headerName;
  private final Optional<File> file;

  public ArchivedFile(boolean isActive, String headerName, File file) {
    this.isActive = isActive;
    this.headerName = isActive ? Optional.of(headerName): Optional.empty();
    this.file = isActive ? Optional.of(file): Optional.empty();
  }

  public boolean isActive() {
    return isActive;
  }

  public Optional<String> getHeaderName() {
    return headerName;
  }

  public Optional<File> getFile() {
    return file;
  }
}
