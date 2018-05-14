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
package org.apache.juneau.rest.annotation;

import static org.apache.juneau.http.HttpMethodName.*;

import java.io.*;
import java.util.*;

import org.apache.juneau.internal.*;
import org.apache.juneau.json.*;
import org.apache.juneau.rest.mock.*;
import org.apache.juneau.rest.testutils.*;
import org.apache.juneau.rest.testutils.DTOs;
import org.apache.juneau.uon.*;
import org.apache.juneau.urlencoding.*;
import org.junit.*;
import org.junit.runners.*;

/**
 * Tests the {@link Body} annotation.
 */
@SuppressWarnings({"javadoc","serial"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BodyAnnotationTest {
	
	//=================================================================================================================
	// @Body on parameter
	//=================================================================================================================
	
	@RestResource(serializers=JsonSerializer.Simple.class, parsers=JsonParser.class)
	public static class A {
		@RestMethod(name=PUT, path="/String")
		public String a01(@Body String b) {
			return b;
		}
		@RestMethod(name=PUT, path="/Integer")
		public Integer a02(@Body Integer b) {
			return b;
		}
		@RestMethod(name=PUT, path="/int")
		public Integer a03(@Body int b) {
			return b;
		}
		@RestMethod(name=PUT, path="/Boolean")
		public Boolean a04(@Body Boolean b) {
			return b;
		}
		@RestMethod(name=PUT, path="/boolean")
		public Boolean a05(@Body boolean b) {
			return b;
		}
		@RestMethod(name=PUT, path="/float")
		public float a06(@Body float f) {
			return f;
		}
		@RestMethod(name=PUT, path="/Float")
		public Float a07(@Body Float f) {
			return f;
		}
		@RestMethod(name=PUT, path="/Map")
		public TreeMap<String,Integer> a08(@Body TreeMap<String,Integer> m) {
			return m;
		}
		@RestMethod(name=PUT, path="/enum")
		public TestEnum a09(@Body TestEnum e) {
			return e;
		}
		public static class A11 {
			public String f1;
		}
		@RestMethod(name=PUT, path="/Bean")
		public A11 a11(@Body A11 b) {
			return b;
		}
		@RestMethod(name=PUT, path="/InputStream")
		public String a12(@Body InputStream b) throws Exception {
			return IOUtils.read(b);
		}
		@RestMethod(name=PUT, path="/Reader")
		public String a13(@Body Reader b) throws Exception {
			return IOUtils.read(b);
		}
		@RestMethod(name=PUT, path="/InputStreamTransform")
		public A14 a14(@Body A14 b) throws Exception {
			return b;
		}
		public static class A14 {
			String s;
			public A14(InputStream in) throws Exception { this.s = IOUtils.read(in); }
			@Override public String toString() { return s; }
		}
		@RestMethod(name=PUT, path="/ReaderTransform")
		public A15 a15(@Body A15 b) throws Exception {
			return b;
		}
		public static class A15 {
			private String s;
			public A15(Reader in) throws Exception { this.s = IOUtils.read(in); }
			@Override public String toString() { return s; }
		}
		@RestMethod(name=PUT, path="/StringTransform")
		public A16 a16(@Body A16 b) throws Exception { return b; }
		public static class A16 {
			private String s;
			public A16(String s) throws Exception { this.s = s; }
			@Override public String toString() { return s; }
		}
	}
	private static MockRest a = MockRest.create(A.class);
	
	@Test
	public void a01a_onParameter_String() throws Exception {
		a.request("PUT", "/String", "'foo'").json().execute().assertBody("'foo'");
	}
	@Test
	public void a01b_onParameter_String_noContentType() throws Exception {
		// If no Content-Type specified, should be treated as plain-text.
		a.request("PUT", "/String", "'foo'").execute().assertBody("'\\'foo\\''");
	}
	@Test
	public void a01c_onParameter_String_noContentType_other() throws Exception {
		// If Content-Type not matched, should be treated as plain-text.
		a.request("PUT", "/String", "'foo'").contentType("").execute().assertBody("'\\'foo\\''");
		a.request("PUT", "/String", "'foo'").contentType("text/plain").execute().assertBody("'\\'foo\\''");
	}
	@Test
	public void a02a_onParameter_Integer() throws Exception {
		a.request("PUT", "/Integer", "123").json().execute().assertBody("123");
	}
	@Test
	public void a02b_onParameter_Integer_noContentType() throws Exception {
		// Integer takes in a String arg, so it can be parsed without Content-Type.
		a.request("PUT", "/Integer", "123").execute().assertBody("123");
	}
	@Test
	public void a03a_onParameter_int() throws Exception {
		a.request("PUT", "/int", "123").json().execute().assertBody("123");
	}
	@Test
	public void a03b_onParameter_int_noContentType() throws Exception {
		a.request("PUT", "/int?noTrace=true", "123").execute().assertBodyContains("Unsupported Media Type");
	}
	@Test
	public void a04a_onParameter_Boolean() throws Exception {
		a.request("PUT", "/Boolean", "true").json().execute().assertBody("true");
	}
	@Test
	public void a04b_onParameter_Boolean_noContentType() throws Exception {
		// Boolean takes in a String arg, so it can be parsed without Content-Type.
		a.request("PUT", "/Boolean", "true").execute().assertBody("true");
	}
	@Test
	public void a05a_onParameter_boolean() throws Exception {
		a.request("PUT", "/boolean", "true").json().execute().assertBody("true");
	}
	@Test
	public void a05b_onParameter_boolean_noContentType() throws Exception {
		a.request("PUT", "/boolean?noTrace=true", "true").execute().assertBodyContains("Unsupported Media Type");
	}
	@Test
	public void a06a_onParameter_float() throws Exception {
		a.request("PUT", "/float", "1.23").json().execute().assertBody("1.23");
	}
	@Test
	public void a06b_onParameter_float_noContentType() throws Exception {
		a.request("PUT", "/float?noTrace=true", "1.23").execute().assertBodyContains("Unsupported Media Type");
	}
	@Test
	public void a07a_onParameter_Float() throws Exception {
		a.request("PUT", "/Float", "1.23").json().execute().assertBody("1.23");
	}
	@Test
	public void a07b_onParameter_Float_noContentType() throws Exception {
		// Float takes in a String arg, so it can be parsed without Content-Type.
		a.request("PUT", "/Float", "1.23").execute().assertBody("1.23");
	}
	@Test
	public void a08a_onParameter_Map() throws Exception {
		a.request("PUT", "/Map", "{foo:123}").json().execute().assertBody("{foo:123}");
	}
	@Test
	public void a08b_onParameter_Map_noContentType() throws Exception {
		a.request("PUT", "/Map?noTrace=true", "{foo:123}").execute().assertBodyContains("Unsupported Media Type");
	}
	@Test
	public void a09a_onParameter_enum() throws Exception {
		a.request("PUT", "/enum", "'ONE'").json().execute().assertBody("'ONE'");
	}
	@Test
	public void a09b_onParameter_enum_noContentType() throws Exception {
		a.request("PUT", "/enum", "ONE").execute().assertBody("'ONE'");
	}
	@Test
	public void a11a_onParameter_Bean() throws Exception {
		a.request("PUT", "/Bean", "{f1:'a'}").json().execute().assertBody("{f1:'a'}");
	}
	@Test
	public void a11b_onParameter_Bean_noContentType() throws Exception {
		a.request("PUT", "/Bean?noTrace=true", "{f1:'a'}").execute().assertBodyContains("Unsupported Media Type");
	}
	@Test
	public void a12a_onParameter_InputStream() throws Exception {
		// Content-Type should always be ignored.
		a.request("PUT", "/InputStream", "'a'").json().execute().assertBody("'\\'a\\''");
	}
	@Test
	public void a12b_onParameter_InputStream_noContentType() throws Exception {
		a.request("PUT", "/InputStream", "'a'").execute().assertBody("'\\'a\\''");
	}
	@Test
	public void a13a_onParameter_Reader() throws Exception {
		// Content-Type should always be ignored.
		a.request("PUT", "/Reader", "'a'").json().execute().assertBody("'\\'a\\''");
	}
	@Test
	public void a13b_onParameter_Reader_noContentType() throws Exception {
		a.request("PUT", "/Reader", "'a'").execute().assertBody("'\\'a\\''");
	}
	@Test
	public void a14a_onParameter_InputStreamTransform() throws Exception {
		// Input stream transform requests must not specify Content-Type or else gets resolved as POJO.
		a.request("PUT", "/InputStreamTransform?noTrace=true", "'a'").json().execute().assertBodyContains("Bad Request");
	}
	@Test
	public void a14b_onParameter_InputStreamTransform_noContentType() throws Exception {
		a.request("PUT", "/InputStreamTransform", "'a'").execute().assertBody("'\\'a\\''");
	}
	@Test
	public void a15a_onParameter_ReaderTransform() throws Exception {
		// Reader transform requests must not specify Content-Type or else gets resolved as POJO.
		a.request("PUT", "/ReaderTransform?noTrace=true", "'a'").json().execute().assertBodyContains("Bad Request");
	}
	@Test
	public void a15b_onParameter_ReaderTransform_noContentType() throws Exception {
		a.request("PUT", "/ReaderTransform", "'a'").execute().assertBody("'\\'a\\''");
	}
	@Test
	public void a16a_onParameter_StringTransform() throws Exception {
		// When Content-Type specified and matched, treated as a parsed POJO.
		a.request("PUT", "/StringTransform", "'a'").json().execute().assertBody("'a'");
	}
	@Test
	public void a16b_onParameter_StringTransform_noContentType() throws Exception {
		// When Content-Type not matched, treated as plain text.
		a.request("PUT", "/StringTransform", "'a'").execute().assertBody("'\\'a\\''");
	}
	
	//=================================================================================================================
	// @Body on POJO
	//=================================================================================================================
	
	@RestResource(serializers=JsonSerializer.Simple.class, parsers=JsonParser.class)
	public static class B {
		@RestMethod(name=PUT, path="/StringTransform")
		public B01 b01(B01 b) {
			return b;
		}
		@Body 
		public static class B01 {
			private String val;
			public B01(String val) { this.val = val; }
			@Override public String toString() { return val; }
		}
		@RestMethod(name=PUT, path="/Bean")
		public B02 b02(B02 b) {
			return b;
		}
		@Body
		public static class B02 {
			public String f1;
		}
		@RestMethod(name=PUT, path="/BeanList")
		public B03 b03(B03 b) {
			return b;
		}
		@Body
		public static class B03 extends LinkedList<B02> {}
		@RestMethod(name=PUT, path="/InputStreamTransform")
		public B04 b04(B04 b) throws Exception {
			return b;
		}
		@Body 
		public static class B04 {
			String s;
			public B04(InputStream in) throws Exception { this.s = IOUtils.read(in); }
			@Override public String toString() { return s; }
		}
		@RestMethod(name=PUT, path="/ReaderTransform")
		public B05 b05(B05 b) throws Exception {
			return b;
		}
		@Body 
		public static class B05 {
			private String s;
			public B05(Reader in) throws Exception { this.s = IOUtils.read(in); }
			@Override public String toString() { return s; }
		}
	}
	private static MockRest b = MockRest.create(B.class);

	@Test
	public void b01a_onPojo_StringTransform() throws Exception {
		b.request("PUT", "/StringTransform", "'foo'").json().execute().assertBody("'foo'");
	}
	@Test
	public void b01b_onPojo_StringTransform_noContentType() throws Exception {
		// When Content-Type not matched, treated as plain text.
		b.request("PUT", "/StringTransform", "'foo'").execute().assertBody("'\\'foo\\''");
	}
	@Test
	public void b02a_onPojo_Bean() throws Exception {
		b.request("PUT", "/Bean", "{f1:'a'}").json().execute().assertBody("{f1:'a'}");
	}
	@Test
	public void b02b_onPojo_Bean_noContentType() throws Exception {
		b.request("PUT", "/Bean?noTrace=true", "{f1:'a'}").execute().assertBodyContains("Unsupported Media Type");
	}
	@Test
	public void b03a_onPojo_BeanList() throws Exception {
		b.request("PUT", "/BeanList", "[{f1:'a'}]").json().execute().assertBody("[{f1:'a'}]");
	}
	@Test
	public void b03b_onPojo_BeanList_noContentType() throws Exception {
		b.request("PUT", "/BeanList?noTrace=true", "[{f1:'a'}]").execute().assertBodyContains("Unsupported Media Type");
	}
	@Test
	public void b04a_onPojo_InputStreamTransform() throws Exception {
		b.request("PUT", "/InputStreamTransform", "a").execute().assertBody("'a'");
	}
	@Test
	public void b04b_onPojo_InputStreamTransform_withContentType() throws Exception {
		// When Content-Type matched, treated as parsed POJO.
		b.request("PUT", "/InputStreamTransform?noTrace=true", "a").json().execute().assertBodyContains("Bad Request");
	}
	@Test
	public void b05a_onPojo_ReaderTransform() throws Exception {
		b.request("PUT", "/ReaderTransform", "a").execute().assertBody("'a'");
	}
	@Test
	public void b05b_onPojo_ReaderTransform_withContentType() throws Exception {
		// When Content-Type matched, treated as parsed POJO.
		b.request("PUT", "/ReaderTransform?noTrace=true", "a").json().execute().assertBodyContains("Bad Request");
	}

	//=================================================================================================================
	// Basic tests using @Body parameter
	//=================================================================================================================

	public void c01_bodyParam_String() throws Exception {
		a.request("PUT", "/String?body=foo").execute().assertBody("'foo'");
		a.request("PUT", "/String?body=null").execute().assertBody("null");
		a.request("PUT", "/String?body=").execute().assertBody("''");
	}
	@Test
	public void c02_bodyParam_Integer() throws Exception {
		a.request("PUT", "/Integer?body=123").execute().assertBody("123");
		a.request("PUT", "/Integer?body=-123").execute().assertBody("-123");
		a.request("PUT", "/Integer?body=null").execute().assertBody("null");
		a.request("PUT", "/Integer?body=").execute().assertBody("null");
		a.request("PUT", "/Integer?body=bad&noTrace=true").execute().assertStatus(400);
	}
	@Test
	public void c03_bodyParam_int() throws Exception {
		a.request("PUT", "/int?body=123").execute().assertBody("123");
		a.request("PUT", "/int?body=-123").execute().assertBody("-123");
		a.request("PUT", "/int?body=null").execute().assertBody("0");
		a.request("PUT", "/int?body=").execute().assertBody("0");
		a.request("PUT", "/int?body=bad&noTrace=true").execute().assertStatus(400);
	}
	@Test
	public void c04_bodyParam_Boolean() throws Exception {
		a.request("PUT", "/Boolean?body=true").execute().assertBody("true");
		a.request("PUT", "/Boolean?body=false").execute().assertBody("false");
		a.request("PUT", "/Boolean?body=null").execute().assertBody("null");
		a.request("PUT", "/Boolean?body=").execute().assertBody("null");
		a.request("PUT", "/Boolean?body=bad&noTrace=true").execute().assertStatus(400);
	}
	@Test
	public void c05_bodyParam_boolean() throws Exception {
		a.request("PUT", "/boolean?body=true").execute().assertBody("true");
		a.request("PUT", "/boolean?body=false").execute().assertBody("false");
		a.request("PUT", "/boolean?body=null").execute().assertBody("false");
		a.request("PUT", "/boolean?body=").execute().assertBody("false");
		a.request("PUT", "/boolean?body=bad&noTrace=true").execute().assertStatus(400);
	}
	@Test
	public void c06_bodyParam_Float() throws Exception {
		a.request("PUT", "/Float?body=1.23").execute().assertBody("1.23");
		a.request("PUT", "/Float?body=-1.23").execute().assertBody("-1.23");
		a.request("PUT", "/Float?body=null").execute().assertBody("null");
		a.request("PUT", "/Float?body=").execute().assertBody("null");
		a.request("PUT", "/Float?body=bad&noTrace=true").execute().assertStatus(400);
	}
	@Test
	public void c07_bodyParam_float() throws Exception {
		a.request("PUT", "/float?body=1.23").execute().assertBody("1.23");
		a.request("PUT", "/float?body=-1.23").execute().assertBody("-1.23");
		a.request("PUT", "/float?body=null").execute().assertBody("0.0");
		a.request("PUT", "/float?body=").execute().assertBody("0.0");
		a.request("PUT", "/float?body=bad&noTrace=true").execute().assertStatus(400);
	}
	@Test
	public void c08_bodyParam_Map() throws Exception {
		a.request("PUT", "/Map?body=(foo=123)").execute().assertBody("{foo:123}");
		a.request("PUT", "/Map?body=()").execute().assertBody("{}");
		a.request("PUT", "/Map?body=null").execute().assertBody("null");
		a.request("PUT", "/Map?body=").execute().assertBody("null");
		a.request("PUT", "/Map?body=bad&noTrace=true").execute().assertStatus(400);
	}
	@Test
	public void c09_bodyParam_enum() throws Exception {
		a.request("PUT", "/enum?body=ONE").execute().assertBody("'ONE'");
		a.request("PUT", "/enum?body=TWO").execute().assertBody("'TWO'");
		a.request("PUT", "/enum?body=null").execute().assertBody("null");
		a.request("PUT", "/enum?body=").execute().assertBody("null");
		a.request("PUT", "/enum?body=bad&noTrace=true").execute().assertStatus(400);
	}
	@Test
	public void c11_bodyParam_Bean() throws Exception {
		a.request("PUT", "/Bean?body=(f1=a)").execute().assertBody("{f1:'a'}");
		a.request("PUT", "/Bean?body=()").execute().assertBody("{}");
		a.request("PUT", "/Bean?body=null").execute().assertBody("null");
		a.request("PUT", "/Bean?body=").execute().assertBody("null");
		a.request("PUT", "/Bean?body=bad&noTrace=true").execute().assertStatus(400);
	}
	@Test
	public void c12_bodyParam_InputStream() throws Exception {
		a.request("PUT", "/InputStream?body=a").execute().assertBody("'a'");
		a.request("PUT", "/InputStream?body=null").execute().assertBody("'null'");
		a.request("PUT", "/InputStream?body=").execute().assertBody("''");
	}
	@Test
	public void c13_bodyParam_Reader() throws Exception {
		a.request("PUT", "/Reader?body=a").execute().assertBody("'a'");
		a.request("PUT", "/Reader?body=null").execute().assertBody("'null'");
		a.request("PUT", "/Reader?body=").execute().assertBody("''");
	}
	
	// It's not currently possible to pass in a &body parameter for InputStream/Reader transforms.
	
	//=================================================================================================================
	// No serializers or parsers needed when using only streams and readers.
	//=================================================================================================================
	
	@RestResource
	public static class D {
		@RestMethod(name=PUT, path="/String")
		public Reader d01(@Body Reader b) throws Exception {
			return b;
		}
		@RestMethod(name=PUT, path="/InputStream")
		public InputStream d02(@Body InputStream b) throws Exception {
			return b;
		}
		@RestMethod(name=PUT, path="/Reader")
		public Reader d03(@Body Reader b) throws Exception {
			return b;
		}
		@RestMethod(name=PUT, path="/StringTransform")
		public Reader d04(@Body D04 b) throws Exception {
			return new StringReader(b.toString());
		}
		public static class D04 {
			private String s;
			public D04(String in) throws Exception { this.s = in; }
			@Override public String toString() { return s; }
		}
		@RestMethod(name=PUT, path="/InputStreamTransform")
		public Reader d05(@Body D05 b) throws Exception {
			return new StringReader(b.toString());
		}
		public static class D05 {
			String s;
			public D05(InputStream in) throws Exception { this.s = IOUtils.read(in); }
			@Override public String toString() { return s; }
		}
		@RestMethod(name=PUT, path="/ReaderTransform")
		public Reader d06(@Body D06 b) throws Exception {
			return new StringReader(b.toString());
		}
		public static class D06 {
			private String s;
			public D06(Reader in) throws Exception{ this.s = IOUtils.read(in); }
			@Override public String toString() { return s; }
		}
		@RestMethod(name=PUT, path="/StringTransformBodyOnPojo")
		public Reader d07(D07 b) throws Exception {
			return new StringReader(b.toString());
		}
		@Body
		public static class D07 {
			private String s;
			public D07(String in) throws Exception { this.s = in; }
			@Override public String toString() { return s; }
		}
		@RestMethod(name=PUT, path="/InputStreamTransformBodyOnPojo")
		public Reader d08(D08 b) throws Exception {
			return new StringReader(b.toString());
		}
		@Body
		public static class D08 {
			String s;
			public D08(InputStream in) throws Exception { this.s = IOUtils.read(in); }
			@Override public String toString() { return s; }
		}
		
		@RestMethod(name=PUT, path="/ReaderTransformBodyOnPojo")
		public Reader d09(D09 b) throws Exception {
			return new StringReader(b.toString());
		}
		@Body
		public static class D09 {
			private String s;
			public D09(Reader in) throws Exception{ this.s = IOUtils.read(in); }
			@Override public String toString() { return s; }
		}
	}
	private static MockRest d = MockRest.create(D.class);
	
	@Test
	public void d01a_noMediaTypes_String() throws Exception {
		d.request("PUT", "/String", "a").execute().assertBody("a");
	}
	@Test
	public void d01b_noMediaTypes_String_withContentType() throws Exception {
		d.request("PUT", "/String", "a").json().execute().assertBody("a");
	}
	@Test
	public void d02a_noMediaTypes_InputStream() throws Exception {
		d.request("PUT", "/InputStream", "a").execute().assertBody("a");
	}
	@Test
	public void d02b_noMediaTypes_InputStream_withContentType() throws Exception {
		d.request("PUT", "/InputStream", "a").json().execute().assertBody("a");
	}
	@Test
	public void d03a_noMediaTypes_Reader() throws Exception {
		d.request("PUT", "/Reader", "a").execute().assertBody("a");
	}
	@Test
	public void d03b_noMediaTypes_Reader_withContentType() throws Exception {
		d.request("PUT", "/Reader", "a").json().execute().assertBody("a");
	}
	@Test
	public void d04a_noMediaTypes_StringTransform() throws Exception {
		d.request("PUT", "/StringTransform", "a").execute().assertBody("a");
	}
	@Test
	public void d04b_noMediaTypes_StringTransform_withContentType() throws Exception {
		d.request("PUT", "/StringTransform?noTrace=true", "a").json().execute().assertStatus(415);
	}
	@Test
	public void d05a_noMediaTypes_InputStreamTransform() throws Exception {
		d.request("PUT", "/InputStreamTransform", "a").execute().assertBody("a");
	}
	@Test
	public void d05b_noMediaTypes_InputStreamTransform_withContentType() throws Exception {
		d.request("PUT", "/InputStreamTransform", "a").json().execute().assertBody("a");
	}
	@Test
	public void d06a_noMediaTypes_ReaderTransform() throws Exception {
		d.request("PUT", "/ReaderTransform", "a").execute().assertBody("a");
	}
	@Test
	public void d06b_noMediaTypes_ReaderTransform_withContentType() throws Exception {
		d.request("PUT", "/ReaderTransform", "a").json().execute().assertBody("a");
	}
	@Test
	public void d07a_noMediaTypes_StringTransformBodyOnPojo() throws Exception {
		d.request("PUT", "/StringTransformBodyOnPojo", "a").execute().assertBody("a");
	}
	@Test
	public void d07b_noMediaTypes_StringTransformBodyOnPojo_withContentType() throws Exception {
		d.request("PUT", "/StringTransformBodyOnPojo?noTrace=true", "a").json().execute().assertStatus(415);
	}
	@Test
	public void d08a_noMediaTypes_InputStreamTransformBodyOnPojo() throws Exception {
		d.request("PUT", "/InputStreamTransformBodyOnPojo", "a").execute().assertBody("a");
	}
	@Test
	public void d08b_noMediaTypes_InputStreamTransformBodyOnPojo_withContentType() throws Exception {
		d.request("PUT", "/InputStreamTransformBodyOnPojo", "a").json().execute().assertBody("a");
	}
	@Test
	public void d09a_noMediaTypes_ReaderTransformBodyOnPojo() throws Exception {
		d.request("PUT", "/ReaderTransformBodyOnPojo", "a").execute().assertBody("a");
	}
	@Test
	public void d09b_noMediaTypes_ReaderTransformBodyOnPojo_withContentType() throws Exception {
		d.request("PUT", "/ReaderTransformBodyOnPojo", "a").json().execute().assertBody("a");
	}
	
	//=================================================================================================================
	// Complex POJOs
	//=================================================================================================================
	
	@RestResource(serializers=JsonSerializer.Simple.class, parsers=JsonParser.class)
	public static class E {
		@RestMethod(name=PUT, path="/B")
		public DTOs.B testPojo1(@Body DTOs.B b) {
			return b;
		}
		@RestMethod(name=PUT, path="/C")
		public DTOs.C testPojo2(@Body DTOs.C c) {
			return c;
		}
	}
	private static MockRest e = MockRest.create(E.class);
	
	@Test
	public void e01_complexPojos_B_body() throws Exception {
		String expected = "{f01:['a','b'],f02:['c','d'],f03:[1,2],f04:[3,4],f05:[['e','f'],['g','h']],f06:[['i','j'],['k','l']],f07:[{a:'a',b:1,c:true},{a:'a',b:1,c:true}],f08:[{a:'a',b:1,c:true},{a:'a',b:1,c:true}],f09:[[{a:'a',b:1,c:true}],[{a:'a',b:1,c:true}]],f10:[[{a:'a',b:1,c:true}],[{a:'a',b:1,c:true}]],f11:['a','b'],f12:['c','d'],f13:[1,2],f14:[3,4],f15:[['e','f'],['g','h']],f16:[['i','j'],['k','l']],f17:[{a:'a',b:1,c:true},{a:'a',b:1,c:true}],f18:[{a:'a',b:1,c:true},{a:'a',b:1,c:true}],f19:[[{a:'a',b:1,c:true}],[{a:'a',b:1,c:true}]],f20:[[{a:'a',b:1,c:true}],[{a:'a',b:1,c:true}]]}";
		e.request("PUT", "/B", JsonSerializer.DEFAULT_LAX.toString(DTOs.B.INSTANCE)).json().execute().assertBody(expected);
	}
	@Test
	public void e02_complexPojos_B_bodyParam() throws Exception {
		String expected = "{f01:['a','b'],f02:['c','d'],f03:[1,2],f04:[3,4],f05:[['e','f'],['g','h']],f06:[['i','j'],['k','l']],f07:[{a:'a',b:1,c:true},{a:'a',b:1,c:true}],f08:[{a:'a',b:1,c:true},{a:'a',b:1,c:true}],f09:[[{a:'a',b:1,c:true}],[{a:'a',b:1,c:true}]],f10:[[{a:'a',b:1,c:true}],[{a:'a',b:1,c:true}]],f11:['a','b'],f12:['c','d'],f13:[1,2],f14:[3,4],f15:[['e','f'],['g','h']],f16:[['i','j'],['k','l']],f17:[{a:'a',b:1,c:true},{a:'a',b:1,c:true}],f18:[{a:'a',b:1,c:true},{a:'a',b:1,c:true}],f19:[[{a:'a',b:1,c:true}],[{a:'a',b:1,c:true}]],f20:[[{a:'a',b:1,c:true}],[{a:'a',b:1,c:true}]]}";
		e.request("PUT", "/B?body=" + UonSerializer.DEFAULT.serialize(DTOs.B.INSTANCE), "a").execute().assertBody(expected);
	}
	@Test
	public void e03_complexPojos_C_body() throws Exception {
		String expected = "{f01:['a','b'],f02:['c','d'],f03:[1,2],f04:[3,4],f05:[['e','f'],['g','h']],f06:[['i','j'],['k','l']],f07:[{a:'a',b:1,c:true},{a:'a',b:1,c:true}],f08:[{a:'a',b:1,c:true},{a:'a',b:1,c:true}],f09:[[{a:'a',b:1,c:true}],[{a:'a',b:1,c:true}]],f10:[[{a:'a',b:1,c:true}],[{a:'a',b:1,c:true}]],f11:['a','b'],f12:['c','d'],f13:[1,2],f14:[3,4],f15:[['e','f'],['g','h']],f16:[['i','j'],['k','l']],f17:[{a:'a',b:1,c:true},{a:'a',b:1,c:true}],f18:[{a:'a',b:1,c:true},{a:'a',b:1,c:true}],f19:[[{a:'a',b:1,c:true}],[{a:'a',b:1,c:true}]],f20:[[{a:'a',b:1,c:true}],[{a:'a',b:1,c:true}]]}";
		e.request("PUT", "/C", JsonSerializer.DEFAULT_LAX.toString(DTOs.B.INSTANCE)).json().execute().assertBody(expected);
	}
	@Test
	public void e04_complexPojos_C_bodyParam() throws Exception {
		String expected = "{f01:['a','b'],f02:['c','d'],f03:[1,2],f04:[3,4],f05:[['e','f'],['g','h']],f06:[['i','j'],['k','l']],f07:[{a:'a',b:1,c:true},{a:'a',b:1,c:true}],f08:[{a:'a',b:1,c:true},{a:'a',b:1,c:true}],f09:[[{a:'a',b:1,c:true}],[{a:'a',b:1,c:true}]],f10:[[{a:'a',b:1,c:true}],[{a:'a',b:1,c:true}]],f11:['a','b'],f12:['c','d'],f13:[1,2],f14:[3,4],f15:[['e','f'],['g','h']],f16:[['i','j'],['k','l']],f17:[{a:'a',b:1,c:true},{a:'a',b:1,c:true}],f18:[{a:'a',b:1,c:true},{a:'a',b:1,c:true}],f19:[[{a:'a',b:1,c:true}],[{a:'a',b:1,c:true}]],f20:[[{a:'a',b:1,c:true}],[{a:'a',b:1,c:true}]]}";
		e.request("PUT", "/C?body=" + UonSerializer.DEFAULT.serialize(DTOs.B.INSTANCE), "a").execute().assertBody(expected);
	}
	
	//=================================================================================================================
	// Form POSTS with @Body parameter
	//=================================================================================================================
	
	@RestResource(serializers=JsonSerializer.class,parsers=JsonParser.class)
	public static class F {
		@RestMethod(name=POST, path="/*")
		public Reader formPostAsContent(
				@Body F01 bean,
				@HasQuery("p1") boolean hqp1, @HasQuery("p2") boolean hqp2,
				@Query("p1") String qp1, @Query("p2") int qp2) throws Exception {
			return new StringReader("bean=["+JsonSerializer.DEFAULT_LAX.toString(bean)+"],qp1=["+qp1+"],qp2=["+qp2+"],hqp1=["+hqp1+"],hqp2=["+hqp2+"]");
		}
		public static class F01 {
			public String p1;
			public int p2;
		}
	}
	static MockRest f = MockRest.create(F.class);

	@Test
	public void f01_formPostAsContent() throws Exception {
		f.request("POST", "/", "{p1:'p1',p2:2}").json().execute().assertBody("bean=[{p1:'p1',p2:2}],qp1=[null],qp2=[0],hqp1=[false],hqp2=[false]");
		f.request("POST", "/", "{}").json().execute().assertBody("bean=[{p2:0}],qp1=[null],qp2=[0],hqp1=[false],hqp2=[false]");
		f.request("POST", "?p1=p3&p2=4", "{p1:'p1',p2:2}").json().execute().assertBody("bean=[{p1:'p1',p2:2}],qp1=[p3],qp2=[4],hqp1=[true],hqp2=[true]");
		f.request("POST", "?p1=p3&p2=4", "{}").json().execute().assertBody("bean=[{p2:0}],qp1=[p3],qp2=[4],hqp1=[true],hqp2=[true]");
	}
	
	//=================================================================================================================
	// Test multi-part parameter keys on bean properties of type array/Collection (i.e. &key=val1,&key=val2)
	// using @UrlEncoding(expandedParams=true) annotation on bean.
	// A simple round-trip test to verify that both serializing and parsing works.
	//=================================================================================================================

	@RestResource(serializers=UrlEncodingSerializer.class,parsers=UrlEncodingParser.class)
	public static class G {
		@RestMethod(name=POST)
		public DTOs.C g(@Body DTOs.C content) throws Exception {
			return content;
		}
	}	
	static MockRest g = MockRest.create(G.class);
	
	@Test
	public void g01() throws Exception {
		String in = ""
			+ "f01=a&f01=b"
			+ "&f02=c&f02=d"
			+ "&f03=1&f03=2"
			+ "&f04=3&f04=4"
			+ "&f05=@(e,f)&f05=@(g,h)"
			+ "&f06=@(i,j)&f06=@(k,l)"
			+ "&f07=(a=a,b=1,c=true)&f07=(a=b,b=2,c=false)"
			+ "&f08=(a=a,b=1,c=true)&f08=(a=b,b=2,c=false)"
			+ "&f09=@((a=a,b=1,c=true))&f09=@((a=b,b=2,c=false))"
			+ "&f10=@((a=a,b=1,c=true))&f10=@((a=b,b=2,c=false))"
			+ "&f11=a&f11=b"
			+ "&f12=c&f12=d"
			+ "&f13=1&f13=2"
			+ "&f14=3&f14=4"
			+ "&f15=@(e,f)&f15=@(g,h)"
			+ "&f16=@(i,j)&f16=@(k,l)"
			+ "&f17=(a=a,b=1,c=true)&f17=(a=b,b=2,c=false)"
			+ "&f18=(a=a,b=1,c=true)&f18=(a=b,b=2,c=false)"
			+ "&f19=@((a=a,b=1,c=true))&f19=@((a=b,b=2,c=false))"
			+ "&f20=@((a=a,b=1,c=true))&f20=@((a=b,b=2,c=false))";
		g.request("POST", "/", in).urlEnc().execute().assertBody(in);
	}
	
	//=================================================================================================================
	// Test multi-part parameter keys on bean properties of type array/Collection (i.e. &key=val1,&key=val2)
	// using URLENC_expandedParams property.
	// A simple round-trip test to verify that both serializing and parsing works.
	//=================================================================================================================

	@RestResource(serializers=UrlEncodingSerializer.class,parsers=UrlEncodingParser.class)
	public static class H {
		@RestMethod(name=POST,
			properties={
				@Property(name=UrlEncodingSerializer.URLENC_expandedParams, value="true"),
				@Property(name=UrlEncodingParser.URLENC_expandedParams, value="true")
			}
		)
		public DTOs.B g(@Body DTOs.B content) throws Exception {
			return content;
		}
	}
	static MockRest h = MockRest.create(H.class);
	
	@Test
	public void h01() throws Exception {
		String in = ""
			+ "f01=a&f01=b"
			+ "&f02=c&f02=d"
			+ "&f03=1&f03=2"
			+ "&f04=3&f04=4"
			+ "&f05=@(e,f)&f05=@(g,h)"
			+ "&f06=@(i,j)&f06=@(k,l)"
			+ "&f07=(a=a,b=1,c=true)&f07=(a=b,b=2,c=false)"
			+ "&f08=(a=a,b=1,c=true)&f08=(a=b,b=2,c=false)"
			+ "&f09=@((a=a,b=1,c=true))&f09=@((a=b,b=2,c=false))"
			+ "&f10=@((a=a,b=1,c=true))&f10=@((a=b,b=2,c=false))"
			+ "&f11=a&f11=b"
			+ "&f12=c&f12=d"
			+ "&f13=1&f13=2"
			+ "&f14=3&f14=4"
			+ "&f15=@(e,f)&f15=@(g,h)"
			+ "&f16=@(i,j)&f16=@(k,l)"
			+ "&f17=(a=a,b=1,c=true)&f17=(a=b,b=2,c=false)"
			+ "&f18=(a=a,b=1,c=true)&f18=(a=b,b=2,c=false)"
			+ "&f19=@((a=a,b=1,c=true))&f19=@((a=b,b=2,c=false))"
			+ "&f20=@((a=a,b=1,c=true))&f20=@((a=b,b=2,c=false))";
		h.request("POST", "/", in).urlEnc().execute().assertBody(in);
	}
}
