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

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.getIdentifierProperty;

public class MissingIdentifier extends SmellDetector {

    /**
     * detection methods
     * @param classes Entity Declarations
     * @return results
     */
    public List<Smell> provideIdentifierPropertyRule(Set<Declaration> classes) {
        List<Smell> smells = new ArrayList<>();
        for (Declaration entityNode : classes) {
            ParametreOrField field = getIdentifierProperty(entityNode);
            if (field == null) {
                Smell smell = initSmell(entityNode).setName("MissingId");
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
        return provideIdentifierPropertyRule(entityDeclarations);
    }


}
