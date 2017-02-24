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
package org.apache.juneau.urlencoding;

import static org.apache.juneau.urlencoding.UrlEncodingParserContext.*;

import java.lang.reflect.*;
import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.parser.*;
import org.apache.juneau.transform.*;

/**
 * Parses URL-encoded text into POJO models.
 *
 * <h5 class='section'>Media types:</h5>
 * <p>
 * Handles <code>Content-Type</code> types: <code>application/x-www-form-urlencoded</code>
 *
 * <h5 class='section'>Description:</h5>
 * <p>
 * Parses URL-Encoded text (e.g. <js>"foo=bar&amp;baz=bing"</js>) into POJOs.
 * <p>
 * Expects parameter values to be in UON notation.
 * <p>
 * This parser uses a state machine, which makes it very fast and efficient.
 *
 * <h5 class='section'>Configurable properties:</h5>
 * <p>
 * This class has the following properties associated with it:
 * <ul>
 * 	<li>{@link UonParserContext}
 * 	<li>{@link BeanContext}
 * </ul>
 */
@SuppressWarnings({ "rawtypes", "unchecked", "hiding" })
@Consumes("application/x-www-form-urlencoded")
public class UrlEncodingParser extends UonParser {

	/** Reusable instance of {@link UrlEncodingParser}. */
	public static final UrlEncodingParser DEFAULT = new UrlEncodingParser().lock();

	/**
	 * Constructor.
	 */
	public UrlEncodingParser() {
		setDecodeChars(true);
	}

	private <T> T parseAnything(UrlEncodingParserSession session, ClassMeta<T> eType, ParserReader r, Object outer) throws Exception {

		if (eType == null)
			eType = (ClassMeta<T>)object();
		PojoSwap<T,Object> transform = (PojoSwap<T,Object>)eType.getPojoSwap();
		ClassMeta<?> sType = eType.getSerializedClassMeta();

		int c = r.peekSkipWs();
		if (c == '?')
			r.read();

		Object o;

		if (sType.isObject()) {
			ObjectMap m = new ObjectMap(session);
			parseIntoMap(session, r, m, session.string(), session.object());
			if (m.containsKey("_value"))
				o = m.get("_value");
			else
				o = session.cast(m, null, eType);
		} else if (sType.isMap()) {
			Map m = (sType.canCreateNewInstance() ? (Map)sType.newInstance() : new ObjectMap(session));
			o = parseIntoMap(session, r, m, sType.getKeyType(), sType.getValueType());
		} else if (sType.canCreateNewBean(outer)) {
			BeanMap m = session.newBeanMap(outer, sType.getInnerClass());
			m = parseIntoBeanMap(session, r, m);
			o = m == null ? null : m.getBean();
		} else {
			// It could be a non-bean with _type attribute.
			ObjectMap m = new ObjectMap(session);
			ClassMeta<Object> valueType = object();
			parseIntoMap(session, r, m, string(), valueType);
			if (m.containsKey(session.getBeanTypePropertyName()))
				o = session.cast(m, null, eType);
			else if (m.containsKey("_value"))
				o = session.convertToType(m.get("_value"), sType);
			else if (sType.isCollection()) {
				// ?1=foo&2=bar...
				Collection c2 = sType.canCreateNewInstance() ? (Collection)sType.newInstance() : new ObjectList(session);
				Map<Integer,Object> t = new TreeMap<Integer,Object>();
				for (Map.Entry<String,Object> e : m.entrySet()) {
					String k = e.getKey();
					if (StringUtils.isNumeric(k))
						t.put(Integer.valueOf(k), session.convertToType(e.getValue(), sType.getElementType()));
				}
				c2.addAll(t.values());
				o = c2;
			} else {
				if (sType.getNotABeanReason() != null)
					throw new ParseException(session, "Class ''{0}'' could not be instantiated as application/x-www-form-urlencoded.  Reason: ''{1}''", sType, sType.getNotABeanReason());
				throw new ParseException(session, "Malformed application/x-www-form-urlencoded input for class ''{0}''.", sType);
			}
		}

		if (transform != null && o != null)
			o = transform.unswap(session, o, eType);

		if (outer != null)
			setParent(eType, o, outer);

		return (T)o;
	}

