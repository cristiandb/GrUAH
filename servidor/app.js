//app.js
var servidor= require('./servidor');//Importar modulo servidor
var router = require('./router'); //Importar el m�dulo router
var manejadores = require('./manejador'); //Importamos el m�dulo manejador
var manejador = {}
manejador["/mapas"] = manejadores.mapas;
manejador["/gestion"] = manejadores.gestion;
manejador["/bigData/posicionGruas"] = manejadores.posicionGruas;
manejador["/bigData/servicios"] = manejadores.servicios;
manejador["/login"] = manejadores.login;
manejador["/registro"] = manejadores.registro;
manejador["/plogin"] = manejadores.plogin;
manejador["/pregistro"] = manejadores.pregistro;
servidor.inicializar(router.route, manejador); //Llama al m�todo inicializar del objeto servidor