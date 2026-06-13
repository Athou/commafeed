package com.commafeed.backend.feed.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class XMLCleanerTest {

	XMLCleaner xmlCleaner = new XMLCleaner();

	@Nested
	class RemoveCharactersBeforeFirstXmlTag {
		@Test
		void removesWhitespaceBeforeXmlTag() {
			String xml = "   \n\t<feed>content</feed>";
			Assertions.assertEquals("<feed>content</feed>", xmlCleaner.removeCharactersBeforeFirstXmlTag(xml));
		}

		@Test
		void removesTextBeforeXmlTag() {
			String xml = "some text here<feed>content</feed>";
			Assertions.assertEquals("<feed>content</feed>", xmlCleaner.removeCharactersBeforeFirstXmlTag(xml));
		}

		@Test
		void returnsUnchangedWhenStartsWithXmlTag() {
			String xml = "<feed>content</feed>";
			Assertions.assertEquals("<feed>content</feed>", xmlCleaner.removeCharactersBeforeFirstXmlTag(xml));
		}

		@Test
		void returnsNullWhenNoXmlTagFound() {
			String xml = "no xml tags here";
			Assertions.assertNull(xmlCleaner.removeCharactersBeforeFirstXmlTag(xml));
		}

		@Test
		void returnsNullWhenInputIsNull() {
			Assertions.assertNull(xmlCleaner.removeCharactersBeforeFirstXmlTag(null));
		}

		@Test
		void returnsNullWhenInputIsEmpty() {
			Assertions.assertNull(xmlCleaner.removeCharactersBeforeFirstXmlTag(""));
		}

		@Test
		void returnsNullWhenInputIsBlank() {
			Assertions.assertNull(xmlCleaner.removeCharactersBeforeFirstXmlTag("   \n\t  "));
		}

		@Test
		void preservesMultipleXmlTags() {
			String xml = "garbage<feed><item>content</item></feed>";
			Assertions.assertEquals("<feed><item>content</item></feed>", xmlCleaner.removeCharactersBeforeFirstXmlTag(xml));
		}
	}

	@Nested
	class RemoveInvalidXmlCharacters {
		@Test
		void removesNullCharacter() {
			String xml = "<feed>content\u0000here</feed>";
			Assertions.assertEquals("<feed>contenthere</feed>", xmlCleaner.removeInvalidXmlCharacters(xml));
		}

		@Test
		void removesInvalidControlCharacters() {
			String xml = "<feed>content\u0001\u0002\u0003here</feed>";
			Assertions.assertEquals("<feed>contenthere</feed>", xmlCleaner.removeInvalidXmlCharacters(xml));
		}

		@Test
		void preservesValidXmlCharacters() {
			String xml = "<feed>content with\ttab\nand newline</feed>";
			Assertions.assertEquals("<feed>content with\ttab\nand newline</feed>", xmlCleaner.removeInvalidXmlCharacters(xml));
		}

		@Test
		void preservesUnicodeCharacters() {
			String xml = "<feed>café résumé 中文 العربية</feed>";
			Assertions.assertEquals("<feed>café résumé 中文 العربية</feed>", xmlCleaner.removeInvalidXmlCharacters(xml));
		}

		@Test
		void preservesEmojiCharacters() {
			String xml = "<feed>🎮💪✅</feed>";
			Assertions.assertEquals("<feed>🎮💪✅</feed>", xmlCleaner.removeInvalidXmlCharacters(xml));
		}

		@Test
		void removesMultipleInvalidCharacters() {
			String xml = "test\u0000test\u0001test\u0002test";
			Assertions.assertEquals("testtesttesttest", xmlCleaner.removeInvalidXmlCharacters(xml));
		}

		@Test
		void returnsNullWhenInputIsNull() {
			Assertions.assertNull(xmlCleaner.removeInvalidXmlCharacters(null));
		}

		@Test
		void returnsNullWhenInputIsEmpty() {
			Assertions.assertNull(xmlCleaner.removeInvalidXmlCharacters(""));
		}

		@Test
		void returnsNullWhenInputIsBlank() {
			Assertions.assertNull(xmlCleaner.removeInvalidXmlCharacters("   "));
		}

		@Test
		void handlesStringWithOnlyInvalidCharacters() {
			String xml = "\u0000\u0001\u0002";
			Assertions.assertEquals("", xmlCleaner.removeInvalidXmlCharacters(xml));
		}
	}

	@Nested
	class Entities {
		@Test
		void testReplaceHtmlEntitiesWithNumericEntities() {
			String source = "<source>T&acute;l&acute;phone &prime;</source>";
			Assertions.assertEquals("<source>T&#180;l&#180;phone &#8242;</source>",
					xmlCleaner.replaceHtmlEntitiesWithNumericEntities(source));
		}

		@Test
		void replacesMultipleOccurrencesOfSameEntity() {
			String source = "&nbsp;&nbsp;&nbsp;";
			Assertions.assertEquals("&#160;&#160;&#160;", xmlCleaner.replaceHtmlEntitiesWithNumericEntities(source));
		}

		@Test
		void preservesTextWithoutEntities() {
			String source = "<feed>regular content</feed>";
			Assertions.assertEquals("<feed>regular content</feed>", xmlCleaner.replaceHtmlEntitiesWithNumericEntities(source));
		}

		@Test
		void preservesNumericEntities() {
			String source = "&#180;&#8242;";
			Assertions.assertEquals("&#180;&#8242;", xmlCleaner.replaceHtmlEntitiesWithNumericEntities(source));
		}

		@Test
		void replacesCommonHtmlEntities() {
			String source = "&amp;&quot;";
			Assertions.assertEquals("&#38;&#34;", xmlCleaner.replaceHtmlEntitiesWithNumericEntities(source));
		}

		@Test
		void handlesPartialEntityMatches() {
			String source = "&amplifier";
			String result = xmlCleaner.replaceHtmlEntitiesWithNumericEntities(source);
			Assertions.assertTrue(result.startsWith("&#38;") || result.equals("&amplifier"));
		}

		@Test
		void returnsNullWhenInputIsNull() {
			Assertions.assertNull(xmlCleaner.replaceHtmlEntitiesWithNumericEntities(null));
		}

		@Test
		void returnsNullWhenInputIsEmpty() {
			Assertions.assertNull(xmlCleaner.replaceHtmlEntitiesWithNumericEntities(""));
		}

		@Test
		void returnsNullWhenInputIsBlank() {
			Assertions.assertNull(xmlCleaner.replaceHtmlEntitiesWithNumericEntities("   "));
		}

		@Test
		void handlesEntityAtStartOfString() {
			String source = "&amp;test";
			Assertions.assertEquals("&#38;test", xmlCleaner.replaceHtmlEntitiesWithNumericEntities(source));
		}

		@Test
		void handlesEntityAtEndOfString() {
			String source = "test&amp;";
			Assertions.assertEquals("test&#38;", xmlCleaner.replaceHtmlEntitiesWithNumericEntities(source));
		}

		@Test
		void handlesMixedEntitiesAndText() {
			String source = "Hello&nbsp;World&excl;&nbsp;Test&period;";
			String result = xmlCleaner.replaceHtmlEntitiesWithNumericEntities(source);
			Assertions.assertTrue(result.contains("&#"));
		}
	}

	@Nested
	class Doctype {
		@Test
		void testRemoveDoctype() {
			String source = "<!DOCTYPE html><html><head></head><body></body></html>";
			Assertions.assertEquals("<html><head></head><body></body></html>", xmlCleaner.removeDoctypeDeclarations(source));
		}

		@Test
		void testRemoveMultilineDoctype() {
			String source = """
					<!DOCTYPE
						html
					>
					<html><head></head><body></body></html>""";
			Assertions.assertEquals("""

					<html><head></head><body></body></html>""", xmlCleaner.removeDoctypeDeclarations(source));
		}

		@Test
		void removesComplexDoctypeWithSystemId() {
			String source = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><html><body></body></html>";
			Assertions.assertEquals("<html><body></body></html>", xmlCleaner.removeDoctypeDeclarations(source));
		}

		@Test
		void removesComplexDoctypeWithPublicId() {
			String source = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html></html>";
			Assertions.assertEquals("<html></html>", xmlCleaner.removeDoctypeDeclarations(source));
		}

		@Test
		void removesCaseInsensitiveDoctype() {
			String source = "<!doctype html><html></html>";
			Assertions.assertEquals("<html></html>", xmlCleaner.removeDoctypeDeclarations(source));
		}

		@Test
		void removesMixedCaseDoctype() {
			String source = "<!DoCtYpE html><html></html>";
			Assertions.assertEquals("<html></html>", xmlCleaner.removeDoctypeDeclarations(source));
		}

		@Test
		void removesMultipleDoctypeDeclarations() {
			String source = "<!DOCTYPE html><!DOCTYPE html><html></html>";
			Assertions.assertEquals("<html></html>", xmlCleaner.removeDoctypeDeclarations(source));
		}

		@Test
		void preservesContentWithoutDoctype() {
			String source = "<html><body>No doctype here</body></html>";
			Assertions.assertEquals("<html><body>No doctype here</body></html>", xmlCleaner.removeDoctypeDeclarations(source));
		}

		@Test
		void returnsNullWhenInputIsNull() {
			Assertions.assertNull(xmlCleaner.removeDoctypeDeclarations(null));
		}

		@Test
		void returnsNullWhenInputIsEmpty() {
			Assertions.assertNull(xmlCleaner.removeDoctypeDeclarations(""));
		}

		@Test
		void returnsNullWhenInputIsBlank() {
			Assertions.assertNull(xmlCleaner.removeDoctypeDeclarations("   "));
		}

		@Test
		void handlesDoctypeWithExtraWhitespace() {
			String source = "<!DOCTYPE   html   ><html></html>";
			Assertions.assertEquals("<html></html>", xmlCleaner.removeDoctypeDeclarations(source));
		}
	}
}
