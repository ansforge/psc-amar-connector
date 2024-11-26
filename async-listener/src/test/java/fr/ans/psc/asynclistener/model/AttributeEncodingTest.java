/*
 * Copyright © 2022-2024 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ans.psc.asynclistener.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author edegenetais
 */
public class AttributeEncodingTest {
  @Test
  public void shouldGiveNullForNull() {
    String actual=AttributeEncoding.encodeStringAttribute(null);
    Assertions.assertNull(actual);
  }

  @Test
  public void shouldGiveNullForEmpty() {
    String actual=AttributeEncoding.encodeStringAttribute("");
    Assertions.assertNull(actual);
  }
  
  @Test
  public void shouldKeepNonEmptValues(){
    final String initialValue = "My attribute is rich";
    String actual=AttributeEncoding.encodeStringAttribute(initialValue);
    Assertions.assertEquals(initialValue, actual);
  }
}
