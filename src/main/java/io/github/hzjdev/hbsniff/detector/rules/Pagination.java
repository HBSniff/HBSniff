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

package io.github.hzjdev.hbsniff.detector.rules;

import com.github.javaparser.ast.CompilationUnit;
import io.github.hzjdev.hbsniff.detector.SmellDetector;
import io.github.hzjdev.hbsniff.model.Declaration;
import io.github.hzjdev.hbsniff.model.HqlAndContext;
import io.github.hzjdev.hbsniff.model.output.Smell;

import java.util.ArrayList;
import java.util.List;

/**
 * detecting the misuse of pagination
 */
public class Pagination extends SmellDetector {


    /**
     * main method of detection
     * @param hqls hqls
     * @param cus CompilationUnits
     * @return results
     */
    public List<Smell> getPaged(List<HqlAndContext> hqls, List<CompilationUnit> cus) {
        List<Smell> pagedSmell = new ArrayList<>();
        if (hqls == null || cus == null) return pagedSmell;
        for (HqlAndContext hql : hqls) {
            for (Declaration calledIn : hql.populateCalledIn(cus).getCalledIn()) {
                String body = calledIn.getBody();
                if (body.toLowerCase().contains("limit") || body.toLowerCase().contains("page")) {
                    if (!hql.getMethodBody().contains(".setFirstResult(") || !hql.getMethodBody().contains(".setMaxResults(")) {
                        Declaration parentDeclaration = findDeclarationFromPath(calledIn.getFullPath());
                        if (parentDeclaration != null) {
                            Smell smell = initSmell(parentDeclaration)
                                    .setName("Pagination")
                                    .setPosition(calledIn.getPosition())
                                    .setComment(calledIn.getName());
                            pagedSmell.add(smell);
                            psr.getSmells().get(parentDeclaration).add(smell);
                        }

                    }
                }
            }
        }
        return pagedSmell;
    }

    /**
     * execute detection
     * @return list of smells
     */
    public List<Smell> exec() {
        return getPaged(hqls, cus);
    }

}
