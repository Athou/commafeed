$(function() {
	var reg = $('#register-panel');
	if (!reg) {
		return;
	}
	$('#login-panel').height(reg.height());
});