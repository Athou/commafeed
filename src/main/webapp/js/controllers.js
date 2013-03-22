var module = angular.module('commafeed.controllers', []);

module.run(function($rootScope) {
	$rootScope.$on('emitMarkAsRead', function(event, args) {
		$rootScope.$broadcast('markAsRead', args);
	});
});

module.controller('CategoryTreeCtrl', function($scope, $routeParams, $location,
		CategoryService) {

	$scope.selectedType = $routeParams._type;
	$scope.selectedId = $routeParams._id;

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

	var markAsRead = function(children, entry, read) {
		for ( var i = 0; i < children.length; i++) {
			var child = children[i];
			if (child.children) {
				markAsRead(child.children, entry, read);
			}
			if (child.feeds) {
				for ( var j = 0; j < child.feeds.length; j++) {
					var feed = child.feeds[j];
					if (feed.id == entry.feedId) {
						var c = read ? -1 : 1;
						feed.unread = feed.unread + c;
					}
				}
			}
		}
	};

	$scope.$on('markAsRead', function(event, args) {
		console.log(args.entry)
		markAsRead($scope.root.children, args.entry, args.read)
	});
});

module.controller('FeedListCtrl', function($scope, $routeParams, $http,
		EntryService) {

	$scope.selectedType = $routeParams._type;
	$scope.selectedId = $routeParams._id;

	$scope.entryList = EntryService.getUnread({
		_type : $scope.selectedType,
		_id : $scope.selectedId,
		_readtype : 'unread'
	})

	$scope.entryList2 = {
		name : 'aaa',
		entries : [ {
			id : '1',
			title : 'my title',
			content : 'my content',
			date : 'my date',
			feedId : '1',
			feedName : 'my feed',
			url : 'my url',
			read : false,
			starred : false,
		}, {
			id : '2',
			title : 'my other title',
			content : 'my other content',
			date : 'my other date',
			feedId : '2',
			feedName : 'my other feed',
			url : 'my other url',
			read : false,
			starred : false,
		} ]
	};

	$scope.markAsRead = function(entry) {
		var read = entry.read;
		entry.read = true;
		if (entry.read != read) {
			$scope.$emit('emitMarkAsRead', {
				entry : entry,
				read : true
			});
		}
	};
});
