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

package io.github.hzjdev.hqlsniffer.detector;

import com.github.javaparser.ast.CompilationUnit;
import io.github.hzjdev.hqlsniffer.model.HqlAndContext;
import io.github.hzjdev.hqlsniffer.model.output.ProjectSmellReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SmellDetectorFactory {

    /**
     * Type of smell detectors, please use the same name as classes to ensure correct initialization.
     */
    public enum SmellType {
        //SBES 2020
        OneByOne,
        Pagination,
        Fetch,
        MissingManyToOne,
        //SBES 2019
        CollectionField,
        FinalEntity,
        GetterSetter,
        HashCodeAndEquals,
        MissingIdentifier,
        MissingNoArgumentConstructor,
        NotSerializable,
    }

    /**
     * create a single smell detector (no population)
     * @param smellType ENUM type of smell detector
     * @return a new SmellDetector
     */
    public static SmellDetector create(SmellType smellType) {
        String className = SmellDetectorFactory.class.getPackage().getName() + ".rules." + smellType.name();
        try {
            return (SmellDetector) Class.forName(className)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Generate and initialize all available smell detectors
     * @param cus compilation units
     * @param hqls hqls
     * @param entities compilation units of entities
     * @param psr project smell report
     * @return list of initialized detectors
     */
    public static List<SmellDetector> createAll(List<CompilationUnit> cus, List<HqlAndContext> hqls, List<CompilationUnit> entities, ProjectSmellReport psr) {
        List<SmellDetector> detectors = new ArrayList<>();
        for (SmellType s : SmellType.values()) {
            detectors.add(Objects.requireNonNull(create(s))
                    .populateContext(cus, hqls, entities, psr));
        }
        return detectors;
    }


}
