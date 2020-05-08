package org.molgenis.filter;

import static java.util.Objects.requireNonNull;

import java.io.File;

public class OutputConfig {
  private final File outputFile;
  private final String infoFieldPrefix;
  private final ArchivedFile archivedFilterFile;
  private final ArchivedFile routesFile;
  private final boolean isLogRoute;

  public OutputConfig(File outputFile, String infoFieldPrefix, ArchivedFile archivedFilterFile,
      ArchivedFile routesFile, boolean isLogRoute) {
    this.outputFile = requireNonNull(outputFile);
    this.infoFieldPrefix = requireNonNull(infoFieldPrefix);
      this.archivedFilterFile = archivedFilterFile;
      this.routesFile = routesFile;
      this.isLogRoute = requireNonNull(isLogRoute);
    }

    public ArchivedFile getArchivedFilterFile() {
      return archivedFilterFile;
    }

    public ArchivedFile getRoutesFile() {
      return routesFile;
    }

  public File getOutputFile() {
    return outputFile;
  }

  public String getInfoFieldPrefix() {
    return infoFieldPrefix;
  }

  public boolean isLogRoute() {
    return isLogRoute;
  }
}
