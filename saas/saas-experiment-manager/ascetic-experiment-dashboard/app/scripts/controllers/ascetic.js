/*
   Apps Ctrl and utility functions
*/
(function(){

  var app = angular.module('asceticApp',[]);
  
 
    
  app.controller('AppsCtrl', function($scope, $http, $filter){
       $scope.sessions = [];
       $scope.selectedApp = { sessions: [] };
       $scope.selectedSession = "";
       $scope.selectedMeasure = "";
       $scope.labels = [];
       $scope.instances = [];
       $scope.allTheData = [];
       $scope.accessors = [];
       $scope.selectedInstance = "";
       $scope.measures = ["processor", "busy", "ioread", "iowrite", "iodata","vm"];
       $http.get("http://localhost:5000/applications/").
        success(function(data, status, headers, config) {
            $scope.apps = data['_items'];
            console.log($scope.apps);
        }).
        error(function(data, status, headers, config) {
           console.log("error@");
            console.log(status);
        });
      
      $scope.updateApp = function() {
           $scope.sessions = $scope.selectedApp.sessions ;
      }
      $scope.updateSession = function() {
            $scope.instances = [];
            var flags = [], array = $scope.selectedSession.measures, l = array.length, i;
            for( i=0; i<l; i++) {
                if( flags[array[i].instance]) continue;
                flags[array[i].instance] = true;
                $scope.instances.push(array[i].instance);
            }
      }
      $scope.updateMeasure = function() {
          $scope.selectedMeasures.push($scope.selectedMeasure);
      }
      
      function getDateTime(s) {
          var re = /([0-9]{2})\/([0-9]{2})\/([0-9]{4})\ ([0-9]{2}):([0-9]{2}):([0-9]{2})/g ;
          var match = re.exec(s);
          
          return { day:parseInt(match[1]), month:parseInt(match[2]), year:parseInt(match[3]), h: parseInt(match[4]), m: parseInt(match[5]),s: parseInt(match[6])};
      }
      
      // PRE : T1 >= T0;
      function difference(t0,t1) {
          var h = 0,m = 0,s = 0 ;
          if(t1.s >= t0.s) {
              s = t1.s - t0.s;
          }
          else {
              s = t1.s + (60 - t0.s);
          }
          if (s > 60) {
              m = Math.floor(s/60);
              s = s % 60;
          }
          if(t1.m >= t0.m) {          
              m += t1.m - t0.m;
          }
          else {
              m += t1.m + (60 - t0.m);
          }
          
          if(m > 60) {
              h = Math.floor(m/60);
              m = m % 60;
          }
          h += t1.h - t0.h;
          
          return { h:h, m:m, s:s};  
      }
      
      function add(time, diff) {
        var h, m, s;
        h = time.h + diff.h;
        m = time.m + diff.m;
        s = time.s + diff.s;

        m += Math.floor(s/60);
        s = s % 60;
        
        h += Math.floor(m/60);
        m = m % 60;
        
          
        return {h:h, m:m, s:s} ;
      }
      
      function makeTimeStamp(t) {
          var out = ""+t.h;
          if(t.h < 10) {
              out = "0"+t.h;
          }
          
          if(t.m < 10) {
              out += ":0"+t.m;
          }
          else {
              out += ":" + t.m;
          }
          
          if(t.s < 10) {
              out += ":0"+t.s;
          }
          else {
              out += ":" + t.s;
          }
          return out;
      }
      
      function normalize_data() {
    
          var data = [];
          var data2 =  $scope.selectedSession['measures'].filter(function(v,i) { return v.instance == $scope.selectedInstance ;}).sort(function(a,b) {
              var atime = a.cleartime.replace(/([0-9]{2})\/([0-9]{2})\/([0-9]{4})/gi, "{3}-{2}-{1}");
              var btime = b.cleartime.replace(/([0-9]{2})\/([0-9]{2})\/([0-9]{4})/gi, "{3}-{2}-{1}");
              if(atime > btime) return 1;
              if(atime < btime) return -1;
              return 0;
          });
          
          var date, offset, ts, prevts, prev;
          
          var first = true;
          for(var i = 0; i < data2.length; i++) {
              var e = data2[i];

              if(e.cleartime) {
                var tt = getDateTime(e.cleartime);
                if (first) {
                    date = ""+tt.year+"-"+tt.month+"-"+tt.day+" ";
                    ts = {h:0, m:0, s:0};
                    offset =  {h:0, m:0, s:0};
                    first = false;
                }
                else {
                    offset = difference(prev, tt);
                    prevts = ts;
                    ts = add(ts, offset);
                }
                
                  
                if(offset.s > 1 || offset.m > 0 || offset.h > 0) {
                    var iterations = (3600*offset.h + 60*offset.m + offset.s) - 1 ;
                    var currentTime = prevts;
                    for (var y = 0 ; y < iterations; y++) {
                        currentTime = add(currentTime, {h:0, m:0, s:1});
                        var o = { time: date+makeTimeStamp(currentTime) };
                        o[$scope.selectedMeasure] = null;
                        data.push(o);        
                    }          
                }
                prev = tt ;
                var x = { time: date+makeTimeStamp(ts) };
                x[$scope.selectedMeasure] = e[$scope.selectedMeasure];
                //console.log(x);
                data.push(x);
              }
              
          }
          return data;
      }
      
      /* PRE : both allTheData and data are normalized,
               begin at 0 and have no gaps.
               data.length <= allTheData.length
      */
      function mergeWithGlobalData(data, attributes) {
         var x = $scope.accessors.length;

         var limit = data.length;
         for(var i=0; i < limit; i++) {
            if(i >= $scope.allTheData.length) {
                    $scope.allTheData.push({ time: data[i].time });
                    for(var j = 0; j < $scope.accessors.length; j++) {
                        $scope.allTheData[i][$scope.accessors[j]] = null; 
                    }
            }
            for(var a = 0; a < attributes.length; a++) {
                $scope.allTheData[i][(x+a)+"-"+attributes[a]] = data[i][attributes[a]];
            }

         }
         for(var a = 0; a < attributes.length; a++) {
                $scope.accessors.push((x+a)+"-"+attributes[a]);       
         }    
      }
         
      $scope.lineColors = ["#004358","#1F8A70","#BEDB39","#FFE11A","#FD7400"] ;
      $scope.fontColors = ["#FFFFFF","#FFFFFF","#333333","#333333","#333333"] ;
      $scope.updateGraph = function() {
          var data = normalize_data();
          mergeWithGlobalData(data,[$scope.selectedMeasure]);
          $scope.labels.push($scope.selectedSession.name+" ("+$scope.selectedInstance+") : "+$scope.selectedMeasure);
          $("#legends").append("<a href=\"#\" class=\"list-group-item\" style='background-color:"+$scope.lineColors[$scope.labels.length - 1]+";  color:"+$scope.fontColors[$scope.labels.length - 1]+"'><i class=\"fa fa-bolt fa-fw\"></i>"+$scope.selectedMeasure+"<span class=\"pull-right text-muted small\"  style='color:"+$scope.fontColors[$scope.labels.length - 1]+";'><em>"+$scope.selectedSession.name+" ("+$scope.selectedInstance+")</em></span></a>");
          $("#morris-area-chart").empty();
          Morris.Line({
                element: 'morris-area-chart',
                data: $scope.allTheData,
                continuousLine: true,
                lineColors: $scope.lineColors ,
                xkey: 'time',
                ykeys: $scope.accessors,
                labels: $scope.labels,
                pointSize: 2,
                hideHover: 'auto',
                resize: true
          });
      }     
  });

})();