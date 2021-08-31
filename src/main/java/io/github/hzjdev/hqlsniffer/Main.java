package io.github.hzjdev.hqlsniffer;

import com.github.javaparser.ast.CompilationUnit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.hzjdev.hqlsniffer.metric.MappingMetrics;
import io.github.hzjdev.hqlsniffer.smell.*;
import java.io.*;
import java.util.*;
import static io.github.hzjdev.hqlsniffer.parser.EntityParser.*;
import static io.github.hzjdev.hqlsniffer.parser.HqlExtractor.getHqlNodes;

public class Main {

    public static void output(String path, Object results) throws FileNotFoundException {
        Gson gs = new GsonBuilder()
                .setPrettyPrinting()
//                .disableHtmlEscaping()
                .create();
        try (PrintWriter out = new PrintWriter(path)) {
            out.println(gs.toJson(results));
        }
    }

    public static void exec(String project, String root_path, String output_path) throws FileNotFoundException {
        //init
        ProjectSmellReport psr = new ProjectSmellReport();
        List<CompilationUnit> cus = new ArrayList<>();
        parseFromDir(root_path+"\\"+project, cus);
        setCusCache(cus);
        List<CompilationUnit> entities = getEntities(cus);
        List<Result> hqls = getHqlNodes(cus);

        List<Smell> results = new ArrayList<>();
        List<SmellDetector> detectors = SmellDetectorFactory.createAll(cus, hqls, entities, psr);
        for (SmellDetector sd: detectors){
            results.addAll(sd.exec());
        }
        results.addAll(MappingMetrics.exec(entities));

        output(output_path+"\\"+project+"_smells.json", psr);
    }


    public static void main(String[] args) throws IOException {
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
