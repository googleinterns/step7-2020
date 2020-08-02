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

package com.google.sps.model;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.sps.exceptions.GmailMessageFormatException;
import com.google.sps.utility.GmailUtility;
import java.util.Arrays;
import java.util.List;

/** Contains methods that help create the non-trivial fields in an ActionableMessage. */
public final class ActionableMessageHelperImpl implements ActionableMessageHelper {
  // Public for testing purposes
  public static final String UNDISCLOSED_RECIPIENTS_TO_HEADER = "undisclosed-recipients:;";
  public static final int MAILING_LIST_LOWER_BOUND = 15;

  @Override
  public ActionableMessage.MessagePriority assignMessagePriority(
      Message message, String userEmail) {
    if (isStarred(message)) {
      return ActionableMessage.MessagePriority.HIGH;
    }

    if (!isFromMailingList(message, userEmail) || isImportant(message)) {
      return ActionableMessage.MessagePriority.MEDIUM;
    }

    return ActionableMessage.MessagePriority.LOW;
  }

  /**
   * If "STARRED" is included in a message's labelIds, it has been starred by the user
   *
   * @param message Message object to be checked
   * @return true if starred, false otherwise
   */
  private boolean isStarred(Message message) {
    List<String> labelIds = message.getLabelIds();
    return labelIds != null && labelIds.contains("STARRED");
  }

  /**
   * If the user's email is not in the "To" header, or if there are more than 15 recipients, the
   * message is considered to be sent from a mailing list. This is somewhat of a naive approach,
   * however it should still be relatively accurate (particularly based on the first check listed)
   *
   * @param message Message object to be checked
   * @param userEmail the email address of the current user. Used to check if the email was actually
   *     sent to them or sent as a part of a mailing list
   * @return true if from mailing list, false otherwise
   * @throws GmailMessageFormatException if the message does not contain a "To" header (which is a
   *     mandatory header, indicating the Message object is not of the correct type [METADATA with
   *     correct header settings, or FULL])
   */
  private boolean isFromMailingList(Message message, String userEmail) {
    MessagePartHeader toHeader = GmailUtility.extractHeader(message, "To");
    if (toHeader == null) {
      throw new GmailMessageFormatException("'To' header not present. Check message format");
    }
    String headerValue = toHeader.getValue();
    if (headerValue.equals(UNDISCLOSED_RECIPIENTS_TO_HEADER)) {
      return true;
    }

    List<String> recipientHeaders = Arrays.asList(headerValue.split(","));
    if (recipientHeaders.size() >= MAILING_LIST_LOWER_BOUND) {
      return true;
    }

    boolean userEmailInHeader =
        recipientHeaders.stream().filter((header) -> userEmailInHeader(header, userEmail)).count()
            == 1;
    return !userEmailInHeader;
  }

  /**
   * Gets a sender's contact name or email address.
   *
   * <p>"To" header values have two possible formats: "<sampleemail@sample.com>" (if name is not
   * available) OR "Sample Name <sampleemail@sample.com>" In either case, the email is checked
   * against the userEmail passed
   *
   * @param headerValue the value of a "From" header from which a contact name / email should be
   *     extracted
   * @param userEmail the email address of the current user.
   * @return true if the userEmail is in the header, false otherwise
   */
  private boolean userEmailInHeader(String headerValue, String userEmail) {
    String emailInHeader = headerValue.split("<")[1].replace(">", "");

    return emailInHeader.equals(userEmail);
  }

  /**
   * If "IMPORTANT" is included in a message's labelIds, it has been marked important by Gmail
   *
   * @param message Message object to be checked
   * @return true if starred, false otherwise
   */
  private boolean isImportant(Message message) {
    List<String> labelIds = message.getLabelIds();
    return labelIds != null && labelIds.contains("IMPORTANT");
  }
}
