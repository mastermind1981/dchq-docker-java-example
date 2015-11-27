'use strict';

var NameDirectoryController = function($scope, $http) {
    $scope.names = {};

    $scope.fetchNamesList = function() {
        $http.get('names/getList.json').success(function(list){
            $scope.names = list;
        });
    };

    $scope.addNewName = function(newItem) {
        $scope.resetError();

        $http.post('names/add', newItem).success(function() {
            $scope.fetchNamesList();
            $scope.name.firstName = '';
            $scope.name.lastName = '';
        }).error(function() {
            $scope.setError('Could not add a new name');
        });
    };

    $scope.removeName = function(id) {
        $scope.resetError();

        $http.delete('names/remove/' + id).success(function() {
            $scope.fetchNamesList();
        }).error(function() {
            $scope.setError('Could not remove name');
        });
        $scope.name.firstName = '';
        $scope.name.lastName = '';
    };

    $scope.removeAllNames = function() {
        $scope.resetError();

        $http.delete('names/removeAll').success(function() {
            $scope.fetchNamesList();
        }).error(function() {
            $scope.setError('Could not remove all names');
        });
    };

    $scope.resetNamesForm = function() {
        $scope.resetError();
        $scope.names = {};
        $scope.editMode = false;
    };

    $scope.resetError = function() {
        $scope.error = false;
        $scope.errorMessage = '';
    };

    $scope.setError = function(message) {
        $scope.error = true;
        $scope.errorMessage = message;
    };

    $scope.fetchNamesList();

    $scope.predicate = 'id';
};