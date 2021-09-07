/*
 * This file is part of hqlSniffer.
 *
 *     hqlSniffer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     hqlSniffer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with hqlSniffer.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    /**
     * initialize
     * @param cus compilation units
     * @param hqls hqls
     * @param entities entity compilation units
     * @param psr project smell report
     * @return intialized smell detector
     */
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

    /**
     * execute detection
     * @return list of results
     */
    public abstract List<Smell> exec();

}
