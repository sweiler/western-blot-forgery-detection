function thumbUrl(id) {
	return "/ForgeryWeb/Thumbnails/" + id + "/";
}

function shorten(data) {
	if (data.length > 20) {
		data = data.substring(0, 19) + "&hellip;";
	}
	return data;
}

function createFileItem(elem) {
	var item = $("<div class='fileitem'></div>");
	var deleteButton = $("<div class='deleteButton'></div>").appendTo(item);
	var thumb = $(
			"<div class='filethumb'><img src='" + thumbUrl("dummy")
					+ "' width='244' height='110' /></div>").appendTo(item);
	var filename = $("<div class='filename'></div>").appendTo(item);
	var state = $("<div class='filestate'></div>").appendTo(item);
	var progressBar = $("<div class='progressBar'><div></div></div>").appendTo(
			item);
	var abort = $("<button class='abort'>Abort</button>").appendTo(item);

	item.css("visibility", "hidden");
	item.css("width", "0px");
	item.animate({
		"width" : "244px"
	}, 300, "swing", function() {
		item.css("visibility", "visible");

	});
	item.prependTo(elem);
	state.hide();
	
	

	this.continueButton = null;

	this.setProgress = function(progress) {
		progressBar.find('div').animate({
			width : progress + "%"
		}, 10).html(progress + "% ");

		if (parseInt(progress) >= 100) {
			abort.hide();
			if (this.continueButton == null)
				this.continueButton = $(
						"<div class='bigbutton'><a href='javascript:void(0)'>" + lang_continue
								+ "</a></div>").appendTo(item);
			progressBar.hide();
			
			state.text("Hochgeladen");
			state.show();
			addUpdater(item);
		}
	};

	this.setFileNameSize = function(name, size) {
		filename.html(shorten(name));
	};

	this.setAbort = function(jqxhr) {
		abort.click(function() {
			jqxhr.abort();
			item.hide();
		});
	};

	this.setFileId = function(id) {
		item.attr("data-id", id);
		thumb.find("img").attr("src", thumbUrl(id));
		this.continueButton.find("a").click(function(event) {
			continueButtonHandler(this.item, id);
		});
		deleteButton.click(function () {
			var deleteReally = window.confirm(lang_delete);
			if(deleteReally) {
				$.ajax(contentPath + "/filestate/" + id, {
					method: "DELETE",
					success: function () {
						document.location.href = contentPath + "/";
					}
				});
			}
		});
	};
	
	this.setFileType = function (fileType) {
		if(fileType == "PDF" && this.continueButton != null) {
			this.continueButton.hide();
			state.attr("data-state", "NEWLY_CREATED");
			addUpdater(item, true);
		}
	}

}

var factorW;
var factorH;

function continueButtonHandler(item, id) {
	openDialog(item, id);
	$("div.image img").load(function () {
		$.ajax({
			url : contentPath + "/rects/" + id,
			type : "GET",
			contentType : false,
			processData : false,
			cache : false,
			dataType : "json",
			success : function(data, statusX, xhrobj) {
				if(xhrobj.status >= 200 && xhrobj.status <= 299) {
					
					$("div.image").attr("data-width", data.imgWidth);
					$("div.image").attr("data-height", data.imgHeight);
					var img_w = $("div.image img").width();
					var img_h = $("div.image img").height();
					factorW = data.imgWidth / img_w;
					factorH = data.imgHeight / img_h;
					
					
					
					var new_rects = $(data.data).appendTo($("div.image"));
					new_rects.each(function (i) {

						var leftCSS = $(this).css("left");
						leftCSS = leftCSS.substring(0, leftCSS.length - 2) / factorW;
						
						var topCSS = $(this).css("top");
						topCSS = topCSS.substring(0, topCSS.length - 2) / factorH;
						
						var widthCSS = $(this).css("width");
						widthCSS = widthCSS.substring(0, widthCSS.length - 2) / factorW;
						
						var heightCSS = $(this).css("height");
						heightCSS = heightCSS.substring(0, heightCSS.length - 2) / factorH;
						
						$(this).css("left", leftCSS + "px");
						$(this).css("top", topCSS + "px");
						$(this).css("width", widthCSS + "px");
						$(this).css("height", heightCSS + "px");
						
						$(this).click(function (event) {
							if(removing_active) {
								$(this).remove();
							}
						});
					});
					
				} else {
					alert("Error: Status " + xhrobj.status);
				}
				
			},
			error : function(xhr, text, error) {
				alert(text + " " + error);
			}
		});
	});
	
}

