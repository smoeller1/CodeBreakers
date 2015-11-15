describe('TodoCtrl', function() {
    
    var $controller;
    
    beforeEach(module('starter'));
    
    beforeEach(inject(function(_$controller_){
        $controller = _$controller_;
    }));
    
    
    describe('calcRoute', function() {
        it('Tests calcRoute function of TodoCtrl', function() {
            var $scope = {};
            var controller = $controller('TodoCtrl', { $scope: $scope });
            var endLoc = "Kansas City, MO";
            var startLoc = "Olathe, KS";
            expect($scope.calcRoute(startLoc, endLoc)).toEqual(true);  //function ran successfully
        });
    });
    
    describe('setRequest', function() {
        it('Tests setRequest origin function of TodoCtrl', function() {
            var $scope = {};
            var controller = $controller('TodoCtrl', { $scope: $scope });
            var endLoc = "Kansas City, MO";
            var startLoc = "Olathe, KS";
            expect($scope.setRequest(startLoc, endLoc).origin).toEqual(startLoc);
        });
    });
    
    describe('setRequest', function() {
        it('Tests setRequest driving mode function of TodoCtrl', function() {
            var $scope = {};
            var controller = $controller('TodoCtrl', { $scope: $scope });
            var endLoc = "Kansas City, KS";
            var startLoc = "Liberty, MO";
            expect($scope.setRequest(startLoc, endLoc).travelMode).toEqual(google.maps.TravelMode.DRIVING);
        });
    });
    
});
