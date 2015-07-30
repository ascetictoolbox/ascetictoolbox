(function() {
	var currentView;
	var getIdFromTitle = function(title) {
		return title.toLowerCase().split(/[^(a-z)]/).join("");
	}
	var showPage = function(id, url) {
		var fid =  "#f_"+id;
		if(fid !== currentView) {
			$(currentView).hide();
			var iframe = $(fid);
			if(iframe.length) {
				$(fid).show();
			} else {
				$("#component-view").append("<iframe id='f_"+id+"' src='"+url+"'></iframe>");
			}
		}
		currentView = fid;
	}

	$(document).ready(function() {
		$.getJSON("js/components.json", function(json) {

			var items = [];
			for(var i = 0 ; i < json.length ; i++) {
				items.push( "<li class='navitem" + (i==0 ? " active":"") + "' id='" + getIdFromTitle(json[i].title)
					+ "'><a href='" + json[i].url + "'>" + json[i].title + "</a></li>" );
			}
			$("#paas-components").append(items.join(""));
			showPage(getIdFromTitle(json[0].title),json[0].url);
			$(".navitem").click(function(event) {
				event.preventDefault();
				$(".navitem").removeClass("active");
				$(event.currentTarget).addClass("active");
				var id = $(event.currentTarget).attr("id");
				var url = $(event.target).attr("href");
				showPage(id,url);
			});
		});
	});
})();