function saveRects(image_area, id) {
	var formData = {};
	
	var num = 0;
	
	image_area.find('.rect').each(function (i) {
		var t = $(this).css('top');
		t = t.substring(0,t.length - 2);
		var l = $(this).css('left');
		l = l.substring(0,l.length - 2);
		var w = $(this).css('width');
		w = w.substring(0,w.length - 2);
		var h = $(this).css('height');
		h = h.substring(0,h.length - 2);
		num++;
		formData['rect' + i + '_t'] = t * factorH;
		formData['rect' + i + '_l'] = l * factorW;
		formData['rect' + i + '_w'] = w * factorW;
		formData['rect' + i + '_h'] = h * factorH;
	});
	formData['num'] = num;
	$.post(
		contentPath + "/rects/" + id,
		formData,
		function(data, statusX, xhrobj) {
			if(!(xhrobj.status >= 200 && xhrobj.status <= 299)) {
				alert("Error: Status " + xhrobj.status);
			} else {
				$(".overlay .closebutton").click();
				$(".fileitem[data-id='" + id + "'] .filestate").text(lang_states["START_ANALYZE"]);
				$(".fileitem[data-id='" + id + "'] .filestate").attr("data-state", "START_ANALYZE");
				addUpdater($(".fileitem[data-id='" + id + "']"));
			}
			
		}
	);
}
var removing_active = false;
function openDialog(item, id) {
	var darkness = $("<div class='darkness'></div>").appendTo($("body"));
	darkness.hide();
	darkness.fadeIn();
	var overlay = $("<div class='overlay'></div>").appendTo($("body"));
	var active_rect = null;
	var closebutton = $("<div class='closebutton'></div>").appendTo(overlay);
	$("<div class='headline'>" + lang_roi + "</div>").appendTo(overlay);
	var buttonline = $("<div class='buttonline'></div>").appendTo(overlay);
	var image_area = $("<div class='image'><img src='data/" + id + "' /></div>").appendTo(
			overlay);
	var compute_button = $("<div class='bigbutton'><a href='#'>" + lang_start
			+ "</a></div>").appendTo(overlay);
	compute_button.click(function (event) {
		saveRects(image_area, id);
		$(item).find(".bigbutton").hide();
	});
	var adding_started = false;
	var adding_active = false;
	
	
	var adding_x = 0, adding_y = 0;
	
	image_area.mousedown(function (event) {
		if(adding_active) {
			active_rect = $("<div class='rect'></div>").appendTo(image_area);
			adding_x = event.pageX - $(this).offset().left;
			adding_y = event.pageY - $(this).offset().top;
			active_rect.css("left", adding_x + "px");
			active_rect.css("top", adding_y + "px");
			active_rect.click(function (event) {
				if(removing_active) {
					$(this).remove();
				}
			});
			adding_active = false;
			adding_started = true;
		}
		event.preventDefault();
	});
	
	image_area.mouseup(function (event) {
		if (adding_started) {
			active_rect = null;
			adding_started = false;
			adding_active = true;
			
		}
	});
	
	image_area.mousemove(function (event) {
		if(adding_started) {
			var current_x = event.pageX - $(this).offset().left;
			var current_y = event.pageY - $(this).offset().top;
			if(current_x > adding_x) {
				active_rect.css("left", adding_x + "px");
				active_rect.css("width", (current_x - adding_x) + "px");
			} else {
				active_rect.css("left", current_x + "px");
				active_rect.css("width", (adding_x - current_x) + "px");
			}
			if(current_y > adding_y) {
				active_rect.css("top", adding_y + "px");
				active_rect.css("height", (current_y - adding_y) + "px");
			} else {
				active_rect.css("top", current_y + "px");
				active_rect.css("height", (adding_y - current_y) + "px");
			}
			
		}
	});
	
	
	
	

	$("<button><img src='images/add.png' /></button>").appendTo(buttonline)
			.click(function(event) {
				if(adding_active) {
					image_area.css("cursor", "default");
					adding_active = false;
				} else {
					image_area.css("cursor", "crosshair");
					adding_active = true;
					removing_active = false;
				}
			});
	
	$("<button><img src='images/remove.png' /></button>").appendTo(buttonline)
	.click(function(event) {
		adding_active = false;
		if(removing_active) {
			image_area.css("cursor", "default");
			removing_active = false;
		} else {
			image_area.css("cursor", "url(images/cursor_remove.gif), default");
			removing_active = true;
		}
	});

	overlay.hide();
	overlay.fadeIn();
	closebutton.click(function(event) {
		darkness.fadeOut(400, function() {
			darkness.remove();
			overlay.remove();
		});
		overlay.fadeOut();
	});
}

