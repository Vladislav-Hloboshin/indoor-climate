function CO2Checker() {
    var self = this;
    kango.ui.browserButton.addEventListener(kango.ui.browserButton.event.COMMAND, function() {
        self._onCommand();
    });
	self.refresh();
	window.setInterval(function(){self.refresh()}, self._refreshTimeout);
}

CO2Checker.prototype = {
	_refreshTimeout: 1000*60,//1 min
	_code: '954e31d7-6746-440e-b4ad-47ef8d5443c0',
	_siteUrl: 'http://indoor-climate.appspot.com',
	_co2firstLevel: 800,
	_co2seconLevel2: 900,
	_warningTime: null,
	_warningInterval: 1000*60*5,//5 min

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
				kango.ui.browserButton.setBadgeValue(co2 > self._co2firstLevel ? co2 : null);
				var now = new Date();
				if(co2 > self._co2seconLevel2 && (self._warningTime===null || (now.getTime()-self._warningTime.getTime())>self._warningInterval)){
					kango.ui.notifications.show('Предупреждение', 'Текущий уровень CO2 равен '+co2+'ppm. Это превышает рекомендуемый уровень равный 800ppm', 'icons/icon100.png');
					self._warningTime = now;
				}
			}
		});
	}
};

var extension = new CO2Checker();