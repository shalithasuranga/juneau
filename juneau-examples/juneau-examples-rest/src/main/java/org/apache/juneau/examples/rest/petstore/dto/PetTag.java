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
package org.apache.juneau.examples.rest.petstore.dto;

import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.http.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.transform.*;

@Bean(typeName="Tag", fluentSetters=true)
@Swap(PetTag.TagNameOnly.class)
public class PetTag {
	private long id;
	private String name;

	public long getId() {
		return id;
	}

	public PetTag id(long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public PetTag name(String name) {
		this.name = name;
		return this;
	}

	@Example
	public static PetTag example() {
		return new PetTag()
			.id(123)
			.name("MyTag");
	}

	public static class TagNameOnly extends PojoSwap<PetTag,String> {
		@Override
		public String swap(BeanSession bs, PetTag o) throws Exception {
			return o.getName();
		}
		@Override
		public MediaType[] forMediaTypes() {
			return new MediaType[] { MediaType.HTML };
		}
	}

	public static String asString(List<PetTag> tags) {
		if (tags == null)
			return "";
		List<String> l = new ArrayList<>(tags.size());
		for (PetTag t : tags)
			l.add(t.getName());
		return StringUtils.join(l, ',');
	}
}
