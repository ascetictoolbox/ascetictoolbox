$(document).ready(function() {
    $.getJSON("js/components.js", function(json) {
        var items = [];
        for(var i = 0 ; i < json.length ; i++) {
            items.push( "<li class='navitem" + (i==0 ? " active":"") + "'><a id='" + json[i].id + "' href='"
                       + "#" /*json[i].url*/ + "'>" + json[i].title + "</a></li>" );            
        }
        $("#paas-components").append(items.join(""));
        $("#component-view").attr("src",json[0].url);
        $(".navitem").click(function(event) {
            $(".navitem").removeClass("active");
            $(event.currentTarget).addClass("active");
            console.log(event);         
        });
    });
});