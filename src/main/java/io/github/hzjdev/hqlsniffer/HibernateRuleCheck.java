package io.github.hzjdev.hqlsniffer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.TypeDeclaration;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hqlsniffer.Main.findTypeDeclaration;
import static io.github.hzjdev.hqlsniffer.Main.getTypeFromCache;

// transferred code from https://github.com/tacianosilva/designtests
// M. Silva, D. Serey, J. Figueiredo, J. Brunet. Automated design tests to check Hibernate design recommendations. SBES 2019

public class HibernateRuleCheck {

    private Set<Declaration> resultsTrue = new HashSet<>();

    private Set<Declaration> resultsFalse = new HashSet<>();

    private String report = "";

    public void addReport(final String s){
        this.report += s;
    }

    protected final void addResultTrue(final Declaration node) {
        resultsTrue.add(node);
    }


    protected final void addResultFalse(final Declaration node) {
        resultsFalse.add(node);
    }


    public final boolean isEmptyReport() {
        if ("".equals(this.getReport())) {
            return true;
        }
        return false;
    }

    public final String getReport() {
        return this.report;
    }


    /**
     * Verifies if exists set method for the field.
     * @param fieldNode The field contained in the class.
     * @param entityNode Entity that contains the field.
     * @return True if exists set method in the entity for the field.
     */
    protected final boolean hasSetMethod(final Parametre fieldNode, final Declaration entityNode) {
        String fieldName = fieldNode.getName();
        String type = fieldNode.getType();

        String setName = "set" + fieldName.substring(0, 1).toUpperCase()
                + fieldName.substring(1) + "(" + type + ")";

        Declaration methodNode = entityNode.findDeclaration(setName);

        if (methodNode == null) {
            return false;
        }

        String methodName = methodNode.getName();
        String methodType = methodNode.getReturnTypeName();

        if (methodName.equals(setName) && methodType.equals("void")) {
            return true;
        }
        return false;
    }

    /**
     * Verifies if exists get method for the field.
     * @param fieldNode The field contained in the class.
     * @param entityNode Entity that contains the field.
     * @return True if exists get method in the entity for the field.
     */
    protected final boolean hasGetMethod(final Parametre fieldNode, final Declaration entityNode) {

        String shortFieldName = fieldNode.getName();
        String type = fieldNode.getType();
        String strGetOrIs;

        if (type != null && (type.equals(Utils.BOOLEAN_PRIMITIVE) || type.equals(Utils.BOOLEAN_CLASS))) {
            strGetOrIs = "is";
        } else {
            strGetOrIs = "get";
        }

        String methodGetField = strGetOrIs + shortFieldName.substring(0, 1).toUpperCase()
                + shortFieldName.substring(1) + "()";

        Declaration methodNode = entityNode.findDeclaration(methodGetField);

        if (methodNode == null) {
            return false;
        }

        String methodName = methodNode.getName();
        String methodType = methodNode.getReturnTypeName();

        if (methodName.equals(methodGetField) && methodType.equals(type)) {
            return true;
        }

        return false;
    }


    protected final Declaration getEqualsMethod(final Declaration classNode) {
        Declaration equalsMethod = classNode.findDeclaration("equals(java.lang.Object)");
//        CompilationUnit superClass = classNode.getSuperClass();
//        if (equalsMethod == null && !getObjectClass().equals(superClass)) {
//            equalsMethod = classNode.getInheritedMethod("equals(java.lang.Object)");
//        }
        return equalsMethod;
    }

    protected final Declaration getHashCodeMethod(final Declaration classNode) {
        Declaration hashCodeMethod = classNode.findDeclaration("hashCode()");
//        CompilationUnit superClass = classNode.getSuperClass();
//        if (hashCodeMethod == null && !getObjectClass().equals(superClass)) {
//            hashCodeMethod = classNode.getInheritedMethod("hashCode()");
//        }
        return hashCodeMethod;
    }

