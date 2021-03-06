techlooper.controller("homeController", function ($scope, securityService, apiService, localStorageService, $location,
                                                  jsonValue, utils, $timeout, vnwConfigService, $translate, resourcesService) {
  utils.sendNotification(jsonValue.notifications.loading, $(window).height());
  apiService.getPersonalHomepage().success(function (data) {
    $scope.homePage = data;
    $scope.homePage.termStatistic.logo = "images/" + $.grep(jsonValue.technicalSkill, function (skill) {
        return skill.term == $scope.homePage.termStatistic.term;
      })[0].logo;
    $.each(jsonValue.viewTerms.listedItems, function (j, viewTerm) {
      if (viewTerm.term === $scope.homePage.termStatistic.term) {
        $scope.homePage.termStatistic = $.extend({}, viewTerm, $scope.homePage.termStatistic);
      }
    });
  }).finally(function () {
    utils.sendNotification(jsonValue.notifications.loaded);
  });

  $scope.status = function (type) {
    if (arguments.length > 1) {
      var project = arguments[1];
      if (!project) return false;
      var option = resourcesService.getOption(project.payMethod, resourcesService.paymentConfig);

      switch (type) {
        case "show-fixed-price-fields":
          if (!option) return false;
          return option.id == "fixedPrice";

        case "show-hourly-price-fields":
          if (!option) return false;
          return option.id == "hourly";

        case "get-payment-method-translate":
          if (!option) return false;
          return option.reviewTranslate;
      }
    }
    return false;
  }

  $timeout(function () {
    var tallest = 0;
    $('.main-feature').find('.box-content').each(function () {
      var thisHeight = $(this).height();
      if (thisHeight > tallest)
        tallest = thisHeight;
    });
    $('.main-feature').find('.box-content').height(tallest + $('.cta-button').height());
  }, 2500);

  $scope.locationsConfig = vnwConfigService.locationsSelectize;

  $scope.createJobAlert = function () {
    utils.sendNotification(jsonValue.notifications.loading);
    $scope.jobAlertForm.$setSubmitted();
    if ($scope.jobAlertForm.$invalid) {
      utils.sendNotification(jsonValue.notifications.loaded);
      return;
    }

    var location = null;
    var locationId = null;
    if ($scope.jobAlert.locationId && $scope.jobAlert.locationId !== "0" && $scope.jobAlert.locationId !== "1") {
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
      .error(function (data, status) {
        if (status == "405") {
          $scope.sendMailFailMessage = true;
          $scope.sendMailSuccessfulMessage = false;
          $scope.jobAlertForm.$setPristine();
          $scope.jobAlert = {};
        }
      }).finally(function () {
      utils.sendNotification(jsonValue.notifications.loaded);
    });
  }

  $scope.goToJobListing = function () {
    ga("send", {
      hitType: "event",
      eventCategory: "techlooperjobhub",
      eventAction: "click",
      eventLabel: "searchallbtn"
    });
    window.location.href = "#/job-listing";
  }

  $scope.dateFormation = function (date) {
    return moment(date).format('LL');
  }
  $scope.gotoTopics = function () {
    ga("send", {
      hitType: "event",
      eventCategory: "forum",
      eventAction: "click",
      eventLabel: "moretopics"
    });
    window.location.href = "#/topics";
  }
});