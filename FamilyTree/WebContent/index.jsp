<!doctype html>
<html ng-app="ftreeApp">
<head>
<meta charset="utf-8" />
<script src="//ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.0/angular.min.js"></script>

<title>        Display remote data    </title>
<style type="text/css">
  a[ ng-click ] {
            color: #0000FF ;
            cursor: pointer ;
            text-decoration: underline ;
  }
</style>   

    <script type="text/javascript">

        var app = angular.module( "ftreeApp", [] );

        app.controller(
            "MemberServiceController",
            function( $scope, getMemberService, $timeout ) {

                $scope.members = [];

                loadRemoteData();

                $scope.displayMemberTree = function( member ) {

                	console.log ( member.firstname );
	    				var searchString = $('#inputstring').val();
	 				   $.ajax({url: "/FamilyTree/FTRequestServlet?searchstring=" + member.firstname, success: function(result){
	 				        $("#contents").html(result);
	 				    }});

                };

                // Instantiate these variables outside the watch
                var tempFilterText = '',
                    filterTextTimeout;
                
                $scope.$watch('searchText', function (val) {
                    if (filterTextTimeout) $timeout.cancel(filterTextTimeout);

                    tempFilterText = val;
                    filterTextTimeout = $timeout(function() {
                        $scope.filterText = tempFilterText;
                    }, 250); // delay 250 ms
                })
                
                // I apply the remote data to the local scope.
                function applyRemoteData( newmembers ) {
                    $scope.members = newmembers;
                }


                // I load the remote data from the server.
                function loadRemoteData() {
                    getMemberService.getMembers()
                        .then(
                            function( members ) {
                                applyRemoteData( members );
                            }
                        )
                    ;
                }

            }
        );

        app.service(
            "getMemberService",
            function( $http, $q ) {
                return({
                    getMembers: getMembers
                });
                
                function getMembers() {
                    var request = $http({
                        method: "get",
                        url: "/FamilyTree/FTRequestServlet",
                        params: {
                            action: "get"
                        }
                    });
                    return( request.then( handleSuccess, handleError ) );
                }

                function handleError( response ) {
                		console.log ( response );
							alert('ERROR : ' + response);
                }

                function handleSuccess( response ) {
                    return( response.data );
                }

            }
        );

    </script>
     
</head>

<body ng-controller="MemberServiceController">
         Directory of Family Heads

    <!-- Show existing members. -->
    	
	<input id="searchText" type="search" placeholder="Filter..." ng-model="searchText" />
   <ul>
	<li class="entry" ng-repeat="member in members | filter:filterText">
    <span>
    	{{ member.firstname }}, {{member.lastname}}, (Has {{member.relations}} direct relative[s])
      <br>
      {{member.relatives}}
      ( <a ng-click="displayMemberTree(member)">Get Family</a> )
    </span>
	</li>
	 </ul>
	<script>
		$(document).ready(function() {
			$("button").click(function() {
				var searchString = $('#inputstring').val();
				   $.ajax({url: "/FamilyTree/FTRequestServlet?searchstring=" + searchString, success: function(result){
				        $("#contents").html(result);
				    }});
			});
		});
	</script>
	
 	<br> Member Search ( Regex ):<br> 
 	<input id="inputstring" type="text" ng-model="name"	name="searchstring"> 
	<button>Find</button>
	<div id="contents">
	</div>

</body>
</html>