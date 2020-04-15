package com.paulhammant.ngwebdriver;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.MovedContextHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.StdErrLog;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.seleniumhq.selenium.fluent.FluentBy;
import org.seleniumhq.selenium.fluent.FluentExecutionStopped;
import org.seleniumhq.selenium.fluent.FluentMatcher;
import org.seleniumhq.selenium.fluent.FluentWebDriver;
import org.seleniumhq.selenium.fluent.FluentWebElement;
import org.seleniumhq.selenium.fluent.FluentWebElementMap;
import org.seleniumhq.selenium.fluent.FluentWebElements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.tagName;
import static org.openqa.selenium.By.xpath;

public class AngularAndWebDriverTest {

  private static final String[] messageArray = {
    "both angularJS testability and angular testability are undefined.",
    "  This could be either because this is a non-angular page or because ",
    "your test involves client-side navigation, which can interfere ",
    "with Protractor's bootstrapping.  See http://git.io/v4gXM for details"
  };

  private static final String message = StringUtils.join(messageArray);

  private static ChromeDriver webDriver;
  private static Server webServer;
  private static NgWebDriver ngWebDriver;

  @BeforeClass
  public static void before_suite() throws Exception {

    // Launch Protractor's own test app on http://localhost:8080
    ((StdErrLog) Log.getRootLogger()).setLevel(StdErrLog.LEVEL_OFF);
    webServer = new Server(new QueuedThreadPool(6));
    ServerConnector connector = new ServerConnector(webServer, new HttpConnectionFactory());
    connector.setPort(8080);
    webServer.addConnector(connector);
    ResourceHandler resource_handler = new ResourceHandler();
    resource_handler.setDirectoriesListed(true);
    resource_handler.setWelcomeFiles(new String[]{"index.html"});
    resource_handler.setResourceBase("src/test/webapp");
    HandlerList handlers = new HandlerList();
    MovedContextHandler effective_symlink = new MovedContextHandler(webServer, "/lib/angular", "/lib/angular_v1.2.9");
    handlers.setHandlers(new Handler[]{effective_symlink, resource_handler, new DefaultHandler()});
    webServer.setHandler(handlers);
    webServer.start();

    webDriver = new ChromeDriver();
    webDriver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
    ngWebDriver = new NgWebDriver(webDriver);
  }

  @AfterClass
  public static void after_suite() throws Exception {
    webDriver.quit();
    webServer.stop();
  }

  @Before
  public void resetBrowser() {
    webDriver.get("about:blank");
  }

  @Test
  public void waitForAngularRequestsToFinish_non_angular_app() {
    webDriver.get("http://www.google.com");
    Object actual = ngWebDriver.waitForAngularRequestsToFinish();
    Assertions.assertThat(actual).isEqualTo(message);
  }

  @Test
  public void find_by_angular_model() {

    //webDriver.get("http://www.angularjshub.com/code/examples/basics/02_TwoWayDataBinding_HTML/index.demo.php");
    webDriver.get("http://localhost:8080/");
    ngWebDriver.waitForAngularRequestsToFinish();

    WebElement firstname = webDriver.findElement(ByAngular.model("username"));
    firstname.clear();
    firstname.sendKeys("Mary");
    Assertions.assertThat(webDriver.findElement(xpath("//input")).getAttribute("value"))
      .isEqualTo("Mary");
  }

  @Test
  public void find_all_for_an_angular_options() {

    webDriver.get("http://localhost:8080/#/form");
    ngWebDriver.waitForAngularRequestsToFinish();

    List<WebElement> weColors = webDriver.findElements(ByAngular.options("fruit for fruit in fruits"));
    assertThat(weColors.get(0).getText(), containsString("apple"));
    assertThat(weColors.get(3).getText(), containsString("banana"));

  }

  @Test
  public void find_by_angular_buttonText() {

    webDriver.get("http://localhost:8080/#/form");
    ngWebDriver.waitForAngularRequestsToFinish();

    webDriver.findElement(ByAngular.buttonText("Open Alert")).click();
    Alert alert = webDriver.switchTo().alert();
    assertThat(alert.getText(), containsString("Hello"));
    alert.accept();
  }

  @Test
  public void find_by_angular_partialButtonText() {

    webDriver.get("http://localhost:8080/#/form");
    ngWebDriver.waitForAngularRequestsToFinish();

    webDriver.findElement(ByAngular.partialButtonText("Alert")).click();
    Alert alert = webDriver.switchTo().alert();
    assertThat(alert.getText(), containsString("Hello"));
    alert.accept();
  }

