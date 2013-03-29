var module = angular.module('commafeed.controllers', []);

module.run(function($rootScope) {
	$rootScope.$on('emitMark', function(event, args) {
		// args.entry - the entry
		$rootScope.$broadcast('mark', args);
	});
	$rootScope.$on('emitReload', function(event, args) {
		$rootScope.$broadcast('reload');
	});
});

module.controller('CategoryTreeCtrl', function($scope, $stateParams, $location,
		$state, $route, SubscriptionService) {

	$scope.$on('$stateChangeSuccess', function() {
		$scope.selectedType = $stateParams._type;
		$scope.selectedId = $stateParams._id;
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
		if (feed.unread > 0) {
			label = label + " (" + feed.unread + ")";
		}
		return label;
	}

	$scope.feedClicked = function(id) {
		if ($scope.selectedType == 'feed' && id == $scope.selectedId) {
			$scope.$emit('emitReload');
		} else {
			$state.transitionTo('feeds.view', {
				_type : 'feed',
				_id : id
			});
		}
	};

	$scope.categoryClicked = function(id) {
		if ($scope.selectedType == 'category' && id == $scope.selectedId) {
			$scope.$emit('emitReload');
		} else {
			$state.transitionTo('feeds.view', {
				_type : 'category',
				_id : id
			});
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

module.controller('FeedListCtrl', function($scope, $stateParams, $http, $route,
		$window, EntryService, SettingsService) {

	$scope.selectedType = $stateParams._type;
	$scope.selectedId = $stateParams._id;

	$scope.name = null;
	$scope.entries = [];

	$scope.settingsService = SettingsService;
	$scope.$watch('settingsService.settings.readingMode', function(newValue,
			oldValue) {
		if (newValue && oldValue && newValue != oldValue) {
			$scope.$emit('emitReload');
		}
	});

	$scope.limit = 10;
	$scope.busy = false;
	$scope.hasMore = true;

	$scope.loadMoreEntries = function() {
		if (!$scope.hasMore)
			return;
		if ($scope.busy)
			return;
		$scope.busy = true;

		var limit = $scope.limit;
		if ($scope.entries.length == 0) {
			$window = angular.element($window);
			limit = $window.height() / 33;
			limit = parseInt(limit) + 5;
		}
		EntryService.get({
			type : $scope.selectedType,
			id : $scope.selectedId,
			readType : $scope.settingsService.settings.readingMode,
			offset : $scope.entries.length,
			limit : limit
		}, function(data) {
			for ( var i = 0; i < data.entries.length; i++) {
				$scope.entries.push(data.entries[i]);
			};
			$scope.name = data.name;
			$scope.busy = false;
			$scope.hasMore = data.entries.length == limit
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

	$scope.isOpen = false;
	$scope.entryClicked = function(entry, event) {
		$scope.mark(entry, true);
		if (!event.ctrlKey && event.which != 2) {
			if ($scope.current != entry) {
				$scope.isOpen = true;
			} else {
				$scope.isOpen = !$scope.isOpen;
			}
			$scope.current = entry;
			event.preventDefault();
			event.stopPropagation();
		}
	}

	var openNextEntry = function(event) {
		var entry = null;
		if ($scope.current) {
			var index;
			for ( var i = 0; i < $scope.entries.length; i++) {
				if ($scope.current == $scope.entries[i]) {
					index = i;
					break;
				}
			}
			index = index + 1;
			if (index < $scope.entries.length) {
				entry = $scope.entries[index];
			}
		} else if ($scope.entries.length > 0) {
			entry = $scope.entries[0];
		}
		if (entry) {
			$scope.entryClicked(entry, event);
		}
	};

	var openPreviousEntry = function(event) {
		var entry = null;
		if ($scope.current) {
			var index;
			for ( var i = 0; i < $scope.entries.length; i++) {
				if ($scope.current == $scope.entries[i]) {
					index = i;
					break;
				}
			}
			index = index - 1;
			if (index >= 0) {
				entry = $scope.entries[index];
			}
		}
		if (entry) {
			$scope.entryClicked(entry, event);
		}
	};

	Mousetrap.bind('space', function(e) {
		$scope.$apply(function() {
			openNextEntry(e);
		})
	});
	Mousetrap.bind('j', function(e) {
		$scope.$apply(function() {
			openNextEntry(e);
		})
	});

	Mousetrap.bind('shift+space', function(e) {
		$scope.$apply(function() {
			openPreviousEntry(e);
		})
	});
	Mousetrap.bind('k', function(e) {
		$scope.$apply(function() {
			openPreviousEntry(e);
		})
	});

	$scope.$on('reload', function(event, args) {
		$scope.name = null;
		$scope.entries = [];
		$scope.busy = false;
		$scope.hasMore = true;
		$scope.loadMoreEntries();
	});
});

module.controller('ManageUsersCtrl', function($scope, AdminUsersService) {
	$scope.users = AdminUsersService.get();
	$scope.gridOptions = {
		data : 'users'
	};
});