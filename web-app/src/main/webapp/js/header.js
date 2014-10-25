$(document).ready(function () {
	$(".langselect select").change(function (event) {
		$(".langselect form").submit();
	});
	
	var toggle_login = $(".login").attr("data-error") == "true";
	if(!toggle_login)
		$(".login").hide();
	else {
		$(".menubutton").css('border-bottom', 'none');
	}
	$(".menubutton.loginButton").click(function (event) {
		if(toggle_login) {
			$(".login").slideUp(function () {
				$(".menubutton").css('border-bottom', '1px solid #666666');
			});
			
		} else {
			$(".login").slideDown();
			$(".menubutton").css('border-bottom', 'none');
		}
		toggle_login = !toggle_login;
	});
	$("#logoutLink").click(function() {
		$("#logoutForm").submit();
	});
});