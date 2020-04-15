package com.paulhammant.ngwebdriver;

import org.junit.Test;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.Annotations;

public class FindByNgWebDriverTest {

    @ByAngularBinding.FindBy(rootSelector = "butter", binding = "cheese")
    public WebElement findByBinding;

    @ByAngularButtonText.FindBy(rootSelector = "butter", buttonText = "cheese")
    public WebElement findByButtonText;

    @ByAngularCssContainingText.FindBy(rootSelector = "butter", cssSelector = "cheese", searchText = "crackers")
    public WebElement findByCssContainingText;

    @ByAngularExactBinding.FindBy(rootSelector = "butter", exactBinding = "cheese")
    public WebElement findByExactBinding;

    @ByAngularModel.FindBy(rootSelector = "butter", model = "cheese")
    public WebElement findByModel;

    @ByAngularOptions.FindBy(rootSelector = "butter", options = "cheese")
    public WebElement findByOptions;

    @ByAngularPartialButtonText.FindBy(rootSelector = "butter", partialButtonText = "cheese")
    public WebElement findByPartialButtonText;

    @ByAngularRepeater.FindBy(rootSelector = "butter", repeater = "cheese", exact = true)
    public WebElement findByRepeater;

    @ByAngularRepeaterCell.FindBy(rootSelector = "butter", repeater = "cheese", exact = true, row = 99, column = "cracker")
    public WebElement findByRepeaterCell;

    @ByAngularRepeaterColumn.FindBy(rootSelector = "butter", repeater = "cheese", exact = true, column = "cracker")
    public WebElement findByRepeaterColumn;

    @ByAngularRepeaterRow.FindBy(rootSelector = "butter", repeater = "cheese", exact = true, row = 99)
    public WebElement findByRepeaterRow;

    @Test
    public void findByAngularBinding() throws Exception {
        ByAngularBinding built = (ByAngularBinding) new Annotations(getClass().getField("findByBinding")).buildBy();
        ByAngularBinding expected = new ByAngularBinding("butter", "cheese");
        Assertions.assertThat(built).isEqualTo(expected);
    }

    @Test
    public void findByButtonText() throws Exception {
        ByAngularButtonText built = (ByAngularButtonText) new Annotations(getClass().getField("findByButtonText")).buildBy();
        ByAngularButtonText expected = new ByAngularButtonText("butter", "cheese");
        Assertions.assertThat(built).isEqualTo(expected);
    }

    @Test
    public void findByCssContainingText() throws Exception {
        ByAngularCssContainingText built = (ByAngularCssContainingText) new Annotations(getClass().getField("findByCssContainingText")).buildBy();
        ByAngularCssContainingText expected = new ByAngularCssContainingText("butter", "cheese", "crackers");
        Assertions.assertThat(built).isEqualTo(expected);
    }

    @Test
    public void findByExactBinding() throws Exception {
        ByAngularExactBinding built = (ByAngularExactBinding) new Annotations(getClass().getField("findByExactBinding")).buildBy();
        ByAngularExactBinding expected = new ByAngularExactBinding("butter", "cheese");
        Assertions.assertThat(built).isEqualTo(expected);
    }

    @Test
    public void findByModel() throws Exception {
        ByAngularModel built = (ByAngularModel) new Annotations(getClass().getField("findByModel")).buildBy();
        ByAngularModel expected = new ByAngularModel("butter", "cheese");
        Assertions.assertThat(built).isEqualTo(expected);
    }

    @Test
    public void findByOptions() throws Exception {
        ByAngularOptions built = (ByAngularOptions) new Annotations(getClass().getField("findByOptions")).buildBy();
        ByAngularOptions expected = new ByAngularOptions("butter", "cheese");
        Assertions.assertThat(built).isEqualTo(expected);
    }

    @Test
    public void findByPartialButtonText() throws Exception {
        ByAngularPartialButtonText built = (ByAngularPartialButtonText) new Annotations(getClass().getField("findByPartialButtonText")).buildBy();
        ByAngularPartialButtonText expected = new ByAngularPartialButtonText("butter", "cheese");
        Assertions.assertThat(built).isEqualTo(expected);
    }

    @Test
    public void findByRepeater() throws Exception {
        ByAngularRepeater built = (ByAngularRepeater) new Annotations(getClass().getField("findByRepeater")).buildBy();
        ByAngularRepeater expected = new ByAngularRepeater("butter", "cheese", true);
        Assertions.assertThat(built).isEqualTo(expected);
    }
    
    @Test
    public void findByRepeaterCell() throws Exception {
        ByAngularRepeaterCell built = (ByAngularRepeaterCell) new Annotations(getClass().getField("findByRepeaterCell")).buildBy();
        ByAngularRepeaterCell expected = new ByAngularRepeaterCell("butter", "cheese", true,  99,  "cracker");
        Assertions.assertThat(built).isEqualTo(expected);
    }

    @Test
    public void findByRepeaterColumn() throws Exception {
        ByAngularRepeaterColumn built = (ByAngularRepeaterColumn) new Annotations(getClass().getField("findByRepeaterColumn")).buildBy();
        ByAngularRepeaterColumn expected = new ByAngularRepeaterColumn("butter", "cheese", true,  "cracker");
        Assertions.assertThat(built).isEqualTo(expected);
    }

    @Test
    public void findByRepeaterRow() throws Exception {
        ByAngularRepeaterRow built = (ByAngularRepeaterRow) new Annotations(getClass().getField("findByRepeaterRow")).buildBy();
        ByAngularRepeaterRow expected = new ByAngularRepeaterRow("butter", "cheese", true,  99);
        Assertions.assertThat(built).isEqualTo(expected);
    }
}