  @Test
  public void find_by_angular_cssContainingText() {

    webDriver.get("http://localhost:8080/#/form");
    ngWebDriver.waitForAngularRequestsToFinish();

    List<WebElement> wes = webDriver.findElements(ByAngular.cssContainingText("#animals ul .pet", "dog"));
    Assertions.assertThat(wes.size()).isEqualTo(2);
    Assertions.assertThat(wes.get(1).getText()).containsPattern("small dog");
  }

  @Test
  public void find_by_angular_cssContainingTextRegexp() {

    webDriver.get("http://localhost:8080/#/form");
    ngWebDriver.waitForAngularRequestsToFinish();

    List<WebElement> wes = webDriver
      .findElements(ByAngular.cssContainingText("#animals ul .pet",
        "__REGEXP__/(?:BIG|small) *(?:CAT|dog)(something else)*/i"));
    Assertions.assertThat(wes.size()).isEqualTo(4);
    String[] results = new String[]{"big dog", "small dog", "big cat",
      "small cat"};
    HashSet<String> elementTexts = new HashSet<>();
    for (WebElement we : wes) {
      elementTexts.add(we.getText());
    }
    Assertions.assertThat(elementTexts).contains(results);
    // hasItems(results));
  }

  @Test
  @Ignore(value = "ignore message for find_multiple_hits_for_ng_repeat_in_page: angularjshub is down")
  public void find_multiple_hits_for_ng_repeat_in_page() {

    webDriver.get("http://www.angularjshub.com/code/examples/collections/01_Repeater/index.demo.php");
    ngWebDriver.waitForAngularRequestsToFinish();

    List<WebElement> wes = webDriver.findElement(tagName("table")).findElements(ByAngular.repeater("person in people"));

    Assertions.assertThat(wes.size()).isEqualTo(4);
    Assertions.assertThat(wes.get(0).findElement(tagName("td")).getText()).contains("John");
    Assertions.assertThat(wes.get(1).findElement(tagName("td")).getText()).contains("Bob");
    Assertions.assertThat(wes.get(2).findElement(tagName("td")).getText()).contains("Jack");
    Assertions.assertThat(wes.get(3).findElement(tagName("td")).getText()).contains("Michael");
  }

  @Test
  @Ignore(value = "ignore message for find_multiple_hits_for_ng_repeat_and_subset_to_first_matching_predicate_for_fluent_selenium_example: angularjshub is down")
  public void find_multiple_hits_for_ng_repeat_and_subset_to_first_matching_predicate_for_fluent_selenium_example() {

    // As much as anything, this is a test of FluentSelenium

    webDriver.get("http://www.angularjshub.com/code/examples/collections/01_Repeater/index.demo.php");
    ngWebDriver.waitForAngularRequestsToFinish();

    new FluentWebDriver(webDriver).lis(ByAngular.repeater("(propName, propValue) in person"))
      .first(new TextContainsTerm("name ="))
      .getText().shouldContain("John");

  }

  public static class TextContainsTerm implements FluentMatcher {

    @SuppressWarnings("FieldMayBeFinal")
    private String term;

    public TextContainsTerm(String term) {
      this.term = term;
    }

    public boolean matches(FluentWebElement webElement, int ix) {
      return webElement.getWebElement().getText().contains(term);
    }

    @Override
    public String toString() {
      return "TextContainsTerm{term='" + term + "'}";
    }
  }

  @Test
  @Ignore("ignore message for find_second_row_in_ng_repeat: angularjshub is down")
  public void find_second_row_in_ng_repeat() {

    webDriver.get("http://www.angularjshub.com/code/examples/collections/01_Repeater/index.demo.php");
    ngWebDriver.waitForAngularRequestsToFinish();

    // find the second person
    // index starts withRootSelector 0 of course

    Assertions.assertThat(webDriver.findElement(ByAngular.repeater("person in people").row(1)).getText()).isEqualTo("Bob Smith");

  }

  @Ignore(value = "ignoring find_specific_cell_in_ng_repeat()")
  @Test
  public void find_specific_cell_in_ng_repeat() {

    webDriver.get("http://www.angularjshub.com/code/examples/collections/01_Repeater/index.demo.php");
    ngWebDriver.waitForAngularRequestsToFinish();

    webDriver.findElement(ByAngular.repeater("person in selectablePeople").row(2).column("person.isSelected")).click();

    Assertions.assertThat(webDriver.findElement(xpath("//tr[@byNg-repeat='person in selectablePeople']")).getText()).isEqualTo("x y z");
  }

