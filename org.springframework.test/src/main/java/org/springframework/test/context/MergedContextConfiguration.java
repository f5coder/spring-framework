/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.test.context;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * <code>MergedContextConfiguration</code> encapsulates the <em>merged</em>
 * context configuration declared on a test class and all of its superclasses
 * via {@link ContextConfiguration @ContextConfiguration} and
 * {@link ActiveProfiles @ActiveProfiles}.
 * 
 * <p>Merged resource locations, configuration classes, and active profiles
 * represent all declared values in the test class hierarchy taking into
 * consideration the semantics of the
 * {@link ContextConfiguration#inheritLocations inheritLocations} and
 * {@link ActiveProfiles#inheritProfiles inheritProfiles} flags in
 * {@code @ContextConfiguration} and {@code @ActiveProfiles}, respectively.
 * 
 * <p>A {@link SmartContextLoader} uses <code>MergedContextConfiguration</code>
 * to load an {@link org.springframework.context.ApplicationContext ApplicationContext}.
 * 
 * @author Sam Brannen
 * @since 3.1
 * @see ContextConfiguration
 * @see ActiveProfiles
 * @see ContextConfigurationAttributes
 * @see SmartContextLoader#loadContext(MergedContextConfiguration)
 */
public class MergedContextConfiguration {

	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

	private final Class<?> testClass;

	private final String[] locations;

	private final Class<?>[] classes;

	private final String[] activeProfiles;

	private final ContextLoader contextLoader;

	private final String contextKey;


	private static String[] processLocations(String[] locations) {
		return locations == null ? EMPTY_STRING_ARRAY : locations;
	}

	private static Class<?>[] processClasses(Class<?>[] classes) {
		return classes == null ? EMPTY_CLASS_ARRAY : classes;
	}

	private static String[] processActiveProfiles(String[] activeProfiles) {
		if (activeProfiles == null) {
			return EMPTY_STRING_ARRAY;
		}

		// Active profiles must be unique and sorted in order to support proper
		// cache key generation. Specifically, profile sets {foo,bar} and
		// {bar,foo} must both result in the same array (e.g., [bar,foo]).
		SortedSet<String> sortedProfilesSet = new TreeSet<String>(Arrays.asList(activeProfiles));
		return StringUtils.toStringArray(sortedProfilesSet);
	}

	/**
	 * Generate a null-safe {@link String} representation of the supplied {@link ContextLoader}.
	 */
	private static String nullSafeToString(ContextLoader contextLoader) {
		return contextLoader == null ? "null" : contextLoader.getClass().getName();
	}

	/**
	 * Generate a context <em>key</em> from the supplied values.
	 */
	private static String generateContextKey(String[] locations, Class<?>[] classes, String[] activeProfiles,
			ContextLoader contextLoader) {

		String locationsKey = ObjectUtils.nullSafeToString(locations);
		String classesKey = ObjectUtils.nullSafeToString(classes);
		String activeProfilesKey = ObjectUtils.nullSafeToString(activeProfiles);
		String contextLoaderKey = nullSafeToString(contextLoader);

		return String.format("locations = %s, classes = %s, activeProfiles = %s, contextLoader = %s", locationsKey,
			classesKey, activeProfilesKey, contextLoaderKey);
	}

	/**
	 * Create a new <code>MergedContextConfiguration</code> instance for the
	 * supplied {@link Class test class}, resource locations, configuration
	 * classes, active profiles, and {@link ContextLoader}.
	 * <p>If a <code>null</code> value is supplied for <code>locations</code>,
	 * <code>classes</code>, or <code>activeProfiles</code> an empty array will
	 * be stored instead. Furthermore, active profiles will be sorted, and duplicate
	 * profiles will be removed. 
	 * @param testClass the test class for which the configuration was merged
	 * @param locations the merged resource locations
	 * @param classes the merged configuration classes
	 * @param activeProfiles the merged active bean definition profiles
	 * @param contextLoader the resolved <code>ContextLoader</code>
	 */
	public MergedContextConfiguration(Class<?> testClass, String[] locations, Class<?>[] classes,
			String[] activeProfiles, ContextLoader contextLoader) {
		this.testClass = testClass;
		this.locations = processLocations(locations);
		this.classes = processClasses(classes);
		this.activeProfiles = processActiveProfiles(activeProfiles);
		this.contextLoader = contextLoader;
		this.contextKey = generateContextKey(this.locations, this.classes, this.activeProfiles, this.contextLoader);
	}

	/**
	 * Get the {@link Class test class} associated with this
	 * <code>MergedContextConfiguration</code>.
	 */
	public Class<?> getTestClass() {
		return testClass;
	}

	/**
	 * Get the merged resource locations for the
	 * {@link #getTestClass() test class}.
	 */
	public String[] getLocations() {
		return locations;
	}

	/**
	 * Get the merged configuration classes for the
	 * {@link #getTestClass() test class}.
	 */
	public Class<?>[] getClasses() {
		return classes;
	}

	/**
	 * Get the merged active bean definition profiles for the
	 * {@link #getTestClass() test class}.
	 */
	public String[] getActiveProfiles() {
		return activeProfiles;
	}

	/**
	 * Get the resolved {@link ContextLoader} for the
	 * {@link #getTestClass() test class}.
	 */
	public ContextLoader getContextLoader() {
		return contextLoader;
	}

	/**
	 * Get the unique context key for all properties of this
	 * <code>MergedContextConfiguration</code> excluding the
	 * {@link #getTestClass() test class}. 
	 * <p>Intended to be used for caching an 
	 * {@link org.springframework.context.ApplicationContext ApplicationContext}
	 * that was loaded using properties of this <code>MergedContextConfiguration</code>.
	 */
	public String getContextKey() {
		return contextKey;
	}

	/**
	 * Provide a String representation of the test class, merged context
	 * configuration, and context key.
	 */
	@Override
	public String toString() {
		return new ToStringCreator(this)//
		.append("testClass", testClass)//
		.append("locations", ObjectUtils.nullSafeToString(locations))//
		.append("classes", ObjectUtils.nullSafeToString(classes))//
		.append("activeProfiles", ObjectUtils.nullSafeToString(activeProfiles))//
		.append("contextLoader", nullSafeToString(contextLoader))//
		.append("contextKey", contextKey)//
		.toString();
	}

}