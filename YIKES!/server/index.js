const min_hole_width = 120
const max_additional_hole_width = 60
const num_platforms = 6
const platform_interval = 190;
const min_velocity = 20;
const max_additional_velocity = 10;
const yikes_width = 480;
const ground_height = 150;
var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);
var players = [];
var platforms = [];
var boulders = [];

server.listen(8080, function(){
	console.log("Server is now running...");
    for (var i = 0; i < num_platforms; i++) {
         var width = min_hole_width + Math.random() * max_additional_hole_width;
         platforms.push(new platform(width, Math.random() * (yikes_width - width), ground_height + (i+1) * platform_interval));
         boulders.push(new boulder(0, platform_interval * i, -100));
    }
});

io.on('connection', function(socket) {
	console.log("Player Connected!");
	socket.emit('socketID', { id: socket.id });
	socket.emit('getPlayers', players);
	socket.emit('getPlatforms', platforms);
	socket.emit('getBoulders', boulders);
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

    socket.on('startCamera', function() {
        socket.broadcast.emit('startCamera', { start: true });
    });

    socket.on('repositionPlatform', function(data) {
        platforms[data.index].x = data.x;
        platforms[data.index].y = data.y;
        platforms[data.index].width = data.width;
        socket.broadcast.emit('repositionPlatform', data);
    });

    socket.on('repositionBoulder', function(data) {
        boulders[data.index].x = data.x;
        boulders[data.index].y = data.y;
        boulders[data.index].velocity = data.velocity;
        socket.broadcast.emit('repositionBoulder', data);
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
	players.push(new player(socket.id, yikes_width/2, ground_height));
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

function boulder(velocity, x, y) {
    this.velocity = velocity;
    this.x = x;
    this.y = y;
}