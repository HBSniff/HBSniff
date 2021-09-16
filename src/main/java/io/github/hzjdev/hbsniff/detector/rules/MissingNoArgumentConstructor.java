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
import io.github.hzjdev.hbsniff.model.ParametreOrField;
import io.github.hzjdev.hbsniff.model.output.Smell;

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
        for (Declaration clazz : classes) {

            // Checks the class and the inherited methods from the super class
            List<Declaration> constructors = clazz.getConstructors();
            boolean smelly = true;

            if (constructors != null) {
                if(constructors.size()<1){
                    // java will implement default no arg constructor if no constructor is specified
                    smelly = false;
                }
                for (Declaration methodNode : constructors) {
                    List<ParametreOrField> parameters = methodNode.getParametres();
                    if (parameters.isEmpty()) {
                        smelly = false;
                        break;
                    }
                }
            }else{
                smelly = false;
            }

            if (smelly) {
                Smell smell = initSmell(clazz).setName("MissingNoArgumentConstructor");
                psr.getSmells().get(clazz).add(smell);
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
