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

package io.github.hzjdev.hbsniff.detector.rules;

import io.github.hzjdev.hbsniff.detector.SmellDetector;
import io.github.hzjdev.hbsniff.model.Declaration;
import io.github.hzjdev.hbsniff.model.ParametreOrField;
import io.github.hzjdev.hbsniff.model.output.Smell;
import io.github.hzjdev.hbsniff.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CollectionField extends SmellDetector {

    /**
     * check if entities use set or list as interfaces
     * @param allModelClasses entities
     * @return list of smells
     */
    public List<Smell> useInterfaceSetOrListRule(Set<Declaration> allModelClasses) {
        List<Smell> smells = new ArrayList<>();

        for (Declaration entityNode : allModelClasses) {

            List<ParametreOrField> declaredFields = entityNode.getFields();
            boolean passed = true;
            StringBuilder comment = new StringBuilder();
            for (ParametreOrField fieldNode : declaredFields) {
                String type = fieldNode.getType();
                if (type.contains("<")) {
                    type = type.split("<")[0];
                }
                if (Utils.isCollection(type) && !type.equals(Utils.SET_NAME)
                        && !type.equals(Utils.LIST_NAME)) {
                    comment.append("The field <").append(fieldNode.getName()).append("> of the class <").append(entityNode.getName()).append("> implements interface Collection but it ").append("doesn't implements interface Set or interface List.\n");
                    passed = false;
                }
            }

            if (!passed) {
                Smell smell = initSmell(entityNode).setName("CollectionField").setComment(comment.toString());
                psr.getSmells().get(entityNode).add(smell);
                smells.add(smell);
            }
        }
        return smells;
    }

    /**
     * check if entities use set as interfaces
     * @param allModelClasses entities
     * @return list of smells
     */
    public List<Smell> useSetCollectionRule(Set<Declaration> allModelClasses) {
        List<Smell> smells = new ArrayList<>();

        for (Declaration entityNode : allModelClasses) {

            List<ParametreOrField> declaredFields = entityNode.getFields();
            boolean passed = true;
            StringBuilder comment = new StringBuilder();

            for (ParametreOrField fieldNode : declaredFields) {
                String type = fieldNode.getType();
                if (type.contains("<")) {
                    type = type.split("<")[0];
                }
                if (Utils.isCollection(type) && !Utils.isSet(type)) {
                    comment.append("The field <").append(fieldNode.getName()).append("> of the class <").append(entityNode.getName()).append(" should use Set collection.");
                    passed = false;
                }
            }

            if (!passed) {
                Smell smell = initSmell(entityNode).setName("CollectionField").setComment(comment.toString());
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
        List<Smell> interfaces = useSetCollectionRule(entityDeclarations);
//        interfaces.addAll(useSetCollectionRule(entityDeclarations));
        return interfaces;
    }

}
