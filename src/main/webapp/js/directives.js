var module = angular.module('commafeed.directives', []);

module.directive('focus', [ '$timeout', function($timeout) {
	return {
		restrict : 'A',
		link : function(scope, element, attrs) {
			scope.$watch(attrs.focus, function(value) {
				if (!value)
					return;
				$timeout(function() {
					$(element).focus();
				});
			});
		}
	};
} ]);

/**
 * Open a popup window pointing to the url in the href attribute
 */
module.directive('popup', function() {
	var opts = 'menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=600,width=800';
	return {
		link : function(scope, elm, attrs) {
			elm.bind('click', function(event) {
				window.open(this.href, '', opts);
				event.preventDefault();
			});
		}
	};
});

/**
 * Reusable favicon component
 */
module.directive('favicon', function() {
	var tpl = '<img ng-src="{{url}}" class="favicon"></img>';
	return {
		restrict : 'E',
		scope : {
			url : '='
		},
		replace : true,
		template : tpl
	};
});

/**
 * Support for the blur event
 */
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

/**
 * Fired when the top of the element is not visible anymore
 */
module.directive('onScrollMiddle', function() {
	return {
		restrict : 'A',
		link : function(scope, element, attrs) {

			var w = $(window);
			var e = $(element);
			var d = $(document);
			
			var offset = parseInt(attrs.onScrollMiddleOffset, 10);

			var down = function() {
				var docTop = w.scrollTop();
				var elemTop = e.offset().top;
				var threshold = docTop === 0 ? elemTop - 1 : docTop + offset;
				return (elemTop > threshold) ? 'below' : 'above';
			};
			var up = function() {
				var docTop = w.scrollTop();
				var elemTop = e.offset().top;
				var elemBottom = elemTop + e.height();
				var threshold = docTop === 0 ? elemBottom - 1 : docTop + offset;
				return (elemBottom > threshold) ? 'below' : 'above';
			};

			if (!w.data.scrollInit) {
				w.data.scrollPosition = d.scrollTop();
				w.data.scrollDirection = 'down';
				
				var onScroll = function(e) {
					var scroll = d.scrollTop();
					w.data.scrollDirection = (scroll
							- w.data.scrollPosition > 0) ? 'down'
							: 'up';
					w.data.scrollPosition = scroll;
					scope.$apply();
				};
				w.bind('scroll', _.throttle(onScroll, 500));
				w.data.scrollInit = true;
			}
			scope.$watch(down, function downCallback(value, oldValue) {
				if (value != oldValue && value == 'above')
					scope.$eval(attrs.onScrollMiddle);
			});
			scope.$watch(up, function upCallback(value, oldValue) {
				if (value != oldValue && value == 'below')
					scope.$eval(attrs.onScrollMiddle);
			});
		}
	};
});

/**
 * Scrolls to the element if the value is true and the attribute is not fully visible, 
 * unless the attribute scroll-to-force is true
 */
