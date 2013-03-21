var app = angular.module('commafeed', [ 'commafeed.directives',
		'commafeed.controllers' ]);

app.config([ '$routeProvider', function($routeProvider) {
	$routeProvider.when('/feeds/view/:_type/:_id', {
		templateUrl : 'templates/feeds.html'
	});
	$routeProvider.otherwise({
		redirectTo : '/feeds/view/category/all'
	});
} ]);