Feature: Web Search

  Scenario: Simple Google Search
    Given visit "$search.page.url" page
    When enter "panda" into "$search.page.txt-search.xpath"
    And click on "$search.page.btn-search.xpath"
    Then search results for "panda" are shown
    And take screenshot
