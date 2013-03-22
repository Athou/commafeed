var app = angular
		.module('commafeed', [ 'ui', 'ui.bootstrap', 'commafeed.directives',
				'commafeed.controllers', 'commafeed.services', 'ngSanitize' ]);

app.config([ '$routeProvider', function($routeProvider) {
	$routeProvider.when('/feeds/view/:_type/:_id', {
		templateUrl : 'templates/feeds.html'
	});
	$routeProvider.otherwise({
		redirectTo : '/feeds/view/category/all'
	});
} ]);