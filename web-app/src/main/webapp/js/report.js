var factorW, factorH;

function updateFactors() {
	var img = $(".report_content img");
	var img_w = img.width();
	var img_h = img.height();
	factorW = img.attr("data-width") / img_w;
	factorH = img.attr("data-height") / img_h;
	$(".report_rect").each(function () {
		var rect = $(this);
		rect.css("top", (rect.attr("data-y") / factorH) + "px");
		rect.css("height", (rect.attr("data-height") / factorH) + "px");
		rect.css("left", (rect.attr("data-x") / factorW) + "px");
		rect.css("width", (rect.attr("data-width") / factorW) + "px");
	});
}

function magnify(id) {
	$(".report_images img").addClass("hidden");
	$(".report_images img[data-id='" + id + "']").removeClass("hidden");
}




$(document).ready(function () {
	$(".report_images img").addClass("hidden");
	$(".report_images img").first().removeClass("hidden");
	$(".report_rect").first().addClass("selected");
	$(".report_rect").first().next().addClass("selected");
	$(".report_content img").load(function () {
		updateFactors();
		$(".report_rect").mouseenter(function () {
			$(".report_rect").removeClass("selected");
			$(this).addClass("selected");
			magnify($(this).parent().attr("data-id"));
			var nextOne = $(this).next();
			var prevOne = $(this).prev();
			if(nextOne.length == 1) {
				$(this).next().addClass("selected");
				
			} else if(prevOne.length == 1) {
				$(this).prev().addClass("selected");
			
			}
			
		});
		
	});
});

$(window).resize(function (event) {
	updateFactors();
});