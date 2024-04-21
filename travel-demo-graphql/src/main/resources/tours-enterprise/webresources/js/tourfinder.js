var TourFinder = TourFinder || (function(){
    return {
        init : function(args) {
            angular
                .module('TourFinder', ['ngAnimate', 'ngRoute'])
                .config(function($routeProvider) {
                    $routeProvider.when("/", {
                        templateUrl: `${args.contextPath}/.resources/tours-enterprise/webresources/views/index.html`,
                        controller: 'MainController',
                        reloadOnSearch: false
                    });
                })
                .controller('MainController', ['$scope', '$routeParams', '$http', '$location', function($scope, $routeParams, $http, $location) {
                    var notFoundMessages = ["tourFinder.search.noResults1",
                                            "tourFinder.search.noResults2",
                                            "tourFinder.search.noResults3"];
                    var randomIndex = Math.floor(Math.random() * notFoundMessages.length);
                    $scope.notFoundMessage = notFoundMessages[randomIndex];
                    $scope.durations = [{ value: 2, name: 'tourFinder.duration.options.2-days' },
                                        { value: 7, name: 'tourFinder.duration.options.7-days' },
                                        { value: 14, name: 'tourFinder.duration.options.14-days' },
                                        { value: 21, name: 'tourFinder.duration.options.21-days' }];
                    $scope.useDurations = {};
                    $scope.useDestinations = {};
                    $scope.useTourTypes = {};
                    $scope.search = {};
                    $scope.serviceType = 'rest';

                    $scope.contextPath = args.contextPath;
                    $scope.language = args.language;
                    $scope.i18n = args.i18n;

                    if ($routeParams.service && ['rest', 'graphQL'].includes($routeParams.service)) {
                        $scope.serviceType = $routeParams.service;
                    }

                    let tourService = new TourService(args.contextPath, $http, $location, $scope.serviceType);

                    if ($routeParams.duration) {
                        $scope.useDurations = {};
                        var split = $routeParams.duration.split(',');
                        for (var i in split) {
                            $scope.useDurations[split[i]] = true;
                        }
                    }
                    if ($routeParams.q) {
                        $scope.search.query = $routeParams.q;
                    }

                    // obtain the data
                    tourService.getDestinations($scope.language).then(response => {
                        $scope.destinations = response;
                        if ($routeParams.destination) {
                            let split = $routeParams.destination.split(',');
                            for (let i in split) {
                                $scope.useDestinations[split[i]] = true;
                            }
                        }
                    }, function(response) {
                        console.error("Couldn't reach endpoint.");
                    });

                    tourService.getTourTypes($scope.language).then(response => {
                        $scope.tourTypes = response;
                        if ($routeParams.tourTypes) {
                            let split = $routeParams.tourTypes.split(',');
                            for (let i in split) {
                                $scope.useTourTypes[split[i]] = true;
                            }
                        }
                    }, function(response) {
                        console.error("Couldn't reach endpoint.");
                    });

                    tourService.filterTours({}).then(response => { $scope.tours = response; }
                    , function (response) {
                        console.error("Couldn't reach endpoint.");
                    })

                    // watch for changes
                    $scope.$watch(function() {
                        return {
                            useDurations: $scope.useDurations,
                            useDestinations: $scope.useDestinations,
                            useTourTypes: $scope.useTourTypes,
                            search: $scope.search,
                            destinations: $scope.destinations,
                            tourTypes: $scope.tourTypes,
                            serviceType: $scope.serviceType,
                        };
                    }, function (newValues, oldValues) {
                        // wait for both tourTypes & destinations to be populated by async calls
                        if (newValues !== oldValues && newValues.tourTypes && newValues.destinations) {
                            var randomIndex = Math.floor(Math.random() * notFoundMessages.length);
                            $scope.notFoundMessage = notFoundMessages[randomIndex];

                            var qs = '';
                            var parameters = {duration: [], destination: [], tourTypes: [], q: [], serviceType: []};
                            var durations = Object.keys(newValues.useDurations).reduce(function (filtered, key) {
                                    if (newValues.useDurations[key]) filtered.push(key);
                                    return filtered;
                            }, []);
                            var destinationKeys = Object.keys(newValues.useDestinations).reduce(function (filtered, key) {
                                    if (newValues.useDestinations[key]) filtered.push(key);
                                    return filtered;
                            }, []);
                            var tourTypeKeys = Object.keys(newValues.useTourTypes).reduce(function (filtered, key) {
                                    if (newValues.useTourTypes[key]) filtered.push(key);
                                    return filtered;
                            }, []);

                            if (durations.length > 0 && durations.length < $scope.durations.length) {
                                parameters.duration = durations;
                            }
                            if (destinationKeys.length > 0 && destinationKeys.length < newValues.destinations.length) {
                                parameters.destination = destinationKeys;
                            }
                            if (tourTypeKeys.length > 0 && tourTypeKeys.length < newValues.tourTypes.length) {
                                parameters.tourTypes = tourTypeKeys;
                            }
                            if (newValues.search.query) {
                                parameters.q = [newValues.search.query];
                            }
                            parameters.lang = [args.language];

                            $location.search('service', $scope.serviceType);

                            new TourService(args.contextPath, $http, $location, $scope.serviceType).filterTours(parameters)
                              .then(response => $scope.filteredTours = response)
                        }
                    }, true);
                }]);
        }
    }
})();
