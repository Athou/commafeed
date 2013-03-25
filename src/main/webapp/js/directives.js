var module = angular.module('commafeed.directives', []);

module.directive('subscribe', function(SubscriptionService) {
	return {
		scope : {},
		restrict : 'E',
		replace : true,
		templateUrl : 'directives/subscribe.html',
		controller : function($scope, SubscriptionService) {
			$scope.opts = {
				backdropFade : true,
				dialogFade : true
			};
			
		    $scope.isOpen = false;
		    $scope.isOpenImport = false;
		    $scope.sub = {};
		    
			$scope.SubscriptionService = SubscriptionService;

			$scope.open = function() {
				$scope.sub = {};
				$scope.isOpen = true;
			};

			$scope.close = function() {
				$scope.isOpen = false;
			};
			
			$scope.save = function() {
				SubscriptionService.subscribe($scope.sub, function() {
					$scope.close();
				});
			};
			
			$scope.openImport = function() {
				$scope.isOpenImport = true;
			};

			$scope.closeImport = function() {
				$scope.isOpenImport = false;
			};
			
			$scope.uploadComplete = function(contents, completed) {
				SubscriptionService.init();
				$scope.closeImport();
			};
		}
	};
});

module.directive('category', function($compile) {
	return {
		scope: {
			node: '=',
			selectedType: '=',
			selectedId: '=',
			feedClick: '&',
			categoryClick: '&',
			formatCategoryName: '&',
			formatFeedName: '&'
		},
		restrict : 'E',
		replace: true,
		templateUrl: 'directives/category.html',
		link: function(scope, element) {
            var ul = element.find('ul');
            ul.prepend('<category ng-repeat="child in node.children" node="child" feed-click="feedClick({id:id})" \
            		category-click="categoryClick({id:id})" selected-type="selectedType" selected-id="selectedId" \
            		format-category-name="formatCategoryName({category:category})" format-feed-name="formatFeedName({feed:feed})">\
            		</category>');
            $compile(ul.contents())(scope);
	     },
	     controller: function($scope, $dialog, SubscriptionService) {
	    	 $scope.unsubscribe = function(subscription) {
				var title = 'Unsubscribe';
			    var msg = 'Unsubscribe from ' + subscription.name + ' ?';
			    var btns = [{result:'cancel', label: 'Cancel'}, {result:'ok', label: 'OK', cssClass: 'btn-primary'}];
			    
				$dialog.messageBox(title, msg, btns)
			      .open()
			      .then(function(result){
			    	  if(result == 'ok'){
			    		  SubscriptionService.unsubscribe(subscription.id);
			    	  }
			    });
	    	 }
	     }
	};
});

module.directive('toolbar', function(SettingsService) {
	return {
		scope : {},
		restrict : 'E',
		replace : true,
		templateUrl : 'directives/toolbar.html',
		controller : function($scope, $route, SettingsService) {
			$scope.settings = SettingsService.settings;
			$scope.refresh = function() {
				$route.reload();
			}
		},
		link : function($scope, element) {
			element.find('.read-mode button').bind('click', function() {
				SettingsService.save();
			});
		}
	};
});