package io.github.hzjdev.hqlsniffer.smell;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import io.github.hzjdev.hqlsniffer.Declaration;
import io.github.hzjdev.hqlsniffer.ProjectSmellReport;
import io.github.hzjdev.hqlsniffer.Result;
import io.github.hzjdev.hqlsniffer.Smell;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.findTypeDeclaration;


/**
 * transferred code from https://github.com/tacianosilva/designtests
 * M. Silva, D. Serey, J. Figueiredo, J. Brunet. Automated design tests to check Hibernate design recommendations. SBES 2019
 */
public abstract class SmellDetector {

    private Set<Declaration> resultsTrue = new HashSet<>();

    private Set<Declaration> resultsFalse = new HashSet<>();

    public ProjectSmellReport psr;

    public List<CompilationUnit> cus;

    public List<Result> hqls;

    public List<CompilationUnit> entities;

    public HashSet<Declaration> declarations;

    public SmellDetector populateContext(List<CompilationUnit> cus,  List<Result> hqls, List<CompilationUnit> entities, ProjectSmellReport psr){
        this.psr = psr;
        this.cus = cus;
        this.hqls = hqls;
        this.entities = entities;
        if(cus!=null) {
            declarations = new HashSet<>();
            for (CompilationUnit cu : cus) {
                for (TypeDeclaration td : cu.getTypes()) {
                    Declaration d = findTypeDeclaration(td.getNameAsString(), cus, 1);
                    if (d != null) {
                        declarations.add(d);
                    }
                }
            }
        }
        return this;
    }

    private String report = "";

    public void addReport(final String s){
        report += s;
    }

    protected final void addResultTrue(final Declaration node) {
        resultsTrue.add(node);
    }


    protected final void addResultFalse(final Declaration node) {
        resultsFalse.add(node);
    }


    public final List<Smell> isEmptyReport() {
        return null;
    }

    public final String getReport() {
        return this.report;
    }

    public Set<Declaration> getResultsTrue() {
        return resultsTrue;
    }


    public Set<Declaration> getResultsFalse() {
        return resultsFalse;
    }

    public List<Smell> exec() {
        return null;
    }

}
