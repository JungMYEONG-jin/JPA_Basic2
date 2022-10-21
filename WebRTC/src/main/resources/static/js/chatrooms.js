var ws = null; // sockjs를 이용한 서버와 연결되는 객체

function setConnected(connected){

}

function showMessage(message){
    console.log(message);
    var jsonMessage = JSON.parse(message);

    $("#chatArea").append(jsonMessage.name + ' : '+jsonMessage.message + '\n');
    var textArea = $('#chatArea');
    textArea.scrollTop(textArea[0].scrollHeight - textArea.height());
}

function connect(){
    // sockjs로 서버에 연결
    ws = new SockJS('/socket');
    // 서버가 메세지를 보내면 함수 호출 됨
    ws.onmessage = function (message){
        showMessage(message.data);
    }
}

function disconnect(){
    if (ws != null)
        ws.close();
    setConnected(false);
    console.log("Disconnected");
}

function send(){
    // 서버에 메세지 전송
    ws.send(JSON.stringify({'message': $("#chatInput").val()}));
    // 채팅 입력창 지우고 포커싱
    $("#chatInput").val('');
    $("#chatInput").focus();
}

$(function () {
    connect();

    // 채팅창에서 키가 눌리면 함수가 호출
    // 엔터 입력시 send
    $("#chatInput").keypress(function (e) {
        if (e.keyCode == 13)
            send();
    });

    $("#sendBtn").click(function (){send();});
});