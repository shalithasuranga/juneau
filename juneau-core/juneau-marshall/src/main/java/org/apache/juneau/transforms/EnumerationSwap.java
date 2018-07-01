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
package org.apache.juneau.transforms;

import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.transform.*;

/**
 * Transforms {@link Enumeration Enumerations} to {@code List<Object>} objects.
 *
 * <p>
 * This is a one-way transform, since {@code Enumerations} cannot be reconstituted.
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class EnumerationSwap extends PojoSwap<Enumeration,List> {

	/**
	 * Converts the specified {@link Enumeration} to a {@link List}.
	 */
	@Override /* PojoSwap */
	public List swap(BeanSession session, Enumeration o) {
		List l = new LinkedList();
		while (o.hasMoreElements())
			l.add(o.nextElement());
		return l;
	}
}
