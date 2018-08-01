// ***************************************************************************************************************************
// * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file *
// * distributed with this work for additional information regarding copyright ownership.  The ASF licenses this file        *
// * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance            *
// * with the License.  You may obtain a copy of the License at                                                              *
// *                                                                                                                         *
// *  http://www.apache.org/licenses/LICENSE-2.0                                                                             *
// *                                                                                                                         *
// * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an  *
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the        *
// * specific language governing permissions and limitations under the License.                                              *
// ***************************************************************************************************************************
package org.apache.juneau.rest.response;

import org.apache.juneau.http.annotation.*;

/**
 * Represents an <code>HTTP 206 Partial Content</code> response.
 *
 * <p>
 * The server is delivering only part of the resource (byte serving) due to a range header sent by the client.
 * The range header is used by HTTP clients to enable resuming of interrupted downloads, or split a download into multiple simultaneous streams.
 */
@Response(code=206, example="'Partial Content'")
public class PartialContent extends HttpResponse {

	/** Reusable instance. */
	public static final PartialContent INSTANCE = new PartialContent();

	/**
	 * Constructor using HTTP-standard message.
	 */
	public PartialContent() {
		this("Partial Content");
	}

	/**
	 * Constructor using custom message.
	 * @param message Message to send as the response.
	 */
	public PartialContent(String message) {
		super(message);
	}
}