    protected final Declaration getMethod(final Declaration classNode, final String methodName) {
        for (Declaration method: classNode.getMembers()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    public final Parametre getIdentifierProperty(final Declaration entity) {
        List<Parametre> declaredFields = entity.getFields();
        for (Parametre fieldNode : declaredFields) {
            List<String> annotations = fieldNode.getAnnotations();
            String ID="@Id";
            if (annotations.contains(ID)) {
                return fieldNode;
            }
        }
        return null;
    }

    
    public final boolean hashCodeAndEqualsNotUseIdentifierPropertyRule(Set<Declaration> classes) {
        for (Declaration entityNode : classes) {
            Declaration equalsMethod = getEqualsMethod(entityNode);
            Declaration hashCodeMethod = getHashCodeMethod(entityNode);

            Parametre field = getIdentifierProperty(entityNode);

            List<String> accessedFieldsEquals = null;
            List<String> accessedFieldsHash = null;

            if (equalsMethod != null) {
                accessedFieldsEquals = equalsMethod.getAccessedFieldNames();
            }
            if (hashCodeMethod != null) {
                accessedFieldsHash = hashCodeMethod.getAccessedFieldNames();
            }

            boolean contem = false;
            if (accessedFieldsEquals != null && field!=null && accessedFieldsEquals.contains(field.getName())) {
                this.addReport("The class <" + entityNode.getName()
                        + "> contains the identifier property <"
                        + field.getName() + "> in the equals method.\n");
                contem = true;
            }

            if (accessedFieldsHash != null && field!=null && accessedFieldsHash.contains(field.getName())) {
                this.addReport("The class <" + entityNode.getName()
                        + "> contains the identifier property <"
                        + field.getName() + "> in the hashCode method.\n");
                contem = true;
            }
            if (contem) {
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return this.isEmptyReport();
    }

    public final boolean hashCodeAndEqualsRule(Set<Declaration> classes) {
        for (Declaration entityNode : classes) {

            Declaration equalsMethod = getEqualsMethod(entityNode);
            Declaration hashCodeMethod = getHashCodeMethod(entityNode);

            boolean contem = equalsMethod != null && hashCodeMethod != null;

            if (!(contem)) {
                if (equalsMethod == null) {
                    this.addReport("The class <" + entityNode.getName()
                            + "> doesn't contain the equals method.\n");
                }
                if (hashCodeMethod == null) {
                    this.addReport("The class <" + entityNode.getName()
                            + "> doesn't contain the hashCode method.\n");
                }
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return this.isEmptyReport();
    }

    public final boolean checkRule(List<Declaration> classes) {

        for (Declaration entityNode : classes) {
            String serializable = "java.io.Serializable";
            List<String> superClasses = entityNode.getSuperClass();
            if (!superClasses.contains(serializable)) {
                //TODO: checkIfSuperClassesImplementsSerializable//&& !superClass.implementsInterface(serializable)
                this.addReport("The class <" + entityNode.getName() + "> "
                        + "doesn't implements interface Serializable.\n");
                this.addResultFalse(entityNode);
            } else {
                this.addResultTrue(entityNode);
            }
        }
        return this.isEmptyReport();
    }

    public final boolean noArgumentConstructorRule(Set<Declaration> classes) {


        for (Declaration entityNode : classes) {

            // Checks the class and the inherited methods from the super class
            List<Declaration> constructors = entityNode.getConstructors();
            boolean passed = false;

            for (Declaration methodNode : constructors) {
                List<Parametre> parameters = methodNode.getParametres();
                if (parameters.isEmpty()) {
                    passed = true;
                    break;
                }
            }

            if (!passed) {
                this.addReport("The class <" + entityNode.getName()
                        + "> doesn't contain a default constructor.\n");
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return this.isEmptyReport();
    }

    public final boolean noFinalClassRule(Set<Declaration> classes) {

        for (Declaration entityNode : classes) {

            List<Parametre> members = entityNode.getFields();
            Set<String> modifiers = new HashSet<>();
            for (Parametre m :members){
                modifiers.addAll(m.getModifiers());
            }
            if (modifiers.contains(Modifier.Keyword.FINAL.asString())) {
                this.addReport("The class <" + entityNode.getName()
                        + "> can't to be a final class.\n");
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return this.isEmptyReport();
    }

    public final boolean provideGetsSetsFieldsRule(Set<Declaration> classes) {


        for (Declaration entityNode : classes) {

            List<Parametre> declaredFields = entityNode.getFields();

            boolean passed = true;

            for (Parametre fieldNode : declaredFields) {

                if (fieldNode.isStatic()) {
                    continue;
                }

                if (!hasGetMethod(fieldNode, entityNode)) {
                    this.addReport("The field <" + fieldNode.getName() + "> of the class <"
                            + fieldNode.getName()
                            + " doesn't implement the get method.\n");
                    passed = false;
                }
                if (!hasSetMethod(fieldNode, entityNode)) {
                    this.addReport("The field <" + fieldNode.getName() + "> of the class <"
                            + fieldNode.getName()
                            + " doesn't implement the set method.\n");
                    passed = false;
                }
            }

            if (!passed) {
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return this.isEmptyReport();
    }

    public final boolean provideIdentifierPropertyRule(Set<Declaration> classes) {


        for (Declaration entityNode : classes) {

            Parametre field = getIdentifierProperty(entityNode);

            if (field == null) {
                this.addReport("The class <" + entityNode.getName()
                        + " doesn't provide identifier property.\n");
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return this.isEmptyReport();
    }

    public final boolean useInterfaceSetOrListRule(Set<Declaration> allModelClasses) {

        for (Declaration entityNode : allModelClasses) {

            List<Parametre> declaredFields = entityNode.getFields();
            boolean passed = true;

            for (Parametre fieldNode : declaredFields) {
                String type = fieldNode.getType();

                if (Utils.isCollection(type) && !type.equals(Utils.SET_NAME)
                        && !type.equals(Utils.LIST_NAME)) {
                    this.addReport("The field <"
                            + fieldNode.getName()
                            + "> of the class <" + entityNode.getName()
                            + " implements interface Collection but it "
                            + "doesn't implements interface Set or interface List.\n");
                    passed = false;
                    addResultFalse(entityNode);
                }
            }

            if (!passed) {
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return this.isEmptyReport();
    }

    public final boolean useListCollectionRule(Set<Declaration> allModelClasses) {

        for (Declaration entityNode : allModelClasses) {

            List<Parametre> declaredFields = entityNode.getFields();
            boolean passed = true;

            for (Parametre fieldNode : declaredFields) {
                String type = fieldNode.getType();

                if (Utils.isCollection(type) && !Utils.isList(type)) {
                    this.addReport("The field <" + fieldNode.getName()
                            + "> of the class <" + fieldNode.getName()
                            + " implements interface Collection but "
                            + "it doesn't implements interface Set.\n");
                    passed = false;
                    addResultFalse(entityNode);
                }
            }

            if (!passed) {
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return this.isEmptyReport();
    }

    public final boolean useSetCollectionRule(Set<Declaration> allModelClasses) {

        for (Declaration entityNode : allModelClasses) {

            List<Parametre> declaredFields = entityNode.getFields();
            boolean passed = true;

            for (Parametre fieldNode : declaredFields) {
                String type = fieldNode.getType();

                if (Utils.isCollection(type) && !Utils.isSet(type)) {
                    this.addReport("The field <" + fieldNode.getName()
                            + "> of the class <" + fieldNode.getName()
                            + " implements interface Collection but "
                            + "it doesn't implements interface Set.\n");
                    passed = false;
                    addResultFalse(entityNode);
                }
            }

            if (!passed) {
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return this.isEmptyReport();
    }

    public void check(List<CompilationUnit> cus){
        Set<Declaration> declrs = new HashSet<>();
        for(CompilationUnit cu: cus){
            for(TypeDeclaration td: cu.getTypes()){
                Declaration d = findTypeDeclaration(td.getNameAsString(), cus, 1);
                if(d!=null) {
                    declrs.add(d);
                }
            }
        }
        hashCodeAndEqualsNotUseIdentifierPropertyRule(declrs);
        hashCodeAndEqualsRule(declrs);
        noArgumentConstructorRule(declrs);
        noFinalClassRule(declrs);
        provideGetsSetsFieldsRule(declrs);
        provideIdentifierPropertyRule(declrs);
        useInterfaceSetOrListRule(declrs);
        useListCollectionRule(declrs);
        useSetCollectionRule(declrs);
    }

}
