function CO2Checker() {
    var self = this;
    kango.ui.browserButton.addEventListener(kango.ui.browserButton.event.COMMAND, function() {
        self._onCommand();
    });
	self.refresh();
	window.setInterval(function(){self.refresh()}, self._refreshTimeout);
}

CO2Checker.prototype = {
	_refreshTimeout: 5000,
	_code: '954e31d7-6746-440e-b4ad-47ef8d5443c0',
	_siteUrl: 'http://indoor-climate.appspot.com',

    _onCommand: function() {
		var self = this;
        kango.browser.tabs.create({url: self._siteUrl + '/?code=' + self._code});
    },
	
	refresh: function() {
		var self = this;
		var details = {
			url: self._siteUrl + '/data?code=' + self._code,
			method: 'GET',
			async: true,
			contentType: 'json'
		};
		kango.xhr.send(details, function(data) {
			if(data.status == 200) {
				var co2 = data.response.co2;
				kango.ui.browserButton.setBadgeValue(co2);
			}
		});
	}
};

var extension = new CO2Checker();