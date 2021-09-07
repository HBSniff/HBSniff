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

package io.github.hzjdev.hqlsniffer.model.output;

import com.github.javaparser.ast.CompilationUnit;
import com.google.gson.annotations.Expose;
import io.github.hzjdev.hqlsniffer.model.Declaration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.genDeclarationsFromCompilationUnits;

public class ProjectSmellJSONReport implements Serializable {

    @Expose
    Map<Declaration, List<Smell>> smells;

    public ProjectSmellJSONReport() {
        smells = new HashMap<>();
    }

    /**
     * generate ProjectSmellJSONReport from CompilationUnits
     * @param entities compilation units
     * @return ProjectSmellJSONReport
     */
    public static ProjectSmellJSONReport fromCompilationUnits(List<CompilationUnit> entities) {
        ProjectSmellJSONReport toReturn = new ProjectSmellJSONReport();
        for (Declaration d : genDeclarationsFromCompilationUnits(entities)) {
            toReturn.getSmells().put(d, new ArrayList<>());
        }
        return toReturn;
    }

    public Map<Declaration, List<Smell>> getSmells() {
        return smells;
    }

    public ProjectSmellJSONReport setSmells(Map<Declaration, List<Smell>> smells) {
        this.smells = smells;
        return this;
    }
}
