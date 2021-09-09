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

package io.github.hzjdev.hbsniff.detector.rules;

import io.github.hzjdev.hbsniff.detector.SmellDetector;
import io.github.hzjdev.hbsniff.model.Declaration;
import io.github.hzjdev.hbsniff.model.output.Smell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hbsniff.parser.EntityParser.findTypeDeclaration;
import static io.github.hzjdev.hbsniff.parser.EntityParser.getSuperClassDeclarations;
import static io.github.hzjdev.hbsniff.utils.Const.SERIALIZABLE_EXPR;

public class NotSerializable extends SmellDetector {

    /**
     * detection methods
     * @param classes Entity Declarations
     * @return results
     */
    public final List<Smell> checkRule(Set<Declaration> classes) {
        List<Smell> smells = new ArrayList<>();
        for (Declaration entityNode : classes) {
            boolean pass = false;
            Set<Declaration> toDetect = entityNode.getExtendedOrImplementedTypes();
            for (Declaration superclass : toDetect) {
                for (String i : superclass.getImplementedInterface()) {
                    if (i.contains(SERIALIZABLE_EXPR)) {
                        pass = true;
                        break;
                    }
                }
                for (String i : superclass.getSuperClass()) {
                    if (!pass && i.contains(SERIALIZABLE_EXPR)) {
                        pass = true;
                        break;
                    }
                }
            }
            if (!pass) {
                Smell smell = initSmell(entityNode).setName("NotSerializable");
                psr.getSmells().get(entityNode).add(smell);
                smells.add(smell);
            }
        }

        return smells;
    }

    /**
     * execute detection
     * @return list of smells
     */
    public List<Smell> exec() {
        return checkRule(entityDeclarations);
    }

}
