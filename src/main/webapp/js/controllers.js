var module = angular.module('commafeed.controllers', []);

module.run(function($rootScope) {
	$rootScope.$on('emitMark', function(event, args) {
		// args.entry - the entry
		$rootScope.$broadcast('mark', args);
	});
});

module.controller('CategoryTreeCtrl', function($scope, $routeParams, $location,
		CategoryService) {

	$scope.$on('$routeChangeSuccess', function() {
		$scope.selectedType = $routeParams._type;
		$scope.selectedId = $routeParams._id;
	});

	$scope.root = CategoryService.get();

	var unreadCount = function(category) {
		var count = 0;
		if (category.children) {
			for ( var i = 0; i < category.children.length; i++) {
				count = count + unreadCount(category.children[i]);
			}
		}
		if (category.feeds) {
			for ( var i = 0; i < category.feeds.length; i++) {
				var feed = category.feeds[i];
				count = count + feed.unread;
			}
		}
		return count;
	}

	$scope.formatCategoryName = function(category) {
		var count = unreadCount(category);
		var label = category.name;
		if (count > 0) {
			label = label + " (" + count + ")";
		}
		return label;
	}

	$scope.formatFeedName = function(feed) {
		var label = feed.name;
		if (feed.unread > 0) {
			label = label + " (" + feed.unread + ")";
		}
		return label;
	}

	$scope.feedClicked = function(id) {
		$location.path('/feeds/view/feed/' + id);
	};

	$scope.categoryClicked = function(id) {
		$location.path('/feeds/view/category/' + id);
	};

	var mark = function(node, entry) {
		if (node.children) {
			for ( var i = 0; i < node.children.length; i++) {
				mark(node.children[i], entry);
			}
		}
		if (node.feeds) {
			for ( var i = 0; i < node.feeds.length; i++) {
				var feed = node.feeds[i];
				if (feed.id == entry.feedId) {
					var c = entry.read ? -1 : 1;
					feed.unread = feed.unread + c;
				}
			}
		}
	};

	$scope.$on('mark', function(event, args) {
		mark($scope.root, args.entry)
	});
});

module.controller('FeedListCtrl', function($scope, $routeParams, $http,
		EntryService) {

	$scope.selectedType = $routeParams._type;
	$scope.selectedId = $routeParams._id;

	$scope.entryList = EntryService.get({
		_type : $scope.selectedType,
		_id : $scope.selectedId,
		_readtype : 'unread'
	})

	$scope.mark = function(entry, read) {
		if (entry.read != read) {
			entry.read = read;
			$scope.$emit('emitMark', {
				entry : entry
			});
			EntryService.mark({
				_type : 'entry',
				_id : entry.id,
				_readtype : read
			});
		}
	};
});
