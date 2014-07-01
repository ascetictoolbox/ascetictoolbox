var routes = {
    "/api" : "./api"
};

module.exports = function(app) {
    console.log("Configuring routes...");
    for (var p in routes) {
        var route = routes[p];
        console.log(p + " -> " + route);
        app.use(p, require(route));
    }
}