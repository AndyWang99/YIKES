var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);
var players = [];
var platforms = [];

server.listen(8080, function(){
	console.log("Server is now running...");
});

io.on('connection', function(socket) {
	console.log("Player Connected!");
	socket.emit('socketID', { id: socket.id });
	socket.emit('getPlayers', players);
	socket.emit('getPlatforms', platforms);
    socket.broadcast.emit('newPlayer', { id: socket.id});
    socket.on('playerUpdate', function(data) {
        data.id = socket.id;
        socket.broadcast.emit('playerUpdate', data);
        for (var i = 0; i < players.length; i++) {
            if (players[i].id == data.id) {
                players[i].x = data.x;
                players[i].y = data.y;
            }
        }
    });

    socket.on('setInitialPlatforms', function(array) {
            for (var i = 0; i < array.length; i++) {
                platforms.push(new platform(array[i].width, array[i].x, array[i].y));
            }
        });

    socket.on('platformsUpdate', function(array) {
        for (var i = 0; i < array.length; i++) {
            platforms[i].x = array[i].x;
            platforms[i].y = array[i].y;
            platforms[i].width = array[i].width;
        }
    });

	socket.on('disconnect', function() {
		console.log("Player Disconnected");
		socket.broadcast.emit('playerDisconnected', { id: socket.id });
		for (var i = 0; i < players.length; i++) {
            if (players[i].id == socket.id) {
                players.splice(i, 1);
            }
		}
	});
	players.push(new player(socket.id, 200, 200));
});

function player(id, x, y) {
    this.id = id;
    this.x = x;
    this.y = y;
}

function platform(width, x, y) {
    this.width = width;
    this.x = x;
    this.y = y;
}