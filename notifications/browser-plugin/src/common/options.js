var storage = window.localStorage;

$(window).ready(function(){
    var $inputActive = $('input#active');
    $inputActive.prop('checked',storage.active==='true');
    $inputActive.change(function(){
        storage.active = $inputActive.prop('checked');
    });
    
    var $inputCode = $('input#code');
    $inputCode.val(storage.code);
    $inputCode.change(function(){
        storage.code = $inputCode.val();
    });
    
    var $inputAlwaysShowCO2Level = $('input#alwaysShowCO2Level');
    $inputAlwaysShowCO2Level.prop('checked',storage.alwaysShowCO2Level==='true');
    $inputAlwaysShowCO2Level.change(function(){
        storage.alwaysShowCO2Level = $inputAlwaysShowCO2Level.prop('checked');
    });
});
