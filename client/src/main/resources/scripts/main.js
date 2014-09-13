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
            groupId: "testing-group-id",
            topic: "testing-topic"}))
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

