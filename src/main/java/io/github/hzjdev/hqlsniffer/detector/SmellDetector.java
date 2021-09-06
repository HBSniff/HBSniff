package io.github.hzjdev.hqlsniffer.detector;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.HqlAndContext;
import io.github.hzjdev.hqlsniffer.model.output.ProjectSmellJSONReport;
import io.github.hzjdev.hqlsniffer.model.output.Smell;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.findTypeDeclaration;


/**
 * transferred code from https://github.com/tacianosilva/designtests
 * M. Silva, D. Serey, J. Figueiredo, J. Brunet. Automated design tests to check Hibernate design recommendations. SBES 2019
 */
public abstract class SmellDetector {

    public ProjectSmellJSONReport psr;

    public List<CompilationUnit> cus;

    public List<HqlAndContext> hqls;

    public List<CompilationUnit> entities;

    public HashSet<Declaration> declarations;

    public Set<Declaration> entityDeclarations;

    protected static Smell initSmell(Declaration entity) {
        return new Smell().setClassName(entity.getName()).setFile(entity.getFullPath()).setPosition(entity.getPosition());
    }

    public void setEntityDeclarations(Set<Declaration> entityDeclarations) {
        this.entityDeclarations = entityDeclarations;
    }

    public SmellDetector populateContext(List<CompilationUnit> cus, List<HqlAndContext> hqls, List<CompilationUnit> entities, ProjectSmellJSONReport psr) {
        this.psr = psr;
        this.cus = cus;
        this.hqls = hqls;
        this.entities = entities;
        if (cus != null) {
            declarations = new HashSet<>();
            entityDeclarations = new HashSet<>();
            for (CompilationUnit cu : cus) {
                for (TypeDeclaration td : cu.getTypes()) {
                    Declaration d = findTypeDeclaration(td.getNameAsString(), cus, 1);
                    if (d != null) {
                        declarations.add(d);
                        if (entities != null && entities.contains(cu)) {
                            entityDeclarations.add(d);
                        }
                    }
                }
            }
        }
        return this;
    }

    public abstract List<Smell> exec();

}