module.directive('scrollTo', [ '$timeout', function($timeout) {
	return {
		restrict : 'A',
		link : function(scope, element, attrs) {
			scope.$watch(attrs.scrollTo, function(value) {
				if (!value)
					return;
				var force = scope.$eval(attrs.scrollToForce);
				
				// timeout here to execute after dom update
				$timeout(function() {
					var docTop = $(window).scrollTop();
					var docBottom = docTop + $(window).height();

					var elemTop = $(element).offset().top;
					var elemBottom = elemTop + $(element).height();

					if (!force && (elemTop > docTop) && (elemBottom < docBottom)) {
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
} ]);

/**
 * Prevent mousewheel scrolling from propagating to the parent when scrollbar
 * reaches top or bottom
 */
module.directive('mousewheelScrolling', function() {
	return {
		restrict : 'A',
		link : function(scope, elem, attr) {
			elem.bind('mousewheel', function(e, d) {
				var t = $(this);
				if (d > 0 && t.scrollTop() === 0) {
					e.preventDefault();
				} else {
					if (d < 0
							&& (t.scrollTop() == t.get(0).scrollHeight
									- t.innerHeight())) {
						e.preventDefault();
					}
				}
			});
		}
	};
});

/**
 * Needed to use recursive directives. Wrap a recursive element with a
 * <recursive> tag
 */
module.directive('recursive', [ '$compile', function($compile) {
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
} ]);

/**
 * Reusable category component
 */
module.directive('category', [ function() {
	return {
		scope : {
			node : '=',
			level : '=',
			selectedType : '=',
			selectedId : '=',
			showLabel : '=',
			showChildren : '=',
			unreadCount : '&'
		},
		restrict : 'E',
		replace : true,
		templateUrl : 'templates/_category.html',
		controller : [
			'$scope',
			'$state',
			'$dialog',
			'FeedService',
			'CategoryService',
			'SettingsService',
			'MobileService',
			function($scope, $state, $dialog, FeedService, CategoryService,
					SettingsService, MobileService) {
				$scope.settingsService = SettingsService;

				$scope.getClass = function(level) {
					if ($scope.showLabel) {
						return 'indent' + level;
					}
				};

				$scope.categoryLabel = function(category) {
					return $scope.showLabel !== true ? $scope.showLabel
							: category.name;
				};

				$scope.categoryCountLabel = function(category) {
					var count = $scope.unreadCount({
						category : category
					});
					var label = '';
					if (count > 0) {
						label = '(' + count + ')';
					}
					return label;
				};

				$scope.feedCountLabel = function(feed) {
					var label = '';
					if (feed.unread > 0) {
						label = '(' + feed.unread + ')';
					}
					return label;
				};

				$scope.feedClicked = function(id) {
					MobileService.toggleLeftMenu();
					if ($scope.selectedType == 'feed'
							&& id == $scope.selectedId) {
						$scope.$emit('emitReload');
					} else {
						$state.transitionTo('feeds.view', {
							_type : 'feed',
							_id : id
						});
					}
				};

				$scope.categoryClicked = function(id) {
					MobileService.toggleLeftMenu();
					if ($scope.selectedType == 'category'
							&& id == $scope.selectedId) {
						$scope.$emit('emitReload');
					} else {
						$state.transitionTo('feeds.view', {
							_type : 'category',
							_id : id
						});
					}
				};

				$scope.showFeedDetails = function(feed) {
					$state.transitionTo('feeds.feed_details', {
						_id : feed.id
					});
				};

				$scope.showCategoryDetails = function(category) {
					$state.transitionTo('feeds.category_details', {
						_id : category.id
					});
				};

				$scope.toggleCategory = function(category, event) {
					event.preventDefault();
					event.stopPropagation();
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
		]
	};
} ]);

/**
 * Reusable spinner component
 */
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

module.directive('draggable', function() {
	return {
		restrict : 'A',
		link : function(scope, element, attrs) {
			element.draggable({
				revert: 'invalid',
				helper: 'clone',
				distance: 10,
				axis: 'y'
			}).data('source', scope.$eval(attrs.draggable));
		}
	};
});

module.directive('droppable', [ 'CategoryService', 'FeedService',
		function(CategoryService, FeedService) {
			return {
				restrict : 'A',
				link : function(scope, element, attrs) {
					element.droppable({
						tolerance : 'pointer',
						over : function(event, ui) {

						},
						drop : function(event, ui) {
							var draggable = angular.element(ui.draggable);

							var source = draggable.data('source');
							var target = scope.$eval(attrs.droppable);

							if (angular.equals(source, target)) {
								return;
							}

							var data = {
								id : source.id,
								name : source.name
							};

							if (source.children) {
								// source is a category

							} else {
								// source is a feed

								if (target.children) {
									// target is a category
									data.categoryId = target.id;
									data.position = 0;
								} else {
									// target is a feed
									data.categoryId = target.categoryId;
									data.position = target.position;
								}

								FeedService.modify(data, function() {
									CategoryService.init();
								});
							}
							scope.$apply();
						}
					});
				}
			};
		} ]);