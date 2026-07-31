package com.redmath.training.cucumber.steps;

import com.redmath.training.cucumber.core.Page;
import java.nio.file.Files;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;


import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class VocabularyStepDefinition {

    private final Page page;

    public VocabularyStepDefinition(Page page) {
        this.page = page;
    }

    @Given("visit {string} page")
    public void visit_page(String url) {
        page.visit(url);
    }

    @When("enter {string} into {string}")
    public void enter_text_into(String text, String element) {
        page.text(text, By.xpath(page.resolve(element)));
    }

    @When("click on {string}")
    public void click_on(String element) {
        page.click(By.xpath(page.resolve(element)));
    }

    @Then("search results for {string} are shown")
    public void search_results_for_are_shown(String text) {
        Assertions.assertTrue(page.title().startsWith(text));
    }

    @Then("take screenshot")
    public void take_screenshot() {
        Assertions.assertTrue(Files.exists(page.screenshot()));
    }
}
