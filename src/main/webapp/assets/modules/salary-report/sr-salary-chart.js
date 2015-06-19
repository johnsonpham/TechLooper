techlooper.directive("srSalaryChart", function ($translate) {
  return {
    restrict: "A",
    replace: true,
    templateUrl: "modules/salary-report/sr-salary-chart.tem.html",
    link: function (scope, element, attr, ctrl) {
      $('#txtEmailReport').val('');
      $('#txtEmailPromotion').val('');
      $('#txtEmailJobAlert').val('');
      scope.openFacebookShare = function () {
        // Google Analytics Event Tracking
        ga('send', {
          'hitType': 'event',
          'eventCategory': 'facebookshare',
          'eventAction': 'click',
          'eventLabel': 'salaryreport'
        });
        window.open(
          'https://www.facebook.com/sharer/sharer.php?u=' + baseUrl + '/renderSalaryReport/' + $translate.use() + '/' + scope.salaryReview.createdDateTime,
          'name', 'width=450,height=350');
      }
    }
  }
});