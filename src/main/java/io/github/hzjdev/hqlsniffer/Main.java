package io.github.hzjdev.hqlsniffer;

import com.github.javaparser.ast.CompilationUnit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVWriter;
import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.detector.SmellDetectorFactory;
import io.github.hzjdev.hqlsniffer.metric.MappingMetrics;
import io.github.hzjdev.hqlsniffer.model.HqlAndContext;
import io.github.hzjdev.hqlsniffer.model.output.Metric;
import io.github.hzjdev.hqlsniffer.model.output.ProjectSmellCSVLine;
import io.github.hzjdev.hqlsniffer.model.output.ProjectSmellJSONReport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.*;
import static io.github.hzjdev.hqlsniffer.parser.HqlExtractor.getHqlNodes;

public class Main {

    /**
     * export smell detection results
     * @param jsonPath path of the json file
     * @param csvPath path of the csv file
     * @param results results
     */
    public static void outputSmells(String jsonPath, String csvPath, ProjectSmellJSONReport results) {
        //wirte to csv
        List<String[]> csvContent = ProjectSmellCSVLine.toCSV(ProjectSmellCSVLine.fromProjectSmellJSONReport(results));
        try (FileOutputStream fos = new FileOutputStream(csvPath);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             CSVWriter writer = new CSVWriter(osw)) {
            writer.writeAll(csvContent);
        } catch (IOException e) {
            System.out.println("Output path unavailable: " + csvPath);
        }

        //write to json
        Gson gs = new GsonBuilder()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        try (PrintWriter out = new PrintWriter(jsonPath)) {
            out.println(gs.toJson(results));
        } catch (IOException e) {
            System.out.println("Output path unavailable: " + jsonPath);
        }
    }

    /**
     * export metric calculation results
     * @param csvPath path of the csv file
     * @param metrics results
     */
    public static void outputMetrics(String csvPath, List<Metric> metrics) {
        //wirte to csv
        List<String[]> csvContent = Metric.toCSV(metrics);
        try (FileOutputStream fos = new FileOutputStream(csvPath);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             CSVWriter writer = new CSVWriter(osw)) {
            writer.writeAll(csvContent);
        } catch (IOException e) {
            System.out.println("Output path unavailable: " + csvPath);
        }
    }

    /**
     * start detection
     * @param project project name
     * @param root_path project path
     * @param output_path output path
     */
    public static void exec(String project, String root_path, String output_path) {
        //init context
        List<CompilationUnit> cus = new ArrayList<>();
        parseFromDir(root_path + "\\" + project, cus);
        setCusCache(cus);
        List<CompilationUnit> entities = getEntities(cus);
        ProjectSmellJSONReport psr = ProjectSmellJSONReport.fromCompilationUnits(cus);
        List<HqlAndContext> hqls = getHqlNodes(cus);

        //detection
        SmellDetectorFactory
                .createAll(cus, hqls, entities, psr)
                .forEach(SmellDetector::exec);
        List<Metric> metrics = MappingMetrics.exec(entities);

        //output
        outputSmells(output_path + "\\" + project + "_smells.json", output_path + "\\" + project + "_smells.csv", psr);
        outputMetrics(output_path + "\\" + project + "_metrics.csv", metrics);
    }

    /**
     * Entrance of the application
     * @param args arguments from commandline
     */
    public static void main(String[] args) {
        String project;
        String root_path;
        String output_path;
        try {
            project = args[0];
        } catch (Exception e) {
            project = "SpringBlog";
        }
        try {
            root_path = args[1];
        } catch (Exception e) {
            root_path = "D:\\tools\\hql\\projects";
        }
        try {
            output_path = args[2];
        } catch (Exception e) {
            output_path = "D:\\tools\\hql\\projects";
        }
        exec(project, root_path, output_path);
    }
}
