var module = angular.module('commafeed.directives', []);

module.directive('favicon', function() {
	return {
		restrict : 'E',
		scope : {
			url : '='
		},
		replace : true,
		template : '<img ng-src="favicon?url={{url}}" class="favicon"></img>'
	};
});

module.directive('ngBlur', function() {
	return {
		restrict : 'A',
		link : function(scope, elm, attrs) {
			elm.bind('blur', function() {
				scope.$apply(attrs.ngBlur);
			});
		}
	};
});

module.directive('scrollTo', function($timeout) {
	return {
		restrict : 'A',
		link : function(scope, element, attrs) {
			scope.$watch(attrs.scrollTo, function(value) {
				if (!value)
					return;
				$timeout(function() {
					var docTop = $(window).scrollTop();
					var docBottom = docTop + $(window).height();

					var elemTop = $(element).offset().top;
					var elemBottom = elemTop + $(element).height();

					if ((elemTop > docTop) && (elemBottom < docBottom)) {
						// element is entirely visible
						return;
					} else {
						var offset = parseInt(attrs.scrollToOffset, 10);
						var scrollTop = $(element).offset().top + offset;
						$('html, body').animate({
							scrollTop : scrollTop
						}, 0);
					}
				});
			});
		}
	};
});

module.directive('recursive', function($compile) {
	return {
		restrict : 'E',
		priority : 100000,
		compile : function(tElement, tAttr) {
			var contents = tElement.contents().remove();
			var compiledContents;
			return function(scope, iElement, iAttr) {
				if (!compiledContents) {
					compiledContents = $compile(contents);
				}
				iElement.append(compiledContents(scope, function(clone) {
					return clone;
				}));
			};
		}
	};
});

module.directive('category', function($compile) {
	return {
		scope : {
			node : '=',
			selectedType : '=',
			selectedId : '=',
			feedClick : '&',
			categoryClick : '&',
			unreadCount : '&',
			formatCategoryName : '&',
			formatFeedName : '&'
		},
		restrict : 'E',
		replace : true,
		templateUrl : 'directives/category.html',
		controller : function($scope, $dialog, FeedService, CategoryService,
				SettingsService) {
			$scope.settingsService = SettingsService;
			$scope.unsubscribe = function(subscription) {
				var title = 'Unsubscribe';
				var msg = 'Unsubscribe from ' + subscription.name + ' ?';
				var btns = [ {
					result : 'cancel',
					label : 'Cancel'
				}, {
					result : 'ok',
					label : 'OK',
					cssClass : 'btn-primary'
				} ];

				$dialog.messageBox(title, msg, btns).open().then(
						function(result) {
							if (result == 'ok') {
								var data = {
									id : subscription.id
								};
								FeedService.unsubscribe(data, function() {
									CategoryService.init();
								});
							}
						});
			};

			$scope.renameFeed = function(feed) {
				var name = window.prompt('Rename feed : ', feed.name);
				if (name && name != feed.name) {
					feed.name = name;
					FeedService.rename({
						id : feed.id,
						name : name
					});
				}
			};

			$scope.renameCategory = function(category) {
				var name = window.prompt('Rename category: ', category.name);
				if (name && name != category.name) {
					category.name = name;
					CategoryService.rename({
						id : category.id,
						name : name
					});
				}
			};

			$scope.deleteCategory = function(category) {
				var title = 'Delete category';
				var msg = 'Delete category ' + category.name + ' ?';
				var btns = [ {
					result : 'cancel',
					label : 'Cancel'
				}, {
					result : 'ok',
					label : 'OK',
					cssClass : 'btn-primary'
				} ];

				$dialog.messageBox(title, msg, btns).open().then(
						function(result) {
							if (result == 'ok') {
								CategoryService.remove({
									id : category.id
								}, function() {
									CategoryService.init();
								});
							}
						});
			};

			$scope.toggleCategory = function(category) {
				category.expanded = !category.expanded;
				if (category.id == 'all') {
					return;
				}
				CategoryService.collapse({
					id : category.id,
					collapse : !category.expanded
				});
			};
		}
	};
});

module.directive('spinner', function() {
	return {
		scope : {
			shown : '='
		},
		restrict : 'A',
		link : function($scope, element) {
			element.addClass('spinner');
			var opts = {
				lines : 11, // The number of lines to draw
				length : 5, // The length of each line
				width : 3, // The line thickness
				radius : 8, // The radius of the inner circle
				corners : 1, // Corner roundness (0..1)
				rotate : 0, // The rotation offset
				color : '#000', // #rgb or #rrggbb
				speed : 1.3, // Rounds per second
				trail : 60, // Afterglow percentage
				shadow : false, // Whether to render a shadow
				hwaccel : true, // Whether to use hardware acceleration
				zIndex : 2e9, // The z-index (defaults to 2000000000)
				top : 'auto', // Top position relative to parent in px
				left : 'auto' // Left position relative to parent in px
			};
			var spinner = new Spinner(opts);
			$scope.$watch('shown', function(shown) {
				if (shown) {
					spinner.spin();
					element.append(spinner.el);
				} else {
					spinner.stop();
				}
			});
		}
	};
});