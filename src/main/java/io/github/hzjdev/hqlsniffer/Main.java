package io.github.hzjdev.hqlsniffer;

import com.github.javaparser.ast.CompilationUnit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVWriter;
import io.github.hzjdev.hqlsniffer.metric.MappingMetrics;
import io.github.hzjdev.hqlsniffer.model.HqlAndContext;
import io.github.hzjdev.hqlsniffer.model.output.ProjectSmellCSVLine;
import io.github.hzjdev.hqlsniffer.model.output.ProjectSmellJSONReport;
import io.github.hzjdev.hqlsniffer.detector.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static io.github.hzjdev.hqlsniffer.parser.EntityParser.*;
import static io.github.hzjdev.hqlsniffer.parser.HqlExtractor.getHqlNodes;

public class Main {

    public static void output(String jsonPath, String csvPath, ProjectSmellJSONReport results){
        List<String[]> csvContent = ProjectSmellCSVLine.toCSV(ProjectSmellCSVLine.fromProjectSmellJSONReport(results));
        try (FileOutputStream fos = new FileOutputStream(csvPath);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             CSVWriter writer = new CSVWriter(osw)) {
             writer.writeAll(csvContent);
        }catch(IOException e){
            System.out.println("Output path unavailable: "+csvPath);
        }

        Gson gs = new GsonBuilder()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        try (PrintWriter out = new PrintWriter(jsonPath)) {
            out.println(gs.toJson(results));
        }catch(IOException e){
            System.out.println("Output path unavailable: "+jsonPath);
        }
    }

    public static void exec(String project, String root_path, String output_path){
        //init context
        List<CompilationUnit> cus = new ArrayList<>();
        parseFromDir(root_path+"\\"+project, cus);
        setCusCache(cus);
        List<CompilationUnit> entities = getEntities(cus);
        ProjectSmellJSONReport psr = ProjectSmellJSONReport.fromCompilationUnits(cus);
        List<HqlAndContext> hqls = getHqlNodes(cus);

        //detection
        SmellDetectorFactory
                .createAll(cus, hqls, entities, psr)
                .forEach(SmellDetector::exec);
        MappingMetrics.exec(entities);

        //output
        output(output_path+"\\"+project+"_smells.json", output_path+"\\"+project+"_smells.csv", psr);
    }

    public static void main(String[] args){
        String project;
        String root_path;
        String output_path;
        try {
            project = args[0];
        }catch (Exception e){
            project = "SpringBlog";
        }
        try {
            root_path = args[1];
        }catch (Exception e){
            root_path = "D:\\tools\\hql\\projects";
        }
        try {
            output_path = args[2];
        }catch (Exception e){
            output_path = "D:\\tools\\hql\\projects";
        }
        exec(project, root_path, output_path);
    }
}