  @Ignore(value = "ignoring find_specific_cell_in_ng_repeat_the_other_way")
  @Test
  public void find_specific_cell_in_ng_repeat_the_other_way() {

    webDriver.get("http://www.angularjshub.com/code/examples/collections/01_Repeater/index.demo.php");
    ngWebDriver.waitForAngularRequestsToFinish();

    // find the second address' city
    webDriver.findElement(ByAngular.repeater("person in selectablePeople").column("person.isSelected").row(2)).click();

    Assertions.assertThat(webDriver.findElement(xpath("//tr[@byNg-repeat='person in selectablePeople']")).getText()).isEqualTo("x y z");
  }

  @Ignore(value = "ignoring find_all_of_a_column_in_an_ng_repeat")
  @Test
  public void find_all_of_a_column_in_an_ng_repeat() {

    webDriver.get("http://www.angularjshub.com/code/examples/collections/01_Repeater/index.demo.php");
    ngWebDriver.waitForAngularRequestsToFinish();

    List<WebElement> we = webDriver.findElements(ByAngular.repeater("person in selectablePeople").column("person.isSelected"));

    Assertions.assertThat(we.get(0).getText()).isEqualTo("unselected");
    we.get(1).click();
    Assertions.assertThat(we.get(1).getText()).isEqualTo("selected");
    Assertions.assertThat(we.get(2).getText()).isEqualTo("unselected");
  }

  @Test
  public void find_by_angular_binding() {

    webDriver.get("http://localhost:8080/#/form");
    ngWebDriver.waitForAngularRequestsToFinish();

    // An example showcasing ByBinding, where we give a substring of the binding attribute
    List<WebElement> wes = webDriver.findElements(ByAngular.binding("user"));
    Assertions.assertThat(wes.get(0).getText()).isEqualTo("Anon");
    //An exmple showcasing ByExactBinding, where it strictly matches the given Biding attribute name.
    List<WebElement> weeb = webDriver.findElements(ByAngular.exactBinding("username"));
    Assertions.assertThat(weeb.get(0).getText()).isEqualTo("Anon");
  }

  @Test
  @Ignore(value = "ignore message for find_all_for_an_angular_binding: angularjshub is down")
  public void find_all_for_an_angular_binding() {

    webDriver.get("http://www.angularjshub.com/code/examples/forms/04_Select/index.demo.php");
    ngWebDriver.waitForAngularRequestsToFinish();

    List<WebElement> wes = webDriver.findElements(ByAngular.binding("peopleArrayValue4"));

    Assertions.assertThat(wes.get(0).getTagName()).isEqualTo("textarea");

    // really need an example that return more than one.

  }

  // Model interaction

