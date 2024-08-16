package com.commafeed;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.WireFeedGenerator;
import com.rometools.rome.io.WireFeedParser;

import io.quarkus.runtime.annotations.RegisterForReflection;

class NativeImageClassesTest {

	@Test
	void annotationContainsAllRequiredRomeClasses() {
		Reflections reflections = new Reflections("com.rometools");
		Set<Class<?>> classesInAnnotation = Set
				.copyOf(List.of(NativeImageClasses.class.getAnnotation(RegisterForReflection.class).targets()));

		for (Class<?> clazz : Set.of(Module.class, WireFeedParser.class, WireFeedGenerator.class)) {
			Set<Class<?>> moduleClasses = new HashSet<>(reflections.get(Scanners.SubTypes.of(clazz).asClass()));
			moduleClasses.removeIf(c -> c.isInterface() || Modifier.isAbstract(c.getModifiers()));
			moduleClasses.removeAll(classesInAnnotation);

			moduleClasses.forEach(c -> System.out.println(c.getName() + ".class,"));
			Assertions.assertEquals(Set.of(), moduleClasses);
		}

	}

}