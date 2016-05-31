
function objToString (obj) {
    var str = '';
    for (var p in obj) {
        if (obj.hasOwnProperty(p)) {
            str += p + '::' + obj[p] + '\n';
        }
    }
    return str;
}






function getAllMetrics(metrics) {
         var paths = [];
         for(var m in metrics) {
             if(metrics[m] instanceof Object) {
                 var ppaths = getAllMetrics(metrics[m]);
                 if(ppaths) for(var p in ppaths) {
                     paths.push(m + "." + ppaths[p]);
                 }
             } else if(typeof metrics[m] == "number") { // only adds properties that parse into a number
                 paths.push(m);
             }
         }
         return paths;
     }


function getRandomInt(min, max) {
  return Math.floor(Math.random() * (max - min)) + min;
}

function sleep(milliseconds) {
  var start = new Date().getTime();
  for (var i = 0; i < 1e7; i++) {
    if ((new Date().getTime() - start) > milliseconds){
      break;
    }
  }
}
