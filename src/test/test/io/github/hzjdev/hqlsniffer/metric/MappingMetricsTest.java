package io.github.hzjdev.hqlsniffer.metric;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.output.Metric;
import io.github.hzjdev.hqlsniffer.model.output.ProjectSmellJSONReport;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.genDeclarationsFromCompilationUnits;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MappingMetricsTest {
    List<Declaration> fig1;
    List<Declaration> fig3;
    List<CompilationUnit> fig1Cus = new ArrayList<>();
    List<CompilationUnit> fig3Cus = new ArrayList<>();
    ProjectSmellJSONReport psr = new ProjectSmellJSONReport();

    @Before
    public void before() throws FileNotFoundException {
        String rootPath = "src/test/resources/entities/metric/";
        String[] fig1EntityNames = {"ClerkFig1.java", "EmployeeFig1.java", "ManagerFig1.java", "PersonSingleTableFig1.java", "PersonPerClassFig1.java", "PersonJoinFig1.java", "StudentFig1.java"};
        String[] fig3EntityNames = {"EmployeeFig3.java", "ManagerFig3.java", "PersonSingleTableFig3.java", "StudentFig3.java"};

        for (String entity : fig1EntityNames) {
            fig1Cus.add(StaticJavaParser.parse(new File(rootPath + "fig1/" + entity)));
        }
        fig1Cus.add(StaticJavaParser.parse(new File(rootPath + "Person.java")));
        fig1 = genDeclarationsFromCompilationUnits(fig1Cus);
//        fig1 = MappingMetrics.getEntitiesWithTableAnnotation(fig1);
        MappingMetrics.initInheritance(fig1);

        for (String entity : fig3EntityNames) {
            fig3Cus.add(StaticJavaParser.parse(new File(rootPath + "fig3/" + entity)));
        }
        fig3Cus.add(StaticJavaParser.parse(new File(rootPath + "Person.java")));
        fig3 = genDeclarationsFromCompilationUnits(fig3Cus);
//        fig3 = MappingMetrics.getEntitiesWithTableAnnotation(fig3);
        MappingMetrics.initInheritance(fig3);


        for (Declaration d : fig1) {
            psr.getSmells().put(d, new ArrayList<>());
        }

        for (Declaration d : fig3) {
            psr.getSmells().put(d, new ArrayList<>());
        }
    }

    @Test
    public void initInheritance() {
    }

    @Test
    public void getEntitiesWithTableAnnotation() {
    }

    @Test
    public void TATI() {
        // Hibernate does not support hybrid inheritance types in an inheritance tree
        // We need to define separate Person(root) instances for different inheritance types
        // We cannot specify sub inheritance types, so employee cannot determine different inheritance types of Manager and Clerk

        List<Metric> result = MappingMetrics.TATI(fig1);
        for (Metric m : result) {
            String className = m.getClassName();
            if (className.equals("PersonPerClassFig1")) {
                assertEquals(m.getIntensity() + 0.0, 2.0, 0.01);
                assertTrue(m.getComment().contains("StudentFig1") && m.getComment().contains("ManagerFig1"));
            } else if (className.equals("PersonJoinFig1")) {
                assertEquals(m.getIntensity() + 0.0, 1.0, 0.01);
                assertTrue(m.getComment().contains("ClerkFig1"));
            } else if (className.equals("PersonSingleTableFig1")) {
                assertEquals(m.getIntensity() + 0.0, 1.0, 0.01);
                assertTrue(m.getComment().contains("EmployeeFig1"));
            } else if (className.equals("EmployeeFig1")) {
                assertEquals(m.getIntensity() + 0.0, 1.0, 0.01);
            }
        }
    }

    @Test
    public void NCT() {
        List<Metric> result = MappingMetrics.NCT(fig1);
        for (Metric m : result) {
            String className = m.getClassName();
            if (className.equals("ClerkFig1")) {
                assertEquals(m.getIntensity() + 0.0, 2.0, 0.01);
            } else if (className.equals("ManagerFig1")) {
                assertEquals(m.getIntensity() + 0.0, 1.0, 0.01);
            }
        }
    }

    @Test
    public void NCRF() {
        List<Metric> result = MappingMetrics.NCRF(fig1);
        for (Metric m : result) {
            String className = m.getClassName();
            if (className.equals("PersonPerClassFig1")) {
                assertEquals(m.getIntensity() + 0.0, 3.0, 0.01);
            } else {
                assertEquals(m.getIntensity() + 0.0, 0.0, 0.01);
            }
        }
    }

    @Test
    public void ANV() {
        List<Metric> result = MappingMetrics.ANV(fig3);
        for (Metric m : result) {
            String className = m.getClassName();
            if (className.equals("StudentFig3")) {
                assertEquals(m.getIntensity() + 0.0, 6.0, 0.01);
            }
        }
    }

    @Test
    public void exec() {
        List<Metric> metrics = MappingMetrics.exec(fig1Cus);
        metrics.addAll(MappingMetrics.exec(fig3Cus));
        assertTrue(metrics.size() > 0);
    }
}