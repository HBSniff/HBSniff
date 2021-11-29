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

import com.google.gson.annotations.Expose;
import io.github.hzjdev.hbsniff.model.Declaration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Metric extends Smell {
    @Expose
    Double intensity;

    /**
     * convert metric detection results to csv
     * @param lines detectino results
     * @return strings of contents of csv
     */
    public static List<String[]> toCSV(List<Metric> lines) {
        String[] heads = {"metric", "file", "className", "comment", "position", "value"};
        List<String[]> result = new ArrayList<>();
        result.add(heads);
        result.addAll(lines.stream().map(Metric::getLine).collect(Collectors.toList()));
        return result;
    }

    public Double getIntensity() {
        return intensity;
    }

    public Metric setIntensity(Double intensity) {
        this.intensity = intensity;
        return this;
    }

    @Override
    public Metric setClassName(String className) {
        this.className = className;
        return this;
    }

    @Override
    public Metric setRelatedComponent(List<Declaration> relatedComponent) {
        this.relatedComponent = relatedComponent;
        return this;
    }

    @Override
    public Metric setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public Metric setFile(String file) {
        this.file = file;
        return this;
    }

    @Override
    public Metric setPosition(String position) {
        this.position = position;
        return this;
    }

    @Override
    public Metric setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String[] getLine() {
        return new String[]{
                this.getName(), this.getFile(), this.getClassName(), this.getComment(), this.getPosition(), this.getIntensity().toString(),
        };
    }

}
