var module = angular.module('commafeed.controllers', []);

module.controller('CategoryTreeCtrl',
		function($scope, $routeParams, $location) {

			$scope.selectedType = $routeParams._type;
			$scope.selectedId = $routeParams._id;

			$scope.root = {
				children : [ {
					id : "1",
					name : "News",
					feeds : [ {
						id : "2",
						name : "Cyanide & Happiness"
					} ],
					children : [ {
						id : "2",
						name : "Comics",
						feeds : [ {
							id : "1",
							name : "Dilbert"
						} ]
					} ]
				}, {
					id : '3',
					name : "Blogs",
					feeds : [ {
						id : "3",
						name : "Engadget"
					} ]
				} ]
			};

			$scope.feedClicked = function(id) {
				$location.path('/feeds/view/feed/' + id);
			};

			$scope.categoryClicked = function(id) {
				$location.path('/feeds/view/category/' + id);
			};
		});

module.controller('FeedListCtrl', function($scope, $routeParams, $http) {

	$scope.entries = [ {
		id : '1',
		title : 'my title',
		content : 'my content',
		date : 'my date',
		feed : 'my feed',
		url : 'my url',
		read : false,
		starred : false,
	}, {
		id : '1',
		title : 'my title',
		content : 'my content',
		date : 'my date',
		feed : 'my feed',
		url : 'my url',
		read : false,
		starred : false,
	} ];

	$scope.markAsRead = function(entry) {
		entry.read = true;
	};
});
