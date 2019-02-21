//app.js
var servidor= require('./servidor');//Importar modulo servidor
var router = require('./router'); //Importar el módulo router
var manejadores = require('./manejador'); //Importamos el módulo manejador
var manejador = {}
manejador["/mapas"] = manejadores.mapas;
manejador["/gestion"] = manejadores.gestion;
manejador["/bigData/posicionGruas"] = manejadores.posicionGruas;
manejador["/bigData/servicios"] = manejadores.servicios;
manejador["/login"] = manejadores.login;
manejador["/registro"] = manejadores.registro;
manejador["/plogin"] = manejadores.plogin;
manejador["/pregistro"] = manejadores.pregistro;
servidor.inicializar(router.route, manejador); //Llama al método inicializar del objeto servidor