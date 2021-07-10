/* global app */

/*
 * Controller for the settings page.
 */
app.controller('SettingsController',
    ['$scope', '$q', 'settings', 'documentationMetaData', 'SettingsService', 'NotificationService',
        function ($scope, $q, settings, documentationMetaData, SettingsService, NotificationService) {
            let vm = this;

            //Extend controller for settings and meta data object
            vm.settings = settings;
            vm.documentationMetaData = documentationMetaData;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Check if settings could be loaded
                if (settings == null) {
                    NotificationService.notify("Could not load application settings.", "error");
                }
                //Check if documentation meta data could be loaded
                if (documentationMetaData == null) {
                    NotificationService.notify("Could not load documentation meta data.", "error");
                }
            })();

            /**
             * [Public]
             * Issues a REST request in order to load default operators from the resources directory of the
             * MBP repository and add them to the operator overview so that they are available for all users.
             *
             * @returns A promise of the created REST request
             */
            function addDefaultOperators() {
                //Perform request
                return SettingsService.addDefaultOperators().then(function (response) {
                    //Success callback
                    NotificationService.notify('The default operators were added successfully.', 'success');
                }, function (response) {

                });
            }

            /**
             * [Public]
             * Issues a REST request in order to reinstall default components for the Testing-Tool from the resources directory of the
             * MBP repository. These components are invisible for the user but can be uses via the Testing-Tool.
             *
             * @returns A promise of the created REST request
             */
            function reinstallTestingComponents(){
                // Perform request
                return SettingsService.reinstallTestingComponents().then(function () {
                    //Success callback
                    NotificationService.notify('The default testing components were reinstalled successfully.', 'success');
                })

            }

            /**
             * [Public]
             * Issues a REST request in order to redeploy default components for the Testing-Tool from the resources directory of the
             * MBP repository. These components are invisible for the user but can be uses via the Testing-Tool.
             *
             * @returns A promise of the created REST request
             */
            function redeployTestingComponents(){
                // Perform request
                return SettingsService.redeployTestingComponents().then(function () {
                    //Success callback
                    NotificationService.notify('The default testing components were redeployed successfully.', 'success');
                })

            }

            /**
             * [Public]
             * Issues a REST request in order to save the settings.
             *
             * @returns A promise of the created REST request
             */
            function saveSettings() {
                //Perform request
                return SettingsService.saveSettings(vm.settings).then((response) => {
                    //Success callback
                    NotificationService.notify('The settings were saved successfully.', 'success');
                }, (response) => {
                    //Error callback
                    NotificationService.notify('The settings could not be saved.', 'error');
                    return $q.reject(response);
                });
            }

            //Expose functions that are triggered externally
            angular.extend(vm, {
                addDefaultOperators: addDefaultOperators,
                reinstallTestingComponents: reinstallTestingComponents,
                redeployTestingComponents: redeployTestingComponents,
                saveSettings: saveSettings
            });
        }
    ]);
