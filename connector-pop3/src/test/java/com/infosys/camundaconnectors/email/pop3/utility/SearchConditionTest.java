/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.email.pop3.utility;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import javax.mail.search.SearchTerm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchConditionTest {
  private final SearchCondition searchCondition = new SearchCondition();

  @DisplayName("Should return SearchTerm object for simple input")
  @Test
  void shouldReturnSearchTerm() {
    // When
    SearchTerm searchTerm =
            searchCondition.convertToSearchTerm(Map.of("filter", "subject = 'Test Email'"));
    // Then
    assertNotNull(searchTerm);
  }

  @DisplayName("Should return SearchTerm object for complex input i.e more than one condition")
  @Test
  void shouldReturnAndTerm() {
    // When
    SearchTerm searchTerm =
            searchCondition.convertToSearchTerm(
                    Map.of(
                            "logicalOperator",
                            "And",
                            "filterList",
                            List.of(
                                    Map.of("filter", "subject = 'Test Email'"),
                                    Map.of("filter", "Flag seen false"),
                                    Map.of("filter", "SentDate <= '14/03/2023 12:01:00 AM'"),
                                    Map.of("filter", "Size > 2465"),
                                    Map.of("filter", "from = 'dev@abc.com'"),
                                    Map.of("filter", "body contains 'this string'"),
                                    Map.of("filter", "recipient cc 'test@abc.com'"))));
    // Then
    assertNotNull(searchTerm);
  }

  @DisplayName("Should return null for null input")
  @Test
  void shouldReturnNullForNullInput() {
    // When
    SearchTerm searchTerm = searchCondition.convertToSearchTerm(null);
    // Then
    assertNull(searchTerm);
  }

  @DisplayName("Should return null for empty map")
  @Test
  void shouldReturnNullForEmptyMap() {
    // When
    SearchTerm searchTerm = searchCondition.convertToSearchTerm(Map.of());
    // Then
    assertNull(searchTerm);
  }
}