function handleFileUpload(files, obj) {
	for ( var i = 0; i < files.length; i++) {
		var fd = new FormData();
		fd.append('file', files[i]);
		fd.append('filename', files[i].name);

		var status = new createFileItem(obj);
		status.setFileNameSize(files[i].name, files[i].size);
		sendFileToServer(fd, status);

	}
}

function sendFileToServer(formData, status) {
	
	// URL
	var jqXHR = $.ajax({
		xhr : function() {
			var xhrobj = $.ajaxSettings.xhr();
			if (xhrobj.upload) {
				xhrobj.upload.addEventListener('progress', function(event) {
					var percent = 0;
					var position = event.loaded || event.position;
					var total = event.total;
					if (event.lengthComputable) {
						percent = Math.ceil(position / total * 100);
					}
					// Set progress
					status.setProgress(percent);
				}, false);
			}
			return xhrobj;
		},
		url : contentPath + "/FileUpload",
		type : "POST",
		contentType : false,
		processData : false,
		cache : false,
		data : formData,
		dataType : "json",
		success : function(jsonData, statusX, xhrobj) {
			
			status.setFileId(jsonData.id);
			status.setProgress(100);
			status.setFileType(jsonData.type);
			checkComeback();
			// $("#status1").append("File upload Done<br>");
		},
		error : function(xhr, text, error) {
			alert(text + " " + error);
		}
	});

	status.setAbort(jqXHR);
}

$(window).resize(function (event) {
	var img_w_new = $("div.image img").width();
	var img_h_new = $("div.image img").height();
	var imgWidth = $("div.image").attr("data-width");
	var imgHeight = $("div.image").attr("data-height");
	var factorW_new = imgWidth / img_w_new;
	var factorH_new = imgHeight / img_h_new;
	$("div.image .rect").each(function (i) {
		var leftCSS = $(this).css("left");
		leftCSS = leftCSS.substring(0, leftCSS.length - 2) * factorW / factorW_new;
		
		var topCSS = $(this).css("top");
		topCSS = topCSS.substring(0, topCSS.length - 2) * factorH / factorH_new;
		
		var widthCSS = $(this).css("width");
		widthCSS = widthCSS.substring(0, widthCSS.length - 2) * factorW / factorW_new;
		
		var heightCSS = $(this).css("height");
		heightCSS = heightCSS.substring(0, heightCSS.length - 2) * factorH / factorH_new;
		
		$(this).css("left", leftCSS + "px");
		$(this).css("top", topCSS + "px");
		$(this).css("width", widthCSS + "px");
		$(this).css("height", heightCSS + "px");
	});
	factorW = factorW_new;
	factorH = factorH_new;
});

