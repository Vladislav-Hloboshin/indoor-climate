var storage = window.localStorage;

function CO2Checker() {
    var self = this;
    if(storage.code){
        self.code = storage.code;
    }
    if(storage.active==='true'){
        self.start();
    } else {
        kango.ui.browserButton.setBadgeValue("off");
    }
    window.addEventListener('storage', function(e) {
        switch(e.key){
            case 'active':
                if(e.newValue==='true') self.start();
                else self.stop();
                break;
            case 'code':
                self.code = e.newValue;
                self.refresh();
                break;
            case 'alwaysShowCO2Level':
                self.refresh();
                break;
        }
    });
    kango.ui.browserButton.addEventListener(
        kango.ui.browserButton.event.COMMAND,
        function(){
            if(self.code) kango.browser.tabs.create({url: self.siteUrl + '/?code=' + self.code});
        }
    );
}

CO2Checker.prototype = {
    active: false,
    refreshTimeout: 60*1000,//1 min
    code: '',
    siteUrl: 'http://indoor-climate.appspot.com',
    co2WarningLevel: 800,
    co2NotificationLevel: 1000,
    lastWarningTime: null,
    warningInterval: 10*60*1000,//10 min
    intervalId: null,
    
    refresh: function() {
        var self = this,
            details = {
                url: this.siteUrl + '/data?code=' + this.code,
                method: 'GET',
                async: true,
                contentType: 'json'
            };
        if(!self.active || self.code==='') return;
        kango.xhr.send(details, function(data) {
            if(!self.active || self.code==='') return;
            if(data.status == 200) {
                var co2 = data.response.co2;
                kango.ui.browserButton.setBadgeValue(storage.alwaysShowCO2Level==='true' || co2 > self.co2WarningLevel ? co2 : null);
                var now = new Date();
                if(co2 > self.co2NotificationLevel && (self.lastWarningTime===null || (now.getTime()-self.lastWarningTime.getTime())>self.warningInterval)){
                    kango.ui.notifications.show('Предупреждение', 'Текущий уровень CO2 равен '+co2+'ppm. Это превышает рекомендуемый уровень равный 800ppm', 'icons/icon100.png');
                    self.lastWarningTime = now;
                }
            }
        });
    },
    
    start: function(){
        var self = this;
        if(self.active) return;
        self.active = true;
        kango.ui.browserButton.setBadgeValue(null);
        this.refresh();
        this.intervalId = window.setInterval(function(){self.refresh()}, this.refreshTimeout);
    },
    
    stop: function(){
        var self = this;
        if(!self.active) return;
        self.active = false;
        window.clearInterval(self.intervalId);
        kango.ui.browserButton.setBadgeValue("off");
    }
};

var extension = new CO2Checker();
