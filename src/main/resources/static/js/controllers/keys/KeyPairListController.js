/* global app */

app.controller('KeyPairListController',
    ['$scope', '$controller', '$q', 'keyPairList', 'addKeyPair', 'deleteKeyPair', 'KeyPairService', 'NotificationService',
        function ($scope, $controller, $q, keyPairList, addKeyPair, deleteKeyPair, KeyPairService, NotificationService) {
            let vm = this;

            vm.generation = {
                name: "",
                error: false
            };

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {

            })();

            /**
             * [Public]
             * Allows to download the public key of a key pair with a certain ID.
             * @param keyPairId the ID of the key pair
             */
            function downloadPublicKey(keyPairId) {
                //Search for key pair with matching ID
                let keyPair = null;
                for (let i = 0; i < keyPairList.length; i++) {
                    //Check for matching ID
                    if (keyPairList[i].id === keyPairId) {
                        keyPair = keyPairList[i];
                        break;
                    }
                }

                //Check if key pair could be found
                if (keyPair == null) {
                    return;
                }

                //Prepare download file as blob
                let filename = keyPair.name + ".pubk";
                let blob = new Blob([keyPair.publicKey], {
                    type: "text/plain;charset=utf-8"
                });

                //Initiate download
                saveAs(blob, filename);
            }

            /**
             * [Public]
             * Called when the user submitted the form form generating a key pair.
             */
            function generateKeyPair() {
                KeyPairService.generate(vm.generation.name).then(function (response) {
                    //Sanity check
                    if (!response.data) {
                        return;
                    }
                    vm.keyPairListCtrl.pushItem(response.data);
                    vm.generation.error = false;

                    NotificationService.notify("Successfully generated a new key.", "success");

                    $("#generateKeyPairModal").modal('hide');
                }, function (response) {
                    if (response.status === 400) {
                        vm.generation.error = "Invalid name or a key pair with this name already exists.";
                    }
                    //Failure
                    NotificationService.notify("Could not generate a new key.", "error");
                });
            }

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain key pair. It also
             * shows a list of all devices that are affected by this deletion.
             *
             * @param data A data object that contains the id of the key pair that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                let keyPairID = data.id;
                let keyPairName = "";
                let affectedWarning = "";

                //Determines the key pair's name by checking all key pairs in the list
                for (let i = 0; i < keyPairList.length; i++) {
                    if (keyPairID === keyPairList[i].id) {
                        keyPairName = keyPairList[i].name;
                        break;
                    }
                }

                //Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete key pair',
                    type: 'warning',
                    html: "Are you sure you want to delete the key pair \"" +
                        keyPairName + "\"?" + affectedWarning,
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }

            //Expose controllers
            angular.extend(vm, {
                keyPairListCtrl: $controller('ItemListController as keyPairListCtrl',
                    {
                        $scope: $scope,
                        list: keyPairList
                    }),
                addKeyPairCtrl: $controller('AddItemController as addKeyPairCtrl',
                    {
                        $scope: $scope,
                        addItem: addKeyPair
                    }),
                deleteKeyPairCtrl: $controller('DeleteItemController as deleteKeyPairCtrl',
                    {
                        $scope: $scope,
                        deleteItem: deleteKeyPair,
                        confirmDeletion: confirmDelete
                    }),
                generateKeyPair: generateKeyPair,
                downloadPublicKey: downloadPublicKey
            });

            // Watch addition of new items
            $scope.$watch(() => vm.addKeyPairCtrl.result,
                function () {
                    //Callback
                    let data = vm.addKeyPairCtrl.result;

                    //Sanity check
                    if (!data) {
                        return;
                    }

                    //Close modal on success
                    $("#addKeyPairModal").modal('toggle');

                    //Add new item to list
                    vm.keyPairListCtrl.pushItem(data);
                }
            );

            // $watch 'deleteItem' result and remove from 'itemList'
            $scope.$watch(
                function () {
                    // value being watched
                    return vm.deleteKeyPairCtrl.result;
                },
                function () {
                    var id = vm.deleteKeyPairCtrl.result;
                    vm.keyPairListCtrl.removeItem(id);
                }
            );
        }
    ]
);
