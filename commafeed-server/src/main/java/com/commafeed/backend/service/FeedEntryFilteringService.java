package com.commafeed.backend.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.model.FeedEntry;

import dev.cel.common.CelAbstractSyntaxTree;
import dev.cel.common.CelValidationException;
import dev.cel.common.types.SimpleType;
import dev.cel.compiler.CelCompiler;
import dev.cel.compiler.CelCompilerFactory;
import dev.cel.runtime.CelEvaluationException;
import dev.cel.runtime.CelRuntime;
import dev.cel.runtime.CelRuntimeFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Singleton
public class FeedEntryFilteringService {

	private static final CelCompiler CEL_COMPILER = CelCompilerFactory.standardCelCompilerBuilder()
			.addVar("title", SimpleType.STRING)
			.addVar("titleLower", SimpleType.STRING)
			.addVar("author", SimpleType.STRING)
			.addVar("authorLower", SimpleType.STRING)
			.addVar("content", SimpleType.STRING)
			.addVar("contentLower", SimpleType.STRING)
			.addVar("url", SimpleType.STRING)
			.addVar("urlLower", SimpleType.STRING)
			.addVar("categories", SimpleType.STRING)
			.addVar("categoriesLower", SimpleType.STRING)
			.build();
	private static final CelRuntime CEL_RUNTIME = CelRuntimeFactory.standardCelRuntimeBuilder().build();

	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final CommaFeedConfiguration config;

	public boolean filterMatchesEntry(String filter, FeedEntry entry) throws FeedEntryFilterException {
		if (StringUtils.isBlank(filter)) {
			return true;
		}

		String title = entry.getContent().getTitle() == null ? "" : Jsoup.parse(entry.getContent().getTitle()).text();
		String author = entry.getContent().getAuthor() == null ? "" : entry.getContent().getAuthor();
		String content = entry.getContent().getContent() == null ? "" : Jsoup.parse(entry.getContent().getContent()).text();
		String url = entry.getUrl() == null ? "" : entry.getUrl();
		String categories = entry.getContent().getCategories() == null ? "" : entry.getContent().getCategories();

		Map<String, Object> data = new HashMap<>();
		data.put("title", title);
		data.put("titleLower", title.toLowerCase());

		data.put("author", author);
		data.put("authorLower", author.toLowerCase());

		data.put("content", content);
		data.put("contentLower", content.toLowerCase());

		data.put("url", url);
		data.put("urlLower", url.toLowerCase());

		data.put("categories", categories);
		data.put("categoriesLower", categories.toLowerCase());

		Future<Object> future = executor.submit(() -> evaluateCelExpression(filter, data));
		Object result;
		try {
			result = future.get(config.feedRefresh().filteringExpressionEvaluationTimeout().toMillis(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new FeedEntryFilterException("interrupted while evaluating expression " + filter, e);
		} catch (ExecutionException e) {
			throw new FeedEntryFilterException("Exception while evaluating expression " + filter, e);
		} catch (TimeoutException e) {
			throw new FeedEntryFilterException("Took too long evaluating expression " + filter, e);
		}

		return Boolean.TRUE.equals(result);
	}

	private Object evaluateCelExpression(String expression, Map<String, Object> data)
			throws CelValidationException, CelEvaluationException {
		CelAbstractSyntaxTree ast = CEL_COMPILER.compile(expression).getAst();
		CelRuntime.Program program = CEL_RUNTIME.createProgram(ast);
		return program.eval(data);
	}

	@SuppressWarnings("serial")
	public static class FeedEntryFilterException extends Exception {
		public FeedEntryFilterException(String message, Throwable t) {
			super(message, t);
		}
	}
}
