package org.molgenis.vkgl;

import static java.util.Arrays.asList;
import static org.molgenis.filter.FilterTool.GZIP_EXTENSION;
import static org.molgenis.vcf.utils.VcfUtils.getVcfReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.molgenis.genotype.Allele;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.utils.VcfUtils;
import org.molgenis.vcf.utils.VepUtils;

public class VkglValidator {

  public static final String INPUT = "input";
  public static final String SYMBOLS = "symbols";
  private static final String TAB = "\t";

  public static void main(String[] args) {
    OptionParser parser = createOptionParser();
    OptionSet options = parser.parse(args);
    new VkglValidator().run(options);
  }

  private static OptionParser createOptionParser() {
    OptionParser parser = new OptionParser();
    parser.acceptsAll(asList("i", INPUT), "Input VCF file").withRequiredArg().ofType(File.class);
    parser.acceptsAll(asList("s", SYMBOLS), "Symbols file").withRequiredArg().ofType(File.class);
    return parser;
  }

  public void run(OptionSet options) {
    File inputFile = (File) options.valueOf(INPUT);
    if (!inputFile.exists()) {
      System.out.println("Input VCF file not found at " + inputFile);
      return;
    } else if (inputFile.isDirectory()) {
      System.out.println("Input VCF file is a directory, not a file!");
      return;
    }
    File symbolsFile = (File) options.valueOf(SYMBOLS);
    if (!symbolsFile.exists()) {
      System.out.println("symbolsFile not found at " + symbolsFile);
      return;
    } else if (symbolsFile.isDirectory()) {
      System.out.println("symbolsFile is a directory, not a file!");
      return;
    }

    Map<String, String> umcg = processLab(
        "C:\\Users\\bartc\\Downloads\\vkgl_umcg_2020-04-10_07_28_57.csv");
    Map<String, String> umcu = processLab(
        "C:\\Users\\bartc\\Downloads\\vkgl_umcu_2020-04-10_07_29_37.csv");
    Map<String, String> lumc = processLab(
        "C:\\Users\\bartc\\Downloads\\vkgl_lumc_2020-04-10_07_22_34.csv");
    Map<String, String> radboud = processLab(
        "C:\\Users\\bartc\\Downloads\\vkgl_radboud_mumc_2020-04-10_07_26_28.csv");
    Map<String, String> nki = processLab(
        "C:\\Users\\bartc\\Downloads\\vkgl_nki_2020-04-10_07_26_07.csv");
    Map<String, String> amc = processLab(
        "C:\\Users\\bartc\\Downloads\\vkgl_amc_2020-04-10_07_20_27.csv");
    Map<String, String> erasmus = processLab(
        "C:\\Users\\bartc\\Downloads\\vkgl_erasmus_2020-04-10_07_35_34.csv");
    Map<String, String> vumc = processLab(
        "C:\\Users\\bartc\\Downloads\\vkgl_vumc_2020-04-10_07_28_29.csv");

    Map<String, Position> positions = processPositions(
        "C:\\Users\\bartc\\Downloads\\mart_export (4).txt");
    Scanner scanner = null;
    try {
      scanner = new Scanner(symbolsFile);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    Map<String, List<String>> symbols = new HashMap<>();
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] split = line.split("\t");
      List<String> synonyms = Arrays.asList(split[8].split("\\|")).stream().map(String::toUpperCase)
          .map(symbol -> symbol.replace("\"", "")).collect(
              Collectors.toList());
      synonyms.add(split[1].toUpperCase());
      synonyms.addAll(Arrays.asList(split[10].split("\\|")).stream().map(String::toUpperCase)
          .map(symbol -> symbol.replace("\"", "")).collect(
              Collectors.toList()));
      for (String syn : synonyms) {
        symbols.put(syn, synonyms);
      }
    }

