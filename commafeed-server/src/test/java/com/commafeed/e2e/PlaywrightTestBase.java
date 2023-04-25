package com.commafeed.e2e;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;

/**
 * Base class for all Playwright tests.
 * 
 * <ul>
 * <li>Takes a screenshot on failure</li>
 * <li>Keeps the video on failure</li>
 * <li>Saves a trace file on failure</li>
 * </ul>
 * 
 * inspired by https://github.com/microsoft/playwright-java/issues/503#issuecomment-872636373
 * 
 */
@ExtendWith(PlaywrightTestBase.SaveArtifactsOnTestFailed.class)
public class PlaywrightTestBase {

	private static Playwright playwright;
	private static Browser browser;

	protected Page page;
	private BrowserContext context;

	@BeforeAll
	static void initBrowser() {
		playwright = Playwright.create();
		browser = playwright.chromium().launch();
	}

	@AfterAll
	static void closeBrowser() {
		playwright.close();
	}

	protected void customizeNewContextOptions(NewContextOptions options) {
	}

	protected static class SaveArtifactsOnTestFailed implements TestWatcher, BeforeEachCallback {

		// defined in the config of maven-failsafe-plugin in pom.xml
		private final String buildDirectory = System.getProperty("buildDirectory", "target");
		private final String directory = buildDirectory + "/playwright-artifacts";

		@Override
		public void beforeEach(ExtensionContext context) throws Exception {
			PlaywrightTestBase testInstance = getTestInstance(context);

			NewContextOptions newContextOptions = new Browser.NewContextOptions().setRecordVideoDir(Paths.get(directory));
			testInstance.customizeNewContextOptions(newContextOptions);
			testInstance.context = PlaywrightTestBase.browser.newContext(newContextOptions);
			testInstance.context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));

			testInstance.page = testInstance.context.newPage();
		}

		@Override
		public void testFailed(ExtensionContext context, Throwable cause) {
			PlaywrightTestBase testInstance = getTestInstance(context);

			String fileName = getFileName(context);

			saveScreenshot(testInstance, fileName);
			saveTrace(testInstance, fileName);

			testInstance.context.close();

			saveVideo(testInstance, fileName);
		}

		@Override
		public void testAborted(ExtensionContext context, Throwable cause) {
			PlaywrightTestBase testInstance = getTestInstance(context);
			testInstance.context.close();
			testInstance.page.video().delete();
		}

		@Override
		public void testDisabled(ExtensionContext context, Optional<String> reason) {
			PlaywrightTestBase testInstance = getTestInstance(context);
			testInstance.context.close();
			testInstance.page.video().delete();
		}

		@Override
		public void testSuccessful(ExtensionContext context) {
			PlaywrightTestBase testInstance = getTestInstance(context);
			testInstance.context.close();
			testInstance.page.video().delete();
		}

		private PlaywrightTestBase getTestInstance(ExtensionContext context) {
			return (PlaywrightTestBase) context.getRequiredTestInstance();
		}

		private String getFileName(ExtensionContext context) {
			return String.format("%s.%s-%s", context.getRequiredTestClass().getSimpleName(), context.getRequiredTestMethod().getName(),
					new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss").format(new Date()));
		}

		private void saveScreenshot(PlaywrightTestBase testInstance, String fileName) {
			byte[] screenshot = testInstance.page.screenshot();
			try {
				Files.write(Paths.get(directory, fileName + ".png"), screenshot);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private void saveTrace(PlaywrightTestBase testInstance, String fileName) {
			testInstance.context.tracing().stop(new Tracing.StopOptions().setPath(Paths.get(directory, fileName + ".zip")));
		}

		private void saveVideo(PlaywrightTestBase testInstance, String fileName) {
			testInstance.page.video().saveAs(Paths.get(directory, fileName + ".webm"));
			testInstance.page.video().delete();
		}
	}
}
