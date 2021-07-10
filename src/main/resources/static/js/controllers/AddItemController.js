/* global app */

'use strict';

app.controller('AddItemController', ['$rootScope', 'entity', 'addItem', 'NotificationService',
    function ($rootScope, entity, addItem, NotificationService) {
        let vm = this;

        // public
        function addItemPromise() {
            vm.item.errors = {};
            return addItem(vm.item).then(
                function (data) {
                    //Success
                    vm.result = data;
                    vm.success = 'Registered successfully!';

                    //Clean the item object
                    vm.item = {};

                    //Sanitize entity type
                    let entityName = entity || 'entity';

                    //Capitalize first letter
                    entityName = entityName.charAt(0).toUpperCase() + entityName.slice(1);

                    //Notify the user
                    NotificationService.notify(entityName + ' successfully created.', 'success')
                },
                function (response) {
                    //Failure, add the received errors to the item object
                    vm.item.errors = response.responseJSON.detailMessages || {};
                }
            ).then(function () {
                //Trigger angular digest for $watch checks if not already in progress
                if (!$rootScope.$$phase) {
                    $rootScope.$digest();
                }
            });
        }

        // expose
        angular.extend(vm,
            {
                item: {},
                addItem: addItemPromise,
            });
    }]);

