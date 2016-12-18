package eme.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class IntermediateModelTest {

    IntermediateModel model;

    @Before
    public void setUp() throws Exception {
        model = new IntermediateModel("TestModel");
    }

    @Test
    public void testAutoAdd() {
        addMVCPackages();
        model.add(new ExtractedClass("NormalClass", false));
        model.add(new ExtractedClass("main.AbstractClass", true));
        model.add(new ExtractedInterface("main.model.Interface"));
        model.add(new ExtractedEnumeration("main.view.Enumeration"));
        model.add(new ExtractedClass("main.controller.OuterClass$InnerClass", false));
        checkTypeAmount(model.getRoot(), 1);
    }

    @Test
    public void testDuplicatePackage() {
        addMVCPackages();
        addMVCPackages();
        assertEquals(4, countPackages(model.getRoot()));
    }

    @Test(expected = RuntimeException.class)
    public void testUnknownPackage() {
        addMVCPackages();
        model.add(new ExtractedPackage("illegal.subpackage"));
        model.add(new ExtractedPackage("main.illegal.subpackage"));
    }

    private void addMVCPackages() {
        model.add(new ExtractedPackage(""));
        model.add(new ExtractedPackage("main"));
        model.add(new ExtractedPackage("main.model"));
        model.add(new ExtractedPackage("main.view"));
        model.add(new ExtractedPackage("main.controller"));
    }

    private void checkTypeAmount(ExtractedPackage fromPackage, int amountPerPackage) {
        assertEquals(amountPerPackage, fromPackage.getTypes().size());
        for (ExtractedPackage subpackage : fromPackage.getSubpackages()) {
            checkTypeAmount(subpackage, amountPerPackage);
        }
    }

    private int countPackages(ExtractedPackage inPackage) {
        int amount = 0;
        for (ExtractedPackage subpackage : inPackage.getSubpackages()) {
            amount += 1 + countPackages(subpackage);
        }
        return amount;
    }
}