var connection = new WebSocket('ws://localhost:9003/');

var KafkaMessage = React.createClass({
    render: function () {
        return React.DOM.li({}, this.props.message);
    }
});

connection.onopen = function () {
    console.log('Connection open!');
    //connection.send('Hey server, whats up?');
    connection.send(
        msgpack.encode({command: "init",
            groupId: makeid(),
            topics: ["testing-topic1", "testing-topic2"]}))
}

// Source: http://stackoverflow.com/questions/1349404/generate-a-string-of-5-random-characters-in-javascript
function makeid() {
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for (var i = 0; i < 5; i++)
        text += possible.charAt(Math.floor(Math.random() * possible.length));

    return text;
}

connection.onclose = function () {
    console.log('Connection closed');
}

connection.onmessage = function (e) {
    var server_message = e.data;
    console.log(server_message);

    document.getElementById('messages').innerHTML
        += '<li>' + server_message + '</li>';
}

connection.onerror = function (error) {
    console.log('Error detected: ' + error);
}

