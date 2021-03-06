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
var deathCounter = 0;
var startCamera = false;

server.listen(process.env.PORT || 5000, function(){
	console.log("Server is now running...");
	// when the server starts, initialize some platforms and boulders
    for (var i = 0; i < num_platforms; i++) {
         var width = min_hole_width + Math.random() * max_additional_hole_width;
         platforms.push(new platform(width, Math.random() * (yikes_width - width), ground_height + (i+1) * platform_interval));
         boulders.push(new boulder(0, platform_interval * i, -100));
    }
});

io.on('connection', function(socket) {
	console.log("Player Connected!");
	socket.emit('socketID', { id: socket.id });
	// when a player connects, give them all currently connected players, and the coordinates of existing platforms and boulders
	socket.emit('getPlayers', players);
	socket.emit('getPlatforms', platforms);
	socket.emit('getBoulders', boulders);
	// when a player connects, give all other players the new player's position and velocity
    socket.broadcast.emit('newPlayer', { id: socket.id});
    // updating the position and velocity of a player on the server, and sending the same properties to all other players
    socket.on('playerUpdate', function(data) {
        data.id = socket.id;
        socket.broadcast.emit('playerUpdate', data);
        for (var i = 0; i < players.length; i++) {
            if (players[i].id == data.id) {
                players[i].x = data.x;
                players[i].y = data.y;
                players[i].velocityX = data.velocityX;
                players[i].velocityY = data.velocityY;
            }
        }
    });

    socket.on('startCamera', function() { // start the camera for all players
        startCamera = true;
        socket.broadcast.emit('startCamera', { start: true });
    });

    socket.on('repositionPlatform', function(data) { // repositions platforms at a given index on the server
        platforms[data.index].x = data.x;
        platforms[data.index].y = data.y;
        platforms[data.index].width = data.width;
        socket.broadcast.emit('repositionPlatform', data);
    });

    socket.on('repositionBoulder', function(data) { // repositions boulders at a given index on the server
        boulders[data.index].x = data.x;
        boulders[data.index].y = data.y;
        boulders[data.index].velocity = data.velocity;
        socket.broadcast.emit('repositionBoulder', data);
    });

    socket.on('addToDeathCounter', function() { // occurs when a player dies
        deathCounter++;
        // everyone is dead, so reset the server
        if (deathCounter == players.length) {
            socket.emit('resetState');
            socket.broadcast.emit('resetState');
        }
    });

	socket.on('disconnect', function() {
		console.log("Player Disconnected");
		socket.broadcast.emit('playerDisconnected', { id: socket.id });
		// remove the player's properties from the server
		for (var i = 0; i < players.length; i++) {
            if (players[i].id == socket.id) {
                players.splice(i, 1);
            }
		}
		// if the last player has left, then reset the server
		if (players.length == 0) {
		    console.log("Resetting Server");
            platforms = [];
            boulders = [];
            deathCounter = 0;
            startCamera = false;
            for (var i = 0; i < num_platforms; i++) {
                var width = min_hole_width + Math.random() * max_additional_hole_width;
                platforms.push(new platform(width, Math.random() * (yikes_width - width), ground_height + (i+1) * platform_interval));
                boulders.push(new boulder(0, platform_interval * i, -100));
            }
		}
	});
	// don't add any new players to the array if the game is in progress. it would mess up death counter.
	if (!startCamera) {
	    players.push(new player(socket.id, yikes_width/2, ground_height, 0, 0));
	}
});

function player(id, x, y, velocityX, velocityY) {
    this.id = id;
    this.x = x;
    this.y = y;
    this.velocityX = velocityX;
    this.velocityY = velocityY;
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