  @SuppressWarnings("rawtypes")
  @Test
  @Ignore(value = "ignore message for model_mutation_and_query_is_possible: angularjshub is down")
  public void model_mutation_and_query_is_possible() {

    webDriver.get("http://www.angularjshub.com/code/examples/forms/08_FormSubmission/index.demo.php");
    ngWebDriver.waitForAngularRequestsToFinish();

    WebElement fn = webDriver.findElement(id("firstNameEdit1"));
    fn.sendKeys("Fred");
    WebElement ln = webDriver.findElement(id("lastNameEdit1"));
    ln.sendKeys("Flintstone");

    WebElement wholeForm = webDriver.findElement(FluentBy.attribute("name", "personForm1"));

    NgWebDriver ngModel = new NgWebDriver(webDriver);

    // change something via the $scope model
    ngModel.mutate(wholeForm, "person1.firstName", "'Wilma'");
    // assert the change happened via regular WebDriver.
    assertThat(fn.getAttribute("value"), containsString("Wilma"));

    // retrieve the JSON for the location via the $scope model
    String tv = ngModel.retrieveJson(wholeForm, "person1");
    Assertions.assertThat(tv).isEqualTo("{\"firstName\":\"Wilma\",\"lastName\":\"Flintstone\"}");

    // retrieve a single field as JSON
    String v = ngModel.retrieveJson(wholeForm, "person1.firstName");

    // assert that it comes back indicating its type (presence of quotes)
    Assertions.assertThat(v).isEqualTo("\"Wilma\"");

    // retrieve it again, but directly as String
    v = ngModel.retrieveAsString(wholeForm, "person1.firstName");

    // assert it is still what we expect
    Assertions.assertThat(v).isEqualTo("Wilma");


    // WebDriver naturally hands back as a Map if it is not one
    // variable..
    Object rv = ngModel.retrieve(wholeForm, "person1");
    Assertions.assertThat(((Map) rv).get("firstName").toString()).isEqualTo("Wilma");

    // If something is numeric, WebDriver hands that back
    // naturally as a long.
//        long id = ngModel.retrieveAsLong(wholeForm, "location.Id");
//        assertThat(id, is(1675L));

    // Can't process scoped variables that don't exist
    try {
      ngModel.retrieve(wholeForm, "person1.Cityyyyyyy");
      Assertions.fail("should have barfed");
    }
    catch (WebDriverException e) {
      assertThat(e.getMessage(), startsWith("$scope variable 'person1.Cityyyyyyy' not found in same scope as the element passed in."));
    }
    // Can't process scoped variables that don't exist
    try {
      ngModel.retrieveJson(wholeForm, "locationnnnnnnnn");
    }
    catch (WebDriverException e) {
      assertThat(e.getMessage(), startsWith("$scope variable 'locationnnnnnnnn' not found in same scope as the element passed in."));
    }
    // Can't process scoped variables that don't exist
    try {
      ngModel.retrieveAsLong(wholeForm, "person1.Iddddddd");
    }
    catch (WebDriverException e) {
      assertThat(e.getMessage(), startsWith("$scope variable 'person1.Iddddddd' not found in same scope as the element passed in."));
    }

    // You can set whole parts of the tree within the scope..
    ngModel.mutate(wholeForm, "person1",
      "{" +
        "  firstName: 'Barney'," +
        "  lastName: 'Rubble'" +
        "}");

    Assertions.assertThat(fn.getAttribute("value")).isEqualTo("Barney");
    Assertions.assertThat(ln.getAttribute("value")).isEqualTo("Rubble");

    // Keys can be in quotes (single or double) or not have quotes at all.
    // Values can be in quotes (single or double) or not have quotes if
    // they are not of type string.

  }

  //  All the failure tests

  @Test
  @Ignore(value = "ignore message for findElement_should_barf_with_message_for_bad_repeater: angularjshub is down")
  public void findElement_should_barf_with_message_for_bad_repeater() {

    webDriver.get("http://www.angularjshub.com/code/examples/forms/04_Select/index.demo.php");
    ngWebDriver.waitForAngularRequestsToFinish();


    try {
      webDriver.findElement(ByAngular.repeater("location in Locationssss"));
      Assertions.fail("should have barfed");
    }
    catch (NoSuchElementException e) {
      assertThat(e.getMessage(), startsWith("repeater(location in Locationssss) didn't have any matching elements at this place in the DOM"));
    }

  }

  @Test
  @Ignore(value = "ignore message for findElement_should_barf_with_message_for_bad_repeater_and_row: angularjshub is down")
  public void findElement_should_barf_with_message_for_bad_repeater_and_row() {

    webDriver.get("http://www.angularjshub.com/code/examples/forms/04_Select/index.demo.php");
    ngWebDriver.waitForAngularRequestsToFinish();


    try {
      webDriver.findElement(ByAngular.repeater("location in Locationssss").row(99999));
      Assertions.fail("should have barfed");
    }
    catch (NoSuchElementException e) {
      assertThat(e.getMessage(), startsWith("repeater(location in Locationssss).row(99999) didn't have any matching elements at this place in the DOM"));
    }

  }

