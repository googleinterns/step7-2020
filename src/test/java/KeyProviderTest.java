// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import com.google.sps.utility.KeyProvider;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test Key Provider functions */
@RunWith(JUnit4.class)
public class KeyProviderTest {

  private static final String SAMPLE_KEY = "sampleKey";
  private static final String CAPITALISED_SAMPLE_KEY = "SAMPLEKEY";
  private static final String INVALID_KEY = "invalidKey";

  private static final String SAMPLE_VALUE = "sampleValue";

  @Test
  public void getSampleKeyValue() throws IOException {
    // Gets the value of sampleKey which is in src/main/resources/KEYS.json.
    // invalidKey is expected.
    String expected = SAMPLE_VALUE;
    String actual = KeyProvider.getKey(SAMPLE_KEY);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void getCapitalisedSampleKeyValue() throws IOException {
    // Gets the value of SAMPLEKEY which is not in src/main/resources/KEYS.json since keys are cases
    // sensitive.
    // null is expected.
    String actual = KeyProvider.getKey(CAPITALISED_SAMPLE_KEY);
    Assert.assertNull(actual);
  }

  @Test
  public void getInvalidKeyValue() throws IOException {
    // Gets the value of an invalid key which is not in src/main/resources/KEYS.json.
    // null is expected.
    String actual = KeyProvider.getKey(INVALID_KEY);
    Assert.assertNull(actual);
  }
}
