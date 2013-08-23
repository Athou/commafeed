var module = angular.module('commafeed.filters', []);

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

module.filter('escape', function() {
	return encodeURIComponent;
});

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
			for ( var i = 0; i < tokens.length; i++) {
				html = handleKeyword(tokens[i], html);
			}
		}
		return html;
	};
});