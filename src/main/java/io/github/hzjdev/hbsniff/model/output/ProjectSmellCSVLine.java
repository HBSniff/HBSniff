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

import io.github.hzjdev.hbsniff.model.Declaration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectSmellCSVLine implements Serializable {
    String[] line;

    public ProjectSmellCSVLine(Smell smell) {
        line = new String[]{
                smell.getName(), smell.getFile(), smell.getClassName(), smell.getComment(), smell.getPosition()
        };
    }

    /**
     * generate a list of this object from the ProjectSmellReport object
     * @param psr detection results
     * @return a list of the csv line objectg
     */
    public static List<ProjectSmellCSVLine> fromProjectSmellJSONReport(ProjectSmellReport psr) {
        List<ProjectSmellCSVLine> results = new ArrayList<>();
        if (psr == null) {
            return results;
        }
        for (HashMap.Entry<Declaration, List<Smell>> kv : psr.getSmells().entrySet()) {
            results.addAll(kv.getValue().stream().map(ProjectSmellCSVLine::new).collect(Collectors.toList()));
        }
        return results;
    }

    /**
     * generate csv
     * @param lines the detection results
     * @return the lines of csv
     */
    public static List<String[]> toCSV(List<ProjectSmellCSVLine> lines) {
        String[] heads = {"smell", "file", "className", "comment", "position"};
        List<String[]> result = new ArrayList<>();
        result.add(heads);
        result.addAll(lines.stream().map(ProjectSmellCSVLine::getLine).collect(Collectors.toList()));
        return result;
    }

    public String[] getLine() {
        return line;
    }
}
