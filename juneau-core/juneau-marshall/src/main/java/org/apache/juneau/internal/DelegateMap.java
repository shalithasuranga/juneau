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
package org.apache.juneau.internal;

import java.util.*;

import org.apache.juneau.*;

/**
 * Represents a wrapped {@link Map} where entries in the map can be removed without affecting the underlying map.
 *
 * @param <T> The class type of the wrapped bean.
 */
public class DelegateMap<T> extends ObjectMap implements Delegate<T> {
	private static final long serialVersionUID = 1L;

	private transient ClassMeta<T> classMeta;

	/**
	 * Constructor.
	 *
	 * @param classMeta The metadata object that created this delegate object.
	 */
	public DelegateMap(ClassMeta<T> classMeta) {
		this.classMeta = classMeta;
	}

	@Override /* Delegate */
	public ClassMeta<T> getClassMeta() {
		return classMeta;
	}

	/**
	 * Remove all but the specified keys from this map.
	 *
	 * <p>
	 * This does not affect the underlying map.
	 *
	 * @param keys The remaining keys in the map (in the specified order).
	 */
	public void filterKeys(List<String> keys) {
		ObjectMap m2 = new ObjectMap();
		for (String k : keys)
			m2.put(k, get(k));
		this.clear();
		this.putAll(m2);
	}
}