  @Test
  @Ignore(value = "ignore message for findElements_should_barf_with_message_for_any_repeater_and_row2: angularjshub is down")
  public void findElements_should_barf_with_message_for_any_repeater_and_row2() {

    webDriver.get("http://www.angularjshub.com/code/examples/forms/04_Select/index.demo.php");
    ngWebDriver.waitForAngularRequestsToFinish();


    try {
      webDriver.findElements(ByAngular.repeater("location in Locationssss").row(99999));
      Assertions.fail("should have barfed");
    }
    catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), startsWith("This locator zooms in on a single row, findElements() is meaningless"));
    }

  }

  @Test
  public void findElement_should_barf_with_message_for_bad_repeater_and_row_and_column() {

    try {
      webDriver.findElement(ByAngular.repeater("location in Locationssss").row(99999).column("blort"));
      Assertions.fail("should have barfed");
    }
    catch (NoSuchElementException e) {
      assertThat(e.getMessage(),
        startsWith("repeater(location in Locationssss).row(99999).column(blort) didn't have any matching elements at this place in the DOM"));
    }
  }

  @Test
  public void findElements_should_barf_with_message_for_any_repeater_and_row_and_column() {

    webDriver.get("http://www.angularjshub.com/code/examples/forms/04_Select/index.demo.php");
    ngWebDriver.waitForAngularRequestsToFinish();


    try {
      webDriver.findElements(ByAngular.repeater("location in Locationssss").row(99999).column("blort"));
      Assertions.fail("should have barfed");
    }
    catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), startsWith("This locator zooms in on a single cell, findElements() is meaningless"));
    }
  }

  @Test
  public void findElement_should_barf_when_element_not_in_the_dom() {

    webDriver.get("http://www.angularjshub.com/code/examples/forms/04_Select/index.demo.php");
    ngWebDriver.waitForAngularRequestsToFinish();

    try {
      webDriver.findElement(ByAngular.repeater("location in Locationssss").column("blort"));
      Assertions.fail("should have barfed");
    }
    catch (NoSuchElementException e) {
      assertThat(e.getMessage(), startsWith("repeater(location in Locationssss).column(blort) didn't have any matching elements at this place in the DOM"));
    }
  }

  @Test
  public void findElements_should_barf_with_message_for_bad_repeater_and_column() {

    webDriver.get("http://www.angularjshub.com/code/examples/forms/04_Select/index.demo.php");
    ngWebDriver.waitForAngularRequestsToFinish();

    try {
      webDriver.findElements(ByAngular.repeater("location in Locationssss").column("blort"));
      Assertions.fail("should have barfed");
    }
    catch (NoSuchElementException e) {
      assertThat(e.getMessage(), startsWith("repeater(location in Locationssss).column(blort) didn't have any matching elements at this place in the DOM"));
    }
  }

  /*
    Ported from protractor/stress/spec.js
   */
  @Test
  public void stress_test() {
    FluentWebDriver fwd = new FluentWebDriver(webDriver);
    for (int i = 0; i < 20; ++i) {
      resetBrowser();

      webDriver.get("http://localhost:8080/index.html#/form");
      FluentWebElement usernameInput = fwd.input(ByAngular.model("username"));
      FluentWebElement name = fwd.span(ByAngular.binding("username"));

      ngWebDriver.waitForAngularRequestsToFinish();

      name.getText().shouldBe("Anon");
      usernameInput.clearField().sendKeys("B");
      name.getText().shouldBe("B");
    }
  }

  /*
    Ported from protractor/spec/altRoot/findelements_spec.js
   */
  @Test
  public void altRoot_find_elements() {
    FluentWebDriver fwd = new FluentWebDriver(webDriver);
    webDriver.get("http://localhost:8080/alt_root_index.html#/form");
    ngWebDriver.waitForAngularRequestsToFinish();


    fwd.span(ByAngular.binding("{{greeting}}")).getText().shouldBe("Hiya");

    fwd.div(id("outside-ng")).getText().shouldBe("{{1 + 2}}");
    fwd.div(id("inside-ng")).getText().shouldBe("3");
  }

  /*
    Ported from protractor/spec/basic/action_spec.js
   */
  @Test
  @Ignore(value = "ignoring basic_actions")
  public void basic_actions() {
    FluentWebDriver fwd = new FluentWebDriver(webDriver);
    webDriver.get("http://localhost:8080/index.html#/form");
    ngWebDriver.waitForAngularRequestsToFinish();

    FluentWebElement sliderBar = fwd.input(By.name("points"));

    sliderBar.getAttribute("value").shouldBe("1");

    try {
      new Actions(webDriver).dragAndDropBy(sliderBar.getWebElement(), 400, 20).build().perform();
    } catch(MoveTargetOutOfBoundsException ignored) {

    }

    sliderBar.getAttribute("value").shouldBe("10");
  }

  /*
    Ported from protractor/spec/basic/elements_spec.js
    TODO - many more specs in here
   */
  @Test
  public void basic_elements_should_chain_with_index_correctly() {
    FluentWebDriver fwd = new FluentWebDriver(webDriver);
    webDriver.get("http://localhost:8080/index.html");

    fwd.inputs(By.cssSelector("#checkboxes input")).last(new IsIndex2Or3()).click();

    fwd.span(By.cssSelector("#letterlist")).getText().shouldBe("'x'");

  }

  private static class IsIndex2Or3 implements FluentMatcher {
    public boolean matches(FluentWebElement webElement, int ix) {
      return ix == 2 || ix == 3;
    }
  }

  /*
    Ported from protractor/spec/basic/elements_spec.js
    TODO - many more specs in here
   */
  @Test
  public void basic_elements_chained_call_should_wait_to_grab_the_WebElement_until_a_method_is_called() {
    FluentWebDriver fwd = new FluentWebDriver(webDriver);

    webDriver.get("http://localhost:8080/index.html#/conflict");

    FluentWebElement reused = fwd.div(id("baz")).span(ByAngular.binding("item.reusedBinding"));

    reused.getText().shouldBe("Inner: inner");

  }

  /*
    Ported from protractor/spec/basic/elements_spec.js
    TODO - many more specs in here
   */
  @Test
  public void basic_elements_should_allow_using_repeater_locator_within_map() {
    FluentWebDriver fwd = new FluentWebDriver(webDriver);

    webDriver.get("http://localhost:8080/index.html#/repeater");

    Map<String, String> expected = new HashMap<>();
    expected.put("M", "Monday");
    expected.put("T", "Tuesday");
    expected.put("W", "Wednesday");
    expected.put("Th", "Thursday");
    expected.put("F", "Friday");

    Map<String, String> days = fwd.lis(ByAngular.repeater("allinfo in days")).map(new FluentWebElementMap<String, String>() {
      public void map(FluentWebElement elem, int ix) {
        put(elem.element(ByAngular.binding("allinfo.initial")).getText().toString(),
          elem.element(ByAngular.binding("allinfo.name")).getText().toString());
      }
    });

    Assertions.assertThat(days.entrySet()).isEqualTo(expected.entrySet());

  }

  /*
    Ported from protractor/spec/basic/locators_spec.js
    TODO - many more specs in here
   */
  @Test
  public void basic_locators_by_repeater_should_find_by_partial_match() {
    FluentWebDriver fwd = new FluentWebDriver(webDriver);

    webDriver.get("http://localhost:8080/index.html#/repeater");

    fwd.span(ByAngular.repeater("baz in days | filter:'T'").row(0).column("baz.initial")).getText().shouldBe("T");

    fwd.span(ByAngular.repeater("baz in days | fil").row(0).column("baz.initial")).getText().shouldBe("T");

    try {
      fwd.li(ByAngular.exactRepeater("baz in days | fil").row(0).column("baz.initial")).getText().shouldBe("T");
      Assertions.fail("should have barfed");
    }
    catch (FluentExecutionStopped e) {
      Assertions.assertThat(e.getMessage()).startsWith("NoSuchElementException during invocation of: ?.li(exactRepeater(baz in days | fil).row(0).column(baz" +
      ".initial))");
      assertThat(e.getCause().getMessage(),
        startsWith("exactRepeater(baz in days | fil).row(0).column(baz.initial) didn't have any matching elements at this place in the DOM"));
    }

  }

  /*
    Ported from protractor/spec/basic/locators_spec.js
    TODO - many more specs in here
   */
  @Test
  public void basic_locators_by_repeater_should_find_many_rows_by_partial_match() {
    FluentWebDriver fwd = new FluentWebDriver(webDriver);

    webDriver.get("http://localhost:8080/index.html#/repeater");

    FluentWebElements spans = fwd.spans(ByAngular.repeater("baz in days | filter:'T'").column("baz.initial"));
    spans.getText().shouldBe("TTh");
    spans.get(0).getText().shouldBe("T");
    spans.get(1).getText().shouldBe("Th");

    spans = fwd.spans(ByAngular.repeater("baz in days | fil").column("baz.initial"));
    spans.getText().shouldBe("TTh");
    spans.get(0).getText().shouldBe("T");
    spans.get(1).getText().shouldBe("Th");

    try {
      fwd.li(ByAngular.exactRepeater("baz in days | fil").column("baz.initial")).getText().shouldBe("TTh");
      Assertions.fail("should have barfed");
    }
    catch (FluentExecutionStopped e) {
      assertThat(e.getMessage(), startsWith("NoSuchElementException during invocation of: ?.li(exactRepeater(baz in days | fil).column(baz.initial))"));
      assertThat(e.getCause().getMessage(),
        startsWith("exactRepeater(baz in days | fil).column(baz.initial) didn't have any matching elements at this place in the DOM"));
    }
  }

  /*
    Ported from protractor/spec/basic/locators_spec.js
    TODO - many more specs in here
   */
  @Test
  public void basic_locators_by_repeater_should_find_one_row_by_partial_match() {
    FluentWebDriver fwd = new FluentWebDriver(webDriver);

    webDriver.get("http://localhost:8080/index.html#/repeater");

    fwd.li(ByAngular.repeater("baz in days | filter:'T'").row(0)).getText().shouldBe("T");
    fwd.li(ByAngular.repeater("baz in days | filter:'T'").row(1)).getText().shouldBe("Th");

    fwd.li(ByAngular.repeater("baz in days | filt").row(1)).getText().shouldBe("Th");


    try {
      fwd.li(ByAngular.exactRepeater("baz in days | filt").row(1)).getText().shouldBe("T");
      Assertions.fail("should have barfed");
    }
    catch (FluentExecutionStopped e) {
      assertThat(e.getMessage(), startsWith("NoSuchElementException during invocation of: ?.li(exactRepeater(baz in days | filt).row(1))"));
      assertThat(e.getCause().getMessage(), startsWith("exactRepeater(baz in days | filt).row(1) didn't have any matching elements at this place in the DOM"));
    }

  }

  /*
    Ported from protractor/spec/basic/locators_spec.js
    TODO - many more specs in here
   */
  @Test
  public void basic_locators_by_repeater_should_find_many_rows_by_partial_match2() {
    FluentWebDriver fwd = new FluentWebDriver(webDriver);

    webDriver.get("http://localhost:8080/index.html#/repeater");

    FluentWebElements lis = fwd.lis(ByAngular.repeater("baz in days | filter:'T'"));
    lis.getText().shouldBe("TTh");
    lis.get(0).getText().shouldBe("T");
    lis.get(1).getText().shouldBe("Th");

    lis = fwd.lis(ByAngular.repeater("baz in days | fil"));
    lis.getText().shouldBe("TTh");
    lis.get(0).getText().shouldBe("T");
    lis.get(1).getText().shouldBe("Th");

    try {
      fwd.lis(ByAngular.exactRepeater("baz in days | filt")).getText().shouldBe("T");
      Assertions.fail("should have barfed");
    }
    catch (FluentExecutionStopped e) {
      assertThat(e.getMessage(), startsWith("NoSuchElementException during invocation of: ?.lis(exactRepeater(baz in days | filt))"));
      assertThat(e.getCause().getMessage(), startsWith("exactRepeater(baz in days | filt) didn't have any matching elements at this place in the DOM"));
    }
  }

  /*
    Ported from protractor/spec/basic/locators_spec.js
    TODO - many more specs in here
   */
  @Test
  public void basic_locators_by_repeater_should_find_single_rows_by_partial_match() {
    FluentWebDriver fwd = new FluentWebDriver(webDriver);

    webDriver.get("http://localhost:8080/index.html#/repeater");

    fwd.li(ByAngular.repeater("baz in days | filter:'T'")).getText().shouldBe("T");

    fwd.li(ByAngular.withRootSelector("[ng-app]").repeater("baz in days | filt")).getText().shouldBe("T");

    try {
      fwd.li(ByAngular.exactRepeater("baz in days | filt")).getText().shouldBe("T");
      Assertions.fail("should have barfed");
    }
    catch (FluentExecutionStopped e) {
      assertThat(e.getMessage(), startsWith("NoSuchElementException during invocation of: ?.li(exactRepeater(baz in days | filt))"));
      assertThat(e.getCause().getMessage(), startsWith("exactRepeater(baz in days | filt) didn't have any matching elements at this place in the DOM"));
    }
  }

  /*
    Ported from protractor/spec/basic/lib_spec.js
    TODO - many more specs in here
   */
  @Test
  public void basic_lib_getLocationAbsUrl_gets_url() {
    FluentWebDriver fwd = new FluentWebDriver(webDriver);

    webDriver.get("http://localhost:8080/index.html");

    Assertions.assertThat(ngWebDriver.getLocationAbsUrl()).endsWith("/form");

    fwd.link(By.linkText("repeater")).click();

    Assertions.assertThat(ngWebDriver.getLocationAbsUrl()).endsWith("/repeater");
  }
}
