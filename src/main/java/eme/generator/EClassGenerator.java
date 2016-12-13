package eme.generator;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;

import eme.model.ExtractedClass;
import eme.model.ExtractedEnumeration;
import eme.model.ExtractedInterface;
import eme.model.ExtractedPackage;
import eme.model.ExtractedType;
import eme.properties.ExtractionProperties;

/**
 * This class allows to generate Ecore metamodel components, which are EObjects, with simple method
 * calls. It utilizes the EcoreFactory class.
 * @author Timur Saglam
 */
public class EClassGenerator {

    private EcoreFactory ecoreFactory;
    private ExtractionProperties properties;

    /**
     * Basic constructor
     * @param properties is the properties class for the extraction.
     */
    public EClassGenerator(ExtractionProperties properties) {
        this.properties = properties;
        ecoreFactory = EcoreFactory.eINSTANCE;
    }

    /**
     * Generates an EPackage from an extractedPackage. Recursively calls this method to all
     * contained elements.
     * @param extractedPackage is the package the EPackage gets generated from.
     * @return the generated EPackage.
     */
    public EPackage generateEPackage(ExtractedPackage extractedPackage, String projectName) {
        EPackage ePackage = ecoreFactory.createEPackage();
        if (extractedPackage.isRoot()) {
            ePackage.setName(properties.getDefaultPackageName());
            ePackage.setNsPrefix(properties.getDefaultPackageName());
        } else {
            ePackage.setName(extractedPackage.getName());
            ePackage.setNsPrefix(extractedPackage.getName());
        }
        ePackage.setNsURI(projectName + "/" + extractedPackage.getFullName());
        for (ExtractedPackage subpackage : extractedPackage.getSubpackages()) {
            ePackage.getESubpackages().add(generateEPackage(subpackage, projectName));
        }
        for (ExtractedType type : extractedPackage.getTypes()) {
            ePackage.getEClassifiers().add(generateEClassifier(type));
        }
        return ePackage;
    }

    /**
     * Generates an EClass from an ExtractedClass.
     * @param extractedClass is the ExtractedClass.
     * @return the EClass.
     */
    private EClass generateEClass(ExtractedClass extractedClass) {
        EClass eClass = ecoreFactory.createEClass();
        eClass.setAbstract(extractedClass.isAbstract());
        return eClass;
    }

    /**
     * Generates an EClass from an ExtractedInterface.
     * @param extractedInterface is the ExtractedInterface.
     * @return the EClass.
     */
    private EClass generateEClass(ExtractedInterface extractedInterface) {
        EClass eClass = ecoreFactory.createEClass();
        eClass.setAbstract(true);
        eClass.setInterface(true);
        return eClass;
    }

    /**
     * Generates a EClassifier from an ExtractedType.
     * @param type is the ExtractedType.
     * @return the EClassifier, which is either an EClass, an EInterface or an EEnum.
     */
    private EClassifier generateEClassifier(ExtractedType type) {
        EClassifier eClassifier = null;
        if (type.getClass() == ExtractedInterface.class) {
            eClassifier = generateEClass((ExtractedInterface) type);
        } else if (type.getClass() == ExtractedClass.class) {
            eClassifier = generateEClass((ExtractedClass) type);
        } else if (type.getClass() == ExtractedEnumeration.class) {
            eClassifier = generateEEnum((ExtractedEnumeration) type);
        }
        eClassifier.setName(type.getName());
        return eClassifier;
    }

    /**
     * Generates an EEnum from an ExtractedEnumeration.
     * @param extractedEnum is the ExtractedEnumeration.
     * @return the EEnum.
     */
    private EEnum generateEEnum(ExtractedEnumeration extractedEnum) {
        EEnum eEnum = ecoreFactory.createEEnum(); // create EEnum
        for (String enumeral : extractedEnum.getEnumerals()) { // for very Enumeral
            EEnumLiteral literal = ecoreFactory.createEEnumLiteral(); // create literal
            literal.setName(enumeral); // set name.
            literal.setValue(eEnum.getELiterals().size()); // set ordinal.
            eEnum.getELiterals().add(literal); // add literal to enumm.
        }
        return eEnum;
    }

}