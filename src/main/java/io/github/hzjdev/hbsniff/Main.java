/*
 * This file is part of HBSniff.
 *
 *     HBSniff is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     HBSniff is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with HBSniff.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.hzjdev.hbsniff;

import com.github.javaparser.ast.CompilationUnit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVWriter;
import io.github.hzjdev.hbsniff.detector.SmellDetector;
import io.github.hzjdev.hbsniff.detector.SmellDetectorFactory;
import io.github.hzjdev.hbsniff.metric.MappingMetrics;
import io.github.hzjdev.hbsniff.model.HqlAndContext;
import io.github.hzjdev.hbsniff.model.output.Metric;
import io.github.hzjdev.hbsniff.model.output.ProjectSmellCSVLine;
import io.github.hzjdev.hbsniff.model.output.ProjectSmellReport;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hbsniff.parser.EntityParser.*;
import static io.github.hzjdev.hbsniff.parser.HqlExtractor.getHqlNodes;

public class Main {

    /**
     * export smell detection results
     * @param jsonPath path of the json file
     * @param csvPath path of the csv file
     * @param results results
     */
    public static void outputSmells(String jsonPath, String csvPath, ProjectSmellReport results) {
        //wirte to csv
        results.cleanup();
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
    public static void exec(String project, String root_path, String output_path, List<String> exclude) {
        //init context
        List<CompilationUnit> cus = parseFromDir(root_path + "\\" + project);
        List<CompilationUnit> entities = getEntities(cus);
        ProjectSmellReport psr = ProjectSmellReport.fromCompilationUnits(cus);
        List<HqlAndContext> hqls = getHqlNodes(cus);

        //detection
        SmellDetectorFactory
                .createAll(cus, hqls, entities, psr)
                .forEach(sd->{
                    if(exclude!=null && exclude.size()>0 && exclude.contains(sd.getClass().getSimpleName())){
                        return;
                    }
                    try {
                        sd.exec();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
        //output
        outputSmells(output_path + "\\" + project + "_smells.json", output_path + "\\" + project + "_smells.csv", psr);

        if(exclude == null || !exclude.contains("MappingMetrics")) {
            outputMetrics(output_path + "\\" + project + "_metrics.csv",  MappingMetrics.exec(entities));
        }

    }

    /**
     * Entrance of the application
     * @param args arguments from commandline
     */
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("Main").build()
                .defaultHelp(true)
                .description("HBSniff: Java Hibernate Object Relational Mapping Smell Detector.");
        parser.addArgument("-d", "--directory")
                .help("Root directory of the project, e.g., if your project is in e:\\dir\\project, you should use \"e:\\dir\\\" for this param.");

        parser.addArgument("-p", "--project")
                .help("Project name/directory, e.g., if your project is in e:\\dir\\project, you should use \"project\" for this param.");

        parser.addArgument("-o", "--output")
                .help("Output directory of the project.");

        parser.addArgument("-e", "--exclude")
                .help("Smells to exclude. Split the smells by ',' if you wish to exclude multiple smells/metrics. If you want to exclude metrics, simply use \"MappingMetrics\" for this parameter. Names of Smells: CollectionField,FinalEntity,GetterSetter,HashCodeAndEquals,MissingIdentifier,MissingNoArgumentConstructor,NotSerializable,Fetch,OneByOne,MissingManyToOne,Pagination.");


        String project = null;
        String root_path = null;
        String output_path = null;
        List<String> exclude = null;
        Namespace ns;
        try {
            ns = parser.parseArgs(args);
            project = ns.getString("project");
            root_path = ns.getString("directory");
            output_path = ns.getString("output");
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
        if(project == null){
            project = "2ndInvesta";
            root_path = "D:\\tools\\hql\\projects";
            output_path = "D:\\tools\\hql\\projects";
        }
        try {
            ns = parser.parseArgs(args);
            String excludeExpr = ns.getString("exclude");
            if(excludeExpr != null) {
                exclude = Arrays.asList(ns.getString("exclude").split(","));
            }
        } catch (Exception e) {
            exclude = null;
        }
        exec(project, root_path, output_path, exclude);
    }
}
