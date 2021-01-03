
var stompClient = null;
var gameRoomCode = null;

class GameRoom {

    constructor(roomCode) {
        gameRoomCode = roomCode;
    }

    connectToRoom(onConnected, onMessageReceivedFunc) {

        var socket = new SockJS('/chat');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, function(frame) {

            if (onConnected) {
                onConnected(true);
            }
            console.log('Connected: ' + frame);
            stompClient.subscribe('/topic/room/' + gameRoomCode, function(messageOutput) {

                onMessageReceivedFunc(JSON.parse(messageOutput.body));
            });
        });
    }

    disconnectFromRoom() {

        if(stompClient != null) {
            stompClient.disconnect();
        }

        setConnected(false);
        console.log("Disconnected");
    }

    sendMessage(from, command, data) {

        stompClient.send('/app/room/' + gameRoomCode, {}, JSON.stringify({'from':from, 'command': command, 'data': data}));
    }
}