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
import io.github.hzjdev.hqlsniffer.model.ParametreOrField;
import io.github.hzjdev.hqlsniffer.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MissingNoArgumentConstructor extends SmellDetector {


    /**
     * detection methods
     * @param classes Entity Declarations
     * @return results
     */
    public final List<Smell> noArgumentConstructorRule(Set<Declaration> classes) {
        List<Smell> smells = new ArrayList<>();
        for (Declaration entityNode : classes) {

            // Checks the class and the inherited methods from the super class
            List<Declaration> constructors = entityNode.getConstructors();
            boolean passed = false;

            if (constructors != null) {
                for (Declaration methodNode : constructors) {
                    List<ParametreOrField> parameters = methodNode.getParametres();
                    if (parameters.isEmpty()) {
                        passed = true;
                        break;
                    }
                }
            }

            if (!passed) {
                Smell smell = initSmell(entityNode).setName("MissingNoArgumentConstructor");
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
        return noArgumentConstructorRule(entityDeclarations);
    }

}
