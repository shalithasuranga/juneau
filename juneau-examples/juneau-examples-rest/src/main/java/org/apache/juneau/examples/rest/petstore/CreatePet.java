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
package org.apache.juneau.examples.rest.petstore;

import java.util.*;

import org.apache.juneau.annotation.*;
import org.apache.juneau.utils.*;

/**
 * Bean for creating {@link Pet} objects.
 */
public class CreatePet {

	private final String name;
	private final float price;
	private final String species;
	private final List<String> tags;
	private final PetStatus status;
	
	@BeanConstructor(properties="name,price,species,tags,status")
	public CreatePet(String name, float price, String species, List<String> tags, PetStatus status) {
		this.name = name;
		this.price = price;
		this.species = species;
		this.tags = tags;
		this.status = status;
	}
	
	public static CreatePet example() {
		return new CreatePet("Doggie", 9.99f, "doc", AList.create("friendly","cute"), PetStatus.AVAILABLE);
	}

	public String getName() {
		return name;
	}

	public float getPrice() {
		return price;
	}

	public String getSpecies() {
		return species;
	}

	public List<String> getTags() {
		return tags;
	}

	public PetStatus getStatus() {
		return status;
	}
}