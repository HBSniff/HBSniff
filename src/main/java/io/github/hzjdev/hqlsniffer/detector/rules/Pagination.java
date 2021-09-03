package io.github.hzjdev.hqlsniffer.detector.rules;

import com.github.javaparser.ast.CompilationUnit;
import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.HqlAndContext;
import io.github.hzjdev.hqlsniffer.model.output.Smell;

import java.util.ArrayList;
import java.util.List;

public class Pagination extends SmellDetector {


    public List<Smell> getPaged(List<HqlAndContext> hqls, List<CompilationUnit> cus) {
        List<Smell> pagedSmell = new ArrayList<>();
        for (HqlAndContext hql : hqls) {
            for (Declaration calledIn : hql.getCalledIn()) {
                String body = calledIn.getBody();
                if (body.toLowerCase().contains("limit") || body.toLowerCase().contains("page")) {
                    if (!hql.getMethodBody().contains(".setFirstResult(") || !hql.getMethodBody().contains(".setMaxResults(")) {
                        Smell smell = new Smell();
                        String path = calledIn.getFullPath();
                        smell.setName("Pagination");
                        smell.setPosition(calledIn.getPosition());
                        smell.setFile(path).setComment(calledIn.getName());
                        Declaration parentDeclaration = null;
                        for (CompilationUnit cu : cus) {
                            String cuPath = null;
                            if (cu.getStorage().isPresent()) {
                                cuPath = cu.getStorage().get().getPath().toString();
                                if (path.equals(cuPath)) {
                                    parentDeclaration = new Declaration(cu);
                                    break;
                                }
                            }
                        }
                        if (parentDeclaration != null) {
                            List<Smell> smells = psr.getSmells().get(parentDeclaration);
                            if (smells == null) {
                                smells = new ArrayList<>();
                            }
                            smells.add(smell.setClassName(parentDeclaration.getName()));
                            pagedSmell.add(smell);
                            psr.getSmells().put(parentDeclaration, smells);
                        }

                    }
                }
            }
        }
        return pagedSmell;
    }

    public List<Smell> exec() {
        return getPaged(hqls, cus);
    }

}
