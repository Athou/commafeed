package com.commafeed;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(
		targets = {
				// metrics
				MetricRegistry.class, Meter.class, Gauge.class, Counter.class, Timer.class, Histogram.class,

				// rome
				com.rometools.rome.feed.module.DCModuleImpl.class, com.rometools.rome.feed.module.DCSubjectImpl.class,
				com.rometools.modules.content.ContentModuleImpl.class, com.rometools.modules.mediarss.MediaModuleImpl.class,
				com.rometools.modules.mediarss.MediaEntryModuleImpl.class,

				// extracted from all 3 rome.properties files of rome library
				com.rometools.rome.io.impl.RSS090Parser.class, com.rometools.rome.io.impl.RSS091NetscapeParser.class,
				com.rometools.rome.io.impl.RSS091UserlandParser.class, com.rometools.rome.io.impl.RSS092Parser.class,
				com.rometools.rome.io.impl.RSS093Parser.class, com.rometools.rome.io.impl.RSS094Parser.class,
				com.rometools.rome.io.impl.RSS10Parser.class, com.rometools.rome.io.impl.RSS20wNSParser.class,
				com.rometools.rome.io.impl.RSS20Parser.class, com.rometools.rome.io.impl.Atom10Parser.class,
				com.rometools.rome.io.impl.Atom03Parser.class,

				com.rometools.rome.io.impl.SyModuleParser.class, com.rometools.rome.io.impl.DCModuleParser.class,

				com.rometools.rome.io.impl.RSS090Generator.class, com.rometools.rome.io.impl.RSS091NetscapeGenerator.class,
				com.rometools.rome.io.impl.RSS091UserlandGenerator.class, com.rometools.rome.io.impl.RSS092Generator.class,
				com.rometools.rome.io.impl.RSS093Generator.class, com.rometools.rome.io.impl.RSS094Generator.class,
				com.rometools.rome.io.impl.RSS10Generator.class, com.rometools.rome.io.impl.RSS20Generator.class,
				com.rometools.rome.io.impl.Atom10Generator.class, com.rometools.rome.io.impl.Atom03Generator.class,

				com.rometools.rome.feed.synd.impl.ConverterForAtom10.class, com.rometools.rome.feed.synd.impl.ConverterForAtom03.class,
				com.rometools.rome.feed.synd.impl.ConverterForRSS090.class,
				com.rometools.rome.feed.synd.impl.ConverterForRSS091Netscape.class,
				com.rometools.rome.feed.synd.impl.ConverterForRSS091Userland.class,
				com.rometools.rome.feed.synd.impl.ConverterForRSS092.class, com.rometools.rome.feed.synd.impl.ConverterForRSS093.class,
				com.rometools.rome.feed.synd.impl.ConverterForRSS094.class, com.rometools.rome.feed.synd.impl.ConverterForRSS10.class,
				com.rometools.rome.feed.synd.impl.ConverterForRSS20.class,

				com.rometools.modules.mediarss.io.RSS20YahooParser.class,

				com.rometools.modules.cc.io.ModuleParserRSS2.class, com.rometools.modules.content.io.ContentModuleParser.class,
				com.rometools.modules.itunes.io.ITunesParser.class, com.rometools.modules.mediarss.io.MediaModuleParser.class,
				com.rometools.modules.opensearch.impl.OpenSearchModuleParser.class, com.rometools.modules.georss.SimpleParser.class,
				com.rometools.modules.georss.W3CGeoParser.class, com.rometools.modules.photocast.io.Parser.class,
				com.rometools.modules.mediarss.io.MediaModuleParser.class, com.rometools.modules.atom.io.AtomModuleParser.class,
				com.rometools.modules.itunes.io.ITunesParserOldNamespace.class,
				com.rometools.modules.mediarss.io.AlternateMediaModuleParser.class, com.rometools.modules.sle.io.ModuleParser.class,
				com.rometools.modules.yahooweather.io.WeatherModuleParser.class, com.rometools.modules.feedpress.io.FeedpressParser.class,
				com.rometools.modules.fyyd.io.FyydParser.class,

				com.rometools.modules.cc.io.ModuleParserRSS2.class, com.rometools.modules.content.io.ContentModuleParser.class,
				com.rometools.modules.itunes.io.ITunesParser.class, com.rometools.modules.mediarss.io.MediaModuleParser.class,
				com.rometools.modules.opensearch.impl.OpenSearchModuleParser.class, com.rometools.modules.georss.SimpleParser.class,
				com.rometools.modules.georss.W3CGeoParser.class, com.rometools.modules.photocast.io.Parser.class,
				com.rometools.modules.mediarss.io.MediaModuleParser.class, com.rometools.modules.atom.io.AtomModuleParser.class,
				com.rometools.modules.itunes.io.ITunesParserOldNamespace.class,
				com.rometools.modules.mediarss.io.AlternateMediaModuleParser.class, com.rometools.modules.sle.io.ModuleParser.class,
				com.rometools.modules.yahooweather.io.WeatherModuleParser.class, com.rometools.modules.feedpress.io.FeedpressParser.class,
				com.rometools.modules.fyyd.io.FyydParser.class,

				com.rometools.modules.cc.io.ModuleParserRSS1.class, com.rometools.modules.content.io.ContentModuleParser.class,

				com.rometools.modules.cc.io.ModuleParserRSS2.class, com.rometools.modules.opensearch.impl.OpenSearchModuleParser.class,
				com.rometools.modules.georss.SimpleParser.class, com.rometools.modules.georss.W3CGeoParser.class,
				com.rometools.modules.photocast.io.Parser.class, com.rometools.modules.mediarss.io.MediaModuleParser.class,
				com.rometools.modules.mediarss.io.AlternateMediaModuleParser.class,

				com.rometools.modules.cc.io.ModuleParserRSS2.class, com.rometools.modules.opensearch.impl.OpenSearchModuleParser.class,
				com.rometools.modules.georss.SimpleParser.class, com.rometools.modules.georss.W3CGeoParser.class,
				com.rometools.modules.photocast.io.Parser.class, com.rometools.modules.mediarss.io.MediaModuleParser.class,
				com.rometools.modules.mediarss.io.AlternateMediaModuleParser.class,
				com.rometools.modules.feedpress.io.FeedpressParser.class, com.rometools.modules.fyyd.io.FyydParser.class,

				com.rometools.modules.cc.io.ModuleParserRSS2.class, com.rometools.modules.base.io.GoogleBaseParser.class,
				com.rometools.modules.content.io.ContentModuleParser.class, com.rometools.modules.slash.io.SlashModuleParser.class,
				com.rometools.modules.itunes.io.ITunesParser.class, com.rometools.modules.mediarss.io.MediaModuleParser.class,
				com.rometools.modules.atom.io.AtomModuleParser.class, com.rometools.modules.opensearch.impl.OpenSearchModuleParser.class,
				com.rometools.modules.georss.SimpleParser.class, com.rometools.modules.georss.W3CGeoParser.class,
				com.rometools.modules.photocast.io.Parser.class, com.rometools.modules.itunes.io.ITunesParserOldNamespace.class,
				com.rometools.modules.mediarss.io.AlternateMediaModuleParser.class, com.rometools.modules.sle.io.ItemParser.class,
				com.rometools.modules.yahooweather.io.WeatherModuleParser.class,
				com.rometools.modules.psc.io.PodloveSimpleChapterParser.class,

				com.rometools.modules.cc.io.ModuleParserRSS1.class, com.rometools.modules.base.io.GoogleBaseParser.class,
				com.rometools.modules.base.io.CustomTagParser.class, com.rometools.modules.content.io.ContentModuleParser.class,
				com.rometools.modules.slash.io.SlashModuleParser.class,

				com.rometools.modules.cc.io.ModuleParserRSS2.class, com.rometools.modules.base.io.GoogleBaseParser.class,
				com.rometools.modules.base.io.CustomTagParser.class, com.rometools.modules.slash.io.SlashModuleParser.class,
				com.rometools.modules.opensearch.impl.OpenSearchModuleParser.class, com.rometools.modules.georss.SimpleParser.class,
				com.rometools.modules.georss.W3CGeoParser.class, com.rometools.modules.photocast.io.Parser.class,
				com.rometools.modules.mediarss.io.MediaModuleParser.class,
				com.rometools.modules.mediarss.io.AlternateMediaModuleParser.class,

				com.rometools.modules.cc.io.ModuleParserRSS2.class, com.rometools.modules.base.io.GoogleBaseParser.class,
				com.rometools.modules.base.io.CustomTagParser.class, com.rometools.modules.slash.io.SlashModuleParser.class,
				com.rometools.modules.opensearch.impl.OpenSearchModuleParser.class, com.rometools.modules.georss.SimpleParser.class,
				com.rometools.modules.georss.W3CGeoParser.class, com.rometools.modules.photocast.io.Parser.class,
				com.rometools.modules.mediarss.io.MediaModuleParser.class,
				com.rometools.modules.mediarss.io.AlternateMediaModuleParser.class,
				com.rometools.modules.thr.io.ThreadingModuleParser.class, com.rometools.modules.psc.io.PodloveSimpleChapterParser.class,

				com.rometools.modules.cc.io.CCModuleGenerator.class, com.rometools.modules.content.io.ContentModuleGenerator.class,
				com.rometools.modules.itunes.io.ITunesGenerator.class,
				com.rometools.modules.opensearch.impl.OpenSearchModuleGenerator.class, com.rometools.modules.georss.SimpleGenerator.class,
				com.rometools.modules.georss.W3CGeoGenerator.class, com.rometools.modules.photocast.io.Generator.class,
				com.rometools.modules.mediarss.io.MediaModuleGenerator.class, com.rometools.modules.atom.io.AtomModuleGenerator.class,
				com.rometools.modules.sle.io.ModuleGenerator.class, com.rometools.modules.yahooweather.io.WeatherModuleGenerator.class,
				com.rometools.modules.feedpress.io.FeedpressGenerator.class, com.rometools.modules.fyyd.io.FyydGenerator.class,

				com.rometools.modules.content.io.ContentModuleGenerator.class,

				com.rometools.modules.cc.io.CCModuleGenerator.class, com.rometools.modules.opensearch.impl.OpenSearchModuleGenerator.class,
				com.rometools.modules.georss.SimpleGenerator.class, com.rometools.modules.georss.W3CGeoGenerator.class,
				com.rometools.modules.photocast.io.Generator.class, com.rometools.modules.mediarss.io.MediaModuleGenerator.class,

				com.rometools.modules.cc.io.CCModuleGenerator.class, com.rometools.modules.opensearch.impl.OpenSearchModuleGenerator.class,
				com.rometools.modules.georss.SimpleGenerator.class, com.rometools.modules.georss.W3CGeoGenerator.class,
				com.rometools.modules.photocast.io.Generator.class, com.rometools.modules.mediarss.io.MediaModuleGenerator.class,
				com.rometools.modules.feedpress.io.FeedpressGenerator.class, com.rometools.modules.fyyd.io.FyydGenerator.class,

				com.rometools.modules.cc.io.CCModuleGenerator.class, com.rometools.modules.base.io.GoogleBaseGenerator.class,
				com.rometools.modules.base.io.CustomTagGenerator.class, com.rometools.modules.content.io.ContentModuleGenerator.class,
				com.rometools.modules.slash.io.SlashModuleGenerator.class, com.rometools.modules.itunes.io.ITunesGenerator.class,
				com.rometools.modules.opensearch.impl.OpenSearchModuleGenerator.class, com.rometools.modules.georss.SimpleGenerator.class,
				com.rometools.modules.georss.W3CGeoGenerator.class, com.rometools.modules.photocast.io.Generator.class,
				com.rometools.modules.mediarss.io.MediaModuleGenerator.class, com.rometools.modules.atom.io.AtomModuleGenerator.class,
				com.rometools.modules.yahooweather.io.WeatherModuleGenerator.class,
				com.rometools.modules.psc.io.PodloveSimpleChapterGenerator.class,

				com.rometools.modules.base.io.GoogleBaseGenerator.class, com.rometools.modules.content.io.ContentModuleGenerator.class,
				com.rometools.modules.slash.io.SlashModuleGenerator.class,

				com.rometools.modules.cc.io.CCModuleGenerator.class, com.rometools.modules.base.io.GoogleBaseGenerator.class,
				com.rometools.modules.base.io.CustomTagGenerator.class, com.rometools.modules.slash.io.SlashModuleGenerator.class,
				com.rometools.modules.opensearch.impl.OpenSearchModuleGenerator.class, com.rometools.modules.georss.SimpleGenerator.class,
				com.rometools.modules.georss.W3CGeoGenerator.class, com.rometools.modules.photocast.io.Generator.class,
				com.rometools.modules.mediarss.io.MediaModuleGenerator.class,

				com.rometools.modules.cc.io.CCModuleGenerator.class, com.rometools.modules.base.io.CustomTagGenerator.class,
				com.rometools.modules.slash.io.SlashModuleGenerator.class,
				com.rometools.modules.opensearch.impl.OpenSearchModuleGenerator.class, com.rometools.modules.georss.SimpleGenerator.class,
				com.rometools.modules.georss.W3CGeoGenerator.class, com.rometools.modules.photocast.io.Generator.class,
				com.rometools.modules.mediarss.io.MediaModuleGenerator.class, com.rometools.modules.thr.io.ThreadingModuleGenerator.class,
				com.rometools.modules.psc.io.PodloveSimpleChapterGenerator.class,

				com.rometools.modules.mediarss.io.MediaModuleParser.class,

				com.rometools.modules.mediarss.io.MediaModuleGenerator.class,

				com.rometools.opml.io.impl.OPML10Generator.class, com.rometools.opml.io.impl.OPML20Generator.class,

				com.rometools.opml.io.impl.OPML10Parser.class, com.rometools.opml.io.impl.OPML20Parser.class,

				com.rometools.opml.feed.synd.impl.ConverterForOPML10.class, com.rometools.opml.feed.synd.impl.ConverterForOPML20.class, })

public class NativeImageClasses {
}
