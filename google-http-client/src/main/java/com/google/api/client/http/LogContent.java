/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.http;

import com.google.api.client.util.Strings;
import com.google.common.base.Preconditions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;

/**
 * Wraps another source of HTTP content without modifying the content, but also possibly logging
 * this content.
 *
 * <p>
 * Content is only logged if {@link Level#CONFIG} is loggable.
 * </p>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @author Yaniv Inbar
 */
final class LogContent implements HttpContent {

  private final HttpContent httpContent;
  private final String contentType;
  private final String contentEncoding;
  private final long contentLength;
  private final int contentLoggingLimit;

  /**
   * @param httpContent another source of HTTP content
   */
  LogContent(HttpContent httpContent, String contentType, String contentEncoding,
      long contentLength, int contentLoggingLimit) {
    this.httpContent = httpContent;
    this.contentType = contentType;
    this.contentLength = contentLength;
    this.contentEncoding = contentEncoding;
    Preconditions.checkArgument(contentLoggingLimit >= 0,
        "The content logging limit must be non-negative.");
    this.contentLoggingLimit = contentLoggingLimit;
  }

  public void writeTo(OutputStream out) throws IOException {
    ByteArrayOutputStream debugStream = new ByteArrayOutputStream();
    httpContent.writeTo(debugStream);
    byte[] debugContent = debugStream.toByteArray();
    if (debugContent.length <= contentLoggingLimit) {
      HttpTransport.LOGGER.config(Strings.fromBytesUtf8(debugContent));
    }
    out.write(debugContent);
  }

  public String getEncoding() {
    return contentEncoding;
  }

  public long getLength() {
    return contentLength;
  }

  public String getType() {
    return contentType;
  }

  int getContentLoggingLimit() {
    return contentLoggingLimit;
  }

  /**
   * Returns whether the given content type is text rather than binary data.
   *
   * @param contentType content type or {@code null}
   * @return whether it is not {@code null} and text-based
   * @since 1.1
   */
  public static boolean isTextBasedContentType(String contentType) {
    // TODO(yanivi): refine this further
    return contentType != null
        && (contentType.startsWith("text/") || contentType.startsWith("application/"));
  }

  public boolean retrySupported() {
    return httpContent.retrySupported();
  }
}
