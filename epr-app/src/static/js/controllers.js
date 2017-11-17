'use strict';

angular.module('sandManApp.controllers', []).controller('navController', [
    "$rootScope", "$scope", "appsSettings", "fhirApiServices", "userServices", "oauth2", "sandboxManagement", "personaServices", "$location", "$state", "branded", "$timeout", "$window", "$uibModal", "cookieService",
    function ($rootScope, $scope, appsSettings, fhirApiServices, userServices, oauth2, sandboxManagement, personaServices, $location, $state, branded, $timeout, $window, $uibModal, cookieService) {

        $scope.size = {
            navBarHeight: 64,
            footerHeight: 60,
            sandboxBarHeight: 0,
            screenH: 700,
            screenW: 1200
        };

        $scope.showing = {
            signout: false,
            signin: true,
            progress: false,
            loading: false,
            searchloading: false,
            navBar: true,
            sideNavBar: false,
            footer: true,
            largeSidebar: true,
            moreLinks: false,
            apps: true,
            start: false,
            defaultLaunchScenario: true
        };

        $scope.title = {blueBarTitle: branded.mainTitle};
        $scope.sandboxText = branded.sandboxText;
        $scope.copyright = branded.copyright;
        $scope.showCert = branded.showCert;
        $scope.showTermsLink = branded.showTermsLink;
        $scope.userSettingsPWM = branded.userSettingsPWM;
        $scope.loginDoc = branded.loginDoc;
        $scope.mainImage = branded.mainImage;
        $scope.showing.defaultLaunchScenario = branded.defaultLaunchScenario;
        branded.mainImage2x !== undefined ? $scope.mainImage2x = branded.mainImage2x : $scope.mainImage2x = branded.mainImage;
        $scope.whiteImage = branded.whiteImage;
        branded.whiteImage2x !== undefined ? $scope.whiteImage2x = branded.whiteImage2x : $scope.whiteImage2x = branded.whiteImage;
        $scope.imgStyle = function () {
            if (branded.imageStyle !== undefined) {
                return branded.imageStyle;
            }
        };
        $scope.imgTextStyle = function () {
            if (branded.imageTextStyle !== undefined) {
                return branded.imageTextStyle;
            }
        };
        $scope.showing.moreLinks = branded.moreLinks;
        $scope.messages = [];
        $scope.dashboard = {
            sandboxes: [],
            sandbox: {}
        };

        $rootScope.$on('message-notify', function (event, messages) {
            $scope.messages = messages;
            $rootScope.$digest();
        });

        $rootScope.$on("$stateChangeStart", function (event, toState, toParams, fromState, fromParams) {
            if (toState.authenticate && typeof fhirApiServices.fhirClient() === "undefined") {
                // User isn’t authenticated
                if (window.location.hash.indexOf("#/after-auth") !== 0) {
                    $scope.signin();
                }
                event.preventDefault();
            } else if (toState.needsSandbox && !sandboxManagement.hasSandbox()) {
                appsSettings.getSettings().then(function (settings) {
                    if (fhirApiServices.fhirClient().server.serviceUrl === settings.defaultServiceUrl) {
                        $scope.goToDashboard();
                    } else {
                        // User can't go to a page which requires a sandbox without a sandbox
                        $scope.showing.navBar = true;
                        $scope.showing.footer = false;
                        $scope.showing.sideNavBar = false;
                        $state.go('create-sandbox', {});
                    }
                });
                event.preventDefault();
            } else if (toState.name === "progress" && !sandboxManagement.creatingSandbox()) {
//                $scope.signin();
                event.preventDefault();
            } else if (toState.scenarioBuilderStep && sandboxManagement.getScenarioBuilder().userPersona === "") {
                if ($scope.showing.defaultLaunchScenario) {
                    $state.go('launch-scenarios', {});
                } else {
                    $state.go('manage-apps', {});
                }
                event.preventDefault();
            }
        });

        $scope.signin = function () {
            $state.go('login', {});
        };

        $scope.showTerms = function () {
            sandboxManagement.getTermsOfUse().then(function (terms) {
                $uibModal.open({
                    animation: true,
                    templateUrl: 'static/js/templates/termsOfUseModal.html',
                    controller: 'TermsOfUseModalInstanceCtrl',
                    windowClass: 'terms-modal-window',
                    resolve: {
                        getSettings: function () {
                            return {
                                title: "Terms of Use and Privacy Statement",
                                cancel: "Close",
                                showAccept: false,
                                text: terms.value,
                                callback: function (result) { //setting callback
                                }

                            };
                        }
                    }
                });
            });
        };

        $rootScope.$on('signed-in', function (event, arg) {
            var canceledSandboxCreate = (arg !== undefined && arg === 'cancel-sandbox-create');

            appsSettings.getSettings().then(function () {
                userServices.getOAuthUserFromServer().then(function () {
                    $scope.oauthUser = userServices.getOAuthUser();
                    userServices.getSandboxManagerUser($scope.oauthUser.sbmUserId).then(function (sandboxManagerUser) {
                        if (sandboxManagerUser === undefined || sandboxManagerUser === "") {
                            $scope.signout();
                        } else if (sandboxManagerUser.hasAcceptedLatestTermsOfUse === false) {
                            sandboxManagement.getTermsOfUse().then(function (terms) {
                                var modalInstance = $uibModal.open({
                                    animation: true,
                                    templateUrl: 'static/js/templates/termsOfUseModal.html',
                                    controller: 'TermsOfUseModalInstanceCtrl',
                                    windowClass: 'terms-modal-window',
                                    resolve: {
                                        getSettings: function () {
                                            return {
                                                title: "Terms of Use and Privacy Statement",
                                                ok: "Accept",
                                                cancel: "Decline",
                                                showAccept: true,
                                                isUpdate: (sandboxManagerUser.termsOfUseAcceptances.length > 0),
                                                text: terms.value,
                                                callback: function (result) { //setting callback
                                                    if (result === true) {
                                                        sandboxManagement.acceptTermsOfUse($scope.oauthUser.sbmUserId, terms.id);
                                                    } else {
                                                        $scope.signout();
                                                    }
                                                }
                                            };
                                        }
                                    }
                                });
                                modalInstance.result.then(function (result) {
                                }, function () {
                                    $scope.signout();
                                });
                            });

                        }
                    });
                    $scope.showing.signin = false;
                    $scope.showing.signout = true;
                    getSandboxes();

                    if (canceledSandboxCreate) {
                        $scope.goToDashboard();
                    } else {
                        appsSettings.getSettings().then(function (settings) {

                            //Initial sign in with no sandbox specified
                            if (appsSettings.getSandboxUrlSettings().sandboxId === undefined && fhirApiServices.fhirClient().server.serviceUrl === settings.defaultServiceUrl) {
                                $scope.goToDashboard();
                            } else {
                                sandboxManagement.getSandboxById().then(function (sandboxExists) {
                                    if (sandboxExists === "invalid") {
                                        $state.go('404', {});
                                    } else if (sandboxExists) {
                                        sandboxManagement.sandboxLogin($scope.oauthUser.sbmUserId);
                                        if (sandboxManagement.getSandbox().name !== "") {
                                            $scope.title.blueBarTitle = sandboxManagement.getSandbox().name;
                                        }
                                        sandboxSignIn();
                                    } else {
                                        $scope.goToDashboard();
                                    }
                                });
                            }
                        });
                    }
                });

            });

        });

        function sandboxSignIn() {
            $scope.showing.signin = false;
            $scope.showing.signout = true;
            $scope.showing.navBar = true;
            $scope.showing.footer = true;
            $scope.showing.sideNavBar = true;
            $rootScope.$digest();
            if ($scope.showing.defaultLaunchScenario) {
                $state.go('launch-scenarios', {});
            } else {
                $state.go('manage-apps', {});
            }
        }

        $rootScope.$on('hide-nav', function () {
            $scope.showing.navBar = false;
            $scope.showing.sideNavBar = false;
            $scope.showing.footer = false;
        });

        $scope.signout = function () {
            fhirApiServices.clearClient();
            userServices.clearOAuthUser();
            $scope.showing.signin = true;
            $scope.showing.signout = false;
            $scope.showing.navBar = true;
            $scope.showing.sideNavBar = false;
            $scope.showing.footer = true;
            oauth2.logout();
        };

        $scope.userSettings = function () {
            if ($scope.userSettingsPWM) {
                userServices.userSettingsPWM();
            } else {
                appsSettings.getSettings().then(function (settings) {
                    $window.open(settings.userManagementUrl, '_blank');
                });
            }
        };

        $scope.selectSandbox = function (sandbox) {
            if (sandboxManagement.getSandbox().sandboxId !== sandbox.sandboxId) {
                var sandboxUrlSettings = appsSettings.getSandboxUrlSettings();
                window.location.href = sandboxUrlSettings.sandboxManagerRootUrl + "/" + sandbox.sandboxId;
            } else if (sandboxManagement.getSandbox().sandboxId === sandbox.sandboxId && $state.current.name === "create-sandbox") {
                $scope.showing.sideNavBar = true;
                if ($scope.showing.defaultLaunchScenario) {
                    $state.go('launch-scenarios', {});
                } else {
                    $state.go('manage-apps', {});
                }
            }
        };

        $scope.createSandbox = function () {
            $state.go('create-sandbox', {});
        };

        $scope.canCreateSandbox = function () {
            return $scope.isSystemAdmin() || (userServices.sandboxManagerUser() !== undefined && (userServices.hasSystemRole("CREATE_SANDBOX")));
        };

        $scope.isSystemAdmin = function () {
            return userServices.sandboxManagerUser() !== undefined && (userServices.hasSystemRole("ADMIN"));
        };

        $scope.isSandboxOwner = function () {
            return (sandboxManagement.getSandbox().createdBy.sbmUserId.toLowerCase() === userServices.getOAuthUser().sbmUserId.toLowerCase());
        };

        $scope.isSandboxAdmin = function () {
            return sandboxManagement.getSandbox().userRoles !== undefined && userServices.hasSandboxRole(sandboxManagement.getSandbox().userRoles, "ADMIN");
        };

        $scope.canManageUsers = function () {
            return $scope.isSandboxAdmin() || (sandboxManagement.getSandbox().userRoles !== undefined && userServices.hasSandboxRole(sandboxManagement.getSandbox().userRoles, "MANAGE_USERS"));
        };

        $scope.canManageData = function () {
            return $scope.isSandboxAdmin() || (sandboxManagement.getSandbox().userRoles !== undefined && userServices.hasSandboxRole(sandboxManagement.getSandbox().userRoles, "MANAGE_DATA"));
        };

        $scope.goToDashboard = function () {
            if (appsSettings.getSandboxUrlSettings().sandboxId === undefined) {
                $state.go('dashboard-view', {});
            } else {
                window.location.href = appsSettings.getSandboxUrlSettings().sandboxManagerRootUrl + "/#/dashboard-view";
            }
        };

        $rootScope.$on('refresh-sandboxes', function () {
            getSandboxes();
        });

        function getSandboxes() {
            sandboxManagement.getUserSandboxesByUserId().then(function (sandboxesExists) {
                if (sandboxesExists) {
                    $scope.showing.signin = false;
                    $scope.showing.signout = true;
                    $scope.dashboard.sandboxes = sandboxManagement.getSandboxes();
                    if (sandboxManagement.getSandbox().name !== "") {
                        $scope.title.blueBarTitle = sandboxManagement.getSandbox().name;
                    }
                    $rootScope.$digest();
                }
            });
        }

        // $scope.$on('$viewContentLoaded', function(){
        if (fhirApiServices.clientInitialized()) {
            // $rootScope.$emit('signed-in');
        } else if (sessionStorage.tokenResponse) {
            fhirApiServices.initClient();
        } else if (sessionStorage.hspcAuthorized && window.location.hash.indexOf("#/after-auth") !== 0) {
            oauth2.login();
        } else if (window.location.hash.indexOf("#/after-auth") !== 0) {
            appsSettings.getSettings().then(function (settings) {
                if (cookieService.getHSPCAccountCookie(settings)) {
                    oauth2.login();
                }
            });
        }
        // });

        // Set the Side Nav Bar to the height to the height of the uiView
        // This sets a watcher to catch when the uiView and Side Nav Bar heights
        // are different. A timeout runs after each digest to check since the
        // uiView height is modified outside of angular's detection.
        $scope.sideNavHeight = document.getElementById('uiView').offsetHeight;

        function postDigest(callback) {
            var unregister = $rootScope.$watch(function () {
                unregister();
                $timeout(function () {
                    callback();
                    postDigest(callback);
                }, 0, false);
            });
        }

        postDigest(function () {
            var offset = $scope.size.navBarHeight + $scope.size.footerHeight + $scope.size.sandboxBarHeight;
            var uiViewHeight = document.getElementById('uiView').offsetHeight;
            var windowHeight = $window.innerHeight - offset;
            var sideNav = 0;
            if (document.getElementById('sideNav')) {
                sideNav = document.getElementById('sideNav').offsetHeight;
            }
            var largerHeight = uiViewHeight > windowHeight ? uiViewHeight : windowHeight;
            largerHeight = largerHeight > sideNav ? largerHeight : sideNav;

            if ($scope.sideNavHeight !== largerHeight) {

                $scope.sideNavHeight = largerHeight;
                $rootScope.$digest();
            }
        });

    }]).controller("AfterAuthController", // After auth
    function (fhirApiServices) {
        fhirApiServices.initClient();
    }).controller("404Controller",
    function () {

    }).controller("ErrorController",
    function ($scope, errorService) {
        $scope.errorMessage = errorService.getErrorMessage();

    }).controller("StartController",
    function ($scope, $state, $timeout, userServices, branded, appsSettings, cookieService) {
        $scope.showing.navBar = true;
        $scope.showing.sideNavBar = false;
        $scope.showing.footer = !sessionStorage.hspcAuthorized;
        $scope.showing.start = !sessionStorage.hspcAuthorized;

        $scope.title = branded.sandboxDescription.title;
        $scope.description = branded.sandboxDescription.description;
        $scope.bottomNote = branded.sandboxDescription.bottomNote;
        $scope.checkList = branded.sandboxDescription.checkList;

        appsSettings.getSettings().then(function (settings) {
            if (!cookieService.getHSPCAccountCookie(settings)) {
                $scope.showing.footer = true;
                $scope.showing.start = true;
            }
        });

        $scope.signin = function () {
            $state.go('login', {});
        };
        $scope.signup = function () {
            userServices.createUser();
        };

    }).controller("DashboardViewController",
    function ($scope, $rootScope, $state, userServices, sandboxManagement, sandboxInviteServices, appsSettings, branded) {
        $scope.showing.navBar = true;
        $scope.showing.footer = true;
        $scope.showing.sideNavBar = false;
        $scope.sandboxInvites = [];
        $scope.title.blueBarTitle = branded.dashboardTitle;

        $rootScope.$on('user-loaded', function () {
            if (userServices.sandboxManagerUser() !== undefined && userServices.sandboxManagerUser() !== "") {
                getSandboxInvites();
                if ($scope.isSystemAdmin()) {
                    $rootScope.$digest();
                }
            }
        });

        $scope.showInvitations = function () {
            return branded.showEmptyInviteList || $scope.sandboxInvites.length > 0;
        };

        $scope.selectSandbox = function (sandbox) {
            var sandboxUrlSettings = appsSettings.getSandboxUrlSettings();
            window.location.href = sandboxUrlSettings.sandboxManagerRootUrl + "/" + sandbox.sandboxId;
        };

        $scope.updateSandboxInvite = function (sandboxInvite, status) {
            sandboxInviteServices.updateSandboxInvite(sandboxInvite, status).then(function () {
                getSandboxInvites();
                $rootScope.$emit('refresh-sandboxes');

            });
        };

        function getSandboxInvites() {
            sandboxInviteServices.getSandboxInvitesBySbmUserId("PENDING").then(function (results) {
                $scope.sandboxInvites = results;
            });
        }

    }).controller("SandboxUserViewController",
    function ($scope, $rootScope, sandboxManagement, sandboxInviteServices, userServices, $uibModal) {
        $scope.users = [];
        $scope.sandboxInvites = [];
        $scope.newUserEmail = "";
        $scope.validEmail = false;
        $scope.isSending = false;

        getSandboxInvites();
        getUsers();

        $scope.canInvite = function () {
            return userServices.canInviteUsers(sandboxManagement.getSandbox());
        };

        $scope.showDelete = function (sbmUserId) {

            // Only Sandbox Admin can delete users
            if (userServices.getOAuthUser() !== undefined &&
                userServices.userHasSandboxRole(userServices.getOAuthUser().sbmUserId, sandboxManagement.getSandbox().userRoles, "ADMIN")) {
                // Don't allow deleting self or Sandbox creator
                return ((sandboxManagement.getSandbox().createdBy.sbmUserId.toLowerCase() !== sbmUserId.toLowerCase()) &&
                    (userServices.getOAuthUser().sbmUserId.toLowerCase() !== sbmUserId.toLowerCase()));
            }
            return false;
        };

        $scope.removeUser = function (user) {
            $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/confirmModal.html',
                controller: 'ConfirmModalInstanceCtrl',
                resolve: {
                    getSettings: function () {
                        return {
                            title: "Remove User from Sandbox",
                            ok: "Yes",
                            cancel: "Cancel",
                            type: "confirm-error",
                            text: "Are you sure you want to remove the user " + user.email + "?",
                            callback: function (result) { //setting callback
                                if (result === true) {
                                    sandboxManagement.removeUserFromSandboxByUserId(user.sbmUserId).then(function () {
                                        sandboxManagement.getSandboxById().then(function () {
                                            getUsers();
                                        });
                                    });
                                }
                            }
                        };
                    }
                }
            });
        };

        $scope.updateRole = function (user, role, add) {
            sandboxManagement.updateSandboxUserRoleByUserId(user.sbmUserId, role, add).then(function () {
            });
        };

        $scope.revokeInvite = function (invite) {
            sandboxInviteServices.updateSandboxInvite(invite, "REVOKED").then(function () {
                getSandboxInvites();
            });
        };

        $scope.resendInvite = function (email) {
            sandboxInviteServices.createSandboxInvite(email).then(function () {
                getSandboxInvites();
            });
        };

        $scope.sendInvite = function () {
            $scope.isSending = true;
            var inviteUser = angular.copy($scope.newUserEmail);
            $scope.newUserEmail = "";
            sandboxInviteServices.createSandboxInvite(inviteUser).then(function () {
                getSandboxInvites();
                $scope.isSending = false;
            }, function (results) {
                $scope.isSending = false;
            });
        };

        $scope.$watch('newUserEmail', function () {
            $scope.validEmail = validateEmail($scope.newUserEmail);
        });

        function validateEmail(email) {
            var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
            return re.test(email);
        }

        function getSandboxInvites() {
            sandboxInviteServices.getSandboxInvitesBySandboxId("PENDING").then(function (results) {
                $scope.sandboxInvites = results;
                sandboxInviteServices.getSandboxInvitesBySandboxId("REJECTED").then(function (results) {
                    angular.forEach(results, function (invite) {
                        $scope.sandboxInvites.push(invite);
                    });
                });
            });
        }

        function getUsers() {
            $scope.users = [];
            var userRoles = sandboxManagement.getSandbox().userRoles;
            userRoles.forEach(function (userRole) {
                if (!contains($scope.users, userRole.user)) {
                    if (userServices.userHasSandboxRole(userRole.user.sbmUserId, sandboxManagement.getSandbox().userRoles, "ADMIN")) {
                        userRole.user.isAdmin = true;
                    }
                    $scope.users.push(userRole.user);
                }
            });
        }

        function contains(array, item) {
            var found = false;
            array.forEach(function (cur) {
                if (cur.sbmUserId.toLocaleLowerCase() === item.sbmUserId.toLocaleLowerCase()) {
                    found = true;
                }
            });
            return found;
        }

    }).controller("SettingsViewController",
    function ($scope, $rootScope, sandboxManagement, appsSettings, userServices, $uibModal, apiEndpointIndexServices) {

        $scope.sandbox = angular.copy(sandboxManagement.getSandbox());
        $scope.sandboxURL = appsSettings.getSandboxUrlSettings().sandboxManagerRootUrl + "/" + $scope.sandbox.sandboxId;
        $scope.allowOpenAccess = $scope.sandbox.allowOpenAccess;
        $scope.defaultDataSet = true;

        appsSettings.getSettings().then(function (settings) {
            $scope.openFhirUrl = settings.baseServiceUrl_1 + $scope.sandbox.sandboxId + "/open";
            if ($scope.sandbox.apiEndpointIndex === "2") {
                $scope.openFhirUrl = settings.baseServiceUrl_2 + $scope.sandbox.sandboxId + "/open";
            } else if ($scope.sandbox.apiEndpointIndex === "3") {
                $scope.openFhirUrl = settings.baseServiceUrl_3 + $scope.sandbox.sandboxId + "/open";
            } else if ($scope.sandbox.apiEndpointIndex === "4") {
                $scope.openFhirUrl = settings.baseServiceUrl_4 + $scope.sandbox.sandboxId + "/open";
            } else if ($scope.sandbox.apiEndpointIndex === "5") {
                $scope.openFhirUrl = settings.baseServiceUrl_5 + $scope.sandbox.sandboxId + "/open";
            } else if ($scope.sandbox.apiEndpointIndex === "6") {
                $scope.openFhirUrl = settings.baseServiceUrl_6 + $scope.sandbox.sandboxId + "/open";
            }
            $scope.secureFhirUrl = settings.baseServiceUrl_1 + $scope.sandbox.sandboxId + "/data";
            if ($scope.sandbox.apiEndpointIndex === "2") {
                $scope.secureFhirUrl = settings.baseServiceUrl_2 + $scope.sandbox.sandboxId + "/data";
            } else if ($scope.sandbox.apiEndpointIndex === "3") {
                $scope.secureFhirUrl = settings.baseServiceUrl_3 + $scope.sandbox.sandboxId + "/data";
            } else if ($scope.sandbox.apiEndpointIndex === "4") {
                $scope.secureFhirUrl = settings.baseServiceUrl_4 + $scope.sandbox.sandboxId + "/data";
            } else if ($scope.sandbox.apiEndpointIndex === "5") {
                $scope.secureFhirUrl = settings.baseServiceUrl_5 + $scope.sandbox.sandboxId + "/data";
            } else if ($scope.sandbox.apiEndpointIndex === "6") {
                $scope.secureFhirUrl = settings.baseServiceUrl_6 + $scope.sandbox.sandboxId + "/data";
            }
        });

        $scope.fhirVersion = apiEndpointIndexServices.getSandboxApiEndpointIndex().name;
        $scope.supportsDataSets = apiEndpointIndexServices.getSandboxApiEndpointIndex().supportsDataSets;

        $scope.canEdit = function () {
            return userServices.canModifySandbox(sandboxManagement.getSandbox())
        };

        // Only the owner can delete
        $scope.canDelete = function () {
            return (sandboxManagement.getSandbox().createdBy.sbmUserId.toLowerCase() === userServices.getOAuthUser().sbmUserId.toLowerCase());
        };

        // Only an Admin can reset
        $scope.canReset = function () {
            return (userServices.getOAuthUser() !== undefined &&
                userServices.userHasSandboxRole(userServices.getOAuthUser().sbmUserId, sandboxManagement.getSandbox().userRoles, "ADMIN"));
        };

        $scope.updateSandbox = function () {
            sandboxManagement.updateSandbox($scope.sandbox);
        };

        $scope.deleteSandbox = function () {
            $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/sandboxDeleteModal.html',
                controller: 'SandboxDeleteModalInstanceCtrl',
                resolve: {
                    getSettings: function () {
                        return {
                            title: "Delete Sandbox",
                            ok: "Yes",
                            cancel: "Cancel",
                            type: "confirm-error",
                            text: "Are you sure you want to delete sandbox " + sandboxManagement.getSandbox().name + "? " +
                            "This is not reversible and will delete all FHIR data, launch scenarios, registered app, etc.",
                            callback: function (result) { //setting callback
                                if (result === true) {
                                    var modalProgress = openModalProgressDialog("Deleting...");
                                    sandboxManagement.deleteSandbox().then(function () {
                                        window.location.href = appsSettings.getSandboxUrlSettings().sandboxManagerRootUrl + "/#/dashboard-view";
                                        modalProgress.dismiss();
                                    }, function () {
                                        //TODO display error
                                        modalProgress.dismiss();
                                    });
                                }
                            }
                        };
                    }
                }
            });
        };

        $scope.resetSandbox = function () {
            $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/sandboxResetModal.html',
                controller: 'SandboxResetModalInstanceCtrl',
                resolve: {
                    getSettings: function () {
                        return {
                            title: "Reset Sandbox",
                            ok: "Yes",
                            cancel: "Cancel",
                            type: "confirm-error",
                            text: "Are you sure you want to reset sandbox " + sandboxManagement.getSandbox().name + "? " +
                            "This is not reversible and will delete all FHIR data, launch scenarios, and personas",
                            callback: function (result) { //setting callback
                                if (result == true) {
                                    var modalProgress = openModalProgressDialog("Resetting...");
                                    var dataSet = $scope.supportsDataSets ? ($scope.defaultDataSet ? "DEFAULT" : "NONE") : "NA";
                                    sandboxManagement.resetSandbox(dataSet).then(function () {
                                        modalProgress.dismiss();
                                    }, function () {
                                        //TODO display error
                                        modalProgress.dismiss();
                                    });
                                }
                            }
                        };
                    }
                }
            });
        };

        function openModalProgressDialog(progressTitle) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/progressModal.html',
                controller: 'ProgressModalCtrl',
                size: 'sm',
                resolve: {
                    getTitle: function () {
                        return progressTitle;
                    }
                }
            });
        }

    }).controller("AdminDashboardViewController",
    function ($scope, $rootScope, sandboxManagement) {
        $scope.title.blueBarTitle = "Admin Dashboard";
        $scope.statistics = {};

        if ($scope.isSystemAdmin()) {
            sandboxManagement.sandboxManagerStatistics().then(function (result) {
                $scope.statistics = result;
                $rootScope.$digest();
            });
        }

    }).controller("FutureController",
    function () {

    }).controller("DataAcquisitionController",
    function ($scope, $rootScope, fhirApiServices, sandboxManagement, $uibModal, $filter, dataManagerResources,
              dataManagerService, externalFhirDataServices, apiEndpointIndexServices, pdmService, patientResources) {

        $scope.hasError = false;
        $scope.showing.patientDetail = false;
        $scope.selected = {selectedPatient: {}, fhirEndpoint: {}, patientSelected: false};
        $scope.settings = dataManagerService.getSettings();
        // $scope.fhirEndpoints = [{ endpoint: "https://api3.hspconsortium.org/HSPCplusSynthea/open", match: true, name: "HSPC with Synthea", version: "FHIR v1.8" }];
        // $scope.selected.fhirEndpoint = $scope.fhirEndpoints[0];

        sandboxManagement.externalFhirServers().then(function (servers) {
            $scope.fhirEndpoints = [];
            servers.forEach(function (server) {
                $scope.fhirEndpoints.push({endpoint: server.value, name: server.keyName});
            });
            checkFhirVersions($scope.fhirEndpoints);
        });

        if ($scope.settings.allQuerySuggestions === undefined || $scope.settings.allQuerySuggestions.length === 0) {
            sandboxManagement.fhirQuerySuggestions().then(function (suggestions, defaultSuggestions) {
                $scope.settings.allQuerySuggestions = suggestions;
                $scope.settings.defaultSuggestions = defaultSuggestions;
            });
        }

        function checkFhirVersions(fhirEndpoints) {
            angular.forEach(fhirEndpoints, function (endpoint) {
                externalFhirDataServices.queryExternalFhirVersion(endpoint.endpoint).then(function (version) {
                    endpoint.match = (apiEndpointIndexServices.fhirVersion() === version);
                    endpoint.version = "FHIR " + version;
                    endpoint.fhirIdPrefix = "SYNTHEA-";
                    $scope.selected.fhirEndpoint = $scope.fhirEndpoints[0];
                    $rootScope.$digest();
                });
            });
        }

        sandboxManagement.getSandboxImports().then(function (imports) {
            $scope.sandboxImports = imports;
        });

        $scope.selectEndpoint = function (endpoint) {
            $scope.selected.fhirEndpoint = endpoint;
        };


        // ****  Loads Patient Resource Counts for Patient Details ****//
        var resourcesNames = [];
        var resourceCounts = [];

        function emptyArray(array) {
            while (array.length > 0) {
                array.pop();
            }
        }

        $scope.selected.chartConfig = {
            options: {
                chart: {
                    type: 'bar'
                },
                legend: {
                    enabled: false
                }
            },
            xAxis: {
                categories: resourcesNames,
                title: {
                    text: null
                }
            },
            yAxis: {
                min: 0,
                labels: {
                    overflow: 'justify'
                },
                title: {
                    text: null
                }
            }, series: [{
                type: 'bar',
                name: "Resource Count",
                data: resourceCounts,
                dataLabels: {
                    enabled: true
                },
                color: '#00AEEF'
            }],
            subtitle: {
                text: null
            },
            title: {
                text: null
            },
            credits: {
                enabled: false
            }
        };

        $scope.getDynamicModel = function (inputResource, path, item) {
            var resource = angular.copy(inputResource);
            var root = $scope.getModelParent(resource, path);
            var leaf = $scope.getModelLeaf(path);

            if (typeof root !== 'undefined' && typeof leaf !== 'undefined') {
                if (typeof root[leaf] !== 'undefined') {
                    item.show = true;
                    return root[leaf];
                } else {
                    item.show = false;
                    return "";
                }
            }
            item.show = false;
            return "";
        };

        $scope.getModelParent = function (obj, path) {
            var segs = path.split('.');
            var rootParent = obj;
            var parentStep = "";
            var root = obj;

            while (segs.length > 1) {
                var pathStep = segs.shift();
                if (typeof root[pathStep] === 'undefined') {
                    if (isNaN(pathStep)) {
                        root[pathStep] = {};
                    } else {
                        rootParent[parentStep] = [{}];
                        root = rootParent[parentStep];
                    }
                }
                parentStep = pathStep;
                rootParent = root;
                root = root[pathStep];
            }
            return root;
        };

        $scope.getModelLeaf = function (path) {
            var segs = path.split('.');
            return segs[segs.length - 1];
        };

        $scope.filterExternalQuery = function (filterValue) {
            if ($scope.settings.allQuerySuggestions.length === 0) {
                sandboxManagement.fhirQuerySuggestions().then(function (suggestions, defaultSuggestions) {
                    $scope.settings.allQuerySuggestions = suggestions;
                    $scope.settings.defaultSuggestions = defaultSuggestions;
                    if (filterValue.length === 0) {
                        return $scope.settings.defaultSuggestions;
                    }
                    return $filter('filter')($scope.settings.allQuerySuggestions, filterValue);
                });
            } else {
                if (filterValue.length === 0) {
                    return $scope.settings.defaultSuggestions;
                }
                return $filter('filter')($scope.settings.allQuerySuggestions, filterValue);
            }
        };

        $scope.queryExternalFhirServer = function (query) {
            $scope.hasError = false;
            $scope.showing.patientDetail = false;
            $scope.settings.showing.results = false;
            $scope.settings.externalResourceList = [];
            $scope.settings.resultTotal = 0;
            $scope.settings.resultSet = 0;
            if (query === 'clear') {
                return;
            }

            if (query.indexOf('_count=') === -1) {
                if (query.indexOf('?') === -1) {
                    query = query + "?_count=50";
                } else {
                    query = query + "&_count=50";
                }
            }

            externalFhirDataServices.queryExternalFhirServer($scope.selected.fhirEndpoint.endpoint, query).then(function (results) {
                dataManagerResources.getDataManagerResources().done(function (resources) {
                    if (results.resourceType == "Bundle") {
                        if (results && results.entry && results.entry.length > 0) {
                            $scope.settings.externalResourceList = results.entry;
                            selectResourceType(resources, $scope.settings.externalResourceList[0].resource.resourceType);
                        }
                    } else {
                        $scope.settings.externalResourceList[0] = {resource: results};
                        selectResourceType(resources, $scope.settings.externalResourceList[0].resource.resourceType);
                    }
                });

                if (results && results.total) {
                    $scope.settings.showing.results = true;
                    $scope.settings.externalResultTotal = results.total;
                    $scope.settings.externalResultSet = results.entry.length;
                }
                $rootScope.$digest();
            }, function (error) {
                $scope.hasError = true;
                $scope.queryError = error.responseText;
                $rootScope.$digest();
            });
        };

        $scope.importPatient = function (patient) {
            $scope.settings.externalImportRunning = true;
            externalFhirDataServices.importExternalFhirPatient($scope.selected.fhirEndpoint.endpoint, patient.id, $scope.selected.fhirEndpoint.fhirIdPrefix).then(function () {
                $scope.settings.externalImportRunning = false;
                $rootScope.$digest();
            }, function (error) {
                $scope.settings.externalImportRunning = false;
                $rootScope.$digest();
            });
        };

        //not used
        $scope.acquireData = function (syntheaFhirQuery) {
            externalFhirDataServices.importExternalFhirData(syntheaFhirQuery).then(function () {
                sandboxManagement.getSandboxImports().then(function (imports) {
                    $scope.sandboxImports = imports;
                    $rootScope.$digest();
                });
            });
        };

        function selectResourceType(resourceTypes, type) {
            $scope.settings.selectedResourceType = "";
            angular.forEach(resourceTypes, function (resource) {
                if (resource.resourceType === type) {
                    $scope.settings.selectedResourceType = resource;
                }
            });
            if ($scope.settings.selectedResourceType === "") {
                selectResourceType(resourceTypes, "Default")
            }
        }

        $scope.selectExternalResource = function (resource) {
            $scope.settings.selected.externalSelectedResource = resource;
            $scope.showing.patientDetail = true;
            var patientId = [];
            getPatientId(resource.resource, patientId);
            externalFhirDataServices.externalFhirServerPatient($scope.selected.fhirEndpoint.endpoint, patientId[0]).then(function (result) {
                $scope.selected.selectedPatient = result;
                $scope.selected.patientSelected = true;
                $scope.showing.importExternalPatient = true;
                $scope.showing.patientDataManager = $scope.canManageData() && pdmService.hasPDMSupport();
                patientResources.getSupportedResources().done(function (resources) {
                    $scope.selected.patientResources = [];
                    for (var i = 0; i < resources.length; i++) {
                        var query = resources[i].resourceType + "?" + resources[i].patientSearch + "=Patient/" + patientId[0] + "&_count=0";
                        externalFhirDataServices.queryExternalFhirServer($scope.selected.fhirEndpoint.endpoint, query, resources[i].resourceType)
                            .then(function (queryResult, resourceType) {
                                $scope.selected.patientResources.push({
                                    resourceType: resourceType,
                                    count: queryResult.total
                                });
                                $scope.selected.patientResources = $filter('orderBy')($scope.selected.patientResources, "resourceType");

                                emptyArray(resourcesNames);
                                emptyArray(resourceCounts);
                                angular.forEach($scope.selected.patientResources, function (resource) {
                                    resourcesNames.push(resource.resourceType);
                                    resourceCounts.push(parseInt(resource.count));
                                });
                                $rootScope.$digest();
                            });
                    }
                });
                $rootScope.$digest();
            });

            // var temp = {};
            // $uibModal.open({
            //     animation: true,
            //     templateUrl: 'static/js/templates/resourceDetailModal.html',
            //     controller: 'ResourceDetailModalInstanceCtrl',
            //     resolve: {
            //         getSettings: function () {
            //             return {
            //                 title:"Details",
            //                 ok:"OK",
            //                 cancel:"Cancel",
            //                 type:"confirm-error",
            //                 text:resource.resource,
            //                 patient: $scope.getDynamicModel(resource.resource, $scope.settings.selectedResourceType.patient, temp),
            //                 callback:function(result){ //setting callback
            //                 }
            //             };
            //         }
            //     }
            // });
        };

        function getPatientId(resource, patientIDs) {
            if (resource.resourceType === "Patient") {
                patientIDs.push(resource.id);
            } else if (typeOf(resource) === "object") {
                if (resource.hasOwnProperty("reference")) {
                    var resourceAndId = resource.reference.split("/");
                    if (resourceAndId.length === 2) {
                        if (resourceAndId[0] === "Patient") {
                            patientIDs.push(resourceAndId[1]);
                        }
                    }
                } else {
                    for (var key in resource) {
                        if (resource.hasOwnProperty(key)) {
                            getPatientId(resource[key], patientIDs);
                        }
                    }
                }
            } else if (typeOf(resource) === "array") {
                resource.forEach(function (element) {
                    getPatientId(element, patientIDs);
                });
            }
        }

        function typeOf(value) {
            var s = typeof value;
            if (s === 'object') {
                if (value) {
                    if (value instanceof Array) {
                        s = 'array';
                    }
                } else {
                    s = 'null';
                }
            }
            return s;
        }


        function openModalProgressDialog(progressTitle) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/progressModal.html',
                controller: 'ProgressModalCtrl',
                size: 'sm',
                resolve: {
                    getTitle: function () {
                        return progressTitle;
                    }
                }
            });
        }

    }).controller("DataQueryBrowserController",
    function ($scope, $rootScope, $http, fhirApiServices, sandboxManagement, $uibModal, $filter, dataManagerResources,
              dataManagerService, externalFhirDataServices) {

        $scope.settings = dataManagerService.getSettings();

        if ($scope.settings.allQuerySuggestions === undefined || $scope.settings.allQuerySuggestions.length === 0) {
            sandboxManagement.fhirQuerySuggestions().then(function (suggestions, defaultSuggestions) {
                $scope.settings.allQuerySuggestions = suggestions;
                $scope.settings.defaultSuggestions = defaultSuggestions;
            });
        }

        $scope.getDynamicModel = function (inputResource, path, item) {
            var resource = angular.copy(inputResource);
            var root = $scope.getModelParent(resource, path);
            var leaf = $scope.getModelLeaf(path);

            if (typeof root !== 'undefined' && typeof leaf !== 'undefined') {
                if (typeof root[leaf] !== 'undefined') {
                    item.show = true;
                    return root[leaf];
                } else {
                    item.show = false;
                    return "";
                }
            }
            item.show = false;
            return "";
        };

        $scope.getModelParent = function (obj, path) {
            var segs = path.split('.');
            var rootParent = obj;
            var parentStep = "";
            var root = obj;

            while (segs.length > 1) {
                var pathStep = segs.shift();
                if (typeof root[pathStep] === 'undefined') {
                    if (isNaN(pathStep)) {
                        root[pathStep] = {};
                    } else {
                        rootParent[parentStep] = [{}];
                        root = rootParent[parentStep];
                    }
                }
                parentStep = pathStep;
                rootParent = root;
                root = root[pathStep];
            }
            return root;
        };

        $scope.getModelLeaf = function (path) {
            var segs = path.split('.');
            return segs[segs.length - 1];
        };

        $scope.filterQuery = function (filterValue) {
            if ($scope.settings.allQuerySuggestions.length === 0) {
                sandboxManagement.fhirQuerySuggestions().then(function (suggestions, defaultSuggestions) {
                    $scope.settings.allQuerySuggestions = suggestions;
                    $scope.settings.defaultSuggestions = defaultSuggestions;
                    if (filterValue.length === 0) {
                        return $scope.settings.defaultSuggestions;
                    }
                    return $filter('filter')($scope.settings.allQuerySuggestions, filterValue);
                });
            } else {
                if (filterValue.length === 0) {
                    return $scope.settings.defaultSuggestions;
                }
                return $filter('filter')($scope.settings.allQuerySuggestions, filterValue);
            }
        };

        $scope.runQuery = function (query) {
            $scope.settings.resourceList = [];
            $scope.settings.queryResults = '';
            $scope.settings.resultTotal = 0;
            $scope.settings.resultSet = 0;
            $scope.settings.showing.results = false;
            if (query === 'clear') {
                return;
            }

            if (query.indexOf('_count=') === -1) {
                if (query.indexOf('?') === -1) {
                    query = query + "?_count=50";
                } else {
                    query = query + "&_count=50";
                }
            }

            fhirApiServices.runRawQuery(query).then(function (results) {
                dataManagerResources.getDataManagerResources().done(function (resources) {
                    if (results.resourceType == "Bundle") {
                        if (results && results.entry && results.entry.length > 0) {
                            $scope.settings.resourceList = results.entry;
                            selectResourceType(resources, $scope.settings.resourceList[0].resource.resourceType);
                        }
                    } else {
                        $scope.settings.resourceList[0] = {resource: results};
                        selectResourceType(resources, $scope.settings.resourceList[0].resource.resourceType);
                    }
                });

                $scope.settings.queryResults = $filter('json')(results);
                if (results && results.total) {
                    $scope.settings.resultTotal = results.total;
                    $scope.settings.resultSet = results.entry.length;
                }
                $scope.settings.showing.results = true;
                $rootScope.$digest();
            }, function (error) {
                $scope.settings.queryResults = error.responseText;
                $rootScope.$digest();
            });
        };

        function selectResourceType(resourceTypes, type) {
            $scope.settings.selectedResourceType = "";
            angular.forEach(resourceTypes, function (resource) {
                if (resource.resourceType === type) {
                    $scope.settings.selectedResourceType = resource;
                }
            });
            if ($scope.settings.selectedResourceType === "") {
                selectResourceType(resourceTypes, "Default")
            }
        }

        $scope.selectResource = function (resource) {
            $scope.settings.selected.selectedResource = resource;
            var temp = {};
            $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/resourceDetailModal.html',
                controller: 'ResourceDetailModalInstanceCtrl',
                resolve: {
                    getSettings: function () {
                        return {
                            title: "Details",
                            ok: "OK",
                            cancel: "Cancel",
                            type: "confirm-error",
                            text: resource.resource,
                            patient: $scope.getDynamicModel(resource.resource, $scope.settings.selectedResourceType.patient, temp),
                            callback: function (result) { //setting callback
                            }
                        };
                    }
                }
            });
        };

        function openModalProgressDialog(progressTitle) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/progressModal.html',
                controller: 'ProgressModalCtrl',
                size: 'sm',
                resolve: {
                    getTitle: function () {
                        return progressTitle;
                    }
                }
            });
        }

    }).controller("DataImportController",
    function ($scope, $rootScope, fhirApiServices, $uibModal, $filter, dataManagerService) {

        $scope.settings = dataManagerService.getSettings();

        $scope.import = function (bundle) {
            var modalProgress = openModalProgressDialog("Importing...");
            $scope.settings.importBundleInput = bundle;
            fhirApiServices.importBundle(bundle).then(function (results) {
                $scope.settings.importBundleResults = $filter('json')(results);
                $scope.settings.showing.import.results = true;
                modalProgress.dismiss();
            }, function (results) {
                $scope.settings.importBundleResults = results;
                $scope.settings.showing.import.results = true;
                modalProgress.dismiss();
            });
        };

        $scope.uploadFile = function (files) {

            var reader = new FileReader();
            reader.onload = function (e) {
                $scope.import(e.target.result);
            };
            reader.readAsText(files[0]);
        };

        $scope.saveToFile = function () {

            if (!$scope.settings.importBundleResults) {
                console.log('No data');
                return;
            }

            if (typeof $scope.settings.importBundleResults === 'object') {
                $scope.settings.importBundleResults = JSON.stringify($scope.settings.importBundleResults, undefined, 2);
            }

            var blob = new Blob([$scope.settings.importBundleResults], {type: 'text/json'});

            // FOR IE:

            if (window.navigator && window.navigator.msSaveOrOpenBlob) {
                window.navigator.msSaveOrOpenBlob(blob, 'sandbox-import-results.json');
            }
            else {
                var e = document.createEvent('MouseEvents'),
                    a = document.createElement('a');

                a.download = 'sandbox-import-results.json';
                a.href = window.URL.createObjectURL(blob);
                a.dataset.downloadurl = ['text/json', a.download, a.href].join(':');
                e.initEvent('click', true, false, window,
                    0, 0, 0, 0, 0, false, false, false, false, 0, null);
                a.dispatchEvent(e);
            }
        };


        function openModalProgressDialog(progressTitle) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/progressModal.html',
                controller: 'ProgressModalCtrl',
                size: 'sm',
                resolve: {
                    getTitle: function () {
                        return progressTitle;
                    }
                }
            });
        }

    }).controller("DataExportController",
    function ($scope, $rootScope, fhirApiServices, $uibModal, $filter, dataManagerService) {

        $scope.settings = dataManagerService.getSettings();

        $scope.export = function (query) {
            if (query === "" || query === 'clear') {
                $scope.settings.exportResults = "";
                $scope.settings.showing.export.results = false;
            } else {
                var modalProgress = openModalProgressDialog("Exporting...");
                fhirApiServices.exportData(query).then(function (results) {
                    $scope.settings.exportResults = $filter('json')(results);
                    $scope.settings.showing.export.results = true;
                    modalProgress.dismiss();
                }, function (results) {
                    $scope.settings.exportResults = $filter('json')(results);
                    $scope.settings.showing.export.results = true;
                    modalProgress.dismiss();
                });
            }
        };

        $scope.saveToFile = function () {

            if (!$scope.settings.exportResults) {
                console.log('No data');
                return;
            }

            if (typeof $scope.settings.exportResults === 'object') {
                $scope.settings.exportResults = JSON.stringify($scope.settings.exportResults, undefined, 2);
            }

            var blob = new Blob([$scope.settings.exportResults], {type: 'text/json'});

            // FOR IE:

            if (window.navigator && window.navigator.msSaveOrOpenBlob) {
                window.navigator.msSaveOrOpenBlob(blob, 'sandbox-export.json');
            }
            else {
                var e = document.createEvent('MouseEvents'),
                    a = document.createElement('a');

                a.download = 'sandbox-export.json';
                a.href = window.URL.createObjectURL(blob);
                a.dataset.downloadurl = ['text/json', a.download, a.href].join(':');
                e.initEvent('click', true, false, window,
                    0, 0, 0, 0, 0, false, false, false, false, 0, null);
                a.dispatchEvent(e);
            }
        };

        function openModalProgressDialog(progressTitle) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/progressModal.html',
                controller: 'ProgressModalCtrl',
                size: 'sm',
                resolve: {
                    getTitle: function () {
                        return progressTitle;
                    }
                }
            });
        }

    }).controller("CreateSandboxController",
    function ($rootScope, $scope, $state, sandboxManagement, tools, appsSettings, branded, apiEndpointIndexServices, docLinks) {

        $scope.showing.navBar = true;
        $scope.showing.footer = true;
        $scope.showing.sideNavBar = false;
        $scope.isIdValid = false;
        $scope.showError = false;
        $scope.isNameValid = true;
        $scope.tempSandboxId = "<sandbox id>";
        $scope.sandboxName = "";
        $scope.sandboxId = "";
        $scope.sandboxDesc = "";
        $scope.sandboxAllowOpenAccess = false;
        $scope.defaultDataSet = true;
        $scope.apiEndpointIndex = branded.defaultApiEndpointIndex;
        $scope.sandboxApiEndpointIndexes = apiEndpointIndexServices.getSandboxApiEndpointIndexes(true);
        $scope.selectedSandboxApiEndpointIndex = apiEndpointIndexServices.getSandboxApiEndpointIndexDetails(branded.defaultApiEndpointIndex);
        $scope.createEnabled = true;

        $scope.title.blueBarTitle = "Create Sandbox";
        $scope.title = branded.sandboxDescription.title;
        $scope.description = branded.sandboxDescription.description;
        $scope.bottomNote = branded.sandboxDescription.bottomNote;
        $scope.checkList = branded.sandboxDescription.checkList;
        $scope.docLink = docLinks.docLink;

        $scope.baseUrl = appsSettings.getSandboxUrlSettings().sandboxManagerRootUrl;

        $scope.$watchGroup(['sandboxId', 'sandboxName'], _.debounce(function () {
            $scope.validateId($scope.sandboxId).then(function (valid) {
                $scope.isIdValid = valid;
                $scope.showError = !$scope.isIdValid && ($scope.sandboxId !== "" && $scope.sandboxId !== undefined);
                $scope.isNameValid = $scope.validateName($scope.sandboxName);
                $scope.createEnabled = ($scope.isIdValid && $scope.isNameValid);
                $rootScope.$digest();
            });
        }, 400));

        $scope.$watch("apiEndpointIndex", function () {
            $scope.selectedSandboxApiEndpointIndex = apiEndpointIndexServices.getSandboxApiEndpointIndexDetails($scope.apiEndpointIndex);
        });

        $scope.validateId = function (id) {
            var deferred = $.Deferred();

            $scope.invalidMessage = "ID Not Available";
            if ($scope.tempSandboxId !== id) {
                $scope.tempSandboxId = id;
                if (id !== undefined && id !== "" && id.length <= 20 && /^[a-zA-Z0-9]*$/.test(id)) {
                    tools.checkForSandboxById(id).then(function (sandbox) {
                        deferred.resolve(sandbox === undefined || sandbox === "");
                    });
                } else {
                    $scope.tempSandboxId = "<sandbox id>";
                    $scope.invalidMessage = "ID Is Invalid";
                    deferred.resolve(false);
                }
            } else {
                deferred.resolve($scope.isIdValid);
            }
            return deferred;

        };

        $scope.validateName = function (name) {
            if (name !== undefined && name !== "") {
                if (name.length > 50) {
                    return false;
                }
            }
            return true;
        };

        $scope.cancel = function () {
            $rootScope.$emit('signed-in', 'cancel-sandbox-create');
        };

        $scope.createSandbox = function () {
            sandboxManagement.setCreatingSandbox(true);
            $scope.showing.progress = true;
            if ($scope.sandboxName === undefined || $scope.sandboxName === "") {
                $scope.sandboxName = $scope.sandboxId;
            }
            sandboxManagement.createSandbox({
                sandboxId: $scope.sandboxId,
                sandboxName: $scope.sandboxName,
                description: $scope.sandboxDesc,
                apiEndpointIndex: $scope.apiEndpointIndex,
                allowOpenAccess: $scope.sandboxAllowOpenAccess,
                dataSet: ($scope.selectedSandboxApiEndpointIndex.supportsDataSets ? ($scope.defaultDataSet ? "DEFAULT" : "NONE") : "NA")

            }).then(function (sandbox) {
                sandboxManagement.setCreatingSandbox(false);
                $scope.showing.progress = false;
                $rootScope.$emit('sandbox-created', $scope.sandboxId);
            }).fail(function () {
                sandboxManagement.setCreatingSandbox(false);
                $state.go('error', {});
            });

            $state.go('progress', {});
        };

    }).controller("LoginController",
    function ($rootScope, $scope, $state, oauth2, fhirApiServices) {

        $scope.showing.footer = false;
        if (fhirApiServices.clientInitialized()) {
            // $rootScope.$emit('signed-in');
        } else {
            oauth2.login();
        }

    }).controller("SideBarController",
    function ($rootScope, $scope, docLinks) {

        $scope.docLink = docLinks.docLink;

        var sideBarStates = ['launch-scenarios', 'users', 'personas', 'patients', 'practitioners', 'manage-apps'];

        $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
            if (sideBarStates.indexOf(toState.name) > -1) {
                $scope.selected = toState.name;
            }
        });

        $rootScope.$on('persona-create', function () {
            $scope.selected = 'persona';
        });

        $scope.selected = "";
        $scope.select = function (selection) {
            $scope.selected = selection;
        };

        $scope.toggleSize = function () {
            $scope.showing.largeSidebar = !$scope.showing.largeSidebar;
        };

    }).controller("PatientViewController",
    function ($scope, $rootScope) {

        $scope.showing = {
            patientDetail: false,
            noPatientContext: true,
            createPatient: true,
            patientDataManager: false,
            selectForScenario: false,
            searchloading: true
        };

        $scope.page = {
            title: ""
        };

        $scope.selected = {
            selectedPatient: {},
            patientSelected: false,
            patientResources: [],
            chartConfig: {}
        };

        $scope.setPatient = function (patient) {
            $rootScope.$emit('set-patient', patient);
        };

    }).controller("PatientDetailController",
    function ($scope, $rootScope, $uibModal, $state, $stateParams, sandboxManagement, pdmService, personaServices, $filter, launchApp) {

        var source = $stateParams.source;

        if ($state.current.name === 'patients') {
            $scope.showing.patientDataManager = $scope.canManageData() && pdmService.hasPDMSupport();
        }

        if ($state.current.name === 'patient-view') {
            $scope.showing.selectForScenario = true;
        }

        $rootScope.$on('set-patient', function (e, p) {
            $scope.setPatient(p);
        });

        $scope.setPatient = function (p) {

            if (source === 'persona') {
                personaServices.getUserPersonaBuilder().fhirId = p.id;
                personaServices.getUserPersonaBuilder().resource = p.resourceType;
                personaServices.getUserPersonaBuilder().resourceUrl = p.resourceType + '/' + p.id;
                personaServices.getUserPersonaBuilder().fhirName = $filter('nameGivenFamily')(p);
                personaServices.getUserPersonaBuilder().personaName = $filter('nameGivenFamily')(p);
                openModalDialog(personaServices.getUserPersonaBuilder());
            } else {
                sandboxManagement.getScenarioBuilder().patient =
                    {
                        fhirId: p.id,
                        resource: p.resourceType,
                        name: $filter('nameGivenFamily')(p)
                    };
                $state.go('apps', {source: 'practitioner-patient', action: 'choose'});
            }
        };

        function openModalDialog(user) {

            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/personaModal.html',
                controller: 'ModalPersonaInstanceCtrl',
                size: 'lg',
                resolve: {
                    getUser: function () {
                        return user;
                    }
                }
            });

            modalInstance.result.then(function (result) {
                personaServices.createPersona(result);
                $state.go('personas', {});
            }, function () {
            });
        }


        $scope.launchPatientDataManager = function (patient) {
            launchApp.launchPatientDataManager(patient);
        };

    }).controller("PatientSearchController",
    function ($scope, $rootScope, $state, $filter, $stateParams, fhirApiServices, sandboxManagement, patientResources, $uibModal) {

        var source = $stateParams.source;

        if (source === 'patient') {
            $scope.page.title = "Select the Patient Context";
            $scope.showing.noPatientContext = true;
            $scope.showing.createPatient = false;
        } else if (source === 'persona') {
            $scope.page.title = "Select the Patient Persona";
            $scope.showing.noPatientContext = false;
            $scope.showing.createPatient = false;
        } else if ($state.current.name === 'resolve') {
            $scope.showing.noPatientContext = false;
            $scope.showing.createPatient = false;
            $scope.showing.navBar = false;
            $scope.showing.sideNavBar = false;
            $scope.showing.footer = false;
            $rootScope.$emit('hide-nav');
        } else { // Patient View
            $scope.showing.noPatientContext = false;
            $scope.showing.createPatient = true;
        }
        $scope.shouldBeOpen = true;

        $scope.count = {start: 0, end: 0, total: 0};

        var natural = true;
        var inverse = false;
        $scope.sortMap = new Map();
        $scope.sortMap.set("id", [['_id', natural]]);
        $scope.sortMap.set("gender", [['gender', natural]]);
        $scope.sortMap.set("name", [['family', natural], ['given', natural]]);
        $scope.sortMap.set("age", [['birthdate', inverse]]);
        $scope.sortSelected = "name";
        $scope.sortReverse = false;

        // ****  Loads Patient Resource Counts for Patient Details ****//
        var resourcesNames = [];
        var resourceCounts = [];

        function emptyArray(array) {
            while (array.length > 0) {
                array.pop();
            }
        }

        $scope.selected.chartConfig = {
            options: {
                chart: {
                    type: 'bar'
                },
                legend: {
                    enabled: false
                }
            },
            xAxis: {
                categories: resourcesNames,
//                lineWidth: 0,
//                minorGridLineWidth: 0,
//                lineColor: 'transparent',
//                minorTickLength: 0,
//                tickLength: 0,
                title: {
                    text: null
                }
            },
            yAxis: {
                min: 0,
                labels: {
                    overflow: 'justify'
                },
//                lineWidth: 0,
//                minorGridLineWidth: 0,
//                lineColor: 'transparent',
//                minorTickLength: 0,
//                tickLength: 0,
                title: {
                    text: null
                }
            }, series: [{
                type: 'bar',
                name: "Resource Count",
                data: resourceCounts,
                dataLabels: {
                    enabled: true
                },
                color: '#00AEEF'
            }],
            subtitle: {
                text: null
            },
            title: {
                text: null
            },
            credits: {
                enabled: false
            }
        };

        $scope.onSelected = $scope.onSelected || function (p) {
            if ($scope.selected.selectedPatient !== p) {
                $scope.selected.selectedPatient = p;
                $scope.selected.patientSelected = true;
                $scope.showing.patientDetail = true;

                patientResources.getSupportedResources().done(function (resources) {
                    $scope.selected.patientResources = [];
                    for (var i = 0; i < resources.length; i++) {
                        var query = {};
                        query[resources[i].patientSearch] = "Patient/" + p.id;
                        fhirApiServices.queryResourceInstances(resources[i].resourceType, query, undefined, undefined, 1)
                            .then(function (resource, queryResult) {
                                $scope.selected.patientResources.push({
                                    resourceType: queryResult.config.type,
                                    count: queryResult.data.total
                                });
                                $scope.selected.patientResources = $filter('orderBy')($scope.selected.patientResources, "resourceType");

                                emptyArray(resourcesNames);
                                emptyArray(resourceCounts);
                                angular.forEach($scope.selected.patientResources, function (resource) {
                                    resourcesNames.push(resource.resourceType);
                                    resourceCounts.push(parseInt(resource.count));
                                });
                                $rootScope.$digest();
                            });
                    }
                });
            }
        };
        // **** END Loads Patient Resource Counts for Patient Details ****//

        $scope.skipPatient = function () {
            sandboxManagement.getScenarioBuilder().patient =
                {
                    fhirId: 0,
                    resource: "None",
                    name: "None"
                };
            $state.go('apps', {source: 'practitioner', action: 'choose'});
        };

        $scope.patients = [];
        $scope.genderglyph = {"female": "&#9792;", "male": "&#9794;"};
        $scope.searchterm = "";
        var lastQueryResult;

        $rootScope.$on('set-loading', function () {
            $scope.showing.searchloading = true;
        });

        $scope.loadMore = function (direction) {
            $scope.showing.searchloading = true;
            $rootScope.$emit('patient-search-start');
            var modalProgress = openModalProgressDialog("Searching...");

            fhirApiServices.getNextOrPrevPage(direction, lastQueryResult).then(function (p, queryResult) {
                lastQueryResult = queryResult;
                $scope.patients = p;
                $scope.showing.searchloading = false;
                $scope.count = fhirApiServices.calculateResultSet(queryResult);
                $rootScope.$digest();

                modalProgress.dismiss();
                $rootScope.$emit('patient-search-complete');
            });
        };

        $scope.select = function (i) {
            if ($scope.showing.isModal) {
                $scope.selected.selectedPatient = $scope.patients[i];
            } else {
                $scope.onSelected($scope.patients[i]);
            }
        };

        $scope.hasPrev = function () {
            return fhirApiServices.hasPrev(lastQueryResult);
        };

        $scope.hasNext = function () {
            return fhirApiServices.hasNext(lastQueryResult);
        };

        $scope.$watchGroup(["searchterm", "sortSelected", "sortReverse"], function () {
            var tokens = [];
            ($scope.searchterm || "").split(/\s/).forEach(function (t) {
                tokens.push(t.toLowerCase());
            });
            $scope.tokens = tokens;
            if ($scope.getMore !== undefined) {
                $scope.getMore();
            }
        });

        var loadCount = 0;
        var search = _.debounce(function (thisLoad) {
            var sortDefs = $scope.sortMap.get($scope.sortSelected);
            var sortValues = [];
            for (var i = 0; i < sortDefs.length; i++) {
                sortValues[i] = [];
                sortValues[i][0] = sortDefs[i][0];
                if (sortDefs[i][1]) {
                    // natural
                    sortValues[i][1] = ($scope.sortReverse ? "desc" : "asc");
                } else {
                    // inverted
                    sortValues[i][1] = ($scope.sortReverse ? "asc" : "desc");
                }
            }

            $rootScope.$emit('patient-search-start');
            $scope.shouldBeOpen = false;
            var modalProgress = openModalProgressDialog("Searching...");

            fhirApiServices.queryResourceInstances("Patient", $scope.patientQuery, $scope.tokens, sortValues, $scope.resultCount !== undefined ? $scope.resultCount : 50)
                .then(function (p, queryResult) {
                    lastQueryResult = queryResult;
                    if (thisLoad < loadCount) {   // not sure why this is needed (pp)
                        return;
                    }
                    $scope.patients = p;
                    $scope.showing.searchloading = false;
                    $rootScope.$digest();
                    $scope.count = fhirApiServices.calculateResultSet(queryResult);

                    modalProgress.dismiss();
                    $rootScope.$emit('patient-search-complete');
                    $scope.shouldBeOpen = true;
                });
        }, 600);

        $scope.getMore = function () {
            $scope.showing.searchloading = true;
            search(++loadCount);
        };

        $scope.toggleSort = function (field) {
            $scope.sortReverse = ($scope.sortSelected == field ? !$scope.sortReverse : false);
            $scope.sortSelected = field;
        };

        function openModalProgressDialog(title) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/progressModal.html',
                controller: 'ProgressModalCtrl',
                size: 'sm',
                resolve: {
                    getTitle: function () {
                        return title;
                    }
                }
            });
        }

        $rootScope.$on('patient-created', function () {
            $scope.getMore();
        });

    }).controller("PractitionerViewController",
    function ($scope) {
        $scope.showing = {
            practitionerDetail: false,
            selectForScenario: false,
            createPractitioner: false,
            searchloading: true
        };

        $scope.selected = {
            selectedPractitioner: {},
            practitionerSelected: false
        }

    }).controller("PractitionerDetailController",
    function ($scope, $rootScope, $state, $stateParams, $filter, $uibModal, personaServices, sandboxManagement) {

        var source = $stateParams.source;

        if ($state.current.name === 'practitioner-view') {
            $scope.showing.selectForScenario = true;
        }

        $scope.practitionerSpecialty = function () {
            try {
                return $scope.selected.selectedPractitioner.practitionerRole[0].specialty[0].coding[0].display;
            }
            catch (err) {
                return false;
            }
        };

        $scope.practitionerRole = function () {
            try {
                return $scope.selected.selectedPractitioner.practitionerRole[0].role.coding[0].display;
            }
            catch (err) {
                return false;
            }
        };

        $scope.setPractitioner = function (p) {
            personaServices.getUserPersonaBuilder().fhirId = p.id;
            personaServices.getUserPersonaBuilder().resource = p.resourceType;
            personaServices.getUserPersonaBuilder().resourceUrl = p.resourceType + '/' + p.id;
            personaServices.getUserPersonaBuilder().fhirName = $filter('nameGivenFamily')(p);
            personaServices.getUserPersonaBuilder().personaName = $filter('nameGivenFamily')(p);
            openModalDialog(personaServices.getUserPersonaBuilder());
        };

        function openModalDialog(user) {

            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/personaModal.html',
                controller: 'ModalPersonaInstanceCtrl',
                size: 'lg',
                resolve: {
                    getUser: function () {
                        return user;
                    }
                }
            });

            modalInstance.result.then(function (result) {
                personaServices.createPersona(result);
                $state.go('personas', {});
            }, function () {
            });
        }


    }).controller("PractitionerSearchController",
    function ($scope, $rootScope, $state, $stateParams, fhirApiServices, $uibModal) {

        $scope.onSelected = function (p) {
            $scope.selected.selectedPractitioner = p;
            $scope.selected.practitionerSelected = true;
            $scope.showing.practitionerDetail = true;
        };

        if ($state.current.name === 'practitioners') {
            $scope.showing.createPractitioner = true;
        }

        $scope.count = {start: 0, end: 0, total: 0};

        $scope.practitioners = [];
        $scope.searchterm = "";
        var lastQueryResult;

        $rootScope.$on('set-loading', function () {
            $scope.showing.searchloading = true;
        });

        $scope.loadMore = function (direction) {
            $scope.showing.searchloading = true;
            var modalProgress = openModalProgressDialog("Searching...");
            fhirApiServices.getNextOrPrevPage(direction, lastQueryResult).then(function (p, queryResult) {
                lastQueryResult = queryResult;
                $scope.practitioners = p;
                $scope.count = fhirApiServices.calculateResultSet(queryResult);
                $scope.showing.searchloading = false;
                $rootScope.$digest();
                modalProgress.dismiss();
            });
        };

        $scope.select = function (i) {
            $scope.onSelected($scope.practitioners[i]);
        };

        $scope.hasNext = function () {
            return fhirApiServices.hasNext(lastQueryResult);
        };

        $scope.hasPrev = function () {
            return fhirApiServices.hasPrev(lastQueryResult);
        };

        $scope.$watch("searchterm", function () {
            var tokens = [];
            ($scope.searchterm || "").split(/\s/).forEach(function (t) {
                tokens.push(t.toLowerCase());
            });
            $scope.tokens = tokens;
            if ($scope.getMore !== undefined) {
                $scope.getMore();
            }
        });

        var loadCount = 0;
        var search = _.debounce(function (thisLoad) {
            var modalProgress = openModalProgressDialog("Searching...");
            fhirApiServices.queryResourceInstances("Practitioner", undefined, $scope.tokens, [['family', 'asc'], ['given', 'asc']])
                .then(function (p, queryResult) {
                    lastQueryResult = queryResult;
                    if (thisLoad < loadCount) {   // not sure why this is needed (pp)
                        return;
                    }
                    $scope.practitioners = p;
                    $scope.showing.searchloading = false;
                    $scope.count = fhirApiServices.calculateResultSet(queryResult);
                    modalProgress.dismiss();
                    $rootScope.$digest();
                });
        }, 600);

        $scope.getMore = function () {
            $scope.showing.searchloading = true;
            search(++loadCount);
        };

        function openModalProgressDialog(title) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'static/js/templates/progressModal.html',
                controller: 'ProgressModalCtrl',
                size: 'sm',
                resolve: {
                    getTitle: function () {
                        return title;
                    }
                }
            });
        }

        $rootScope.$on('practitioner-created', function () {
            $scope.getMore();
        });

    }).controller("LaunchScenariosController",
    function ($rootScope, $scope, $state, sandboxManagement, launchApp, userServices, descriptionBuilder, docLinks) {
        $scope.showing = {detail: false, addingContext: false};
        $scope.isCustom = false;
        $scope.canDelete = false;
        $scope.selectedScenario = {};
        $scope.launchEmbedded = false;
        $scope.editDesc = {new: "", showEdit: false};
        $scope.editLaunchUri = {new: "", showEdit: false};
        sandboxManagement.getSandboxLaunchScenarios();
        sandboxManagement.clearScenarioBuilder();
        sandboxManagement.getScenarioBuilder().owner = userServices.getOAuthUser();
        $scope.docLink = docLinks.docLink;

        $scope.$watch('selectedScenario.launchEmbedded', function () {
            if ($scope.selectedScenario.launchEmbedded !== undefined && $scope.selectedScenario.launchEmbedded !== $scope.launchEmbedded) {
                $scope.launchEmbedded = $scope.selectedScenario.launchEmbedded;
                sandboxManagement.updateLaunchScenario($scope.selectedScenario);
            }
        });
        $scope.launch = function (scenario) {
            scenario.lastLaunchSeconds = new Date().getTime();
            sandboxManagement.launchScenarioLaunched(scenario);

            launchApp.launch(scenario.app, scenario.patient, scenario.contextParams, scenario.userPersona, scenario.launchEmbedded);
        };

        $scope.launchPatientDataManager = function (patient) {
            launchApp.launchPatientDataManager(patient);
        };

        $scope.delete = function (scenario) {
            sandboxManagement.deleteLaunchScenario(scenario);
            $scope.selectedScenario = {};
            $scope.showing.detail = false;
        };

        $scope.updateDesc = function (scenario) {
            scenario.description = $scope.editDesc.new;
            sandboxManagement.updateLaunchScenario(scenario);
            $scope.editDesc.showEdit = false;
        };

        $scope.cancelDesc = function (scenario) {
            $scope.editDesc.new = angular.copy(scenario.description);
            $scope.editDesc.showEdit = false;
        };

        $scope.updateUri = function (scenario) {
            scenario.app.launchUri = $scope.editLaunchUri.new;
            sandboxManagement.updateLaunchScenario(scenario);
            $scope.editLaunchUri.showEdit = false;
        };

        $scope.cancelUri = function (scenario) {
            $scope.editLaunchUri.new = angular.copy(scenario.app.launchUri);
            $scope.editLaunchUri.showEdit = false;
        };

        $rootScope.$on('recent-selected', function (event, arg) {
            $scope.showing.detail = true;
            $scope.selectedScenario = arg;
            $scope.launchEmbedded = arg.launchEmbedded;
            $scope.canDelete = userServices.canModify($scope.selectedScenario, sandboxManagement.getSandbox());
            $scope.editDesc.new = angular.copy(arg.description);
            $scope.editLaunchUri.new = angular.copy(arg.app.launchUri);
            $scope.isCustom = ($scope.selectedScenario.app.authClient.authDatabaseId === null &&
                $scope.selectedScenario.app.authClient.clientId !== "bilirubin_chart" &&
                $scope.selectedScenario.app.authClient.clientId !== "my_web_app");
            if ($scope.selectedScenario.app.logoUri) {
                $scope.selectedScenario.app.logoUri = $scope.selectedScenario.app.logoUri + "?" + new Date().getTime();
            }
            $scope.desc = descriptionBuilder.launchScenarioDescription($scope.selectedScenario);
            sandboxManagement.setSelectedScenario(arg);
        });

        $rootScope.$on('full-selected', function (event, arg) {
            $scope.showing.detail = true;
            $scope.selectedScenario = arg;
            $scope.launchEmbedded = arg.launchEmbedded;
            $scope.canDelete = userServices.canModify($scope.selectedScenario, sandboxManagement.getSandbox());
            $scope.editDesc.new = angular.copy(arg.description);
            $scope.editLaunchUri.new = angular.copy(arg.app.launchUri);
            $scope.isCustom = ($scope.selectedScenario.app.authClient.authDatabaseId === null &&
                $scope.selectedScenario.app.authClient.clientId !== "bilirubin_chart" &&
                $scope.selectedScenario.app.authClient.clientId !== "my_web_app");
            if ($scope.selectedScenario.app.logoUri) {
                $scope.selectedScenario.app.logoUri = $scope.selectedScenario.app.logoUri + "?" + new Date().getTime();
            }
            $scope.desc = descriptionBuilder.launchScenarioDescription($scope.selectedScenario);
            sandboxManagement.setSelectedScenario(arg);
        });

    }).controller("PersonaController",
    function ($rootScope, $scope, $state, $filter, sandboxManagement, userServices, personaServices, docLinks) {

        $scope.showing = {
            detail: false,
            deletePersona: false,
            selectForScenario: false
        };
        $scope.editPassword = {new: "", showEdit: false};
        $scope.canDelete = false;
        $scope.canModify = false;
        $scope.selectedPersona = {};
        $scope.personaList = [];
        $scope.docLink = docLinks.docLink;

        personaServices.getPersonaListBySandbox();
        personaServices.clearUserPersonaBuilder();

        if ($state.current.name === 'personas') {
            $scope.showing.deletePersona = true;
        }

        if ($state.current.name === 'persona-view') {
            $scope.showing.selectForScenario = true;
        }

        $scope.goToPersona = function () {
            $rootScope.$emit('persona-create');
            $state.go('personas');
        };

        $scope.updatePassword = function (persona) {
            persona.password = $scope.editPassword.new;
            personaServices.updatePersona(persona);
            $scope.editPassword.showEdit = false;
        };

        $scope.cancelPassword = function (persona) {
            $scope.editPassword.new = angular.copy(persona.password);
            $scope.editPassword.showEdit = false;
        };

        $scope.delete = function (persona) {
            personaServices.deletePersona(persona);
            $scope.selectedPersona = {};
            $scope.showing.detail = false;
        };

        $scope.setPersona = function (selectedPersona) {
            sandboxManagement.getScenarioBuilder().userPersona = selectedPersona;

            if (selectedPersona.resource === "Patient") {
                sandboxManagement.getScenarioBuilder().patient =
                    {
                        fhirId: selectedPersona.fhirId,
                        resource: selectedPersona.resource,
                        name: selectedPersona.fhirName
                    };
                $state.go('apps', {source: 'patient', action: 'choose'});
            } else {
                $state.go('patient-view', {source: 'patient'});
            }
        };

        $rootScope.$on('persona-list-update', function () {
            $scope.personaList = personaServices.getPersonaList();
            $rootScope.$digest();
        });

        $rootScope.personaSelected = function (persona) {
            $scope.showing.detail = true;
            $scope.editPassword.new = angular.copy(persona.password);
            $scope.selectedPersona = persona;
            canDeletePersona(persona)
        };

        function canDeletePersona(persona) {
            sandboxManagement.getLaunchScenarioByUserPersona(persona.id).then(function (launchScenarios) {
                $scope.canDelete = false;
                if (!(launchScenarios.length > 0)) {
                    $scope.canDelete = userServices.canModify(persona, sandboxManagement.getSandbox());
                }
                $scope.canModify = userServices.canModify(persona, sandboxManagement.getSandbox());
                $rootScope.$digest();
            });
        }


    }).controller("PersonaSearchController",
    function ($rootScope, $scope, $state, personaServices) {


    }).controller("ContextParamController",
    function ($scope, sandboxManagement) {

        $scope.selectedContext = {};
        $scope.contextSelected = false;
        $scope.contextName = "";
        $scope.contextValue = "";
        $scope.contextNameIsValid = false;
        $scope.contextValueIsValid = false;

        $scope.toggleAddingContext = function () {
            $scope.showing.addingContext = !$scope.showing.addingContext;
        };

        $scope.$watchGroup(['contextName', 'contextValue'], function () {
            $scope.contextNameIsValid = $scope.contextName.trim() !== "";
            $scope.contextValueIsValid = $scope.contextValue.trim() !== "";
        });

        $scope.contextIsValid = function () {
            return $scope.contextNameIsValid && $scope.contextValueIsValid;
        };

        $scope.saveContextParam = function () {
            if ($scope.contextNameIsValid && $scope.contextValueIsValid) {
                $scope.selectedScenario.contextParams.push({name: $scope.contextName, value: $scope.contextValue});
                sandboxManagement.updateLaunchScenario($scope.selectedScenario);
                $scope.contextName = "";
                $scope.contextValue = "";
                $scope.showing.addingContext = false;
            }
        };

        $scope.delete = function () {
            $scope.selectedScenario.contextParams = $scope.selectedScenario.contextParams.filter(function (obj) {
                return (obj !== $scope.selectedContext );
            });
            sandboxManagement.updateLaunchScenario($scope.selectedScenario);
            $scope.selectedContext = {};
            $scope.contextSelected = false;
        };

        $scope.cancel = function () {
            $scope.contextName = "";
            $scope.contextValue = "";
            $scope.showing.addingContext = false;
        };

        $scope.selectContext = function (contextItem) {
            // Toggle selection
            if ($scope.selectedContext === contextItem) {
                $scope.selectedContext = {};
                $scope.contextSelected = false;
            } else {
                $scope.selectedContext = contextItem;
                $scope.contextSelected = true;
            }
        };

    }).controller("RecentTableCtrl",
    function ($rootScope, $scope, sandboxManagement) {
        $scope.selectedScenario = '';
        $scope.launchScenarioList = [];
        $scope.fullTable = false;

        $scope.scenarioSelected = function (scenario) {
            $scope.selectedScenario = scenario;
            $rootScope.$emit('recent-selected', $scope.selectedScenario)
        };

        $rootScope.$on('launch-scenario-list-update', function () {
            $scope.launchScenarioList = sandboxManagement.getRecentLaunchScenarioList();
            $rootScope.$digest();
        });

        $rootScope.$on('full-selected', function () {
            $scope.selectedScenario = '';
        });

    }).controller("FullTableCtrl",
    function ($rootScope, $scope, sandboxManagement) {
        $scope.selectedScenario = '';
        $scope.launchScenarioList = [];
        $scope.fullTable = true;

        $scope.scenarioSelected = function (scenario) {

            $scope.selectedScenario = scenario;
            $rootScope.$emit('full-selected', $scope.selectedScenario);
        };

        $rootScope.$on('launch-scenario-list-update', function () {
            $scope.launchScenarioList = sandboxManagement.getFullLaunchScenarioList();
            $rootScope.$digest();
        });

        $rootScope.$on('recent-selected', function () {
            $scope.selectedScenario = '';
        });

    }).controller("AppPickerController", function ($rootScope, $scope, $state, $stateParams, appRegistrationServices, appsService, customFhirApp, launchApp, sandboxManagement, $uibModal) {
    $scope.all_user_apps = [];
    var source = $stateParams.source;
    var action = $stateParams.action;

    $scope.title = "Select a Registered App for the Launch Scenario";
    $scope.isAppsPicker = true;

    appsService.getSampleApps().done(function (patientApps) {
        appRegistrationServices.getSandboxApps().done(function () {
            $scope.all_user_apps = angular.copy(appRegistrationServices.getAppList());
            for (var i = 0; i < patientApps.length; i++) {
                if (patientApps[i]["isDefault"] !== undefined) {
                    $scope.all_user_apps.push(angular.copy(patientApps[i]));
                }
            }
            $rootScope.$digest();
        })
    });

    $scope.select = function launch(app) {

        // choose for the launch scenario
        if (action === 'choose') {
            sandboxManagement.getScenarioBuilder().app = app;
            openModalDialog(sandboxManagement.getScenarioBuilder());
            // } else {  // Launch
            //     //TODO fix launch only
            //     if (source === 'patient' || source === 'practitioner-patient') {
            //         launchApp.launch(app, sandboxManagement.getSelectedScenario().patient);
            //     } else {
            //         launchApp.launch(app);
            //     }
        }
    };

    function openModalDialog(scenario) {

        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'static/js/templates/launchScenarioModal.html',
            controller: 'ModalInstanceCtrl',
            size: 'lg',
            resolve: {
                getScenario: function () {
                    return scenario;
                }
            }
        });

        modalInstance.result.then(function (result) {
            var scenario = result.scenario;
            if (result.launch) {
                launchApp.launch(scenario.app, scenario.patient, scenario.contextParams, scenario.userPersona, scenario.launchEmbedded);
            } else {
                sandboxManagement.addFullLaunchScenarioList(scenario);
            }
            $state.go('launch-scenarios', {});
        }, function () {
        });
    }

    // get from localStorage
    $scope.customapp = customFhirApp.get();

    $scope.launchCustom = function launchCustom() {
        //set localStorage
        customFhirApp.set($scope.customapp);
        $scope.select({
            launchUri: $scope.customapp.url,
            authClient: {
                clientName: "Custom App",
                clientId: $scope.customapp.id,
                isCustom: true
            }
        });
    };

}).controller('ModalInstanceCtrl', ['$scope', '$uibModalInstance', "getScenario",
    function ($scope, $uibModalInstance, getScenario) {

        $scope.scenario = getScenario;

        $scope.saveLaunchScenario = function (scenario, launch) {
            var result = {
                scenario: scenario,
                launch: launch
            };
            $uibModalInstance.close(result);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }]).controller('ModalPersonaInstanceCtrl', ['$scope', '$rootScope', '$uibModalInstance', "getUser", "sandboxManagement", "personaServices", "docLinks",
    function ($scope, $rootScope, $uibModalInstance, getUser, sandboxManagement, personaServices, docLinks) {

        $scope.invalidMessage = "User Id Not Available";
        $scope.user = getUser;
        $scope.title = "Save " + $scope.user.resource + " Persona";
        $scope.sandboxId = sandboxManagement.getSandbox().sandboxId;
        $scope.docLink = docLinks.docLink;

        $scope.savePersona = function (persona) {
            persona.personaUserId = persona.personaUserId + "@" + $scope.sandboxId;
            $uibModalInstance.close(persona);
        };

        $scope.$watchGroup(['user.personaUserId', 'user.password'], _.debounce(function () {
            $scope.validateId($scope.user.personaUserId).then(function (valid) {
                $scope.isIdValid = valid;
                $scope.showError = !$scope.isIdValid && ($scope.user.personaUserId !== "" && $scope.user.personaUserId !== undefined);
                $scope.createEnabled = valueSet($scope.user.password) && $scope.isIdValid;
                $rootScope.$digest();
            });
        }, 400));

        function valueSet(value) {
            return (typeof value !== 'undefined' && value !== '');
        }

        $scope.validateId = function (id) {
            var deferred = $.Deferred();

            $scope.invalidMessage = "User Id Not Available";
            if ($scope.tempUserId !== id) {
                $scope.tempUserId = id;
                if (id !== undefined && id !== "" && id.length <= 50 && /^[a-zA-Z0-9]*$/.test(id)) {
                    personaServices.checkForUserPersonaById(id + "@" + $scope.sandboxId).then(function (persona) {
                        deferred.resolve(persona === undefined || persona === "");
                    });
                } else {
                    $scope.tempUserId = "<user id>";
                    $scope.invalidMessage = "User Id Is Invalid";
                    deferred.resolve(false);
                }
            } else {
                deferred.resolve($scope.isIdValid);
            }
            return deferred;

        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }]).controller('ProgressModalCtrl', ['$scope', '$uibModalInstance', "getTitle",
    function ($scope, $uibModalInstance, getTitle) {

        $scope.title = getTitle;

    }]).controller('CreateNewPatientCtrl', function ($scope, $rootScope, $uibModal, fhirApiServices) {
    var now = new Date();
    now.setMilliseconds(0);
    now.setSeconds(0);

    $scope.master = {
        resourceType: "Patient",
        active: true,
        name: [
            {given: [], family: [], text: ""}
        ],
        birthDateTime: now
    };

    $scope.open = function () {

        $scope.newPatient = angular.copy($scope.master);

        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'static/js/templates/patientCreateModal.html',
            controller: 'CreatePatientModalInstanceCtrl',
            size: 'md',
            resolve: {
                modalPatient: function () {
                    return $scope.newPatient;
                }
            }
        });

        modalInstance.result.then(function (modalPatient) {
            // capture the date only for the birthDate value
            // modalPatient.birthDate = new Date(modalPatient.birthDateTime).toISOString().substring(0, 10);
            // todo support storing the birthDateTime in the extention when FHIR supports it
            fhirApiServices.createResourceInstance(modalPatient);
            $rootScope.$emit('patient-created');
        }, function () {
        });
    };

}).controller('CreatePatientModalInstanceCtrl', function ($scope, $filter, $uibModalInstance, modalPatient) {

    $scope.modalPatient = modalPatient;

    $scope.isGivenNameValid = function () {
        return $scope.modalPatient.name[0].given[0] != null && $scope.modalPatient.name[0].given[0] != "";
    };

    $scope.isFamilyNameValid = function () {
        return $scope.modalPatient.name[0].family[0] != null && $scope.modalPatient.name[0].family[0] != "";
    };

    $scope.isGenderValid = function () {
        return $scope.modalPatient.gender != null;
    };

    $scope.isBirthDateValid = function () {
        return $scope.modalPatient.birthDateTime != null;
    };

    $scope.isPatientValid = function () {
        return $scope.isGivenNameValid() && $scope.isFamilyNameValid() && $scope.isGenderValid() && $scope.isBirthDateValid();
    };

    $scope.createPatient = function () {
        if ($scope.isPatientValid()) {
            $scope.modalPatient.name[0].text = $filter('nameGivenFamily')($scope.modalPatient);
            $uibModalInstance.close($scope.modalPatient);
        } else {
            console.log("sorry not valid", arguments);
        }
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };
}).controller('CreateNewPractitionerCtrl', function ($scope, $rootScope, $uibModal, fhirApiServices) {
    var now = new Date();
    now.setMilliseconds(0);
    now.setSeconds(0);

    $scope.master = {
        resourceType: "Practitioner",
        active: true,
        name: {given: [], family: [], text: "", suffix: []},
        practitionerRole: [
            {
                specialty: [{coding: [{display: ""}]}],
                role: {coding: [{display: ""}]}
            }
        ]
    };

    $scope.open = function () {

        $scope.newPractitioner = angular.copy($scope.master);

        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'static/js/templates/practitionerCreateModal.html',
            controller: 'CreatePractitionerModalInstanceCtrl',
            size: 'md',
            resolve: {
                modalPractitioner: function () {
                    return $scope.newPractitioner;
                }
            }
        });

        modalInstance.result.then(function (modalPractitioner) {
            fhirApiServices.createResourceInstance(modalPractitioner);
            $rootScope.$emit('practitioner-created');
        }, function () {
        });
    };

}).controller('CreatePractitionerModalInstanceCtrl', function ($scope, $filter, $uibModalInstance, modalPractitioner) {

    $scope.modalPractitioner = modalPractitioner;

    $scope.isGivenNameValid = function () {
        return $scope.modalPractitioner.name.given[0] != null && $scope.modalPractitioner.name.given[0] != "";
    };

    $scope.isFamilyNameValid = function () {
        return $scope.modalPractitioner.name.family[0] != null && $scope.modalPractitioner.name.family[0] != "";
    };

    $scope.isPractitionerValid = function () {
        return $scope.isGivenNameValid() && $scope.isFamilyNameValid();
    };

    $scope.createPractitioner = function () {
        if ($scope.isPractitionerValid()) {
            $scope.modalPractitioner.name.text = $filter('nameGivenFamily')($scope.modalPractitioner);
            $uibModalInstance.close($scope.modalPractitioner);
        } else {
            console.log("sorry not valid", arguments);
        }
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };
}).controller("BindContextController",
    function ($scope, fhirApiServices, $stateParams, oauth2, tools) {

        $scope.showing = {
            noPatientContext: true,
            content: false,
            searchloading: true
        };

        $scope.selected = {
            selectedPatient: {},
            patientSelected: false,
            preLaunch: false
        };

        $scope.patientQuery = undefined;

        function parseContextParams(contextParams) {
            var decoded = decodeURIComponent(contextParams);
            var paramPairs = decoded.split(",");
            var map = {};
            for (var i = 0; i < paramPairs.length; i++) {
                var parts = paramPairs[i].split('=');
                map[parts[0]] = parts[1];
            }
            return map;
        }

        var showPatientIdStr = parseContextParams($stateParams.context_params)["show_patient_id"];
        $scope.showPatientId = (showPatientIdStr != null && showPatientIdStr == "true");

        if (fhirApiServices.clientInitialized()) {
            // all is good
            $scope.showing.content = true;
        } else {
            // need to complete authorization cycle
            fhirApiServices.initClient();
        }

        $scope.clientName = decodeURIComponent($stateParams.clientName)
            .replace(/\+/, " ");

        if ($stateParams.patients !== undefined) {
            $scope.selected.preLaunch = true;
            $scope.patientQuery = {};
            var queryString = decodeURIComponent($stateParams.patients);
            if (queryString !== "none") {
                // For now the query should only be a Patient query.
                // In the future this query maybe more complex ex. Observations with high blood pressure, where
                // we would display the Patient who are references in the Observations
                if (queryString.indexOf("Patient?") === 0) {
                    queryString = queryString.substr("Patient?".length);
                    var queryItems = queryString.split("&");
                    angular.forEach(queryItems, function (item) {
                        var parts = item.split("=");
                        $scope.patientQuery[parts[0]] = parts[1];
                    });
                }
            } else {
                var to = decodeURIComponent($stateParams.endpoint);
                return window.location = to + "?patient_id=none&iss=" + $stateParams.iss + "&launch_uri=" + $stateParams.launch_uri + "&context_params=" + $stateParams.context_params;
            }
        }

        $scope.onSelected = $scope.onSelected || function (p) {
            var pid = p.id;
            var client_id = tools.decodeURLParam($stateParams.endpoint, "client_id");

            // Pre Launch is for the mock launch flow
            if ($scope.selected.preLaunch) {
                var to = decodeURIComponent($stateParams.endpoint);
                return window.location = to + "?patient_id=" + pid + "&iss=" + $stateParams.iss + "&launch_uri=" + $stateParams.launch_uri + "&context_params=" + $stateParams.context_params;
            } else {

                fhirApiServices
                    .registerContext({client_id: client_id}, {patient: pid})
                    .then(function (c) {
                        var to = decodeURIComponent($stateParams.endpoint);
                        to = to.replace(/scope=/, "launch=" + c.launch_id + "&scope=");
                        return window.location = to;
                    });
            }
        };
    }).controller("AppsController", function ($scope, $rootScope, $state, appRegistrationServices, sandboxManagement,
                                              userServices, tools, fhirApiServices, appsService, personaServices, launchApp, $uibModal, docLinks,
                                              customFhirApp) {

    $scope.all_user_apps = [];
    $scope.default_apps = [];
    $scope.galleryOffset = 246;
    $scope.canDelete = false;
    $scope.canModify = false;
    $scope.isAppsPicker = false;
    $scope.docLink = docLinks.docLink;
    $scope.clientTypes = ["Public Client", "Confidential Client"];

    var defaultPersona;
    personaServices.getDefaultPersonaBySandbox().then(function (persona) {
        defaultPersona = persona;
    });

    $scope.showing = {appDetail: false};

    $scope.selected = {
        selectedApp: {}
    };
    $scope.clientJSON = {};

    appRegistrationServices.getSandboxApps();

    appsService.getSampleApps().done(function (patientApps) {
        for (var i = 0; i < patientApps.length; i++) {
            if (patientApps[i]["isDefault"] !== undefined) {
                $scope.default_apps.push(angular.copy(patientApps[i]));
            }
        }
    });

    $scope.customapp = customFhirApp.get();

    $scope.launchCustom = function launchCustom() {
        //set localStorage
        customFhirApp.set($scope.customapp);
        $scope.select({
            launchUri: $scope.customapp.url,
            authClient: {
                clientName: "Custom App",
                clientId: $scope.customapp.id,
                isCustom: true
            }
        });
    };


    $rootScope.$on('app-list-update', function () {
        $scope.all_user_apps = angular.copy(appRegistrationServices.getAppList());
        $scope.all_user_apps = $scope.all_user_apps.concat($scope.default_apps);
        $scope.isAppsPicker = true;
        $rootScope.$digest();
    });

    $scope.registration = function () {
        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'static/js/templates/appRegisterModal.html',
            controller: 'AppRegistrationModalCtrl',
            size: 'lg'

        });

        modalInstance.result.then(function (app) {
            var modalProgress = openModalProgressDialog();
            appRegistrationServices.createSandboxApp(app).then(function (result) {
                modalProgress.dismiss();
                showClientId(result.authClient.clientId);
            }, function (err) {
                modalProgress.dismiss();
                $state.go('error', {});
            });
        });
    };

    $scope.registrationInbound = function () {
        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'static/js/templates/appRegisterInboundModal.html',
            controller: 'AppRegistrationInboundModalCtrl',
            size: 'lg'

        });

        modalInstance.result.then(function (app) {
            var modalProgress = openModalProgressDialog();
            appRegistrationServices.createSandboxApp(app).then(function (result) {
                modalProgress.dismiss();
                showClientId(result.authClient.clientId);
            }, function (err) {
                modalProgress.dismiss();
                $state.go('error', {});
            });
        });
    };

    function showClientId(client_id) {
        $uibModal.open({
            animation: true,
            templateUrl: 'static/js/templates/messageModal.html',
            controller: 'MessageModalInstanceCtrl',
            size: 'md',
            resolve: {
                getSettings: function () {
                    return {
                        title: "App Client Id",
                        message: "Use this Client Id in your app with the authorization request.",
                        displayValue: {label: "Client Id:", value: client_id}
                    }
                }
            }
        });
    }

    function openModalProgressDialog() {
        return $uibModal.open({
            animation: true,
            templateUrl: 'static/js/templates/progressModal.html',
            controller: 'ProgressModalCtrl',
            size: 'sm',
            resolve: {
                getTitle: function () {
                    return "Saving...";
                }
            }
        });
    }

    $scope.select = function (app) {
        canDeleteApp(app);
        $scope.isInbound = app.appManifestUri !== null;
        $scope.selected.selectedApp = app;
        $scope.showing.appDetail = true;
        if($scope.clientJSON){
            delete $scope.clientJSON.logo;
        }
        $scope.myFile = undefined;
        if (app.clientJSON) {
            $scope.clientJSON = app.clientJSON;
        } else if ($scope.clientJSON) {
            delete $scope.clientJSON.logoUri;
        }
        if (app.isDefault === true) {
            $scope.clientJSON.clientName = $scope.selected.selectedApp.authClient.clientName;
            $scope.clientJSON.redirectUri = $scope.selected.selectedApp.authClient.redirectUri;
            $scope.clientJSON.launchUri = $scope.selected.selectedApp.launchUri;
            $scope.clientJSON.samplePatients = $scope.selected.selectedApp.samplePatients;
            $scope.clientJSON.logoUri = $scope.selected.selectedApp.logoUri + "?" + new Date().getTime();
        } else {
            if(app.id){
                appRegistrationServices.getSandboxApp(app.id).then(function (resultApp) {
                    $scope.galleryOffset = 80;
                    $scope.selected.selectedApp.clientJSON = JSON.parse(resultApp.clientJSON);
                    $scope.clientJSON = $scope.selected.selectedApp.clientJSON;
                    if (resultApp.logoUri) {
                        $scope.clientJSON.logoUri = resultApp.logoUri + "?" + new Date().getTime();
                    }
                    if ($scope.selected.selectedApp.clientJSON.tokenEndpointAuthMethod === "SECRET_BASIC") {
                        $scope.clientJSON.clientType = "Confidential Client";
                    } else {
                        $scope.clientJSON.clientType = "Public Client";
                    }
                    $scope.clientJSON.launchUri = $scope.selected.selectedApp.launchUri;
                    $scope.clientJSON.samplePatients = $scope.selected.selectedApp.samplePatients;
                    $scope.clientJSON.scope = $scope.clientJSON.scope.join(" ");
                    $rootScope.$digest();
                });
            }
        }
    };

    function canDeleteApp(app) {
        $scope.canDelete = false;
        if(app.id) {
            sandboxManagement.getLaunchScenarioByApp(app.id).then(function (launchScenarios) {
                if (!(launchScenarios.length > 0)) {
                    $scope.canDelete = userServices.canModify(app, sandboxManagement.getSandbox());
                }
                $scope.canModify = userServices.canModify(app, sandboxManagement.getSandbox());
                $rootScope.$digest();
            });
        }
    }

    $scope.canModifyApp = function (app) {
        if (app.isDefault === true || app.appManifestUri) {
            return false;
        } else {
            return userServices.canModify(app, sandboxManagement.getSandbox());
        }
    };

    $scope.updateFile = function (files) {

        $scope.myFile = files[0];

        var reader = new FileReader();
        reader.onload = function (e) {
            $scope.clientJSON.logo = e.target.result;
            $rootScope.$digest();
        };
        var url = reader.readAsDataURL(files[0]);
    };

    $scope.quickLaunch = function (app, sample) {
        var patientQuery;
        if (sample !== undefined) {
            app.samplePatients = sample;
        }
        var queryString = app.samplePatients;

        // Some parsing to see if there's exactly one patient id
        if (queryString !== null && queryString !== undefined && queryString.indexOf("_id=") > -1) {
            var i = queryString.indexOf("_id=");
            queryString = queryString.substr(i + "_id=".length);

            var queryItems = queryString.split("&");
            queryItems = queryItems[0];
            queryItems = queryItems.split(",");
            if (queryItems.length === 1) {
                patientQuery = queryItems[0];
            }
        }

        if (patientQuery !== undefined) {
            launchApp.launchFromApp(app, {fhirId: patientQuery}, defaultPersona);
        } else {
            openPatientPicker(app);
        }
    };


    function openPatientPicker(app) {
        var patientQuery = app.samplePatients;
        if (patientQuery !== null && patientQuery !== undefined && patientQuery.indexOf("Patient?") === 0) {
            patientQuery = patientQuery.substring("Patient?".length);
        }

        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'static/js/templates/patientPickerModal.html',
            controller: 'PatientPickerModalCtrl',
            size: 'md',
            resolve: {
                getSettings: function () {
                    return {
                        patientQuery: patientQuery
                        // callback:function(result){ //setting callback
                        //     if (result == true) {
                        //         appRegistrationServices.deleteSandboxApp($scope.selected.selectedApp.id).then(function () {
                        //             $scope.selected.selectedApp = {};
                        //         });
                        //     }
                        // }
                    };
                }
            }

        });

        modalInstance.result.then(function (patient) {
            launchApp.launchFromApp(app, patient, defaultPersona);
        });
    }

    $scope.save = function () {
        if ($scope.myFile !== undefined) {
            $scope.selected.selectedApp.logo = $scope.myFile;
        }
        var updateClientJSON = angular.copy($scope.clientJSON);
        delete updateClientJSON.logo;

        if (updateClientJSON.logoUri) {
            var i = updateClientJSON.logoUri.lastIndexOf("?");
            if (i > -1) {
                updateClientJSON.logoUri = updateClientJSON.logoUri.substr(0, i);
            }
        }

        if ($scope.clientJSON.clientType !== "Public Client") {
            updateClientJSON.tokenEndpointAuthMethod = "SECRET_BASIC";
        } else {
            updateClientJSON.tokenEndpointAuthMethod = "NONE";
        }

        if (Object.prototype.toString.call(updateClientJSON.redirectUris) !== '[object Array]' &&
            typeof updateClientJSON.redirectUris !== 'undefined') {
            updateClientJSON.redirectUris = updateClientJSON.redirectUris.split(',');
        }

        if (Object.prototype.toString.call(updateClientJSON.scope) !== '[object Array]' &&
            typeof updateClientJSON.scope !== 'undefined') {
            updateClientJSON.scope = updateClientJSON.scope.split(' ');
            if (!contains(updateClientJSON.scope, "launch")) {
                updateClientJSON.scope.push("launch");
            }
        }

        if (!contains(updateClientJSON.scope, "offline_access")) {
            var index = updateClientJSON.grantTypes.indexOf("refresh_token");
            if (index > -1) {
                updateClientJSON.grantTypes.splice(index, 1);
            }
        } else {
            if (!contains(updateClientJSON.grantTypes, "refresh_token")) {
                updateClientJSON.grantTypes.push("refresh_token");
            }
            updateClientJSON.requireAuthTime = false;
        }

        function contains(array, item) {
            var found = false;
            array.forEach(function (cur) {
                if (cur === item) {
                    found = true;
                }
            });
            return found;
        }

        $scope.selected.selectedApp.clientJSON = updateClientJSON;
        $scope.selected.selectedApp.launchUri = updateClientJSON.launchUri;
        $scope.selected.selectedApp.samplePatients = updateClientJSON.samplePatients;
        var modalProgress = openModalProgressDialog();
        appRegistrationServices.updateSandboxApp($scope.selected.selectedApp).then(function (result) {
            $scope.select(result);
            modalProgress.dismiss();
        }, function (err) {
            modalProgress.dismiss();
            $state.go('error', {});
        });
    };

    $scope.delete = function () {
        $scope.showing.appDetail = false;
        $uibModal.open({
            animation: true,
            templateUrl: 'static/js/templates/confirmModal.html',
            controller: 'ConfirmModalInstanceCtrl',
            resolve: {
                getSettings: function () {
                    return {
                        title: "Delete " + $scope.selected.selectedApp.authClient.clientName,
                        ok: "Yes",
                        cancel: "Cancel",
                        type: "confirm-error",
                        text: "Are you sure you want to delete?",
                        callback: function (result) { //setting callback
                            if (result == true) {
                                appRegistrationServices.deleteSandboxApp($scope.selected.selectedApp.id).then(function () {
                                    $scope.selected.selectedApp = {};
                                });
                            }
                        }
                    };
                }
            }
        });
    };

}).controller('AppRegistrationInboundModalCtrl', function ($scope, $rootScope, sandboxManagement, appRegistrationServices, docLinks, tools, apiEndpointIndexServices, $uibModalInstance) {

    $scope.docLink = docLinks.docLink;
    $scope.clientJSON = {};
    $scope.sandboxName = sandboxManagement.getSandbox().name;
    $scope.manifestUrl = "";
    $scope.hasError = false;

    $scope.getManifest = function () {
        $scope.hasError = false;
        if ($scope.manifestUrl !== "") {
            $scope.manifestUrl = tools.stripTrailingSlash($scope.manifestUrl);

            appRegistrationServices.getAppManifest($scope.manifestUrl).then(function (manifest) {
                $scope.appManifest = manifest;
                if (!manifest.fhir_versions || !contains(manifest.fhir_versions, apiEndpointIndexServices.fhirVersion())) {
                    $scope.manifestError = "The FHIR version of this sandbox, " + apiEndpointIndexServices.fhirVersion() + ", is not supported by this app.";
                    $scope.hasError = true;
                }
                $rootScope.$digest();
            }, function (err) {
                $scope.manifestError = "Error: " + err.status + " " + err.statusText;
                $scope.hasError = true;
                $rootScope.$digest();
            });
        }
    };

    function contains(array, item) {
        var found = false;
        array.forEach(function (cur) {
            if (cur === item) {
                found = true;
            }
        });
        return found;
    }

    $scope.registerApp = function (appManifest) {

        $scope.clientJSON.clientName = appManifest.client_name;
        $scope.clientJSON.redirectUris = appManifest.redirect_uris;
        $scope.clientJSON.grantTypes = appManifest.grant_types;
        $scope.clientJSON.tokenEndpointAuthMethod = appManifest.token_endpoint_auth_method;
        $scope.clientJSON.scope = appManifest.scope.split(" ");
        $scope.clientJSON.logoUri = appManifest.logo_uri;

        var authClient = {
            clientName: appManifest.client_name,
            logoUri: appManifest.logo_uri
        };

        var newApp = {
            appManifestUri: $scope.manifestUrl + "/.well-known/smart/manifest.json",
            softwareId: appManifest.software_id,
            fhirVersions: appManifest.fhir_versions.join(","),
            launchUri: appManifest.launch_url,
            logoUri: appManifest.logo_uri,
            authClient: authClient
        };
        newApp.clientJSON = $scope.clientJSON;
        $uibModalInstance.close(newApp);
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss();
    };
}).controller('AppRegistrationModalCtrl', function ($scope, $rootScope, sandboxManagement, docLinks, $uibModalInstance) {

    $scope.clientType = "Public Client";
    // $scope.clientTypes = ["Confidential Client", "Public Client", "Backend Service"];
    $scope.clientTypes = ["Public Client", "Confidential Client"];
    $scope.patientScoped = true;
    $scope.offlineAccess = false;
    $scope.docLink = docLinks.docLink;

    $scope.clientJSON = {};

    $scope.sandboxName = sandboxManagement.getSandbox().name;

    $scope.uploadFile = function (files) {

        $scope.myFile = files[0];

        var reader = new FileReader();
        reader.onload = function (e) {
            $scope.clientJSON.logo = e.target.result;
            $rootScope.$digest();
        };
        var url = reader.readAsDataURL(files[0]);
    };

    $scope.$watchGroup(['clientJSON.clientName', 'clientJSON.launchUri', 'clientJSON.redirectUris'], function () {
        $scope.createEnabled = valueSet($scope.clientJSON.launchUri) && valueSet($scope.clientJSON.clientName);
    });

    function valueSet(value) {
        return (typeof value !== 'undefined' && value !== '');
    }

    $scope.registerApp = function (clientJSON) {

        if (Object.prototype.toString.call(clientJSON.redirectUris) !== '[object Array]' &&
            typeof clientJSON.redirectUris !== 'undefined') {
            clientJSON.redirectUris = clientJSON.redirectUris.split(',');
        } else {
            delete clientJSON.redirectUris;
        }

        if ($scope.clientType !== "Backend Service") {
            clientJSON.grantTypes = ["authorization_code"];
        } else {
            clientJSON.grantTypes = ["client_credentials"];
        }

        if ($scope.clientType !== "Public Client") {
            clientJSON.tokenEndpointAuthMethod = "SECRET_BASIC";
        } else {
            clientJSON.tokenEndpointAuthMethod = "NONE";
        }

        // Just adding some default scopes to start with
        if ($scope.patientScoped) {
            clientJSON.scope = ["launch", "patient/*.*", "profile", "openid"];
        } else {
            clientJSON.scope = ["launch", "user/*.*", "profile", "openid"];
        }

        if ($scope.offlineAccess) {
            clientJSON.scope.push("offline_access");
            clientJSON.grantTypes.push("refresh_token");
            clientJSON.requireAuthTime = false;
        }

        // add some default timeouts
        clientJSON.accessTokenValiditySeconds = 3600; // one hour
        clientJSON.idTokenValiditySeconds = 3600; // one hour
        clientJSON.refreshTokenValiditySeconds = 31557600; // one year

        var authClient = {
            clientName: clientJSON.clientName
        };

        var newApp = {
            launchUri: clientJSON.launchUri,
            samplePatients: $scope.samplePatients,
            logo: $scope.myFile,
            authClient: authClient
        };
        delete clientJSON.logo;
        newApp.clientJSON = clientJSON;
        $uibModalInstance.close(newApp);
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss();
    };
}).controller('ProgressCtrl', ['$rootScope', '$scope', '$state', '$timeout', 'appsSettings', 'branded',
    function ($rootScope, $scope, $state, $timeout, appsSettings, branded) {

        $scope.createProgress = 0;
        $scope.showing.navBar = true;
        $scope.showing.sideNavBar = false;
        $scope.showing.footer = false;

        var messageNum = 0;
        var messages = branded.sandboxDescription.checkList;
        updateProgress();
        fadeMessage();

        $rootScope.$on('sandbox-created', function (event, sandboxId) {
            $scope.createProgress = 100;
            $timeout(function () {
                window.location.href = appsSettings.getSandboxUrlSettings().sandboxManagerRootUrl + "/" + sandboxId;
                // $rootScope.$emit('signed-in', sandboxId);
            }, 500);
        });

        function fadeMessage() {
            $timeout(function () {
                $scope.message = messages[messageNum];
                $scope.showMessage = true;
                // Loading done here - Show message for 3 more seconds.
                $timeout(function () {
                    $scope.showMessage = false;
                    messageNum++;
                    if (messageNum <= 7) {
                        fadeMessage();
                    }
                }, 3000);
            }, 500);
        }

        function updateProgress() {
            $scope.createProgress += 0.333;   // Progress .333% at a time
            if ($scope.createProgress < 95) {  // If it hits 95%, hold there
                $timeout(updateProgress, 100);  // Wake up every tenth of a second and progress
            }
        }

    }]).controller('ConfirmModalInstanceCtrl', ['$scope', '$uibModalInstance', 'getSettings',
    function ($scope, $uibModalInstance, getSettings) {

        $scope.title = (getSettings.title !== undefined) ? getSettings.title : "";
        $scope.ok = (getSettings.ok !== undefined) ? getSettings.ok : "Yes";
        $scope.cancel = (getSettings.cancel !== undefined) ? getSettings.cancel : "No";
        $scope.text = (getSettings.text !== undefined) ? getSettings.text : "Continue?";
        var callback = (getSettings.callback !== undefined) ? getSettings.callback : null;

        $scope.confirm = function (result) {
            $uibModalInstance.close(result);
            callback(result);
        };
    }]).controller('TermsOfUseModalInstanceCtrl', ['$scope', '$uibModalInstance', 'getSettings',
    function ($scope, $uibModalInstance, getSettings) {

        $scope.title = (getSettings.title !== undefined) ? getSettings.title : "";
        $scope.ok = (getSettings.ok !== undefined) ? getSettings.ok : "Yes";
        $scope.cancel = (getSettings.cancel !== undefined) ? getSettings.cancel : "No";
        $scope.text = (getSettings.text !== undefined) ? getSettings.text : "Continue?";
        $scope.showAccept = (getSettings.showAccept !== undefined) ? getSettings.showAccept : false;
        $scope.isUpdate = (getSettings.isUpdate !== undefined) ? getSettings.isUpdate : false;

        var callback = (getSettings.callback !== undefined) ? getSettings.callback : null;

        $scope.openPDF = function () {
            window.open('https://content.hspconsortium.org/docs/hspc/privacyterms.pdf', '_blank');
        };

        $scope.confirm = function (result) {
            $uibModalInstance.close(result);
            callback(result);
        };
    }]).controller('MessageModalInstanceCtrl', ['$scope', '$uibModalInstance', 'getSettings',
    function ($scope, $uibModalInstance, getSettings) {

        $scope.title = (getSettings.title !== undefined) ? getSettings.title : "";
        $scope.message = getSettings.message;
        $scope.displayValue = getSettings.displayValue;

        $scope.close = function () {
            $uibModalInstance.close();
        };
    }]).controller('SandboxDeleteModalInstanceCtrl', ['$scope', '$uibModalInstance', 'getSettings',
    function ($scope, $uibModalInstance, getSettings) {

        $scope.canDelete = false;

        $scope.$watch('deleteText', function () {
            $scope.canDelete = $scope.deleteText === "DELETE";
        });

        $scope.title = (getSettings.title !== undefined) ? getSettings.title : "";
        $scope.ok = (getSettings.ok !== undefined) ? getSettings.ok : "Yes";
        $scope.cancel = (getSettings.cancel !== undefined) ? getSettings.cancel : "No";
        $scope.text = (getSettings.text !== undefined) ? getSettings.text : "Continue?";
        var callback = (getSettings.callback !== undefined) ? getSettings.callback : null;

        $scope.confirm = function (result) {
            $uibModalInstance.close(result);
            callback(result);
        };
    }]).controller('SandboxResetModalInstanceCtrl', ['$scope', '$uibModalInstance', 'getSettings',
    function ($scope, $uibModalInstance, getSettings) {

        $scope.canReset = false;

        $scope.$watch('resetText', function () {
            $scope.canReset = $scope.resetText === "RESET";
        });

        $scope.title = (getSettings.title !== undefined) ? getSettings.title : "";
        $scope.ok = (getSettings.ok !== undefined) ? getSettings.ok : "Yes";
        $scope.cancel = (getSettings.cancel !== undefined) ? getSettings.cancel : "No";
        $scope.text = (getSettings.text !== undefined) ? getSettings.text : "Continue?";
        var callback = (getSettings.callback !== undefined) ? getSettings.callback : null;

        $scope.confirm = function (result) {
            $uibModalInstance.close(result);
            callback(result);
        };
    }]).controller('ResourceDetailModalInstanceCtrl', ['$scope', '$rootScope', '$filter', '$uibModalInstance', 'getSettings', 'fhirApiServices', 'launchApp',
    function ($scope, $rootScope, $filter, $uibModalInstance, getSettings, fhirApiServices, launchApp) {

        $scope.hasPatient = false;

        $scope.launchPatientDataManager = function (patient) {
            launchApp.launchPatientDataManager(patient);
        };

        if (getSettings.text.resourceType === 'Patient') {
            $scope.patient = getSettings.text;
            $scope.hasPatient = true;
        } else {
            fhirApiServices.runRawQuery(getSettings.patient).then(function (patient) {
                $scope.hasPatient = true;
                $scope.patient = patient;
                $rootScope.$digest();
            }, function (results) {
                $scope.hasPatient = false;
            });
        }

        $scope.title = (getSettings.title !== undefined) ? getSettings.title : "";
        $scope.ok = (getSettings.ok !== undefined) ? getSettings.ok : "Yes";
        $scope.cancel = (getSettings.cancel !== undefined) ? getSettings.cancel : "No";
        $scope.text = $filter('json')(getSettings.text);
        var callback = (getSettings.callback !== undefined) ? getSettings.callback : null;

        $scope.confirm = function (result) {
            $uibModalInstance.close(result);
            callback(result);
        };
    }]).controller('PatientPickerModalCtrl',
    function ($scope, $rootScope, $uibModalInstance, getSettings) {

        $scope.shouldBeOpen = false;

        $scope.showing = {
            noPatientContext: true,
            createPatient: false,
            searchloading: true,
            isModal: true
        };

        $scope.selected = {
            selectedPatient: undefined,
            patientSelected: false
        };
        $scope.size = {
            navBarHeight: 200,
            footerHeight: 20,
            sandboxBarHeight: 20
        };

        $scope.resultCount = 10;
        $scope.patientQuery = {};
        var queryString = getSettings.patientQuery;

        if (queryString !== null && queryString !== undefined && queryString !== "") {
            var queryItems = queryString.split("&");
            angular.forEach(queryItems, function (item) {
                var parts = item.split("=");
                $scope.patientQuery[parts[0]] = parts[1];
            });
        }

        $scope.$watch('selected.selectedPatient', function () {
            if ($scope.selected.selectedPatient !== undefined) {
                $scope.selected.selectedPatient.fhirId = $scope.selected.selectedPatient.id;
                $uibModalInstance.close($scope.selected.selectedPatient);
            }
        });

        $scope.skipPatient = function () {
            $uibModalInstance.close();
        };

        $rootScope.$on('patient-search-start', function () {
            $scope.shouldBeOpen = false;
        });

        $rootScope.$on('patient-search-complete', function () {
            $scope.shouldBeOpen = true;
        });

        $scope.cancel = function () {
            $uibModalInstance.dismiss();
        };
    });

