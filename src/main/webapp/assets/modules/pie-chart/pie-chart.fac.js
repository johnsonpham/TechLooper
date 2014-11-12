angular.module('Pie').factory('pieFactory', ["utils", "jsonValue", "$location",function(utils, jsonValue, $location){
    var terms = [];
    var totalJobs = 0;
    var pieJson = [];
    var innertDonut = utils.isMobile() ?  '0%' : '30%';
    var termsMap = {};
    var newJson =[];
    var scope;
    
    // TODO: use jsonValue
    var colorJson = [ "#2b908f", "#90ee7e", "#f45b5b", "#7798BF", "#aaeeee", "#ff0066", "#eeaaee", "#55BF3B", "#DF5353", "#7798BF", "#aaeeee" ];
    var instance =  {
       setTerms : function($terms) {
          terms = $terms;
          totalJobs = utils.sum(terms, "count");
       },
       
       generateChartData : function() {
          pieJson.length = 0;
          $.each(terms, function(index, term) {
             pieJson.push([angular.uppercase(term.term), term.count]);
          });
       },

       draw : function(pieItem, force) {
        if(newJson.length > 8){
          newJson.length = 0;
        }
        
        if (force !== true && termsMap[pieItem.termID] !== undefined) {
            newJson.push([pieItem.termName, pieItem.count]);
            var chart = $('.pie-Chart-Container').highcharts();
            $.each(newJson, function(index, item){
                chart.series[0].data[index].update(item);
             });
            return;
         }
         termsMap[pieItem.termID] = pieItem;
         
       }, 
       initializeAnimation : function($scope) {
        instance.generateChartData();
        scope = $scope;
        $('.pie-Chart-Container').highcharts({
            colors : colorJson,
            chart : {
              backgroundColor : '#2e272a',
              plotBorderColor : '#606063'
           },
           title : {
              text : ''
           },
           legend : {
              itemStyle : {
                 color : '#fff'
              },
              itemHoverStyle : {
                 color : '#FFF'
              },
              itemHiddenStyle : {
                 color : '#fff'
              }
           },
           labels : {
              style : {
                 color : '#fff'
              }
           },
           tooltip : {
              pointFormat : '{series.name} <b>: {point.percentage:.1f}%</b>'
           },
           plotOptions : {
              pie : {
                 allowPointSelect : true,
                 cursor : 'pointer',
                 dataLabels : {
                    enabled : true,
                    format : '<b>{point.name}</b>: {point.y}',
                    style : {
                       color : (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                    }
                 }
              },
              series : {
                 dataLabels : {
                    color : '#fff'
                 },
                 marker : {
                    lineColor : '#333'
                 }
              },
              boxplot : {
                 fillColor : '#505053'
              },
              candlestick : {
                 lineColor : 'white'
              },
              errorbar : {
                 color : 'white'
              }
           },
            series : [ {
              type : 'pie',
              name : 'Jobs',
              innerSize :innertDonut,
              point: {
                events: {
                  click: function(e) {
                    var path = jsonValue.routerUris.analyticsSkill + '/' + this.options.name;
                    $location.path(path);
                    scope.$apply();
                    e.preventDefault();
                  }
                }
              },
              data : pieJson
           } ]
        });
        $('tspan[dx=0]').css('font-size', '14px');
        $('text[text-anchor=end]').css('display', 'none');
       }
    }

    return instance;
}]);