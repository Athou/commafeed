var module = angular.module('commafeed.controllers', []);

module.run(function($rootScope) {
	$rootScope.$on('emitMark', function(event, args) {
		// args.entry - the entry
		$rootScope.$broadcast('mark', args);
	});
});

module.controller('CategoryTreeCtrl', function($scope, $routeParams, $location, $route,
		SubscriptionService) {

	$scope.$on('$routeChangeSuccess', function() {
		$scope.selectedType = $routeParams._type;
		$scope.selectedId = $routeParams._id;
	});

	$scope.SubscriptionService = SubscriptionService;

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
		if (feed.message) {
			label = "!!! " + label;
		}
		if (feed.unread > 0) {
			label = label + " (" + feed.unread + ")";
		}
		return label;
	}

	$scope.feedClicked = function(id) {
		if($scope.selectedType == 'feed' && id == $scope.selectedId) {
			$route.reload();
		} else {
			$location.path('/feeds/view/feed/' + id);
		}
	};

	$scope.categoryClicked = function(id) {
		if($scope.selectedType == 'category' && id == $scope.selectedId) {
			$route.reload();
		} else {
			$location.path('/feeds/view/category/' + id);	
		}
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
		mark($scope.SubscriptionService.subscriptions, args.entry)
	});
});

module.controller('FeedListCtrl', function($scope, $routeParams, $http,
		EntryService, SettingsService) {

	$scope.selectedType = $routeParams._type;
	$scope.selectedId = $routeParams._id;

	$scope.settings = SettingsService.settings;
	$scope.$watch('settings.readingMode', function() {
		$scope.refreshList();
	});

	$scope.limit = 20;
	$scope.busy = false;
	$scope.hasMore = true;
	
	$scope.refreshList = function() {
		if ($scope.settings.readingMode) {
			$scope.entryList = EntryService.get({
				type : $scope.selectedType,
				id : $scope.selectedId,
				readType : $scope.settings.readingMode,
				offset : 0,
				limit : 30
			});
		}
	};
	
	$scope.loadMoreEntries = function() {
		if(!$scope.hasMore)
			return;
		if (!$scope.entryList || !$scope.entryList.entries)
			return;
		if ($scope.busy)
			return;
		if (!$scope.settings.readingMode) 
			return;
		$scope.busy = true;
		EntryService.get({
			type : $scope.selectedType,
			id : $scope.selectedId,
			readType : $scope.settings.readingMode,
			offset : $scope.entryList.entries.length,
			limit : $scope.limit
		}, function(data) {
			console.log(data)
			var entries = data.entries
			for ( var i = 0; i < entries.length; i++) {
				$scope.entryList.entries.push(entries[i]);
			}
			$scope.busy = false;
			$scope.hasMore = entries.length == $scope.limit
		});
	}

	$scope.mark = function(entry, read) {
		if (entry.read != read) {
			entry.read = read;
			$scope.$emit('emitMark', {
				entry : entry
			});
			EntryService.mark({
				type : 'entry',
				id : entry.id,
				read : read
			});
		}
	};
	
	$scope.toggle = function(entry) {
		$scope.current = entry;
		$scope.mark(entry, true);
	}
});
