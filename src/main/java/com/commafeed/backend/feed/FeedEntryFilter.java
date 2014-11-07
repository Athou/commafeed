package com.commafeed.backend.feed;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.RequiredArgsConstructor;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.JexlInfo;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.jexl2.Script;
import org.apache.commons.jexl2.introspection.JexlMethod;
import org.apache.commons.jexl2.introspection.JexlPropertyGet;
import org.apache.commons.jexl2.introspection.Uberspect;
import org.apache.commons.jexl2.introspection.UberspectImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;

import com.commafeed.backend.model.FeedEntry;

@RequiredArgsConstructor
public class FeedEntryFilter {

	private static final JexlEngine ENGINE = initEngine();

	private static JexlEngine initEngine() {
		// classloader that prevents object creation
		ClassLoader cl = new ClassLoader() {
			@Override
			public Class<?> loadClass(String name) throws ClassNotFoundException {
				return null;
			}

			@Override
			protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
				return null;
			}
		};

		// uberspect that prevents access to .class and .getClass()
		Uberspect uberspect = new UberspectImpl(LogFactory.getLog(JexlEngine.class)) {
			@Override
			public JexlPropertyGet getPropertyGet(Object obj, Object identifier, JexlInfo info) {
				if ("class".equals(identifier)) {
					return null;
				}
				return super.getPropertyGet(obj, identifier, info);
			}

			@Override
			public JexlMethod getMethod(Object obj, String method, Object[] args, JexlInfo info) {
				if ("getClass".equals(method)) {
					return null;
				}
				return super.getMethod(obj, method, args, info);
			}
		};

		JexlEngine engine = new JexlEngine(uberspect, null, null, null);
		engine.setStrict(true);
		engine.setClassLoader(cl);
		return engine;
	}

	private final String filter;

	public boolean matchesEntry(FeedEntry entry) throws FeedEntryFilterException {
		if (StringUtils.isBlank(filter)) {
			return true;
		}

		Script script = null;
		try {
			script = ENGINE.createScript(filter);
		} catch (JexlException e) {
			throw new FeedEntryFilterException("Exception while parsing expression " + filter, e);
		}

		JexlContext context = new MapContext();
		context.set("title", Jsoup.parse(entry.getContent().getTitle()).text().toLowerCase());
		context.set("author", entry.getContent().getAuthor().toLowerCase());
		context.set("content", Jsoup.parse(entry.getContent().getContent()).text().toLowerCase());
		context.set("url", entry.getUrl().toLowerCase());

		Callable<Object> callable = script.callable(context);
		Future<Object> future = Executors.newFixedThreadPool(1).submit(callable);
		Object result = null;
		try {
			result = future.get(500, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new FeedEntryFilterException("interrupted while evaluating expression " + filter, e);
		} catch (ExecutionException e) {
			throw new FeedEntryFilterException("Exception while evaluating expression " + filter, e);
		} catch (TimeoutException e) {
			throw new FeedEntryFilterException("Took too long evaluating expression " + filter, e);
		}
		return (boolean) result;
	}

	@SuppressWarnings("serial")
	public static class FeedEntryFilterException extends Exception {
		public FeedEntryFilterException(String message, Throwable t) {
			super(message, t);
		}
	}
}