	private <K,V> Map<K,V> parseIntoMap(UonParserSession session, ParserReader r, Map<K,V> m, ClassMeta<K> keyType, ClassMeta<V> valueType) throws Exception {

		if (keyType == null)
			keyType = (ClassMeta<K>)string();

		int c = r.peekSkipWs();
		if (c == -1)
			return m;

		final int S1=1; // Looking for attrName start.
		final int S2=2; // Found attrName end, looking for =.
		final int S3=3; // Found =, looking for valStart.
		final int S4=4; // Looking for & or end.
		boolean isInEscape = false;

		int state = S1;
		K currAttr = null;
		while (c != -1) {
			c = r.read();
			if (! isInEscape) {
				if (state == S1) {
					if (c == -1)
						return m;
					r.unread();
					Object attr = parseAttr(session, r, true);
					currAttr = attr == null ? null : convertAttrToType(session, m, session.trim(attr.toString()), keyType);
					state = S2;
					c = 0; // Avoid isInEscape if c was '\'
				} else if (state == S2) {
					if (c == '\u0002')
						state = S3;
					else if (c == -1 || c == '\u0001') {
						m.put(currAttr, null);
						if (c == -1)
							return m;
						state = S1;
					}
				} else if (state == S3) {
					if (c == -1 || c == '\u0001') {
						V value = convertAttrToType(session, m, "", valueType);
						m.put(currAttr, value);
						if (c == -1)
							return m;
						state = S1;
					} else  {
						// For performance, we bypass parseAnything for string values.
						V value = (V)(valueType.isString() ? super.parseString(session, r.unread(), true) : super.parseAnything(session, valueType, r.unread(), m, true, null));

						// If we already encountered this parameter, turn it into a list.
						if (m.containsKey(currAttr) && valueType.isObject()) {
							Object v2 = m.get(currAttr);
							if (! (v2 instanceof ObjectList)) {
								v2 = new ObjectList(v2).setBeanSession(session);
								m.put(currAttr, (V)v2);
							}
							((ObjectList)v2).add(value);
						} else {
							m.put(currAttr, value);
						}
						state = S4;
						c = 0; // Avoid isInEscape if c was '\'
					}
				} else if (state == S4) {
					if (c == '\u0001')
						state = S1;
					else if (c == -1) {
						return m;
					}
				}
			}
			isInEscape = (c == '\\' && ! isInEscape);
		}
		if (state == S1)
			throw new ParseException(session, "Could not find attribute name on object.");
		if (state == S2)
			throw new ParseException(session, "Could not find '=' following attribute name on object.");
		if (state == S3)
			throw new ParseException(session, "Dangling '=' found in object entry");
		if (state == S4)
			throw new ParseException(session, "Could not find end of object.");

		return null; // Unreachable.
	}

