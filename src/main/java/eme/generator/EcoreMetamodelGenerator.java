package eme.generator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EPackage;

import eme.generator.saving.AbstractSavingStrategy;
import eme.generator.saving.CopyProjectSaving;
import eme.generator.saving.CustomPathSaving;
import eme.generator.saving.ExistingProjectSaving;
import eme.generator.saving.NewProjectSaving;
import eme.generator.saving.OriginalProjectSaving;
import eme.generator.saving.SavingInformation;
import eme.model.ExtractedPackage;
import eme.model.IntermediateModel;
import eme.properties.ExtractionProperties;
import eme.properties.TextProperty;

/**
 * This class generates an Ecore Metamodel from an {@link IntermediateModel}. It also allows to save a generated
 * metamodel as an Ecore file using a specific saving strategy.
 * @author Timur Saglam
 */
public class EcoreMetamodelGenerator {
    private static final Logger logger = LogManager.getLogger(EcoreMetamodelGenerator.class.getName());
    private static final String OUTPUT_PROJECT = "EME-Generator-Output";
    private final EPackageGenerator ePackageGenerator;
    private GeneratedEcoreMetamodel metamodel;
    private String projectName;
    private final ExtractionProperties properties;
    private AbstractSavingStrategy savingStrategy;

    /**
     * Basic constructor.
     * @param properties is the ExtractionProperties class for the exraction.
     */
    public EcoreMetamodelGenerator(ExtractionProperties properties) {
        this.properties = properties;
        ePackageGenerator = new EPackageGenerator(properties); // build generators
    }

    /**
     * Changes the {@link AbstractSavingStrategy} to a new one.
     * @param strategyName is the name of the new saving strategy.
     */
    public final void changeSavingStrategy(String strategyName) { // Add custom strategies here
        if (isStrategy(ExistingProjectSaving.class, strategyName)) {
            savingStrategy = new ExistingProjectSaving(OUTPUT_PROJECT);
        } else if (isStrategy(OriginalProjectSaving.class, strategyName)) {
            savingStrategy = new OriginalProjectSaving();
        } else if (isStrategy(CustomPathSaving.class, strategyName)) {
            savingStrategy = new CustomPathSaving();
        } else if (isStrategy(CopyProjectSaving.class, strategyName)) {
            savingStrategy = new CopyProjectSaving(properties.get(TextProperty.PROJECT_SUFFIX));
        } else if (isStrategy(NewProjectSaving.class, strategyName)) {
            savingStrategy = new NewProjectSaving();
        } else {
            logger.error("Unknown saving strategy: " + strategyName);
            savingStrategy = new NewProjectSaving();
        }
    }

    /**
     * Method starts the Ecore metamodel generation.
     * @param model is the {@link IntermediateModel} that is the source for the generator.
     * @return the root element of the metamodel, an {@link EPackage}.
     */
    public GeneratedEcoreMetamodel generateMetamodel(IntermediateModel model) {
        changeSavingStrategy(properties.get(TextProperty.SAVING_STRATEGY)); // set saving strategy
        logger.info("Started generating the metamodel...");
        ExtractedPackage root = model.getRoot(); // get root package.
        if (root == null || !root.isSelected()) { // check if valid.
            throw new IllegalArgumentException("The root of an model can't be null or deselected: " + model.toString());
        }
        projectName = model.getProjectName(); // store project name.
        EPackage eRoot = ePackageGenerator.generate(model); // generate model model.
        metamodel = new GeneratedEcoreMetamodel(eRoot, model);
        return metamodel;
    }

    /**
     * Saves the metamodel as an Ecore file.
     * @return the saving information.
     */
    public SavingInformation saveMetamodel() {
        logger.info("Started saving the metamodel");
        if (metamodel == null) {
            throw new IllegalStateException("Cannot save Ecore metamodel before extracting one.");
        }
        SavingInformation savingInformation = savingStrategy.save(metamodel.getRoot(), projectName);
        metamodel.setSavingInformation(savingInformation);
        return savingInformation;
    }

    /**
     * Checks whether a strategy class fits to a strategy name.
     */
    private boolean isStrategy(Class<? extends AbstractSavingStrategy> strategy, String strategyName) {
        return strategy.getSimpleName().startsWith(strategyName);
    }
}