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