	private <T> BeanMap<T> parseIntoBeanMap(UrlEncodingParserSession session, ParserReader r, BeanMap<T> m) throws Exception {

		int c = r.peekSkipWs();
		if (c == -1)
			return m;

		final int S1=1; // Looking for attrName start.
		final int S2=2; // Found attrName end, looking for =.
		final int S3=3; // Found =, looking for valStart.
		final int S4=4; // Looking for , or }
		boolean isInEscape = false;

		int state = S1;
		String currAttr = "";
		int currAttrLine = -1, currAttrCol = -1;
		while (c != -1) {
			c = r.read();
			if (! isInEscape) {
				if (state == S1) {
					if (c == -1) {
						return m;
					}
					r.unread();
					currAttrLine= r.getLine();
					currAttrCol = r.getColumn();
					currAttr = parseAttrName(session, r, true);
					if (currAttr == null)  // Value was '%00'
						return null;
					state = S2;
				} else if (state == S2) {
					if (c == '\u0002')
						state = S3;
					else if (c == -1 || c == '\u0001') {
						m.put(currAttr, null);
						if (c == -1)
							return m;
						state = S1;
					}
				} else if (state == S3) {
					if (c == -1 || c == '\u0001') {
						if (! currAttr.equals(session.getBeanTypePropertyName())) {
							BeanPropertyMeta pMeta = m.getPropertyMeta(currAttr);
							if (pMeta == null) {
								onUnknownProperty(session, currAttr, m, currAttrLine, currAttrCol);
							} else {
								session.setCurrentProperty(pMeta);
								// In cases of "&foo=", create an empty instance of the value if createable.
								// Otherwise, leave it null.
								ClassMeta<?> cm = pMeta.getClassMeta();
								if (cm.canCreateNewInstance())
									pMeta.set(m, cm.newInstance());
								session.setCurrentProperty(null);
							}
						}
						if (c == -1)
							return m;
						state = S1;
					} else {
						if (! currAttr.equals(session.getBeanTypePropertyName())) {
							BeanPropertyMeta pMeta = m.getPropertyMeta(currAttr);
							if (pMeta == null) {
								onUnknownProperty(session, currAttr, m, currAttrLine, currAttrCol);
								parseAnything(session, object(), r.unread(), m.getBean(false), true, null); // Read content anyway to ignore it
							} else {
								session.setCurrentProperty(pMeta);
								if (session.shouldUseExpandedParams(pMeta)) {
									ClassMeta et = pMeta.getClassMeta().getElementType();
									Object value = parseAnything(session, et, r.unread(), m.getBean(false), true, pMeta);
									setName(et, value, currAttr);
									pMeta.add(m, value);
								} else {
									ClassMeta<?> cm = pMeta.getClassMeta();
									Object value = parseAnything(session, cm, r.unread(), m.getBean(false), true, pMeta);
									setName(cm, value, currAttr);
									pMeta.set(m, value);
								}
								session.setCurrentProperty(null);
							}
						}
						state = S4;
					}
				} else if (state == S4) {
					if (c == '\u0001')
						state = S1;
					else if (c == -1) {
						return m;
					}
				}
			}
			isInEscape = (c == '\\' && ! isInEscape);
		}
		if (state == S1)
			throw new ParseException(session, "Could not find attribute name on object.");
		if (state == S2)
			throw new ParseException(session, "Could not find '=' following attribute name on object.");
		if (state == S3)
			throw new ParseException(session, "Could not find value following '=' on object.");
		if (state == S4)
			throw new ParseException(session, "Could not find end of object.");

		return null; // Unreachable.
	}

	/**
	 * Parse a URL query string into a simple map of key/value pairs.
	 *
	 * @param qs The query string to parse.
	 * @return A sorted {@link TreeMap} of query string entries.
	 * @throws Exception
	 */
	public Map<String,String[]> parseIntoSimpleMap(String qs) throws Exception {

		Map<String,String[]> m = new TreeMap<String,String[]>();

		if (StringUtils.isEmpty(qs))
			return m;

		UonReader r = new UonReader(qs, true);

		final int S1=1; // Looking for attrName start.
		final int S2=2; // Found attrName start, looking for = or & or end.
		final int S3=3; // Found =, looking for valStart.
		final int S4=4; // Found valStart, looking for & or end.

		try {
			int c = r.peekSkipWs();
			if (c == '?')
				r.read();

			int state = S1;
			String currAttr = null;
			while (c != -1) {
				c = r.read();
				if (state == S1) {
					if (c != -1) {
						r.unread();
						r.mark();
						state = S2;
					}
				} else if (state == S2) {
					if (c == -1) {
						add(m, r.getMarked(), null);
					} else if (c == '\u0001') {
						m.put(r.getMarked(0,-1), null);
						state = S1;
					} else if (c == '\u0002') {
						currAttr = r.getMarked(0,-1);
						state = S3;
					}
				} else if (state == S3) {
					if (c == -1 || c == '\u0001') {
						add(m, currAttr, "");
					} else {
						if (c == '\u0002')
							r.replace('=');
						r.unread();
						r.mark();
						state = S4;
					}
				} else if (state == S4) {
					if (c == -1) {
						add(m, currAttr, r.getMarked());
					} else if (c == '\u0001') {
						add(m, currAttr, r.getMarked(0,-1));
						state = S1;
					} else if (c == '\u0002') {
						r.replace('=');
					}
				}
			}
		} finally {
			r.close();
		}

		return m;
	}

