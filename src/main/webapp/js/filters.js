var module = angular.module('commafeed.filters', []);

module.filter('entryDate', function() {
	return function(timestamp) {
		var d = moment(timestamp);
		var now = moment();
		var formatted;
		if (d.date() === now.date() && Math.abs(d.diff(now)) < 86400000) {
			formatted = d.fromNow();
	    } else {
	    	formatted = d.format('YYYY-MM-DD HH:mm');
	    }
	    return formatted;
	};
});