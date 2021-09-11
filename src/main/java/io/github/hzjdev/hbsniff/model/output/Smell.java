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

import java.io.Serializable;
import java.util.List;

public class Smell implements Serializable {
    @Expose
    String name;

    @Expose
    String file;

    @Expose
    String position;

    @Expose
    String className;

    @Expose
    String comment;

    @Expose
    List<Declaration> relatedComponent;


    public String getClassName() {
        return className;
    }

    public Smell setClassName(String className) {
        this.className = className;
        return this;
    }

    public List<Declaration> getRelatedComponent() {
        return relatedComponent;
    }

    public Smell setRelatedComponent(List<Declaration> relatedComponent) {
        this.relatedComponent = relatedComponent;
        return this;
    }

    public String getName() {
        return name;
    }

    public Smell setName(String name) {
        this.name = name;
        return this;
    }

    public String getFile() {
        return file;
    }

    public Smell setFile(String file) {
        this.file = file;
        return this;
    }

    public String getPosition() {
        return position;
    }

    public Smell setPosition(String position) {
        this.position = position;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public Smell setComment(String comment) {
        this.comment = comment;
        return this;
    }
}
