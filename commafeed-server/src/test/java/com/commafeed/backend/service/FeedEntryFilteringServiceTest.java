package com.commafeed.backend.service;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.service.FeedEntryFilteringService.FeedEntryFilterException;

class FeedEntryFilteringServiceTest {
	private CommaFeedConfiguration config;

	private FeedEntryFilteringService service;

	private FeedEntry entry;

	@BeforeEach
	void init() {
		config = Mockito.mock(CommaFeedConfiguration.class, Mockito.RETURNS_DEEP_STUBS);
		Mockito.when(config.feedRefresh().filteringExpressionEvaluationTimeout()).thenReturn(Duration.ofSeconds(30));

		service = new FeedEntryFilteringService(config);

		entry = new FeedEntry();
		entry.setUrl("https://github.com/Athou/commafeed");

		FeedEntryContent content = new FeedEntryContent();
		content.setAuthor("Athou");
		content.setTitle("Merge pull request #662 from Athou/dw8");
		content.setContent("Merge pull request #662 from Athou/dw8");
		entry.setContent(content);

	}

	@Test
	void emptyFilterMatchesFilter() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry(null, entry));
	}

	@Test
	void blankFilterMatchesFilter() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("", entry));
	}

	@Test
	void simpleEqualsExpression() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("author == \"Athou\"", entry));
	}

	@Test
	void simpleNotEqualsExpression() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("author != \"other\"", entry));
	}

	@Test
	void containsExpression() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("author.contains(\"Athou\")", entry));
	}

	@Test
	void titleContainsExpression() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("title.contains(\"Merge\")", entry));
	}

	@Test
	void urlContainsExpression() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("url.contains(\"github\")", entry));
	}

	@Test
	void andExpression() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("author == \"Athou\" && url.contains(\"github\")", entry));
	}

	@Test
	void orExpression() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("author == \"other\" || url.contains(\"github\")", entry));
	}

	@Test
	void notExpression() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("!(author == \"other\")", entry));
	}

	@Test
	void incorrectExpressionThrowsException() {
		Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("not valid cel", entry));
	}

	@Test
	void falseValueReturnsFalse() throws FeedEntryFilterException {
		Assertions.assertFalse(service.filterMatchesEntry("false", entry));
	}

	@Test
	void trueValueReturnsTrue() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("true", entry));
	}

	@Test
	void startsWithExpression() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("title.startsWith(\"Merge\")", entry));
	}

	@Test
	void endsWithExpression() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("url.endsWith(\"commafeed\")", entry));
	}

	@Test
	void categoriesContainsExpression() throws FeedEntryFilterException {
		FeedEntryContent content = entry.getContent();
		content.setCategories("tech, programming, java");
		entry.setContent(content);
		Assertions.assertTrue(service.filterMatchesEntry("categories.contains(\"programming\")", entry));
	}

	@Test
	void caseInsensitiveAuthorMatchUsingLowerVariable() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("authorLower == \"athou\"", entry));
	}

	@Test
	void caseInsensitiveTitleMatchUsingLowerVariable() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("titleLower.contains(\"merge\")", entry));
	}

	@Test
	void caseInsensitiveUrlMatchUsingLowerVariable() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("urlLower.contains(\"github\")", entry));
	}

	@Test
	void caseInsensitiveContentMatchUsingLowerVariable() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("contentLower.contains(\"merge\")", entry));
	}

	@Test
	void caseInsensitiveCategoriesMatchUsingLowerVariable() throws FeedEntryFilterException {
		FeedEntryContent content = entry.getContent();
		content.setCategories("Tech, Programming, Java");
		entry.setContent(content);
		Assertions.assertTrue(service.filterMatchesEntry("categoriesLower.contains(\"tech\")", entry));
	}

	@Nested
	class Sandbox {

		@Test
		void sandboxBlocksSystemPropertyAccess() {
			Assertions.assertThrows(FeedEntryFilterException.class,
					() -> service.filterMatchesEntry("java.lang.System.getProperty(\"user.home\")", entry));
		}

		@Test
		void sandboxBlocksRuntimeExec() {
			Assertions.assertThrows(FeedEntryFilterException.class,
					() -> service.filterMatchesEntry("java.lang.Runtime.getRuntime().exec(\"calc\")", entry));
		}

		@Test
		void sandboxBlocksProcessBuilder() {
			Assertions.assertThrows(FeedEntryFilterException.class,
					() -> service.filterMatchesEntry("new java.lang.ProcessBuilder(\"cmd\").start()", entry));
		}

		@Test
		void sandboxBlocksClassLoading() {
			Assertions.assertThrows(FeedEntryFilterException.class,
					() -> service.filterMatchesEntry("java.lang.Class.forName(\"java.lang.Runtime\")", entry));
		}

		@Test
		void sandboxBlocksReflection() {
			Assertions.assertThrows(FeedEntryFilterException.class,
					() -> service.filterMatchesEntry("title.getClass().getMethods()", entry));
		}

		@Test
		void sandboxBlocksFileAccess() {
			Assertions.assertThrows(FeedEntryFilterException.class,
					() -> service.filterMatchesEntry("new java.io.File(\"/etc/passwd\").exists()", entry));
		}

		@Test
		void sandboxBlocksFileRead() {
			Assertions.assertThrows(FeedEntryFilterException.class,
					() -> service.filterMatchesEntry("java.nio.file.Files.readString(java.nio.file.Paths.get(\"/etc/passwd\"))", entry));
		}

		@Test
		void sandboxBlocksNetworkAccess() {
			Assertions.assertThrows(FeedEntryFilterException.class,
					() -> service.filterMatchesEntry("new java.net.URL(\"http://evil.com\").openConnection()", entry));
		}

		@Test
		void sandboxBlocksScriptEngine() {
			Assertions.assertThrows(FeedEntryFilterException.class, () -> service
					.filterMatchesEntry("new javax.script.ScriptEngineManager().getEngineByName(\"js\").eval(\"1+1\")", entry));
		}

		@Test
		void sandboxBlocksThreadCreation() {
			Assertions.assertThrows(FeedEntryFilterException.class,
					() -> service.filterMatchesEntry("new java.lang.Thread().start()", entry));
		}

		@Test
		void sandboxBlocksEnvironmentVariableAccess() {
			Assertions.assertThrows(FeedEntryFilterException.class,
					() -> service.filterMatchesEntry("java.lang.System.getenv(\"PATH\")", entry));
		}

		@Test
		void sandboxBlocksExitCall() {
			Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("java.lang.System.exit(0)", entry));
		}

		@Test
		void sandboxBlocksUndeclaredVariables() {
			Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("unknownVariable == \"test\"", entry));
		}

		@Test
		void sandboxBlocksMethodInvocationOnStrings() {
			Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("title.toCharArray()", entry));
		}

		@Test
		void sandboxBlocksArbitraryJavaMethodCalls() {
			Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("title.getBytes()", entry));
		}

		@Test
		void sandboxOnlyAllowsDeclaredVariables() {
			Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("System", entry));
		}

		@Test
		void sandboxBlocksConstructorCalls() {
			Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("new String(\"test\")", entry));
		}

		@Test
		void sandboxBlocksStaticMethodCalls() {
			Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("String.valueOf(123)", entry));
		}

		@Test
		void sandboxBlocksLambdaExpressions() {
			Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("() -> true", entry));
		}

		@Test
		void sandboxBlocksObjectInstantiation() {
			Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("java.util.HashMap{}", entry));
		}
	}

}
