package io.github.hzjdev.hqlsniffer.detector.rules;

import com.github.javaparser.ast.CompilationUnit;
import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.HqlAndContext;
import io.github.hzjdev.hqlsniffer.model.output.Smell;

import java.util.ArrayList;
import java.util.List;

/**
 * detecting the misuse of pagination
 */
public class Pagination extends SmellDetector {

    /**
     * locate Declaration from CompilationUnits
     * @param path declaration path
     * @return result Declaration
     */
    public Declaration findDeclarationFromPath(String path) {
        for (CompilationUnit cu : cus) {
            String cuPath;
            if (cu.getStorage().isPresent()) {
                cuPath = cu.getStorage().get().getPath().toString();
                if (path.equals(cuPath)) {
                    return new Declaration(cu);
                }
            }
        }
        return null;
    }

    /**
     * main method of detection
     * @param hqls hqls
     * @param cus CompilationUnits
     * @return results
     */
    public List<Smell> getPaged(List<HqlAndContext> hqls, List<CompilationUnit> cus) {
        List<Smell> pagedSmell = new ArrayList<>();
        if (hqls == null || cus == null) return pagedSmell;
        for (HqlAndContext hql : hqls) {
            for (Declaration calledIn : hql.populateCalledIn(cus).getCalledIn()) {
                String body = calledIn.getBody();
                if (body.toLowerCase().contains("limit") || body.toLowerCase().contains("page")) {
                    if (!hql.getMethodBody().contains(".setFirstResult(") || !hql.getMethodBody().contains(".setMaxResults(")) {
                        Declaration parentDeclaration = findDeclarationFromPath(calledIn.getFullPath());
                        if (parentDeclaration != null) {
                            Smell smell = initSmell(parentDeclaration)
                                    .setName("Pagination")
                                    .setPosition(calledIn.getPosition())
                                    .setComment(calledIn.getName());
                            pagedSmell.add(smell);
                            psr.getSmells().get(parentDeclaration).add(smell);
                        }

                    }
                }
            }
        }
        return pagedSmell;
    }

    /**
     * execute detection
     * @return list of smells
     */
    public List<Smell> exec() {
        return getPaged(hqls, cus);
    }

}