$(document).ready(
		function() {

			var elem = $("#dropfield");

			$(document).on('dragenter', function(e) {
				e.stopPropagation();
				e.preventDefault();
			});

			$(document).on('dragover', function(e) {
				e.stopPropagation();
				e.preventDefault();
				elem.removeClass("dragenter");
			});

			$(document).on('drop', function(e) {
				e.stopPropagation();
				e.preventDefault();
			});

			elem.on('dragenter', function(e) {
				e.stopPropagation();
				e.preventDefault();
				$(this).addClass("dragenter");
			});
			elem.on('dragover', function(e) {
				e.stopPropagation();
				e.preventDefault();
			});
			elem.on('drop', function(e) {

				$(this).removeClass("dragenter");
				e.preventDefault();
				var files = e.originalEvent.dataTransfer.files;

				handleFileUpload(files, elem);
			});

			$(".fileitem").each(
					function(index) {
						var itm = $(this);
						addUpdater(itm);
						
						if(itm.attr("data-id") != null) {
							deleteButton = itm.find(".deleteButton");
							deleteButton.click(function () {
								var deleteReally = window.confirm(lang_delete);
								if(deleteReally) {
									$.ajax(contentPath + "/filestate/" + itm.attr("data-id"), {
										method: "DELETE",
										success: function () {
											document.location.href = contentPath + "/";
										}
									});
								}
							});
						}

						if (itm.find(".bigbutton").attr("data-id") != null && itm.find(".bigbutton a").attr("href") == "javascript:void(0)") {
							itm.find(".bigbutton a").click(
									function(event) {
										continueButtonHandler(itm, itm.find(
												".bigbutton").attr("data-id"));
									});
						}
					});
			$("#comebacklater").hide();
			checkComeback();
			$("#comebacklater a").click(function () {
				var darkness = $("<div class='darkness'></div>").appendTo($("body"));
				darkness.hide();
				darkness.fadeIn();
				var overlay = $("<div class='overlaySmall'></div>").appendTo($("body"));
				var closebutton = $("<div class='closebutton'></div>").appendTo(overlay);
				$("<div class='headline'>" + lang_comeback + "</div>").appendTo(overlay);
				var authToken = $(this).attr("data-authToken");
				var path = window.location.protocol + "//" + window.location.host + contentPath;
				$("<div class='description'>"+ lang_comeback_desc + "</div>").appendTo(overlay);
				$("<div class='comebackform'><input type='text' value='" + path + "/index.html?authToken=" + authToken + "' /></div>").appendTo(overlay);
				$(".comebackform input").select();
				closebutton.click(function(event) {
					darkness.fadeOut(400, function() {
						darkness.remove();
						overlay.remove();
					});
					overlay.fadeOut();
				});
			});
		});

function checkComeback() {
	$.get(contentPath + "/userstate/", function (data) {
		if(data == "false") {
			$("#comebacklater").hide();
		} else {
			$("#comebacklater").show();
			$("#comebacklater a").attr("data-authToken", data);
		}
	});
}

function addUpdater(item, pdf) {
	var state = item.find(".filestate");
	pdf = typeof pdf !== 'undefined' ? pdf : false;
	if(pdf)
		state.text(lang_states["EXTRACTING"]);
	if(state.attr("data-state") == "PROCESSING" || state.attr("data-state") == "START_ANALYZE"
		|| (pdf && state.attr("data-state") == "NEWLY_CREATED") ) {
		var spinner = $("<div class='spinner'></div>");
		state.before(spinner);
		spinner.spin({lines : 9, length : 2, width : 2, radius : 4, speed : 0.8});
		item.interval = window.setInterval(function () {
			$.get(contentPath + "/filestate/" + item.attr("data-id"), function (data) {
				if(pdf && data == "EXTRACTED") {
					document.location.href = contentPath + "/";
				} else if(data != "PROCESSING" && data != "START_ANALYZE"  ) {
					window.clearInterval(item.interval);
					spinner.spin(false);
					spinner.hide();
					if(data == "FINISHED") {
						item.find(".bigbutton").show();
						if(item.find(".toReports").length == 0) {
							$("<a class='toReports' href='" + 
									contentPath + 
									"/reports/" + 
									item.attr("data-id") + "'>" + 
									lang_toReports + 
									"</a>").insertBefore(item.find(".bigbutton"));
						}
					}
				} else {
					item.find(".bigbutton").hide();
				}
				if(pdf)
					state.text(lang_states["EXTRACTING"]);
				else
					state.text(lang_states[data]);
			});
			
		}, 5000);
	}
}

