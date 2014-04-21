var module = angular.module('commafeed.filters', []);

/**
 * smart date formatter
 */
module.filter('entryDate', function() {
	return function(timestamp, defaultValue) {
		if (!timestamp) {
			return defaultValue;
		}

		var d = moment(timestamp);
		var now = moment();
		var formatted;
		if (Math.abs(d.diff(now)) < 86400000) {
			formatted = d.fromNow();
		} else {
			formatted = d.format('YYYY-MM-DD HH:mm');
		}
		return formatted;
	};
});

/**
 * rewrites iframes to use https if commafeed uses https
 */
module.filter('iframeHttpsRewrite', function() {
	return function(html) {
		var result = html;
		if (location.protocol === 'https:') {
			var wrapper = $('<div></div>').html(html);
			$('iframe', wrapper).each(function(i, elem) {
				var e = $(elem);
				e.attr('src', e.attr('src').replace(/^http:\/\//i, 'https://'));
			});
			result = wrapper.html();
		}
		return result;
	};
});

/**
 * inserts title or alt-text after images, if any
 */
module.filter('appendImageTitles', function() {
	return function(html) {
		var result = html;
		var wrapper = $('<div></div>').html(html);
		$('img', wrapper).each(function(i, elem) {
			var e = $(elem);
			var title = e.attr('title') || e.attr('alt');
			if (title) {
				var text = $('<span style="font-style: italic;"></span>').text(title);
				e.after(text);
			}
		});
		result = wrapper.html();
		return result;
	};
});

/**
 * escapes the url
 */
module.filter('escape', function() {
	return encodeURIComponent;
});

/**
 * returns a trusted html content
 */
module.filter('trustHtml', ['$sce', function($sce) {
	return function(val) {
		return $sce.trustAsHtml(val);
	};
}]);

/**
 * returns a trusted url
 */
module.filter('trustUrl', ['$sce', function($sce) {
	return function(val) {
		return $sce.trustAsResourceUrl(val);
	};
}]);

/**
 * add the 'highlight-search' class to text matching keywords
 */
module.filter('highlight', function() {
	return function(html, keywords) {
		if (keywords) {
			var handleKeyword = function(token, html) {
				var expr = new RegExp(token, 'gi');
				var container = $('<span>').html(html);
				var elements = container.find('*').addBack();
				var textNodes = elements.not('iframe').contents().not(elements);
				textNodes.each(function() {
					var replaced = this.nodeValue.replace(expr, '<span class="highlight-search">$&</span>');
					$('<span>').html(replaced).insertBefore(this);
					$(this).remove();
				});
				return container.html();
			};

			var tokens = keywords.split(' ');
			for (var i = 0; i < tokens.length; i++) {
				html = handleKeyword(tokens[i], html);
			}
		}
		return html;
	};
});