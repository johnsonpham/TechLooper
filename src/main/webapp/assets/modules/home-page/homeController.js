techlooper.controller("homeController", function ($scope, securityService, apiService, localStorageService, $location,
                                                  jsonValue, utils, $timeout, vnwConfigService, $translate) {

  apiService.getPersonalHomepage().success(function (data) {
    $scope.homePage = data;
    $scope.homePage.termStatistic.logo = "images/" + $.grep(jsonValue.technicalSkill, function (skill) {
        return skill.term == $scope.homePage.termStatistic.term;
      })[0].logo;
  });

  //TODO remove timeout function
  $timeout(function () {
    var tallest = 0;
    $('.box-container-block').find('.box-content').each(function () {
      var thisHeight = $(this).height();
      if (thisHeight > tallest)
        tallest = thisHeight;
    });
    $('.box-container-block').find('.box-content').height(tallest + $('.cta-button').height());
  }, 1000);

  $scope.locationsConfig = vnwConfigService.locationsSelectize;

  $scope.createJobAlert = function () {
    $scope.jobAlertForm.$setSubmitted();
    if ($scope.jobAlertForm.$invalid) {
      return;
    }

    var location = null;
    var locationId = null;
    if ($scope.jobAlert.locationId && $scope.jobAlert.locationId !== "0") {
      locationId = $scope.jobAlert.locationId;
      location = vnwConfigService.getLocationText(locationId, "en");
    }
    apiService.createTechlooperJobAlert($scope.jobAlert.email, $scope.jobAlert.keyword, location, locationId, $translate.use())
      .success(function (data) {
        $scope.sendMailSuccessfulMessage = true;
        $scope.sendMailFailMessage = false;
        $scope.jobAlertForm.$setPristine();
        //$scope.jobAlertForm.$submitted = false;
        $scope.jobAlert = {};
      })
      .error(function(data, status) {
         if (status == "405") {
           $scope.sendMailFailMessage = true;
           $scope.sendMailSuccessfulMessage = false;
           $scope.jobAlertForm.$setPristine();
           $scope.jobAlert = {};
         }
      });
  }
});