    String fullInputFileName = inputFile.getName();
    String extension = fullInputFileName.substring(fullInputFileName.indexOf('.'));
    File outputFile = new File(inputFile.getAbsolutePath() + ".output");
    try {
      Writer writer = new FileWriter(outputFile);
      StringBuilder stringBuilder2 = new StringBuilder();
      stringBuilder2.append("Chromosome").append(TAB);
      stringBuilder2.append("position").append(TAB);
      stringBuilder2.append("Reference").append(TAB);
      stringBuilder2.append("Alternative").append(TAB);
      stringBuilder2.append("ConsensusGene").append(TAB);
      stringBuilder2.append("GeneOnCorrectChromosome")
          .append(TAB);
      stringBuilder2.append("PositionWithinGene").append(TAB);
      stringBuilder2.append("VEPGenes").append(TAB);
      stringBuilder2.append("PositionWithinVEPGenes").append(TAB);
      stringBuilder2.append("SynonymsForConsensusGene")
          .append(TAB);
      stringBuilder2.append("UMCG").append(TAB);
      stringBuilder2.append("UMCU").append(TAB);
      stringBuilder2.append("LUMC").append(TAB);
      stringBuilder2.append("AMC").append(TAB);
      stringBuilder2.append("NKI").append(TAB);
      stringBuilder2.append("Erasmus").append(TAB);
      stringBuilder2.append("Radboud").append(TAB);
      stringBuilder2.append("VUMC").append(TAB);
      stringBuilder2.append("LabsInConflict").append("\n");
      writer.write(stringBuilder2.toString());

      VcfReader reader = getVcfReader(inputFile, extension.endsWith(GZIP_EXTENSION));
      for (VcfRecord record : reader) {
        Set<String> genes = VepUtils.getVepValues("SYMBOL", record);
        Set<String> vepGenes = new HashSet<>();
        for (String vepGene : genes) {
          vepGenes.add(vepGene.toUpperCase());
        }
        String consensusGene = VcfUtils.getInfoFieldValue(record, "GENE").toUpperCase();
        boolean match = false;
        boolean isUnknown = false;

        for (String vep : vepGenes) {
          if (symbols.get(vep) != null) {
            if (symbols.get(vep).contains(consensusGene)) {
              match = true;
            }
          }
        }
        if (symbols.get(consensusGene) == null) {
          isUnknown = true;
        }
        if (!match && !isUnknown) {
          System.out.println("Mismatch between VKGL gene and VEP genes: " + VcfUtils
              .getRecordIdentifierString(record) + " VEP: " + String.join(",", vepGenes) + " VKGL: "
              + consensusGene);
          StringBuilder stringBuilder = new StringBuilder();
          String id = createId(record.getChromosome(), "" + record.getPosition(),
              record.getReferenceAllele().toString(),
              String.join(",", record.getAlternateAlleles().stream().map(Allele::toString).collect(
                  Collectors.toList()).get(0)));
          stringBuilder.append(record.getChromosome()).append(TAB);
          stringBuilder.append(record.getPosition()).append(TAB);
          stringBuilder.append(record.getReferenceAllele()).append(TAB);
          stringBuilder.append(
              String.join(",", record.getAlternateAlleles().stream().map(Allele::toString).collect(
                  Collectors.toList()))).append(TAB);
          String consensusGene_Pos =
              consensusGene + "(" + positions.get(consensusGene.toUpperCase()) + ")";
          stringBuilder.append(consensusGene_Pos).append(TAB);
          stringBuilder.append(
              chromMatch(record.getChromosome(), positions.get(consensusGene.toUpperCase())))
              .append(TAB);
          stringBuilder.append(posMatch(record.getChromosome(), record.getPosition(),
              positions.get(consensusGene.toUpperCase()))).append(TAB);
          List<String> vepGenesPos = vepGenes.stream()
              .map(gene -> gene = gene + "(" + positions.get(gene.toUpperCase()) + ")").collect(
                  Collectors.toList());
          stringBuilder.append(String.join(",", vepGenesPos)).append(TAB);
          List<String> vepGenesPosMatch = vepGenes.stream()
              .map(gene -> gene = gene + "(" + posMatch(record.getChromosome(), record.getPosition(), positions.get(gene.toUpperCase())) + ")").collect(
                  Collectors.toList());
          stringBuilder.append(String.join(",", vepGenesPosMatch)).append(TAB);
          stringBuilder.append(String.join(",", symbols.get(consensusGene.toUpperCase())))
              .append(TAB);
          stringBuilder.append(umcg.get(id)!=null?umcg.get(id):"").append(TAB);
          stringBuilder.append(umcu.get(id)!=null?umcu.get(id):"").append(TAB);
          stringBuilder.append(lumc.get(id)!=null?lumc.get(id):"").append(TAB);
          stringBuilder.append(amc.get(id)!=null?amc.get(id):"").append(TAB);
          stringBuilder.append(nki.get(id)!=null?nki.get(id):"").append(TAB);
          stringBuilder.append(erasmus.get(id)!=null?erasmus.get(id):"").append(TAB);
          stringBuilder.append(radboud.get(id)!=null?radboud.get(id):"").append(TAB);
          stringBuilder.append(vumc.get(id)!=null?vumc.get(id):"").append(TAB);
          stringBuilder.append(labsMatch(umcg.get(id), umcu.get(id), lumc.get(id), amc.get(id), nki.get(id), erasmus.get(id), radboud.get(id), vumc.get(id))).append("\n");
          writer.write(stringBuilder.toString());
        }
      }
      writer.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Boolean labsMatch(String umcg, String umcu, String lumc, String amc, String nki, String erasmus, String radboud, String vumc) {
    List<String> genes = Arrays.asList(umcg,umcu,lumc,amc,nki,erasmus,radboud,vumc).stream().filter(
        Objects::nonNull).collect(Collectors.toList());
    String gene = null;
    for(String g : genes){
      if(gene == null){
        gene = g;
      }
      if(!gene.equals(g)){
        return true;
      }
    }
    return false;
  }

  private Boolean posMatch(String chromosome, int position, Position pos) {
    if (pos == null) {
      return null;
    }
    if (!pos.getChrom().equals(chromosome)) {
      return false;
    }
    if(pos.getStart() < pos.getStop()) {
      if (position >= pos.getStart() && position <= pos.getStop()) {
        return true;
      }
    }else{
      //reverse strand
      if (position >= pos.getStop() && position <= pos.getStart()) {
        return true;
      }
    }
    return false;
  }

  private Boolean chromMatch(String chromosome, Position pos) {
    if (pos == null) {
      return null;
    }
    if (!pos.getChrom().equals(chromosome)) {
      return false;
    }
    return true;
  }

  public static String createId(String chromosome, String position, String ref, String alt) {
    return chromosome + "_" + position + "_" + ref + "_" + alt;
  }

  private Map<String, Position> processPositions(String positionsPath) {
    Map<String, Position> result = new HashMap<>();
    Scanner scanner = null;
    try {
      File positionsFile = new File(positionsPath);
      scanner = new Scanner(positionsFile);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    scanner.nextLine();
    while (scanner.hasNextLine()) {
      String[] line = scanner.nextLine().split("\t");
      if (line.length >= 6) {
        String gene = line[5];
        Position position = new Position(line[4], Integer.valueOf(line[2]),
            Integer.valueOf(line[3]));
        result.put(gene.toUpperCase(), position);
      }
    }
    return result;
  }

  public Map<String, String> processLab(String labFilePath) {
    Map<String, String> result = new HashMap<>();
    Scanner scanner = null;
    try {
      File labFile = new File(labFilePath);
      scanner = new Scanner(labFile);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    while (scanner.hasNextLine()) {
      String[] line = scanner.nextLine().split(",");
      String identifier = createId(line[1].replace("\"", ""), line[2].replace("\"", ""),
          line[4].replace("\"", ""), line[5].replace("\"", ""));
      String gene = line[6].replace("\"", "");
      String current = result.get(identifier);
      if(current != null && !current.isEmpty()){
        gene = current + "," + gene;
      }
      result.put(identifier, gene);
    }
    return result;
  }
}
