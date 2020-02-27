package org.molgenis.filter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FileResource {

  private Map<String, Integer> headers= new HashMap<>();
  private List<String[]> lines = new ArrayList<>();

  public FileResource(File file){
    loadFile(file);
  }

  private void loadFile(File file){
    Scanner scanner = null;
    try {
      scanner = new Scanner(file);
      loadHeader(scanner);
      loadValues(scanner);
    } catch (FileNotFoundException e) {
      throw new RuntimeException("File '"+file.getName()+"' not found.");
    }
  }

  private void loadValues(Scanner scanner) {
    while(scanner.hasNext()){
      String line = scanner.nextLine();
      lines.add(line.split("\t"));
    }
  }

  private void loadHeader(Scanner scanner) {
    String header = scanner.nextLine();
    int i = 0;
    for(String value : header.split("\t")){
      headers.put(value,i);
      i++;
    }
  }

  public boolean contains(String key, String value){
    for(String[] line : lines){
      String fileValue = line[getIndexOfKey(key)];
      if(fileValue.equals(value)){
        return true;
      }
    }
    return false;
  }

  private int getIndexOfKey(String key){
    return headers.get(key);
  }
}
