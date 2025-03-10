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
				java.util.Date.class, com.rometools.opml.feed.synd.impl.TreeCategoryImpl.class,
				com.rometools.rome.feed.synd.SyndFeedImpl.class, com.rometools.rome.feed.module.DCSubjectImpl.class,
				com.rometools.rome.feed.synd.SyndEntryImpl.class, com.rometools.modules.psc.types.SimpleChapter.class,
				com.rometools.rome.feed.synd.SyndCategoryImpl.class, com.rometools.rome.feed.synd.SyndImageImpl.class,
				com.rometools.rome.feed.synd.SyndContentImpl.class, com.rometools.rome.feed.synd.SyndEnclosureImpl.class,

				// rome cloneable
				com.rometools.modules.activitystreams.types.Article.class, com.rometools.modules.activitystreams.types.Audio.class,
				com.rometools.modules.activitystreams.types.Bookmark.class, com.rometools.modules.activitystreams.types.Comment.class,
				com.rometools.modules.activitystreams.types.Event.class, com.rometools.modules.activitystreams.types.File.class,
				com.rometools.modules.activitystreams.types.Folder.class, com.rometools.modules.activitystreams.types.List.class,
				com.rometools.modules.activitystreams.types.Note.class, com.rometools.modules.activitystreams.types.Person.class,
				com.rometools.modules.activitystreams.types.Photo.class, com.rometools.modules.activitystreams.types.PhotoAlbum.class,
				com.rometools.modules.activitystreams.types.Place.class, com.rometools.modules.activitystreams.types.Playlist.class,
				com.rometools.modules.activitystreams.types.Product.class, com.rometools.modules.activitystreams.types.Review.class,
				com.rometools.modules.activitystreams.types.Service.class, com.rometools.modules.activitystreams.types.Song.class,
				com.rometools.modules.activitystreams.types.Status.class, com.rometools.modules.base.types.DateTimeRange.class,
				com.rometools.modules.base.types.FloatUnit.class, com.rometools.modules.base.types.GenderEnumeration.class,
				com.rometools.modules.base.types.IntUnit.class, com.rometools.modules.base.types.PriceTypeEnumeration.class,
				com.rometools.modules.base.types.ShippingType.class, com.rometools.modules.base.types.ShortDate.class,
				com.rometools.modules.base.types.Size.class, com.rometools.modules.base.types.YearType.class,
				com.rometools.modules.content.ContentItem.class, com.rometools.modules.georss.GeoRSSPoint.class,
				com.rometools.modules.georss.geometries.Envelope.class, com.rometools.modules.georss.geometries.LineString.class,
				com.rometools.modules.georss.geometries.LinearRing.class, com.rometools.modules.georss.geometries.Point.class,
				com.rometools.modules.georss.geometries.Polygon.class, com.rometools.modules.georss.geometries.Position.class,
				com.rometools.modules.georss.geometries.PositionList.class, com.rometools.modules.mediarss.types.MediaGroup.class,
				com.rometools.modules.mediarss.types.Metadata.class, com.rometools.modules.mediarss.types.Thumbnail.class,
				com.rometools.modules.opensearch.entity.OSQuery.class, com.rometools.modules.photocast.types.PhotoDate.class,
				com.rometools.modules.sle.types.DateValue.class, com.rometools.modules.sle.types.Group.class,
				com.rometools.modules.sle.types.NumberValue.class, com.rometools.modules.sle.types.Sort.class,
				com.rometools.modules.sle.types.StringValue.class, com.rometools.modules.yahooweather.types.Astronomy.class,
				com.rometools.modules.yahooweather.types.Atmosphere.class, com.rometools.modules.yahooweather.types.Condition.class,
				com.rometools.modules.yahooweather.types.Forecast.class, com.rometools.modules.yahooweather.types.Location.class,
				com.rometools.modules.yahooweather.types.Units.class, com.rometools.modules.yahooweather.types.Wind.class,
				com.rometools.opml.feed.opml.Attribute.class, com.rometools.opml.feed.opml.Opml.class,
				com.rometools.opml.feed.opml.Outline.class, com.rometools.rome.feed.atom.Category.class,
				com.rometools.rome.feed.atom.Content.class, com.rometools.rome.feed.atom.Entry.class,
				com.rometools.rome.feed.atom.Feed.class, com.rometools.rome.feed.atom.Generator.class,
				com.rometools.rome.feed.atom.Link.class, com.rometools.rome.feed.atom.Person.class,
				com.rometools.rome.feed.rss.Category.class, com.rometools.rome.feed.rss.Channel.class,
				com.rometools.rome.feed.rss.Cloud.class, com.rometools.rome.feed.rss.Content.class,
				com.rometools.rome.feed.rss.Description.class, com.rometools.rome.feed.rss.Enclosure.class,
				com.rometools.rome.feed.rss.Guid.class, com.rometools.rome.feed.rss.Image.class, com.rometools.rome.feed.rss.Item.class,
				com.rometools.rome.feed.rss.Source.class, com.rometools.rome.feed.rss.TextInput.class,
				com.rometools.rome.feed.synd.SyndLinkImpl.class, com.rometools.rome.feed.synd.SyndPersonImpl.class,
				java.util.ArrayList.class,

				// rome modules
				com.rometools.modules.sse.modules.Conflict.class, com.rometools.modules.sse.modules.Conflicts.class,
				com.rometools.modules.cc.CreativeCommonsImpl.class, com.rometools.modules.feedpress.modules.FeedpressModuleImpl.class,
				com.rometools.modules.opensearch.impl.OpenSearchModuleImpl.class, com.rometools.modules.sse.modules.Sharing.class,
				com.rometools.modules.georss.SimpleModuleImpl.class, com.rometools.modules.atom.modules.AtomLinkModuleImpl.class,
				com.rometools.modules.itunes.EntryInformationImpl.class, com.rometools.modules.sse.modules.Update.class,
				com.rometools.modules.photocast.PhotocastModuleImpl.class, com.rometools.modules.itunes.FeedInformationImpl.class,
				com.rometools.modules.yahooweather.YWeatherModuleImpl.class, com.rometools.modules.feedburner.FeedBurnerImpl.class,
				com.rometools.modules.sse.modules.Related.class, com.rometools.modules.fyyd.modules.FyydModuleImpl.class,
				com.rometools.modules.psc.modules.PodloveSimpleChapterModuleImpl.class, com.rometools.modules.thr.ThreadingModuleImpl.class,
				com.rometools.modules.sse.modules.Sync.class, com.rometools.modules.sle.SimpleListExtensionImpl.class,
				com.rometools.modules.slash.SlashImpl.class, com.rometools.modules.sse.modules.History.class,
				com.rometools.modules.georss.GMLModuleImpl.class, com.rometools.modules.base.CustomTagsImpl.class,
				com.rometools.modules.base.GoogleBaseImpl.class, com.rometools.modules.sle.SleEntryImpl.class,
				com.rometools.modules.mediarss.MediaEntryModuleImpl.class, com.rometools.modules.content.ContentModuleImpl.class,
				com.rometools.modules.georss.W3CGeoModuleImpl.class, com.rometools.rome.feed.module.DCModuleImpl.class,
				com.rometools.modules.mediarss.MediaModuleImpl.class, com.rometools.rome.feed.module.SyModuleImpl.class,

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