	private static void add(Map<String,String[]> m, String key, String val) {
		boolean b = m.containsKey(key);
		if (val == null) {
			if (! b)
				m.put(key, null);
		} else if (b && m.get(key) != null) {
			m.put(key, ArrayUtils.append(m.get(key), val));
		} else {
			m.put(key, new String[]{val});
		}
	}

	private Object[] parseArgs(UrlEncodingParserSession session, ParserReader r, ClassMeta<?>[] argTypes) throws Exception {
		// TODO - This can be made more efficient.
		ClassMeta<TreeMap<Integer,String>> cm = session.getClassMeta(TreeMap.class, Integer.class, String.class);
		TreeMap<Integer,String> m = parseAnything(session, cm, r, session.getOuter());
		Object[] vals = m.values().toArray(new Object[m.size()]);
		if (vals.length != argTypes.length)
			throw new ParseException(session, "Argument lengths don't match.  vals={0}, argTypes={1}", vals.length, argTypes.length);
		for (int i = 0; i < vals.length; i++) {
			String s = String.valueOf(vals[i]);
			vals[i] = super.parseAnything(session, argTypes[i], new UonReader(s, false), session.getOuter(), true, null);
		}

		return vals;
	}

	/**
	 * Parses a single query parameter value into the specified class type.
	 *
	 * @param in The input query string value.
	 * @param type The object type to create.
	 * 	<br>Can be any of the following: {@link ClassMeta}, {@link Class}, {@link ParameterizedType}, {@link GenericArrayType}
	 * @param args The type arguments of the class if it's a collection or map.
	 * 	<br>Can be any of the following: {@link ClassMeta}, {@link Class}, {@link ParameterizedType}, {@link GenericArrayType}
	 * 	<br>Ignored if the main type is not a map or collection.
	 * @return A new instance of the specified type.
	 * @throws ParseException
	 */
	public <T> T parseParameter(CharSequence in, Type type, Type...args) throws ParseException {
		if (in == null)
			return null;
		UonParserSession session = createParameterSession(in);
		try {
			UonReader r = session.getReader();
			return (T)super.parseAnything(session, session.getClassMeta(type, args), r, null, true, null);
		} catch (ParseException e) {
			throw e;
		} catch (Exception e) {
			throw new ParseException(session, e);
		} finally {
			session.close();
		}
	}

	/**
	 * Parses a single query parameter value into the specified class type.
	 *
	 * @param in The input query string value.
	 * @param type The class type of the object to create.
	 * @return A new instance of the specified type.
	 * @throws ParseException
	 */
	public <T> T parseParameter(CharSequence in, Class<T> type) throws ParseException {
		if (in == null)
			return null;
		UonParserSession session = createParameterSession(in);
		try {
			UonReader r = session.getReader();
			return super.parseAnything(session, session.getClassMeta(type), r, null, true, null);
		} catch (ParseException e) {
			throw e;
		} catch (Exception e) {
			throw new ParseException(session, e);
		} finally {
			session.close();
		}
	}

	/**
	 * Same as {@link #parseParameter(CharSequence, Type, Type...)} except the type has already
	 * been converted to a {@link ClassMeta} object.
	 *
	 * @param in The input query string value.
	 * @param type The class type of the object to create.
	 * @return A new instance of the specified type.
	 * @throws ParseException
	 */
	public <T> T parseParameter(CharSequence in, ClassMeta<T> type) throws ParseException {
		if (in == null)
			return null;
		UonParserSession session = createParameterSession(in);
		try {
			UonReader r = session.getReader();
			return super.parseAnything(session, type, r, null, true, null);
		} catch (ParseException e) {
			throw e;
		} catch (Exception e) {
			throw new ParseException(session, e);
		} finally {
			session.close();
		}
	}


	//--------------------------------------------------------------------------------
	// Entry point methods
	//--------------------------------------------------------------------------------

