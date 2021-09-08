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

package io.github.hzjdev.hbsniff.model.output;

import com.github.javaparser.ast.CompilationUnit;
import com.google.gson.annotations.Expose;
import io.github.hzjdev.hbsniff.model.Declaration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.hzjdev.hbsniff.parser.EntityParser.genDeclarationsFromCompilationUnits;

public class ProjectSmellReport implements Serializable {

    @Expose
    Map<Declaration, List<Smell>> smells;

    public ProjectSmellReport() {
        smells = new HashMap<>();
    }

    /**
     * generate ProjectSmellJSONReport from CompilationUnits
     * @param entities compilation units
     * @return ProjectSmellJSONReport
     */
    public static ProjectSmellReport fromCompilationUnits(List<CompilationUnit> entities) {
        ProjectSmellReport toReturn = new ProjectSmellReport();
        for (Declaration d : genDeclarationsFromCompilationUnits(entities)) {
            toReturn.getSmells().put(d, new ArrayList<>());
        }
        return toReturn;
    }

    public Map<Declaration, List<Smell>> getSmells() {
        return smells;
    }

    public ProjectSmellReport setSmells(Map<Declaration, List<Smell>> smells) {
        this.smells = smells;
        return this;
    }
}
