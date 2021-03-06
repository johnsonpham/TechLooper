techlooper.controller("loadingBoxController", function (utils, jsonValue, $scope, localStorageService, $location,
                                                        securityService, $translate, apiService, $route, joinAnythingService) {
  utils.sendNotification(jsonValue.notifications.loading, $(window).height());

  $scope.$on('$destroy', function () {
    utils.sendNotification(jsonValue.notifications.loaded);
  });

  var joinNow = localStorageService.get("joinNow");
  if (joinNow == true) {
    return joinAnythingService.joinChallenge();
  }

  var param = $location.search();
  if ($.isEmptyObject(param)) {
    return $location.url("/home");
  }

  securityService.routeByRole();
});