	@Override /* Parser */
	public UrlEncodingParserSession createSession(Object input, ObjectMap op, Method javaMethod, Object outer, Locale locale, TimeZone timeZone, MediaType mediaType) {
		return new UrlEncodingParserSession(getContext(UrlEncodingParserContext.class), op, input, javaMethod, outer, locale, timeZone, mediaType);
	}

	@Override /* Parser */
	protected <T> T doParse(ParserSession session, ClassMeta<T> type) throws Exception {
		UrlEncodingParserSession s = (UrlEncodingParserSession)session;
		UonReader r = s.getReader();
		T o = parseAnything(s, type, r, s.getOuter());
		return o;
	}

	@Override /* ReaderParser */
	protected Object[] doParseArgs(ParserSession session, ClassMeta<?>[] argTypes) throws Exception {
		UrlEncodingParserSession uctx = (UrlEncodingParserSession)session;
		UonReader r = uctx.getReader();
		Object[] a = parseArgs(uctx, r, argTypes);
		return a;
	}

	@Override /* ReaderParser */
	protected <K,V> Map<K,V> doParseIntoMap(ParserSession session, Map<K,V> m, Type keyType, Type valueType) throws Exception {
		UrlEncodingParserSession s = (UrlEncodingParserSession)session;
		UonReader r = s.getReader();
		if (r.peekSkipWs() == '?')
			r.read();
		m = parseIntoMap(s, r, m, (ClassMeta<K>)session.getClassMeta(keyType), (ClassMeta<V>)session.getClassMeta(valueType));
		return m;
	}


	//--------------------------------------------------------------------------------
	// Properties
	//--------------------------------------------------------------------------------

	/**
	 * <b>Configuration property:</b> Serialize bean property collections/arrays as separate key/value pairs.
	 * <p>
	 * <ul>
	 * 	<li><b>Name:</b> <js>"UrlEncoding.expandedParams"</js>
	 * 	<li><b>Data type:</b> <code>Boolean</code>
	 * 	<li><b>Default:</b> <jk>false</jk>
	 * 	<li><b>Session-overridable:</b> <jk>true</jk>
	 * </ul>
	 * <p>
	 * If <jk>false</jk>, serializing the array <code>[1,2,3]</code> results in <code>?key=$a(1,2,3)</code>.
	 * If <jk>true</jk>, serializing the same array results in <code>?key=1&amp;key=2&amp;key=3</code>.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode'>
	 * 	<jk>public class</jk> A {
	 * 		<jk>public</jk> String[] f1 = {<js>"a"</js>,<js>"b"</js>};
	 * 		<jk>public</jk> List&lt;String&gt; f2 = <jk>new</jk> LinkedList&lt;String&gt;(Arrays.<jsm>asList</jsm>(<jk>new</jk> String[]{<js>"c"</js>,<js>"d"</js>}));
	 * 	}
	 *
	 * 	UrlEncodingSerializer s1 = <jk>new</jk> UrlEncodingParser();
	 * 	UrlEncodingSerializer s2 = <jk>new</jk> UrlEncodingParser().setExpandedParams(<jk>true</jk>);
	 *
	 * 	String s1 = p1.serialize(<jk>new</jk> A()); <jc>// Produces "f1=(a,b)&amp;f2=(c,d)"</jc>
	 * 	String s2 = p2.serialize(<jk>new</jk> A()); <jc>// Produces "f1=a&amp;f1=b&amp;f2=c&amp;f2=d"</jc>
	 * </p>
	 * <p>
	 * This option only applies to beans.
	 * <p>
	 * <h5 class='section'>Notes:</h5>
	 * <ul>
	 * 	<li>If parsing multi-part parameters, it's highly recommended to use Collections or Lists
	 * 		as bean property types instead of arrays since arrays have to be recreated from scratch every time a value
	 * 		is added to it.
	 * </ul>
	 * <p>
	 * <h5 class='section'>Notes:</h5>
	 * <ul>
	 * 	<li>This is equivalent to calling <code>setProperty(<jsf>URLENC_expandedParams</jsf>, value)</code>.
	 * </ul>
	 *
	 * @param value The new value for this property.
	 * @return This object (for method chaining).
	 * @throws LockedException If {@link #lock()} was called on this class.
	 * @see UrlEncodingParserContext#URLENC_expandedParams
	 */
	public UrlEncodingParser setExpandedParams(boolean value) throws LockedException {
		return setProperty(URLENC_expandedParams, value);
	}

