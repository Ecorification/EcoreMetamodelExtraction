package eme.extractor;

import static eme.extractor.JDTUtil.getModifier;
import static eme.extractor.JDTUtil.getName;
import static eme.extractor.JDTUtil.isAbstract;
import static eme.extractor.JDTUtil.isEnum;
import static eme.extractor.JDTUtil.isFinal;
import static eme.extractor.JDTUtil.isStatic;
import static eme.extractor.JDTUtil.isVoid;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import eme.model.ExtractedMethod;
import eme.model.ExtractedType;
import eme.model.MethodType;
import eme.model.datatypes.ExtractedField;

/**
 * Extractor class for Java Members (Methods and fields). Uses the class {@link DataTypeExtractor}.
 * @author Timur Saglam
 */
public class JavaMemberExtractor {
    private final DataTypeExtractor dataTypeParser;

    /**
     * Basic constructor.
     * @param dataTypeParser sets the DataTypeParser.
     */
    public JavaMemberExtractor(DataTypeExtractor dataTypeParser) {
        this.dataTypeParser = dataTypeParser;
    }

    /**
     * Parses Fields from an {@link IType} and adds them to an {@link ExtractedType}.
     * @param type is the {@link IType}.
     * @param extractedType is the {@link ExtractedType}.
     * @throws JavaModelException if there are problem with the JDT API.
     */
    public void parseFields(IType type, ExtractedType extractedType) throws JavaModelException {
        ExtractedField extractedField; // TODO (MEDIUM) move to method extractor, make member extractor.
        for (IField field : type.getFields()) {
            if (!isEnum(field)) { // if is no enumeral
                extractedField = dataTypeParser.parseField(field, type);
                extractedField.setFinal(isFinal(field));
                extractedField.setStatic(isStatic(field));
                extractedField.setModifier(getModifier(field));
                extractedType.addAttribute(extractedField);
            }
        }
    }

    /**
     * Parses the {@link IMethod}s from an {@link IType} and adds them to an ExtractedType.
     * @param type is the {@link IType} whose methods get parsed.
     * @param extractedType is the extracted type where the extracted methods should be added.
     * @throws JavaModelException if there are problem with the JDT API.
     */
    public void parseMethods(IType type, ExtractedType extractedType) throws JavaModelException {
        ExtractedMethod extractedMethod;
        String methodName; // name of the extracted method
        for (IMethod method : type.getMethods()) { // for every method
            methodName = getName(type) + "." + method.getElementName(); // build name
            extractedMethod = new ExtractedMethod(methodName, dataTypeParser.parseReturnType(method));
            extractedMethod.setAbstract(isAbstract(method));
            extractedMethod.setStatic(isStatic(method));
            extractedMethod.setMethodType(parseMethodType(method));
            extractedMethod.setModifier(getModifier(method));
            for (ILocalVariable parameter : method.getParameters()) { // parse parameters:
                extractedMethod.addParameter(dataTypeParser.parseParameter(parameter, method));
            }
            for (String exception : method.getExceptionTypes()) { // parse throw declarations:
                extractedMethod.addThrowsDeclaration(dataTypeParser.parseDataType(exception, type));
            }
            extractedType.addMethod(extractedMethod);
        }
    }

    /**
     * Checks whether a {@link IMethod} is an access method (either an accessor or an mutator, depending on the prefix).
     */
    private boolean isAccessMethod(String prefix, IMethod method) throws JavaModelException {
        IType type = method.getDeclaringType();
        for (IField field : type.getFields()) { // for ever field of IType:
            if (method.getElementName().equalsIgnoreCase(prefix + field.getElementName())) {
                return true; // is access method if name scheme fits for one field
            }
        }
        return false; // is not an access method if no field fits
    }

    /**
     * Checks whether a {@link IMethod} is an accessor method.
     */
    private boolean isAccessor(IMethod method) throws JavaModelException {
        if (isAccessMethod("get", method) || isAccessMethod("is", method)) { // if name fits
            return method.getNumberOfParameters() == 0 && !isVoid(method.getReturnType());
        }
        return false;
    }

    /**
     * Checks whether a {@link IMethod} is a mutator method.
     */
    private boolean isMutator(IMethod method) throws JavaModelException {
        if (isAccessMethod("set", method)) { // if name fits
            return method.getNumberOfParameters() == 1 && isVoid(method.getReturnType());
        }
        return false;
    }

    /**
     * Parses the {@link MethodType} of an {@link IMethod}.
     */
    private MethodType parseMethodType(IMethod method) throws JavaModelException {
        if (method.isConstructor()) {
            return MethodType.CONSTRUCTOR;
        } else if (isAccessor(method)) {
            return MethodType.ACCESSOR;
        } else if (isMutator(method)) {
            return MethodType.MUTATOR;
        } else if (method.isMainMethod()) {
            return MethodType.MAIN;
        }
        return MethodType.NORMAL;
    }
}