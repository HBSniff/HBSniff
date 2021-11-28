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
import io.github.hzjdev.hbsniff.detector.SmellDetectorFactory;
import io.github.hzjdev.hbsniff.detector.MappingMetrics;
import io.github.hzjdev.hbsniff.model.HqlAndContext;
import io.github.hzjdev.hbsniff.model.output.ProjectSmellReport;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.hzjdev.hbsniff.parser.EntityParser.getEntities;
import static io.github.hzjdev.hbsniff.parser.EntityParser.parseFromDir;
import static io.github.hzjdev.hbsniff.parser.HqlExtractor.getHqlNodes;
import static io.github.hzjdev.hbsniff.utils.Const.*;
import static io.github.hzjdev.hbsniff.utils.Utils.outputMetrics;
import static io.github.hzjdev.hbsniff.utils.Utils.outputSmells;

public class Main {


    /**
     * start detection
     * @param root_path project path
     * @param output_path output path
     */
    public static void exec(String root_path, String output_path, List<String> exclude, List<String> outputTypes) {
        //init context
        if(root_path == null || output_path == null){ return; }
        String projectName = Paths.get(root_path).getFileName().toString();
        List<CompilationUnit> cus = parseFromDir(root_path);
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
        outputSmells(
                Paths.get(output_path,projectName).toAbsolutePath().toString(),
                psr,
                outputTypes);

        if(exclude == null || !exclude.contains("MappingMetrics")) {
            outputMetrics(
                    Paths.get(output_path,projectName+"_metrics"+DOT+CSV_FILE_TYPE).toAbsolutePath().toString(),
                    MappingMetrics.exec(entities)
            );
        }

    }

    /**
     * Entrance of the application
     * @param args arguments from commandline
     */
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("HBSniff.jar").build()
                .defaultHelp(true)
                .description("HBSniff: Java Hibernate Object Relational Mapping Smell Detector.");
        parser.addArgument("-i", "--input").required(true)
                .help("Root directory of the project.");

        parser.addArgument("-o", "--output").required(true)
                .help("Output directory of the detection results.");

        parser.addArgument("-e", "--exclude").required(false)
                .help("Smells/Metrics to exclude (Optional). Split the smells by ',' if you wish to exclude multiple smells/metrics. If you want to exclude metrics, simply use \"MappingMetrics\" for this parameter. Names of Smells: CollectionField,FinalEntity,GetterSetter,HashCodeAndEquals,MissingIdentifier,MissingNoArgumentConstructor,NotSerializable,Fetch,OneByOne,MissingManyToOne,Pagination.");

        parser.addArgument("-t", "--type").required(false)
                .help("Types of the output files for smells.  Split the types by ','. Available Options: csv,json,xls. ");

        String input_path = null;
        String output_path = null;
        List<String> exclude = null;
        List<String> types = Arrays.asList(CSV_FILE_TYPE, JSON_FILE_TYPE, XLS_FILE_TYPE);
        Namespace ns;
        try {
            ns = parser.parseArgs(args);
            input_path = ns.getString("input");
            output_path = ns.getString("output");
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
        if(input_path == null || output_path == null){
            input_path = DEFAULT_INPUT_PATH;
            output_path = DEFAULT_OUTPUT_PATH;
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

        try {
            ns = parser.parseArgs(args);
            String typeExpr = ns.getString("type");
            if (typeExpr != null) {
                types = Arrays.stream(ns.getString("type").split(",")).map(String::toLowerCase).collect(Collectors.toList());
            }
        }catch (Exception e) {
            // we do nothing if type is not parsed successfully
        }

        exec(input_path, output_path, exclude, types);
    }
}
