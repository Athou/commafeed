package com.commafeed.frontend.utils;

import org.apache.wicket.model.PropertyModel;

import ch.lambdaj.Lambda;
import ch.lambdaj.function.argument.Argument;
import ch.lambdaj.function.argument.ArgumentsFactory;

/**
 * Utility class to generate PropertyModels in a type-safe way
 * 
 */
public class ModelFactory {

	public static <T> String invokedProperty(T proxiedValue) {
		Argument<T> a = ArgumentsFactory.actualArgument(proxiedValue);
		return a.getInkvokedPropertyName();
	}

	public static <T> PropertyModel<T> model(Object value, T proxiedValue) {
		String invokedPN = invokedProperty(proxiedValue);
		PropertyModel<T> m = new PropertyModel<T>(value, invokedPN);
		return m;
	}

	@SuppressWarnings("unchecked")
	public static <T> T proxy(T t) {
		Object object = Lambda.on(t.getClass());
		return (T) object;
	}

	public static <T> T proxy(Class<T> clazz) {
		return Lambda.on(clazz);
	}

	/**
	 * shortcuts to ModelFactory
	 * 
	 */
	public static class MF {

		public static <T> String i(T proxiedValue) {
			return ModelFactory.invokedProperty(proxiedValue);
		}

		public static <T> PropertyModel<T> m(Object value, T proxiedValue) {
			return ModelFactory.model(value, proxiedValue);
		}

		public static <T> T p(T t) {
			return ModelFactory.proxy(t);
		}

		public static <T> T p(Class<T> clazz) {
			return ModelFactory.proxy(clazz);
		}
	}
}