	@Override /* UonParser */
	public UrlEncodingParser setDecodeChars(boolean value) throws LockedException {
		super.setDecodeChars(value);
		return this;
	}

	@Override /* Parser */
	public UrlEncodingParser setTrimStrings(boolean value) throws LockedException {
		super.setTrimStrings(value);
		return this;
	}

	@Override /* Parser */
	public UrlEncodingParser setStrict(boolean value) throws LockedException {
		super.setStrict(value);
		return this;
	}

	@Override /* Parser */
	public UrlEncodingParser setInputStreamCharset(String value) throws LockedException {
		super.setInputStreamCharset(value);
		return this;
	}

	@Override /* Parser */
	public UrlEncodingParser setFileCharset(String value) throws LockedException {
		super.setFileCharset(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setBeansRequireDefaultConstructor(boolean value) throws LockedException {
		super.setBeansRequireDefaultConstructor(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setBeansRequireSerializable(boolean value) throws LockedException {
		super.setBeansRequireSerializable(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setBeansRequireSettersForGetters(boolean value) throws LockedException {
		super.setBeansRequireSettersForGetters(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setBeansRequireSomeProperties(boolean value) throws LockedException {
		super.setBeansRequireSomeProperties(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setBeanMapPutReturnsOldValue(boolean value) throws LockedException {
		super.setBeanMapPutReturnsOldValue(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setBeanConstructorVisibility(Visibility value) throws LockedException {
		super.setBeanConstructorVisibility(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setBeanClassVisibility(Visibility value) throws LockedException {
		super.setBeanClassVisibility(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setBeanFieldVisibility(Visibility value) throws LockedException {
		super.setBeanFieldVisibility(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setMethodVisibility(Visibility value) throws LockedException {
		super.setMethodVisibility(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setUseJavaBeanIntrospector(boolean value) throws LockedException {
		super.setUseJavaBeanIntrospector(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setUseInterfaceProxies(boolean value) throws LockedException {
		super.setUseInterfaceProxies(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setIgnoreUnknownBeanProperties(boolean value) throws LockedException {
		super.setIgnoreUnknownBeanProperties(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setIgnoreUnknownNullBeanProperties(boolean value) throws LockedException {
		super.setIgnoreUnknownNullBeanProperties(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setIgnorePropertiesWithoutSetters(boolean value) throws LockedException {
		super.setIgnorePropertiesWithoutSetters(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setIgnoreInvocationExceptionsOnGetters(boolean value) throws LockedException {
		super.setIgnoreInvocationExceptionsOnGetters(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setIgnoreInvocationExceptionsOnSetters(boolean value) throws LockedException {
		super.setIgnoreInvocationExceptionsOnSetters(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setSortProperties(boolean value) throws LockedException {
		super.setSortProperties(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setNotBeanPackages(String...values) throws LockedException {
		super.setNotBeanPackages(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setNotBeanPackages(Collection<String> values) throws LockedException {
		super.setNotBeanPackages(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser addNotBeanPackages(String...values) throws LockedException {
		super.addNotBeanPackages(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser addNotBeanPackages(Collection<String> values) throws LockedException {
		super.addNotBeanPackages(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser removeNotBeanPackages(String...values) throws LockedException {
		super.removeNotBeanPackages(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser removeNotBeanPackages(Collection<String> values) throws LockedException {
		super.removeNotBeanPackages(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setNotBeanClasses(Class<?>...values) throws LockedException {
		super.setNotBeanClasses(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setNotBeanClasses(Collection<Class<?>> values) throws LockedException {
		super.setNotBeanClasses(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser addNotBeanClasses(Class<?>...values) throws LockedException {
		super.addNotBeanClasses(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser addNotBeanClasses(Collection<Class<?>> values) throws LockedException {
		super.addNotBeanClasses(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser removeNotBeanClasses(Class<?>...values) throws LockedException {
		super.removeNotBeanClasses(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser removeNotBeanClasses(Collection<Class<?>> values) throws LockedException {
		super.removeNotBeanClasses(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setBeanFilters(Class<?>...values) throws LockedException {
		super.setBeanFilters(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setBeanFilters(Collection<Class<?>> values) throws LockedException {
		super.setBeanFilters(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser addBeanFilters(Class<?>...values) throws LockedException {
		super.addBeanFilters(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser addBeanFilters(Collection<Class<?>> values) throws LockedException {
		super.addBeanFilters(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser removeBeanFilters(Class<?>...values) throws LockedException {
		super.removeBeanFilters(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser removeBeanFilters(Collection<Class<?>> values) throws LockedException {
		super.removeBeanFilters(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setPojoSwaps(Class<?>...values) throws LockedException {
		super.setPojoSwaps(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setPojoSwaps(Collection<Class<?>> values) throws LockedException {
		super.setPojoSwaps(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser addPojoSwaps(Class<?>...values) throws LockedException {
		super.addPojoSwaps(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser addPojoSwaps(Collection<Class<?>> values) throws LockedException {
		super.addPojoSwaps(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser removePojoSwaps(Class<?>...values) throws LockedException {
		super.removePojoSwaps(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser removePojoSwaps(Collection<Class<?>> values) throws LockedException {
		super.removePojoSwaps(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setImplClasses(Map<Class<?>,Class<?>> values) throws LockedException {
		super.setImplClasses(values);
		return this;
	}

	@Override /* CoreApi */
	public <T> CoreApi addImplClass(Class<T> interfaceClass, Class<? extends T> implClass) throws LockedException {
		super.addImplClass(interfaceClass, implClass);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setBeanDictionary(Class<?>...values) throws LockedException {
		super.setBeanDictionary(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setBeanDictionary(Collection<Class<?>> values) throws LockedException {
		super.setBeanDictionary(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser addToBeanDictionary(Class<?>...values) throws LockedException {
		super.addToBeanDictionary(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser addToBeanDictionary(Collection<Class<?>> values) throws LockedException {
		super.addToBeanDictionary(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser removeFromBeanDictionary(Class<?>...values) throws LockedException {
		super.removeFromBeanDictionary(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser removeFromBeanDictionary(Collection<Class<?>> values) throws LockedException {
		super.removeFromBeanDictionary(values);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setBeanTypePropertyName(String value) throws LockedException {
		super.setBeanTypePropertyName(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setDefaultParser(Class<?> value) throws LockedException {
		super.setDefaultParser(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setLocale(Locale value) throws LockedException {
		super.setLocale(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setTimeZone(TimeZone value) throws LockedException {
		super.setTimeZone(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setMediaType(MediaType value) throws LockedException {
		super.setMediaType(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setDebug(boolean value) throws LockedException {
		super.setDebug(value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setProperty(String name, Object value) throws LockedException {
		super.setProperty(name, value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser setProperties(ObjectMap properties) throws LockedException {
		super.setProperties(properties);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser addToProperty(String name, Object value) throws LockedException {
		super.addToProperty(name, value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser putToProperty(String name, Object key, Object value) throws LockedException {
		super.putToProperty(name, key, value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser putToProperty(String name, Object value) throws LockedException {
		super.putToProperty(name, value);
		return this;
	}

	@Override /* CoreApi */
	public UrlEncodingParser removeFromProperty(String name, Object value) throws LockedException {
		super.removeFromProperty(name, value);
		return this;
	}


	//--------------------------------------------------------------------------------
	// Overridden methods
	//--------------------------------------------------------------------------------

	@Override /* CoreApi */
	public UrlEncodingParser setClassLoader(ClassLoader classLoader) throws LockedException {
		super.setClassLoader(classLoader);
		return this;
	}

	@Override /* Lockable */
	public UrlEncodingParser lock() {
		super.lock();
		return this;
	}

	@Override /* Lockable */
	public UrlEncodingParser clone() {
		UrlEncodingParser c = (UrlEncodingParser)super.clone();
		return c;
	}
}
