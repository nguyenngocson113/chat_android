var express = require("express");
var app = express();
var server = require("http").createServer(app);
var io = require("socket.io").listen(server);
var fs = require("fs");
server.listen(process.env.PORT || 3000);

var mangUsernames = [];

io.sockets.on('connection', function (socket) {
	
  console.log("Co client connect toi");
  
   socket.on('client-gui-username', function (data) {
    
    var ketqua = false;
    if(mangUsernames.indexOf(data) > -1){
        ketqua = false;
    }else {
        mangUsernames.push(data);
        socket.un = data;
        ketqua = true;
        io.sockets.emit('server-gui-username', { danhsach: mangUsernames });
    }
	
    socket.emit('ketquadangkuun', { noidung: ketqua });
    
  });
  
  
  socket.on('client-gui-tinchat', function (ndchat) {
        console.log(socket.un + ":" + ndchat);
        io.sockets.emit('server-gui-tinchat', { tinchat: socket.un + " " + ":" + " " + ndchat });
    
  });
  
  
   socket.on('client-gui-amthanh', function (ndamthanh) {
        console.log(ndamthanh);
        io.sockets.emit('server-gui-amthanh', { noidung: ndamthanh });
	
  });
  
  });