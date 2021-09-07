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

package io.github.hzjdev.hqlsniffer.detector.rules;

import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.getSuperClassDeclarations;
import static io.github.hzjdev.hqlsniffer.utils.Const.SERIALIZABLE_ANNOT_EXPR;

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
            String serializable = SERIALIZABLE_ANNOT_EXPR;
            List<Declaration> toDetect = getSuperClassDeclarations(entityNode);
            toDetect.add(entityNode);
            for (Declaration superclass : toDetect) {
                if (pass) {
                    break;
                }
                for (String i : superclass.getImplementedInterface()) {
                    if (i.equals(serializable)) {
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
